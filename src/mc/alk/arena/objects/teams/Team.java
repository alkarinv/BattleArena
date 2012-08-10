package mc.alk.arena.objects.teams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.controllers.MessageController;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;



public class Team {
	static int count = 0;

	protected Set<Player> players = new HashSet<Player>();
	protected Set<Player> deadplayers = new HashSet<Player>();

	protected String name =null; /// Internal name of this team
	protected String displayName =null; /// Display name
	final int id = count++; /// id

	HashMap<OfflinePlayer, Integer> kills = new HashMap<OfflinePlayer,Integer>();
	HashMap<OfflinePlayer, Integer> deaths = new HashMap<OfflinePlayer,Integer>();

	/// Pickup inEvent are transient in nature, once the match end they disband
	protected boolean isPickupTeam = false;
	/// This is only so that teleports can be done to slightly different places for each player
	protected HashMap<String,Integer> playerIndexes = new HashMap<String,Integer>();
	/**
	 * Default Constructor
	 */
	public Team(){}
	
	protected Team(Player p) {
		players.add(p);
		createName();
	}

	protected Team(Set<Player> teammates) {
		this.players.addAll(teammates);
		createName();
	}

	protected Team(Player p, Set<Player> teammates) {
		players.add(p);
		players.addAll(teammates);
		createName();
	}

	protected void createName() {
		/// Sort the names and then append them together
		playerIndexes.clear();
		ArrayList<String> list = new ArrayList<String>();
		for (Player p:players){list.add(p.getName());}
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		int i=0;
		for (String s: list){
			if (!first) sb.append(", ");
			sb.append(s);
			first = false;
			playerIndexes.put(s, i++);
		}
		name= sb.toString();
	}

	public Set<Player> getPlayers() {return players;}
	public Set<Player> getDeadPlayers() {return deadplayers;}
	public Set<Player> getLivingPlayers() {
		Set<Player> living = new HashSet<Player>();
		for (Player p : players){
			if (hasAliveMember(p) && p.isOnline()){
				living.add(p);}
		}
		return living;
	}
	public boolean wouldBeDeadWithout(Player p) {
		Set<Player> living = getLivingPlayers();
		living.remove(p);
		return living.isEmpty();
	}

	public boolean hasMember(OfflinePlayer p) {return players.contains(p);}
	public boolean hasAliveMember(OfflinePlayer p) {return players.contains(p) && !deadplayers.contains(p);}
	public boolean isPickupTeam() {return isPickupTeam;}
	public void setPickupTeam(boolean isPickupTeam) {this.isPickupTeam = isPickupTeam;}
	public void setHealth(int health) {for (Player p: players){p.setHealth(health);}}
	public void setHunger(int hunger) {for (Player p: players){p.setFoodLevel(hunger);}}
	public String getName() {return name;}
	public int getId(){ return id;}
	public void setName(String name) {this.name = name;}
	public void setAlive() {deadplayers.clear();}
	public boolean isDead() {return deadplayers.size() >= players.size();}
	public int size() {return players.size();}
	public void resetScores() {
		deaths.clear();
		kills.clear();
	}

	public void addDeath(OfflinePlayer teamMemberWhoDied) {
		Integer d = deaths.get(teamMemberWhoDied);
		if (d == null){
			d = 0;} 
		deaths.put(teamMemberWhoDied, ++d);
	}

	public void addKill(OfflinePlayer teamMemberWhoKilled){
		Integer d = kills.get(teamMemberWhoKilled);
		if (d == null){
			d = 0;} 
		kills.put(teamMemberWhoKilled, ++d);
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

	public int getNDeaths(Player p) {
		return deaths.get(p);
	}

	public int getNKills(Player p) {
		return kills.get(p);
	}

	/**
	 * 
	 * @param p
	 * @return whether all players are dead
	 */
	public boolean killMember(Player p) {
		if (!hasMember(p))
			return false;
		deadplayers.add(p);
		return deadplayers.size() == players.size();
	}

	public boolean allPlayersOffline() {
		for (Player p: players){
			if (p.isOnline())
				return false;
		}
		return true;
	}

	public void sendMessage(String message) {
		for (Player p: players){
			MessageController.sendMessage(p, message);}
	}
	public void sendToOtherMembers(Player player, String message) {
		for (Player p: players){
			if (!p.equals(player))
				sendMessage(message);}
	}

	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Team)) return false;
		return this.hashCode() == ((Team) other).hashCode();
	}

	public int hashCode() { return id;}

	public String getDisplayName(){return displayName == null ? name : displayName;}
	public void setDisplayName(String n){displayName = n;}
	public String toString(){return "["+getDisplayName()+"]";}

	public String getTeamInfo(Set<Player> insideMatch){

		StringBuilder sb = new StringBuilder("&eTeam: ");
		if (displayName != null) sb.append(displayName); 
		sb.append( " " + (isDead() ? "&4dead" : "&aalive")+"&e, ");

		for (Player p: players){
			sb.append("&6"+p.getName());
			boolean isAlive = hasAliveMember(p);
			boolean online = p.isOnline();
			final String inmatch = insideMatch == null? "": ((insideMatch.contains(p)) ? "&e(in)" : "&4(out)");
			final int k = kills.containsKey(p) ? kills.get(p) : 0;
			final int d = deaths.containsKey(p) ? deaths.get(p) : 0;
			sb.append("&e(&c"+k+"&e,&7"+d+"&e)");
			sb.append("&e:" + (isAlive ? "&ahealth="+p.getHealth() : "&4dead") +
					((!online) ? "&4(O)" : "")+inmatch+"&e ");
		}
		return sb.toString();
	}

	public int getPlayerIndex(Player p) {
		return playerIndexes.get(p.getName());
	}

	public String getTeamSummary() {
		StringBuilder sb = new StringBuilder("&6"+getDisplayName());
		for (Player p: players){
			final int k = kills.containsKey(p) ? kills.get(p) : 0;
			final int d = deaths.containsKey(p) ? deaths.get(p) : 0;
			sb.append("&e(&c"+k+"&e,&7"+d+"&e)");
		}
		return sb.toString();
	}

	public String getOtherNames(Player player) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Player p: players){
			if (p.equals(player))
				continue;
			if (!first) sb.append(", ");
			sb.append(p.getName());
			first = false;
		}
		return sb.toString();
	}

	public boolean hasSetName() {
		return displayName != null;
	}

}

