package mc.alk.arena.objects.arenas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import mc.alk.arena.controllers.SpawnController;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.Util;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Arena implements ArenaListener {
	protected String name;

	protected ArenaParams ap; /// Size and arena type info

	protected TreeMap<Integer,Location> locs = null; /// Team spawn Locations
	protected TreeMap<Integer,Location> wrlocs = null; /// wait room spawn locations
	protected Location vloc = null;

	protected Map<Player, Location> visitorlocations = null;/// tp locations for the visitors
	protected Random rand = new Random(); /// a random 

	protected Map<Long, TimedSpawn> timedSpawns = null; /// Item/mob/other spawn events
	protected SpawnController spawnController = null;

	protected String wgRegionName;
	protected Match match = null;

	/**
	 * Construct a new arena from a name and parameters
	 * @param name
	 * @param ap
	 */
	public Arena (String name, ArenaParams ap){
		this.name = name;
		this.ap = ap;
		visitorlocations = new HashMap<Player,Location>();
		locs = new TreeMap<Integer,Location>();
//		this.addMethods(this, this.getClass().getMethods());
	}

	/**
	 * Copy constructor
	 * @param arena
	 */
	public Arena(Arena arena) {
		this.name = arena.getName();
		this.ap = arena.getParameters();
		this.locs = new TreeMap<Integer,Location>();
		this.locs.putAll(arena.locs);
		this.visitorlocations = new HashMap<Player,Location>();
		this.visitorlocations.putAll(arena.visitorlocations);
		this.vloc = arena.vloc;
		if (arena.wrlocs != null)
			this.wrlocs = new TreeMap<Integer,Location>(arena.wrlocs);
		if (arena.getTimedSpawns() != null){
			timedSpawns = new HashMap<Long,TimedSpawn>();
			timedSpawns.putAll(arena.getTimedSpawns());
		}
	}

	/**
	 * Returns the spawn location of this index
	 * @param index
	 * @return
	 */
	public Location getSpawnLoc(int index){
		if (locs == null)
			return null;
		if (index >= locs.size())
			index %= locs.size();
		return locs.get(index);
	}

	/**
	 * Returns the spawn location of this index
	 * @param index
	 * @return
	 */
	public Location getWaitRoomSpawnLoc(int index){
		if (wrlocs == null)
			return null;
		if (index >= wrlocs.size())
			index %= wrlocs.size();
		return wrlocs.get(index);
	}

	/**
	 * returns a random spawn location
	 * @return
	 */
	public Location getRandomSpawnLoc(){
		if (locs == null)
			return null;
		return locs.get(rand.nextInt(locs.size()));
	}

	/**
	 * returns a random spawn location
	 * @return
	 */
	public Location getRandomWaitRoomSpawnLoc(){
		if (wrlocs == null)
			return null;
		return wrlocs.get(rand.nextInt(wrlocs.size()));
	}

	/**
	 * Return the visitor location (if any)
	 * @return
	 */
	public Location getVisitorLoc() {return vloc;}

	/**
	 * Set the spawn location for the team with the given index
	 * @param index
	 * @param loc
	 */
	public void setSpawnLoc(int index, Location loc){locs.put(index,loc);}
	
	/**
	 * Set the wait room spawn location
	 * @param index
	 * @param loc
	 */
	public void setWaitRoomSpawnLoc(int index, Location loc) {
		if (wrlocs == null){
			wrlocs = new TreeMap<Integer,Location>();}
		wrlocs.put(index,loc);
	}
	
	/**
	 * Set a visitor spawn location
	 * @param loc
	 */
	public void setVisitorLoc(Location loc) {vloc = loc;}
	
	/**
	 * Set the Arena parameters
	 * @param arenaParams
	 */
	public void setParameters(ArenaParams arenaParams){this.ap = arenaParams;}

	/**
	 * Get the arena params
	 * @return
	 */
	public ArenaParams getParameters() {return ap;}

	/**
	 * Get the name of this arena
	 * @return
	 */
	public String getName() {return name;}
	
	/**
	 * Return the team spawn locations
	 * @return
	 */
	public Map<Integer,Location> getSpawnLocs(){return locs;}

	/**
	 * Return the waitroom spawn locations
	 * @return
	 */
	public Map<Integer,Location> getWaitRoomSpawnLocs(){return wrlocs;}

	//	public Map<Integer, ItemSpawn> getItemSpawns() {return spawnsGroups;}

	/**
	 * Get the type of this arena
	 * @return ArenaType
	 */
	public ArenaType getArenaType() {
		return ap.getType();
	}

	/**
	 * Does this arena have a name, at least one spawn, and valid arena parameters
	 * @return
	 */
	public boolean valid() {
		return (!(name == null || locs.size() <1 || locs.get(0) == null || !ap.valid() ));
	}

	/**
	 * TeamJoinResult a Protected Region (only available with worldguard)
	 * @param wgRegionName
	 */
	public void addRegion(String regionName) {
		this.wgRegionName = regionName;
	}

	/**
	 * does this arena have a worlguard wgRegionName attached 
	 * @returns
	 */
	public boolean hasRegion() {
		return wgRegionName != null;
	}

	/**
	 * Get the worldguard wgRegionName for this arena
	 * @return
	 */
	public String getRegion() {
		return wgRegionName;
	}

	/**
	 * Return the timed spawns for this arena
	 * @return
	 */
	public Map<Long, TimedSpawn> getTimedSpawns() {
		return timedSpawns;
	}

	/**
	 * TeamJoinResult a timed spawn bukkitEvent to this arena
	 */
	public void addTimedSpawn(TimedSpawn s) {
		if (timedSpawns == null){
			timedSpawns = new HashMap<Long,TimedSpawn>();
		}
		timedSpawns.put(s.getTimeToStart(), s);	
	}

	/**
	 * Set which match this arena belongs to
	 */
	public void setMatch(Match arenaMatch) {
		this.match = arenaMatch;
	}

	/**
	 * Get the current state of the match
	 * @return
	 */
	public MatchState getMatchState(){
		return match.getMatchState();
	}

	/**
	 * return a list of inEvent inside this match
	 * @return
	 */
	public List<Team> getTeams(){
		return match == null ? null : match.getTeams();
	}

	/**
	 * Return a list of alive inEvent inside this match
	 * @return
	 */
	public List<Team> getAliveTeams(){
		return match == null ? null : match.getAliveTeams();
	}

	/**
	 * Return a list of alive inEvent inside this match
	 * @return
	 */
	public Set<Player> getAlivePlayers(){
		return match == null ? null : match.getAlivePlayers();
	}

	/**
	 * Return the team of this player
	 * @return
	 */
	public Team getTeam(OfflinePlayer p){
		return match == null ? null : match.getTeam(p);
	}

	/**
	 * Start any spawns happening for this arena
	 */
	public void startSpawns(){
		/// Start Spawning Items 
		if (timedSpawns != null && !timedSpawns.isEmpty()){
			spawnController = new SpawnController(timedSpawns);
			spawnController.start();
		}
	}

	/**
	 * Stop any spawns occuring in this arena
	 */
	public void stopSpawns(){
		if (spawnController != null){
			spawnController.stop();}
	}


	public String toString(){
		return toSummaryString();
	}
	
	public String toDetailedString(){
		StringBuilder sb = new StringBuilder("&6" + name+" &e");
		sb.append(headerString());
		sb.append("&e, #Teams:&6"+ap.getNTeamRange());
		sb.append("&e, #spawns:&6" +locs.size() +"\n");
		sb.append("&eteamSpawnLocs=&b"+locationsString()+"\n");
		sb.append("&ewrSpawnLocs=&b"+wrlocationsString()+"\n");
		if (timedSpawns != null){
			sb.append("&e#itemSpawns:&6" +locs.size() +"\n");
			//			sb.append(itemSpawnString());
		}
		return sb.toString();
	}

	public String toSummaryString(){
		StringBuilder sb = new StringBuilder("&4" + name+" &e type=&6"+ap.getType());
		sb.append(" &eTeamSizes:&6"+ap.getTeamSizeRange()+"&e, #inEvent:&6"+ap.getNTeamRange());
		sb.append("&e #spawns:&6" +locs.size() +"&e 1stSpawn:&6");
		for (Integer i: locs.keySet() ){
			Location l = locs.get(i);
			if (l != null) sb.append("["+l.getWorld().getName()+":"+l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ()+"] ");
			break;
		}
		if (timedSpawns != null && !timedSpawns.isEmpty())
			sb.append("&e#itemSpawns:&6" +locs.size());
		return sb.toString();
	}

	public String locationsString(){
		StringBuilder sb = new StringBuilder();
		for (Integer i: locs.keySet() ){
			Location l = locs.get(i);
			if (l != null) sb.append("["+(i+1)+":"+Util.getLocString(l)+"] ");
		}
		return sb.toString();
	}
	public String wrlocationsString(){
		StringBuilder sb = new StringBuilder();
		if (wrlocs == null)
			return sb.toString();
		for (Integer i: wrlocs.keySet() ){
			Location l = wrlocs.get(i);
			if (l != null) sb.append("["+(i+1)+":"+Util.getLocString(l)+"] ");
		}
		return sb.toString();
	}

	//	
	//	public String itemSpawnString(){
	//		if (spawnsGroups == null)
	//			return null;
	//		StringBuilder sb = new StringBuilder();
	//		for (Integer i: spawnsGroups.keySet() ){
	//			ItemSpawn is = spawnsGroups.get(i);
	//			if (is != null) sb.append("["+(i+1)+":"+InventoryUtil.getItemString(is.is)+":"+getLocString(is.loc)+"] ");
	//		}
	//		return sb.toString();
	//	}

	public String headerString(){
		return ("&eTeamSizes=&6"+ap.getTeamSizeRange() + " &eTypes=&6" +ap.getType());
	}

	/**
	 * Called when the match is first opened
	 */
	public void onOpen() {}

	/**
	 * Called when a player joins the bukkitEvent
	 * @param p the player
	 * @param t the team they are on
	 */
	public void onJoin(Player p, Team t){}

	/**
	 * Called when a player is leaving the match ( via typing a command usually) , 
	 * but its still acceptable to leave(usually before the match starts) 
	 * @param p the player
	 * @param t the team they were on
	 */
	public void onLeave(Player p, Team t) {}

	/**
	 * Called before the match starts
	 */
	public void onPrestart(){}

	/**
	 * Called when the match starts
	 */
	public void onStart(){}

	/**
	 * Called after the victor team has won the match
	 * @param victor
	 */
	public void onVictory(MatchResult result){}

	/**
	 * Called when the match is complete
	 */
	public void onComplete(){}

	/**
	 * Called when a command is given to cancel the match
	 */
	public void onCancel(){}

	/**
	 * Called after a player first gets teleported into a match ( does not include a waitroom )
	 * @param Player p
	 * @param team : the team they were in
	 */
	public void onEnter(Player p, Team team) {}

	/**
	 * Called if a player is teleported into a waiting room before a match
	 * @param Player p
	 * @param team: the team they are in
	 */
	public void onEnterWaitRoom(Player p, Team team) {}

	/**
	 * Called when a player is exiting the match (usually through a death)
	 * @param p
	 * @param team : the team they were in
	 */
	public void onExit(Player p, Team team) {}

	public boolean matches(MatchParams q) {
		boolean matches = getParameters().matches(q);
		if (!matches)
			return false;
		final MatchTransitions tops = q.getTransitionOptions();
		if (tops == null)
			return true;
		final boolean mo = tops.hasOptions(TransitionOption.TELEPORTWAITROOM);
		if (mo && (wrlocs == null || wrlocs.isEmpty()))
			return false;
		return true;
	}
}
