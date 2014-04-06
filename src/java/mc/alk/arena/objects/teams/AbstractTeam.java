package mc.alk.arena.objects.teams;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.stats.ArenaStat;
import mc.alk.arena.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

abstract class AbstractTeam implements ArenaTeam{
	static int count = 0;
	final int id = count++; /// id

    final protected Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
    final protected Set<ArenaPlayer> deadplayers = new HashSet<ArenaPlayer>();
    final protected Set<ArenaPlayer> leftplayers = new HashSet<ArenaPlayer>();

	protected boolean nameManuallySet = false;
	protected boolean nameChanged = true;
	protected String name =null; /// Internal name of this team
	protected String displayName =null; /// Display name
	protected String scoreboardDisplayName =null; /// Scoreboard name

    final HashMap<ArenaPlayer, Integer> kills = new HashMap<ArenaPlayer,Integer>();
    final HashMap<ArenaPlayer, Integer> deaths = new HashMap<ArenaPlayer,Integer>();

	/// Pickup teams are transient in nature, once the match end they disband
	protected boolean isPickupTeam = false;
    int minPlayers = -1;
    int maxPlayers = -1;
    ArenaObjective objective;
	protected ChatColor color = null;
	protected ItemStack headItem = null;
	ArenaStat stat;
	MatchParams params;

    int index = -1;
    String strID = null;

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

