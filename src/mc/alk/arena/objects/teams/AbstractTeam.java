package mc.alk.arena.objects.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractTeam implements ArenaTeam{
	static int count = 0;
	final int id = count++; /// id

	protected Set<ArenaPlayer> players = Collections.synchronizedSet(new HashSet<ArenaPlayer>());
	protected Set<ArenaPlayer> deadplayers = Collections.synchronizedSet(new HashSet<ArenaPlayer>());
	protected Set<ArenaPlayer> leftplayers = Collections.synchronizedSet(new HashSet<ArenaPlayer>());

	protected boolean nameManuallySet = false;
	protected boolean nameChanged = true;
	protected String name =null; /// Internal name of this team
	protected String displayName =null; /// Display name
	protected String scoreboardDisplayName =null; /// Scoreboard name

	HashMap<ArenaPlayer, Integer> kills = new HashMap<ArenaPlayer,Integer>();
	HashMap<ArenaPlayer, Integer> deaths = new HashMap<ArenaPlayer,Integer>();

	/// Pickup teams are transient in nature, once the match end they disband
	protected boolean isPickupTeam = false;

	ArenaObjective objective;
	protected ChatColor color = null;
	protected ItemStack headItem = null;

	/**
	 * Default Constructor
	 */
	public AbstractTeam(){
		init();
	}

	protected AbstractTeam(ArenaPlayer p) {
		init();
		players.add(p);
		nameChanged = true;
	}

	protected AbstractTeam(Collection<ArenaPlayer> teammates) {
		init();
		this.players.addAll(teammates);
		nameChanged = true;
	}

	protected AbstractTeam(ArenaPlayer p, Collection<ArenaPlayer> teammates) {
		init();
		players.add(p);
		players.addAll(teammates);
		nameChanged = true;
	}

	@Override
	public void init(){
		reset();
	}

	public void reset() {
		deaths.clear();
		kills.clear();
		setAlive();
		for (ArenaPlayer ap: players){
			if (leftplayers.contains(ap))
				continue;
			ap.reset();
		}
	}

	protected String createName() {
		if (nameManuallySet || !nameChanged){ ///
			return name;}
		/// Sort the names and then append them together
		ArrayList<String> list = new ArrayList<String>(players.size());
		for (ArenaPlayer p:players){list.add(p.getName());}
		for (ArenaPlayer p:leftplayers){list.add(p.getName());}
		if (list.size() > 1)
			Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s: list){
			if (!first) sb.append(", ");
			sb.append(s);
			first = false;
		}
		name= sb.toString();
		nameChanged = false;
		return name;
	}

	public Set<ArenaPlayer> getPlayers() {
		return players;
	}

	public Set<Player> getBukkitPlayers() {
		Set<Player> ps = new HashSet<Player>();

		for (ArenaPlayer ap: players){
			Player p = Bukkit.getPlayerExact(ap.getName());
			if (p != null)
				ps.add(p);
		}
		return ps;
	}

	public Set<ArenaPlayer> getDeadPlayers() {return deadplayers;}
	public Set<ArenaPlayer> getLivingPlayers() {
		Set<ArenaPlayer> living = new HashSet<ArenaPlayer>();
		for (ArenaPlayer p : players){
			if (hasAliveMember(p)){
				living.add(p);}
		}
		return living;
	}
	public boolean wouldBeDeadWithout(ArenaPlayer p) {
		Set<ArenaPlayer> living = getLivingPlayers();
		living.remove(p);
		int offline = 0;
		for (ArenaPlayer ap: living){
			if (!ap.isOnline())
				offline++;
		}
		return living.isEmpty() || living.size() <= offline;
	}

	public boolean hasMember(ArenaPlayer p) {return players.contains(p);}
	public boolean hasLeft(ArenaPlayer p) {return leftplayers.contains(p);}
	public boolean hasAliveMember(ArenaPlayer p) {return hasMember(p) && !deadplayers.contains(p);}
	public boolean isPickupTeam() {return isPickupTeam;}
	public void setPickupTeam(boolean isPickupTeam) {this.isPickupTeam = isPickupTeam;}
	public void setHealth(int health) {for (ArenaPlayer p: players){p.setHealth(health);}}
	public void setHunger(int hunger) {for (ArenaPlayer p: players){p.setFoodLevel(hunger);}}

	public String getName() {return createName();}

	public void setName(String name) {
		this.name = name;
		this.nameManuallySet = true;
	}

	/**
	 * Returns this teams unique ID.
	 * Team ID is unique to everything, and no two teams will have the same ID.
	 * This is NOT equivilant to Arena.getMatch().getTeams().indexOf(this)!
	 */
	public int getId(){ return id;}

	public void setAlive() {deadplayers.clear();}

	@Override
	public void setAlive(ArenaPlayer player){deadplayers.remove(player);}

	public boolean isDead() {
		if (deadplayers.size() >= players.size())
			return true;
		Set<ArenaPlayer> living = getLivingPlayers();
		if (living.isEmpty())
			return true;
		int offline = 0;
		for (ArenaPlayer ap: living){
			if (!ap.isOnline()){
				offline++;}
		}
		return living.size() <= offline;
	}

	@Override
	public boolean isReady() {
		for (ArenaPlayer ap: getLivingPlayers()){
			if (!ap.isReady())
				return false;
		}
		return true;
	}

	public int size() {return players.size();}

	public int addDeath(ArenaPlayer teamMemberWhoDied) {
		Integer d = deaths.get(teamMemberWhoDied);
		if (d == null){
			d = 0;}
		deaths.put(teamMemberWhoDied, ++d);
		return d;
	}

	public int addKill(ArenaPlayer teamMemberWhoKilled){
		Integer d = kills.get(teamMemberWhoKilled);
		if (d == null){
			d = 0;}
		kills.put(teamMemberWhoKilled, ++d);
		if (objective != null){
			objective.addPoints(teamMemberWhoKilled, d);
			objective.addPoints(this, d);
		}
		return d;
	}

	public int getNKills() {
		int nkills = 0;
		for (Integer i: kills.values()) nkills+=i;
		return nkills;
	}

	public int getNDeaths() {
		int nkills = 0;
		for (Integer i: deaths.values()) nkills+=i;
		return nkills;
	}

	public Integer getNDeaths(ArenaPlayer p) {
		return deaths.get(p);
	}

	public Integer getNKills(ArenaPlayer p) {
		return kills.get(p);
	}

	/**
	 *
	 * @param p
	 * @return whether all players are dead
	 */
	public boolean killMember(ArenaPlayer p) {
		if (!hasMember(p))
			return false;
		deadplayers.add(p);
		return deadplayers.size() == players.size();
	}

	/**
	 * Call when a player has left this team
	 */
	public void playerLeft(ArenaPlayer p) {
		if (!hasMember(p))
			return;
		deadplayers.remove(p);
		players.remove(p);
		leftplayers.add(p);
	}

	public boolean allPlayersOffline() {
		for (ArenaPlayer p: players){
			if (p.isOnline())
				return false;
		}
		return true;
	}

	public void sendMessage(String message) {
		for (ArenaPlayer p: players){
			MessageUtil.sendMessage(p, message);}
	}
	public void sendToOtherMembers(ArenaPlayer player, String message) {
		for (ArenaPlayer p: players){
			if (!p.equals(player))
				MessageUtil.sendMessage(p, message);}
	}

	public String getDisplayName(){return displayName == null ? getName() : displayName;}
	public void setDisplayName(String teamName){displayName = teamName;}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof AbstractTeam)) return false;
		return this.hashCode() == ((AbstractTeam) other).hashCode();
	}

	@Override
	public int hashCode() { return id;}

	@Override
	public String toString(){return "["+getDisplayName()+"]";}

	public boolean hasTeam(ArenaTeam team){
		if (team instanceof CompositeTeam){
			for (ArenaTeam t: ((CompositeTeam)team).getOldTeams()){
				if (this.hasTeam(t))
					return true;
			}
			return false;
		} else {
			return this.equals(team);
		}
	}

	public String getTeamInfo(Set<String> insideMatch){
		StringBuilder sb = new StringBuilder("&eTeam: ");
		if (displayName != null) sb.append(displayName);
		sb.append( " " + (isDead() ? "&4dead" : "&aalive")+"&e, ");

		for (ArenaPlayer p: players){
			sb.append("&6"+p.getName());
			boolean isAlive = hasAliveMember(p);
			boolean online = p.isOnline();
			final String inmatch = insideMatch == null? "": ((insideMatch.contains(p.getName())) ? "&e(in)" : "&4(out)");
			final int k = kills.containsKey(p) ? kills.get(p) : 0;
			final int d = deaths.containsKey(p) ? deaths.get(p) : 0;
			sb.append("&e(&c"+k+"&e,&7"+d+"&e)");
			sb.append("&e:" + (isAlive ? "&ah="+p.getHealth() : "&40") +
					((!online) ? "&4(O)" : "")+inmatch+"&e ");
		}
		return sb.toString();
	}

	public String getTeamSummary() {
		StringBuilder sb = new StringBuilder("&6"+getDisplayName());
		for (ArenaPlayer p: players){
			final int k = kills.containsKey(p) ? kills.get(p) : 0;
			final int d = deaths.containsKey(p) ? deaths.get(p) : 0;
			sb.append("&e(&c"+k+"&e,&7"+d+"&e)");
		}
		return sb.toString();
	}

	public String getOtherNames(ArenaPlayer player) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ArenaPlayer p: players){
			if (p.equals(player))
				continue;
			if (!first) sb.append(", ");
			sb.append(p.getName());
			first = false;
		}
		return sb.toString();
	}

	public boolean hasSetName() {
		return this.nameManuallySet;
	}

	public int getPriority() {
		int priority = Integer.MAX_VALUE;
		for (ArenaPlayer ap: players){
			if (ap.getPriority() < priority)
				priority = ap.getPriority();
		}
		return priority;
	}

	@Override
	public void addPlayer(ArenaPlayer player) {
		this.players.add(player);
		this.leftplayers.remove(player);
		this.nameChanged = true;
	}

	@Override
	public void removePlayer(ArenaPlayer player) {
		this.players.remove(player);
		this.deadplayers.remove(player);
		this.leftplayers.remove(player);
		this.kills.remove(player);
		this.deaths.remove(player);
		this.nameChanged = true;
	}

	@Override
	public void addPlayers(Collection<ArenaPlayer> players) {
		this.players.addAll(players);
		this.nameChanged = true;
	}

	@Override
	public void removePlayers(Collection<ArenaPlayer> players) {
		this.players.removeAll(players);
		this.deadplayers.removeAll(players);
		this.leftplayers.removeAll(players);
		for (ArenaPlayer ap: players){
			this.kills.remove(ap);
			this.deaths.remove(ap);
		}
		this.nameChanged = true;
	}

	@Override
	public void clear(){
		this.players.clear();
		this.deadplayers.clear();
		this.leftplayers.clear();
		this.nameManuallySet = false;
		this.nameChanged = false;
		this.name = "Empty";
		this.kills.clear();
		this.deadplayers.clear();
	}

	public void setArenaObjective(ArenaObjective objective){
		this.objective = objective;
		int tk = 0;
		for (ArenaPlayer player: this.getPlayers()){
			Integer kills = getNKills(player);
			if (kills == null) kills = 0;
			objective.setPoints(player, kills);
			tk += kills;
		}
		objective.setPoints(this, tk);
	}

	@Override
	public void setTeamChatColor(ChatColor color) {
		this.color = color;
	}
	@Override
	public ChatColor getTeamChatColor() {
		return color;
	}

	@Override
	public String geIDString(){
		return String.valueOf(id);
	}

	@Override
	public void setScoreboardDisplayName(String name){
		this.scoreboardDisplayName = name;
	}

	@Override
	public String getScoreboardDisplayName(){
		return scoreboardDisplayName;
	}

	public ItemStack getHeadItem(){
		return this.headItem;
	}

	public void setHeadItem(ItemStack item){
		this.headItem = item;
	}
}

