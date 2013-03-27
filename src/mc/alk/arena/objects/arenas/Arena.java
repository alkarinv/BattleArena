package mc.alk.arena.objects.arenas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.SpawnController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.regions.PylamoRegion;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.Persist;
import mc.alk.arena.util.Util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Arena implements ArenaListener {
	protected String name;

	protected ArenaParams ap; /// Size and arena type info

	protected TreeMap<Integer,Location> locs = null; /// Team spawn Locations
	protected TreeMap<Integer,Location> wrlocs = null; /// wait room spawn locations
	protected Location vloc = null;
	/// If this is not null, this is where distance will be based off of, otherwise it's an area around the spawns
	protected Location joinloc = null;

	protected Map<String, Location> visitorlocations = null;/// tp locations for the visitors
	protected Random rand = new Random(); /// a random

	protected Map<Long, TimedSpawn> timedSpawns = null; /// Item/mob/other spawn events
	protected SpawnController spawnController = null;

	protected Match match = null;

	@Persist
	@Deprecated
	protected String wgRegionName;

	@Persist
	@Deprecated
	protected String wgRegionWorld;

	@Persist
	protected WorldGuardRegion wgRegion;

	@Persist
	protected PylamoRegion pylamoRegion;

	/**
	 * Arena constructor
	 */
	public Arena(){
		visitorlocations = new HashMap<String,Location>();
		locs = new TreeMap<Integer,Location>();
	}

	/**
	 * Called after construction or after persistance variables have been assigned, whichever is later
	 */
	void privateInit(){
		/// Transition to the new way of making regions (actually still only halfway complete)
		if (wgRegionWorld != null && wgRegionName != null && wgRegion == null){
			wgRegion = new WorldGuardRegion(wgRegionWorld, wgRegionName);
			wgRegionWorld = wgRegionName = null;
		}
		try{init();}catch(Exception e){e.printStackTrace();}
	}
	/**
	 * private Arena crate events, calls create for subclasses to be able to override
	 */
	void privateCreate(){
		try{create();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena delete events, calls delete for subclasses to be able to override
	 */
	void privateDelete(){
		try{delete();}catch(Exception e){e.printStackTrace();}
		if (wgRegionName != null && wgRegionWorld != null && WorldGuardController.hasWorldGuard())
			WorldGuardController.deleteRegion(wgRegionWorld, wgRegionName);
	}

	/**
	 * private Arena onOpen events, calls onOpen for subclasses to be able to override
	 */
	void privateOnOpen(){
		try{onOpen();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onBegin events, calls onBegin for subclasses to be able to override
	 */
	void privateOnBegin(){
		try{onBegin();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onPrestart events, calls onPrestart for subclasses to be able to override
	 */
	void privateOnPrestart(){
		try{onPrestart();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onStart events, calls onStart for subclasses to be able to override
	 */
	void privateOnStart(){
		startSpawns();
		try{onStart();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onStart events, calls onStart for subclasses to be able to override
	 */
	void privateOnVictory(MatchResult result){
		stopSpawns();
		try{onVictory(result);}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onComplete events, calls onComplete for subclasses to be able to override
	 */
	void privateOnComplete(){
		stopSpawns();
		try{onComplete();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onCancel events, calls onCancel for subclasses to be able to override
	 */
	void privateOnCancel(){
		stopSpawns();
		try{onCancel();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onFinish events, calls onFinish for subclasses to be able to override
	 */
	void privateOnFinish(){
		try{onFinish();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onEnter events, calls onEnter for subclasses to be able to override
	 */
	void privateOnEnter(ArenaPlayer player, Team team){
		try{onEnter(player,team);}catch(Exception e){e.printStackTrace();}
	}
	/**
	 * private Arena onEnterWaitRoom events, calls onEnterWaitRoom for subclasses to be able to override
	 */
	void privateOnEnterWaitRoom(ArenaPlayer player, Team team){
		try{onEnterWaitRoom(player,team);}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onJoin events, calls onJoin for subclasses to be able to override
	 * Happens when a player joins a team
	 */
	void privateOnJoin(ArenaPlayer player, Team team){
		try{onJoin(player,team);}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * private Arena onLeave events, calls onLeave for subclasses to be able to override
	 * Happens when a player leaves a team
	 */
	void privateOnLeave(ArenaPlayer player, Team team){
		try{onLeave(player,team);}catch(Exception e){e.printStackTrace();}
	}


	/**
	 * private Arena onFinish events, calls onFinish for subclasses to be able to override
	 */
	void privateOnFinish(ArenaPlayer player, Team team){
		try{onFinish();}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * Subclasses can override to initialize their own values right after construction
	 * Or subclasses can override the default constructor
	 */
	protected void init(){}

	/**
	 * Called when an arena is first created by a command (not after its constructed or initialized)
	 */
	protected void create(){}

	/**
	 * Called when an arena is deleted
	 */
	protected void delete(){}

	/**
	 * Called when the match is first opened
	 */
	protected void onOpen() {}

	/**
	 * Called when a player joins the Event
	 * @param p the player
	 * @param t the team they are on
	 */
	protected void onJoin(ArenaPlayer p, Team t){}

	/**
	 * Called when a player is leaving the match ( via typing a command usually) ,
	 * but its still acceptable to leave(usually before the match starts)
	 * @param p the player
	 * @param t the team they were on
	 */
	protected void onLeave(ArenaPlayer p, Team t) {}

	/**
	 * Called when the match is first called upon to begin starting
	 */
	protected void onBegin() {}

	/**
	 * Called after onBegin and before onStart
	 */
	protected void onPrestart(){}

	/**
	 * Called when the match starts
	 */
	protected void onStart(){}

	/**
	 * Called after the victor team has won the match
	 * @param victor
	 */
	protected void onVictory(MatchResult result){}

	/**
	 * Called when the match is complete
	 */
	protected void onComplete(){}

	/**
	 * Called when a command is given to cancel the match
	 */
	protected void onCancel(){}

	/**
	 * Called after a match is completed or cancelled
	 */
	protected void onFinish(){}

	/**
	 * Called after a player first gets teleported into a match ( does not include a waitroom )
	 * @param Player p
	 * @param team : the team they were in
	 */
	protected void onEnter(ArenaPlayer p, Team team) {}

	/**
	 * Called if a player is teleported into a waiting room before a match
	 * @param Player p
	 * @param team: the team they are in
	 */
	protected void onEnterWaitRoom(ArenaPlayer p, Team team) {}

	/**
	 * Called when a player is exiting the match (usually through a death)
	 * @param p
	 * @param team : the team they were in
	 */
	protected void onExit(ArenaPlayer p, Team team) {}


	/**
	 * Returns the spawn location of this index
	 * @param index
	 * @return
	 */
	public Location getSpawnLoc(int index){
		if (locs == null || locs.isEmpty() || index < 0)
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
		if (wrlocs == null || wrlocs.isEmpty())
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
	 * Return the spot where players need to join close to
	 * @return
	 */
	public Location getJoinLocation() {
		return joinloc;
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
	 * Set the name of this arena
	 * @param arenaName
	 */
	public void setName(String arenaName) {this.name = arenaName;}

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

	public List<String> getInvalidReasons() {
		List<String> reasons = new ArrayList<String>();
		if (name == null) reasons.add("Arena name is null");
		if (locs.size() <1) reasons.add("needs to have at least 1 spawn location");
		if (locs.get(0) == null) reasons.add("1st spawn is set to a null location");
		reasons.addAll(ap.getInvalidReasons());
		return reasons;
	}


	/**
	 * TeamJoinResult a Protected Region (only available with worldguard)
	 * @param wgRegionName
	 */
	public void addWorldGuardRegion(String regionWorld, String regionName) {
		wgRegion = new WorldGuardRegion(regionWorld, regionName);
	}

	/**
	 * does this arena have a worlguard wgRegionName attached
	 * @returns
	 */
	public boolean hasRegion() {
		return wgRegion != null && wgRegion.valid();
	}

	/**
	 * Get the worldguard wgRegionName for this arena
	 * @return
	 */
	public WorldGuardRegion getWorldGuardRegion() {
		return wgRegion;
	}

	/**
	 * Return the timed spawns for this arena
	 * @return
	 */
	public Map<Long, TimedSpawn> getTimedSpawns() {
		return timedSpawns;
	}

	/**
	 * add a timed spawn to this arena
	 */
	public void addTimedSpawn(Long num, TimedSpawn s) {
		if (timedSpawns == null){
			timedSpawns = new HashMap<Long,TimedSpawn>();
		}
		timedSpawns.put(num, s);
	}

	/**
	 * add a timed spawn to this arena
	 * @return
	 */
	public TimedSpawn deleteTimedSpawn(Long num) {
		return timedSpawns == null ? null : timedSpawns.remove(num);
	}

	/**
	 * Set which match this arena belongs to
	 */
	public void setMatch(Match arenaMatch) {
		this.match = arenaMatch;
	}

	/**
	 * Get which match this arena belongs to
	 */
	public Match getMatch() {
		return match;
	}

	/**
	 * set the winning team, this will also cause the match to be ended
	 * @param team
	 */
	protected void setWinner(Team team) {
		match.setVictor(team);
	}

	/**
	 * set the winning player, this will also cause the match to be ended
	 * @param team
	 */
	protected void setWinner(ArenaPlayer player) {
		match.setVictor(player);
	}

	/**
	 * Get the current state of the match
	 * @return
	 */
	public MatchState getMatchState(){
		return match.getState();
	}

	/**
	 * return a list of teams inside this match
	 * @return
	 */
	public List<Team> getTeams(){
		return match == null ? null : match.getTeams();
	}

	/**
	 * Return a list of live teams inside this match
	 * @return
	 */
	public List<Team> getAliveTeams(){
		return match == null ? null : match.getAliveTeams();
	}

	/**
	 * Return a list of living arena players inside this match
	 * @return
	 */
	public Set<ArenaPlayer> getAlivePlayers(){
		return match == null ? null : match.getAlivePlayers();
	}

	/**
	 * Return a list of alive bukkit players inside this match
	 * @return
	 */
	public Set<Player> getAliveBukkitPlayers(){
		return match == null ? null : BattleArena.toPlayerSet(match.getAlivePlayers());
	}

	/**
	 * Return the team of this player
	 * @return
	 */
	public Team getTeam(ArenaPlayer p){
		return match == null ? null : match.getTeam(p);
	}

	/**
	 * Return the team of this player
	 * @return
	 */
	public Team getTeam(Player p){
		return match == null ? null : match.getTeam(BattleArena.toArenaPlayer(p));
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


	/**
	 * Return a string of appended spawn locations
	 * @return
	 */
	public String getSpawnLocationString(){
		StringBuilder sb = new StringBuilder();
		for (Integer i: locs.keySet() ){
			Location l = locs.get(i);
			if (l != null) sb.append("["+(i+1)+":"+Util.getLocString(l)+"] ");
		}
		return sb.toString();
	}

	/**
	 * Return a string of appended waitroom spawn locations
	 * @return
	 */
	public String getWaitroomLocationString(){
		StringBuilder sb = new StringBuilder();
		if (wrlocs == null)
			return sb.toString();
		for (Integer i: wrlocs.keySet() ){
			Location l = wrlocs.get(i);
			if (l != null) sb.append("["+(i+1)+":"+Util.getLocString(l)+"] ");
		}
		return sb.toString();
	}

	/**
	 * Checks to see whether this arena has paramaters that match the given matchparams
	 * @param eventParams
	 * @param jp
	 * @return
	 */
	public boolean matches(final MatchParams matchParams, final JoinOptions jp) {
		boolean matches = getParameters().matches(matchParams);
		if (!matches)
			return false;
		final MatchTransitions tops = matchParams.getTransitionOptions();
		if (tops == null)
			return true;
		if ((wrlocs == null || wrlocs.isEmpty()) && tops.hasAnyOption(TransitionOption.TELEPORTWAITROOM))
			return false;
		if (jp == null)
			return true;
		if (!jp.matches(this))
			return false;

		final TransitionOptions ops = tops.getOptions(MatchState.PREREQS);
		if (ops == null)
			return true;

		if (ops.hasOption(TransitionOption.WITHINDISTANCE)){
			if (!jp.nearby(this,ops.getWithinDistance())){
				return false;}
		}
		if (ops.hasOption(TransitionOption.SAMEWORLD)){
			if (!jp.sameWorld(this)){
				return false;}
		}
		return true;
	}

	public boolean matches(Arena arena) {
		if (arena == null)
			return false;
		if (this == arena)
			return true;
		if (arena.name == null || this.name==null)
			return false;
		return this.name.equals(arena.name);
	}

	public List<String> getInvalidMatchReasons(MatchParams matchParams, JoinOptions jp) {
		List<String> reasons = new ArrayList<String>();
		reasons.addAll(getParameters().getInvalidMatchReasons(matchParams));
		final MatchTransitions tops = matchParams.getTransitionOptions();
		if (tops != null){
			final boolean mo = tops.hasAnyOption(TransitionOption.TELEPORTWAITROOM);
			if (mo && (wrlocs == null || wrlocs.isEmpty()))
				reasons.add("Needs a waitroom but none has been provided");
		}
		if (jp == null)
			return reasons;
		if (!jp.matches(this))
			reasons.add("You didn't specify this arena");
		final TransitionOptions ops = tops.getOptions(MatchState.PREREQS);
		if (ops == null)
			return reasons;
		if (ops.hasOption(TransitionOption.WITHINDISTANCE)){
			if (!jp.nearby(this,ops.getWithinDistance())){
				reasons.add("You aren't within " + ops.getWithinDistance() +" blocks");}
		}
		if (ops.hasOption(TransitionOption.SAMEWORLD)){
			if (!jp.sameWorld(this)){
				reasons.add("You aren't in the same world");}
		}

		return reasons;
	}

	/**
	 * Arena printing
	 */
	@Override
	public String toString(){
		return toSummaryString();
	}

	/**
	 * return detailed arena details (includes bukkit coloring)
	 * @return
	 */
	public String toDetailedString(){
		StringBuilder sb = new StringBuilder("&6" + name+" &e");
		sb.append("&eTeamSizes=&6"+ap.getTeamSizeRange() + " &eTypes=&6" +ap.getType());
		sb.append("&e, #Teams:&6"+ap.getNTeamRange());
		sb.append("&e, #spawns:&6" +locs.size() +"\n");
		sb.append("&eteamSpawnLocs=&b"+getSpawnLocationString()+"\n");
		sb.append("&ewrSpawnLocs=&b"+getWaitroomLocationString()+"\n");
		if (timedSpawns != null){
			sb.append("&e#itemSpawns:&6" +locs.size() +"\n");
			//			sb.append(itemSpawnString());
		}
		return sb.toString();
	}

	/**
	 * return arena summary string (includes bukkit coloring)
	 * @return
	 */
	public String toSummaryString(){
		StringBuilder sb = new StringBuilder("&4" + name+" &e type=&6"+ap.getType());
		sb.append(" &eTeamSizes:&6"+ap.getTeamSizeRange()+"&e, nTeams:&6"+ap.getNTeamRange());
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

	public SpawnController getSpawnController() {
		if (timedSpawns != null && !timedSpawns.isEmpty() && spawnController == null){
			spawnController = new SpawnController(timedSpawns);
		}
		return spawnController;
	}

	public void setPylamoRegion(PylamoRegion region) {
		this.pylamoRegion = region;
	}

	public PylamoRegion getPylamoRegion() {
		return pylamoRegion;
	}


}