	@Override
    public void reset() {
        players.clear();
		deaths.clear();
		kills.clear();
        deadplayers.clear();
        nameChanged = true;
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

	@Override
    public Set<ArenaPlayer> getPlayers() {
		return players;
	}

	@Override
    public Set<Player> getBukkitPlayers() {
		Set<Player> ps = new HashSet<Player>();

		for (ArenaPlayer ap: players){
			Player p = ap.getPlayer();
			if (p != null)
				ps.add(p);
		}
		return ps;
	}

    @Override
	public Set<ArenaPlayer> getDeadPlayers() {return deadplayers;}

    @Override
    public Set<ArenaPlayer> getLeftPlayers() {return leftplayers;}

    @Override
	public Set<ArenaPlayer> getLivingPlayers() {
		Set<ArenaPlayer> living = new HashSet<ArenaPlayer>();
		for (ArenaPlayer p : players){
			if (hasAliveMember(p)){
				living.add(p);}
		}
		return living;
	}

	@Override
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

	@Override
    public boolean hasMember(ArenaPlayer p) {return players.contains(p);}
    @Override
    public boolean hasLeft(ArenaPlayer p) {return leftplayers.contains(p);}
	@Override
    public boolean hasAliveMember(ArenaPlayer p) {return hasMember(p) && !deadplayers.contains(p);}
	@Override
    public boolean isPickupTeam() {return isPickupTeam;}
	@Override
    public void setPickupTeam(boolean isPickupTeam) {this.isPickupTeam = isPickupTeam;}
	public void setHealth(int health) {for (ArenaPlayer p: players){p.setHealth(health);}}
	public void setHunger(int hunger) {for (ArenaPlayer p: players){p.setFoodLevel(hunger);}}

	@Override
    public String getName() {return createName();}

	@Override
    public void setName(String name) {
		this.name = name;
		this.nameManuallySet = true;
	}

	/**
	 * Returns this teams unique ID.
	 * Team ID is unique to everything, and no two teams will have the same ID.
	 * This is NOT equivilant to Arena.getMatch().getTeams().indexOf(this)!
	 */
	@Override
    public int getId(){ return id;}

	@Override
    public void setAlive() {deadplayers.clear();}

	@Override
	public void setAlive(ArenaPlayer player){deadplayers.remove(player);}

	@Override
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

	@Override
    public int size() {return players.size();}

	@Override
    public int addDeath(ArenaPlayer teamMemberWhoDied) {
		Integer d = deaths.get(teamMemberWhoDied);
		if (d == null){
			d = 0;}
		deaths.put(teamMemberWhoDied, ++d);
		return d;
	}

	@Override
    public int addKill(ArenaPlayer teamMemberWhoKilled){
		Integer d = kills.get(teamMemberWhoKilled);
		if (d == null){
			d = 0;}
		kills.put(teamMemberWhoKilled, ++d);
		if (objective != null){
			objective.setPoints(teamMemberWhoKilled, d);
			objective.setPoints(this, d);
		}
		return d;
	}

	@Override
    public int getNKills() {
		int nkills = 0;
		for (Integer i: kills.values()) nkills+=i;
		return nkills;
	}

	@Override
    public int getNDeaths() {
		int nkills = 0;
		for (Integer i: deaths.values()) nkills+=i;
		return nkills;
	}

	@Override
    public Integer getNDeaths(ArenaPlayer p) {
		return deaths.get(p);
	}

	@Override
    public Integer getNKills(ArenaPlayer p) {
		return kills.get(p);
	}

	/**
	 *
	 * @param p ArenaPlayer
	 * @return whether all players are dead
	 */
	@Override
    public boolean killMember(ArenaPlayer p) {
		if (!hasMember(p))
			return false;
		deadplayers.add(p);
		return deadplayers.size() == players.size();
	}

	@Override
    public boolean allPlayersOffline() {
		for (ArenaPlayer p: players){
			if (p.isOnline())
				return false;
		}
		return true;
	}

	@Override
    public void sendMessage(String message) {
		for (ArenaPlayer p: players){
			MessageUtil.sendMessage(p, message);}
	}
	@Override
    public void sendToOtherMembers(ArenaPlayer player, String message) {
		for (ArenaPlayer p: players){
			if (!p.equals(player))
				MessageUtil.sendMessage(p, message);}
	}

	@Override
    public String getDisplayName(){return displayName == null ? getName() : displayName;}

	@Override
    public void setDisplayName(String teamName){
        displayName = teamName;
        this.nameManuallySet = true;
    }

	@SuppressWarnings("SimplifiableIfStatement")
    @Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof AbstractTeam)) return false;
		return this.hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() { return id;}

	@Override
	public String toString(){return "["+getDisplayName()+"]";}

	@Override
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

    @Override
    public String getTeamInfo(Set<UUID> insideMatch){
		StringBuilder sb = new StringBuilder("&eTeam: ");
		if (displayName != null) sb.append(displayName);
		sb.append(" ").append(isDead() ? "&4dead" : "&aalive").append("&e, ");

		for (ArenaPlayer p: players){
			sb.append("&6").append(p.getName());
			boolean isAlive = hasAliveMember(p);
			boolean online = p.isOnline();
			final String inmatch = insideMatch == null? "": ((insideMatch.contains(p.getID())) ? "&e(in)" : "&4(out)");
			final int k = kills.containsKey(p) ? kills.get(p) : 0;
			final int d = deaths.containsKey(p) ? deaths.get(p) : 0;
			sb.append("&e(&c").append(k).append("&e,&7").append(d).append("&e)");
			sb.append("&e:").append(isAlive ? "&ah=" + p.getHealth() : "&40").
                    append((!online) ? "&4(O)" : "").append(inmatch).append("&e ");
		}
		return sb.toString();
	}

	@Override
    public String getTeamSummary() {
		StringBuilder sb = new StringBuilder("&6"+getDisplayName());
		for (ArenaPlayer p: players){
			final int k = kills.containsKey(p) ? kills.get(p) : 0;
			final int d = deaths.containsKey(p) ? deaths.get(p) : 0;
			sb.append("&e(&c").append(k).append("&e,&7").append(d).append("&e)");
		}
		return sb.toString();
	}

	@Override
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

	@Override
    public boolean hasSetName() {
		return this.nameManuallySet;
	}

	@Override
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
	public boolean removePlayer(ArenaPlayer player) {
		this.deadplayers.remove(player);
		this.leftplayers.remove(player);
		this.kills.remove(player);
		this.deaths.remove(player);
		this.nameChanged = true;
        return this.players.remove(player);
    }

	/**
	 * Call when a player has left this team
	 */
	@Override
    public void playerLeft(ArenaPlayer p) {
		if (!hasMember(p))
			return;
		deadplayers.remove(p);
		players.remove(p);
		leftplayers.add(p);
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

	@Override
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
	public String getIDString(){
		return strID == null ? String.valueOf(id) : strID;
	}

	@Override
	public void setScoreboardDisplayName(String name){
		this.scoreboardDisplayName = name;
	}

	@Override
	public String getScoreboardDisplayName(){
		if (scoreboardDisplayName != null)
			return scoreboardDisplayName;
		String name = getDisplayName();
		return name.length() > Defaults.MAX_SCOREBOARD_NAME_SIZE ? name.substring(0,Defaults.MAX_SCOREBOARD_NAME_SIZE) : name;
	}

	@Override
    public ItemStack getHeadItem(){
		return this.headItem;
	}

	@Override
    public void setHeadItem(ItemStack item){
		this.headItem = item;
	}

	@Override
	public MatchParams getCurrentParams() {
		return params;
	}

	@Override
	public void setCurrentParams(MatchParams params) {
		this.params = params;
	}

	@Override
	public void setArenaStat(ArenaStat stat){
		this.stat = stat;
	}

	@Override
	public ArenaStat getStat(){
		return StatController.loadRecord(getCurrentParams(), this);
//		return stat;
	}

	@Override
	public ArenaStat getStat(MatchParams params){
		return StatController.loadRecord(params, this);
//		return stat;
	}

    @Override
    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }


    @Override
    public void setMinPlayers(int num) {
        this.minPlayers = num;
    }

    @Override
    public void setMaxPlayers(int num) {
        this.maxPlayers = num;
    }


    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIDString(String id) {
        this.strID = id;
    }
}

