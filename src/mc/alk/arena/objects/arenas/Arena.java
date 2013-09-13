package mc.alk.arena.objects.arenas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.ArenaAlterController.ChangeType;
import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.SpawnController;
import mc.alk.arena.controllers.containers.AreaContainer;
import mc.alk.arena.controllers.containers.RoomContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.PlayerContainerState;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.regions.PylamoRegion;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.Persist;
import mc.alk.arena.util.Log;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Arena extends AreaContainer {

	/// If this is not null, this is where distance will be based off of, otherwise it's an area around the spawns
	protected Location joinloc = null;

	protected Map<Long, TimedSpawn> timedSpawns = null; /// Item/mob/other spawn events

	protected SpawnController spawnController = null;

	protected Match match = null;

	protected RoomContainer waitroom;

	protected RoomContainer visitorRoom;

	@Persist
	protected WorldGuardRegion wgRegion;

	@Persist
	protected PylamoRegion pylamoRegion;

	/**
	 * Arena constructor
	 */
	public Arena(){
		super("arena",LocationType.ARENA);
	}

	/**
	 * Called after construction or after persistance variables have been assigned, whichever is later
	 */
	void privateInit(){
		try{init();}catch(Exception e){Log.printStackTrace(e);}
	}
	/**
	 * private Arena crate events, calls create for subclasses to be able to override
	 */
	void privateCreate(){
		try{create();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena delete events, calls delete for subclasses to be able to override
	 */
	void privateDelete(){
		try{delete();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onOpen events, calls onOpen for subclasses to be able to override
	 */
	void privateOnOpen(){
		try{onOpen();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onBegin events, calls onBegin for subclasses to be able to override
	 */
	void privateOnBegin(){
		try{onBegin();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onPrestart events, calls onPrestart for subclasses to be able to override
	 */
	void privateOnPrestart(){
		try{onPrestart();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onStart events, calls onStart for subclasses to be able to override
	 */
	void privateOnStart(){
		startSpawns();
		try{onStart();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onStart events, calls onStart for subclasses to be able to override
	 */
	void privateOnVictory(MatchResult result){
		stopSpawns();
		try{onVictory(result);}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onComplete events, calls onComplete for subclasses to be able to override
	 */
	void privateOnComplete(){
		stopSpawns();
		try{onComplete();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onCancel events, calls onCancel for subclasses to be able to override
	 */
	void privateOnCancel(){
		stopSpawns();
		try{onCancel();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onFinish events, calls onFinish for subclasses to be able to override
	 */
	void privateOnFinish(){
		try{onFinish();}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onEnter events, calls onEnter for subclasses to be able to override
	 */
	void privateOnEnter(ArenaPlayer player, ArenaTeam team){
		try{onEnter(player,team);}catch(Exception e){Log.printStackTrace(e);}
	}
	/**
	 * private Arena onEnterWaitRoom events, calls onEnterWaitRoom for subclasses to be able to override
	 */
	void privateOnEnterWaitRoom(ArenaPlayer player, ArenaTeam team){
		try{onEnterWaitRoom(player,team);}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onJoin events, calls onJoin for subclasses to be able to override
	 * Happens when a player joins a team
	 */
	void privateOnJoin(ArenaPlayer player, ArenaTeam team){
		try{onJoin(player,team);}catch(Exception e){Log.printStackTrace(e);}
	}

	/**
	 * private Arena onLeave events, calls onLeave for subclasses to be able to override
	 * Happens when a player leaves a team
	 */
	void privateOnLeave(ArenaPlayer player, ArenaTeam team){
		try{onLeave(player,team);}catch(Exception e){Log.printStackTrace(e);}
	}


	/**
	 * private Arena onFinish events, calls onFinish for subclasses to be able to override
	 */
	void privateOnFinish(ArenaPlayer player, ArenaTeam team){
		try{onFinish();}catch(Exception e){Log.printStackTrace(e);}
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
	protected void onJoin(ArenaPlayer p, ArenaTeam t){}

	/**
	 * Called when a player is leaving the match ( via typing a command usually) ,
	 * but its still acceptable to leave(usually before the match starts)
	 * @param p the player
	 * @param t the team they were on
	 */
	protected void onLeave(ArenaPlayer p, ArenaTeam t) {}

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
	protected void onEnter(ArenaPlayer p, ArenaTeam team) {}

	/**
	 * Called if a player is teleported into a waiting room before a match
	 * @param Player p
	 * @param team: the team they are in
	 */
	protected void onEnterWaitRoom(ArenaPlayer p, ArenaTeam team) {}

	/**
	 * Called when a player is exiting the match (usually through a death)
	 * @param p
	 * @param team : the team they were in
	 */
	protected void onExit(ArenaPlayer p, ArenaTeam team) {}

	/**
	 * Returns the spawns
	 * Deprecated use getSpawns()
	 * @return
	 */
	@Deprecated
	public Map<Integer, Location> getSpawnLocs(){
		Map<Integer, Location> locs = new HashMap<Integer,Location>();
		for (int i=0;i<this.getSpawns().size();i++){
			locs.put(i, spawns.get(i));
		}
		return locs;
	}

	/**
	 * Returns the spawn location of this index.
	 * Deprecated, use getSpawn(index, random)
	 * @param index
	 * @return
	 */
	@Deprecated
	public Location getSpawnLoc(int index){
		return this.getSpawn(index, false);
	}

	/**
	 * Returns the spawn location of this index
	 * @param index
	 * @return
	 */
	public Location getWaitRoomSpawnLoc(int index){
		return waitroom.getSpawn(index,false);
	}

	/**
	 * returns a random spawn location
	 * @return
	 */
	public Location getRandomWaitRoomSpawnLoc(){
		return waitroom.getSpawn(-1,true);
	}

	/**
	 * Return the visitor location (if any)
	 * @return
	 */
	public Location getVisitorLoc(int index, boolean random) {
		return visitorRoom != null ? visitorRoom.getSpawn(index, random) : null;}

	/**
	 * Set the wait room spawn location
	 * @param index
	 * @param loc
	 */
	public void setWaitRoomSpawnLoc(int index, Location loc) {
		waitroom.setSpawnLoc(index, loc);
	}

	/**
	 * Return the spot where players need to join close to
	 * @return
	 */
	public Location getJoinLocation() {
		return joinloc;
	}

	//	/**
	//	 * Set a visitor spawn location
	//	 * @param loc
	//	 */
	//	public void setVisitorLoc(int index, Location loc) {
	//		if (visitorRoom == null){
	//			this.visitorRoom = new RoomContainer(ParamController.getMatchParams(getArenaType().getName()),
	//					LocationType.VISITORROOM);}
	//		this.visitorRoom.setSpawnLoc(index, loc);
	//	}

	/**
	 * Set the name of this arena
	 * @param arenaName
	 */
	@Override
	public void setName(String arenaName) {this.name = arenaName;}

	/**
	 * Get the name of this arena
	 * @return
	 */
	@Override
	public String getName() {return name;}

	/**
	 * Return the waitroom spawn locations
	 * @return
	 */
	public List<Location> getWaitRoomSpawnLocs(){return waitroom != null ? waitroom.getSpawns() : null;}

	//	public Map<Integer, ItemSpawn> getItemSpawns() {return spawnsGroups;}

	/**
	 * Get the type of this arena
	 * @return ArenaType
	 */
	public ArenaType getArenaType() {
		return params.getType();
	}

	/**
	 * Does this arena have a name, at least one spawn, and valid arena parameters
	 * @return
	 */
	public boolean valid() {
		return (!(name == null || spawns.size() <1 || spawns.get(0) == null || !params.valid() ));
	}

	public List<String> getInvalidReasons() {
		List<String> reasons = new ArrayList<String>();
		if (name == null) reasons.add("Arena name is null");
		if (spawns.size() <1) reasons.add("needs to have at least 1 spawn location");
		if (spawns.get(0) == null) reasons.add("1st spawn is set to a null location");
		reasons.addAll(params.getInvalidReasons());
		return reasons;
	}


	/**
	 * Set the worldguard region for this arena (only available with worldguard)
	 * @param wgRegionName
	 * Deprecated: use setWorldGuardRegion instead
	 */
	@Deprecated
	public void addWorldGuardRegion(String regionWorld, String regionName) {
		wgRegion = new WorldGuardRegion(regionWorld, regionName);
	}

	/**
	 * Set the worldguard region for this arena (only available with worldguard)
	 * @param regionWorld
	 * @param regionName
	 */
	public void setWorldGuardRegion(String regionWorld, String regionName) {
		wgRegion = new WorldGuardRegion(regionWorld, regionName);
	}

	/**
	 * Set the worldguard region for this arena (only available with worldguard)
	 * @param wgRegionName
	 */
	public void setWorldGuardRegion(WorldGuardRegion region) {
		wgRegion = region;
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
	protected void setWinner(ArenaTeam team) {
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
	@Override
	public MatchState getMatchState(){
		return match.getState();
	}

	/**
	 * return a list of teams inside this match
	 * @return
	 */
	public List<ArenaTeam> getTeams(){
		return match == null ? null : match.getTeams();
	}

	/**
	 * Return a list of live teams inside this match
	 * @return
	 */
	public List<ArenaTeam> getAliveTeams(){
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
	@Override
	public ArenaTeam getTeam(ArenaPlayer p){
		return match == null ? null : match.getTeam(p);
	}

	/**
	 * Return the team of this player
	 * @return
	 */
	public ArenaTeam getTeam(Player p){
		return match == null ? null : match.getTeam(BattleArena.toArenaPlayer(p));
	}

	/**
	 * Return the team with this index
	 * @return
	 */
	public ArenaTeam getTeam(int teamIndex){
		return match == null ? null : match.getTeam(teamIndex);
	}

	/**
	 * Start any spawns happening for this arena
	 */
	public void startSpawns(){
		SpawnController sc = getSpawnController();
		if (sc != null)
			sc.start();
	}

	/**
	 * Stop any spawns occuring in this arena
	 */
	public void stopSpawns(){
		if (spawnController != null){
			spawnController.stop();}
	}

	/**
	 * Checks to see whether this arena has paramaters that match the given matchparams
	 * @param eventParams
	 * @param jp
	 * @return
	 */
	public boolean matches(final MatchParams matchParams, final JoinOptions jp) {
		if (!getParams().matches(matchParams))
			return false;
		return matchesIgnoreSize(matchParams,jp);
	}

	/**
	 * Checks to see whether this arena has paramaters that match the given matchparams
	 * @param eventParams
	 * @param jp
	 * @return
	 */
	public boolean matchesIgnoreSize(final MatchParams matchParams, final JoinOptions jp) {
		if (this.getArenaType() != matchParams.getType())
			return false;
		//		if (!getParams().matches(matchParams))
		//			return false;
		final MatchTransitions tops = matchParams.getTransitionOptions();
		if (tops == null)
			return true;
		if ((waitroom == null || !waitroom.hasSpawns()) && matchParams.needsWaitroom())
			return false;
		if (jp == null)
			return true;
		if (!jp.matches(this))
			return false;

		if (matchParams.hasOptionAt(MatchState.PREREQS,TransitionOption.WITHINDISTANCE)){
			if (!jp.nearby(this,matchParams.getDoubleOption(MatchState.PREREQS, TransitionOption.WITHINDISTANCE))){
				return false;}
		}
		if (matchParams.hasOptionAt(MatchState.PREREQS, TransitionOption.SAMEWORLD)){
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

	public boolean withinDistance(Location location, double distance){
		for (Location l: spawns){
			if (location.getWorld().getUID() == l.getWorld().getUID() &&
					location.distance(l) < distance)
				return true;
		}
		return false;
	}

	public List<String> getInvalidMatchReasons(MatchParams matchParams, JoinOptions jp) {
		List<String> reasons = new ArrayList<String>();
		reasons.addAll(getParams().getInvalidMatchReasons(matchParams));
		final MatchTransitions tops = matchParams.getTransitionOptions();
		if (tops != null){
			if (matchParams.needsWaitroom() && (waitroom == null || !waitroom.hasSpawns()))
				reasons.add("Needs a waitroom but none has been provided");
			if (matchParams.needsLobby() && (!RoomController.hasLobby(matchParams.getType())))
				reasons.add("Needs a lobby but none has been provided");
		}
		if (jp == null)
			return reasons;
		if (!jp.matches(this))
			reasons.add("You didn't specify this arena");
		if (matchParams.hasOptionAt(MatchState.PREREQS,TransitionOption.WITHINDISTANCE)){
			if (!jp.nearby(this,matchParams.getDoubleOption(MatchState.PREREQS,TransitionOption.WITHINDISTANCE))){
				reasons.add("You aren't within " +
						matchParams.getDoubleOption(MatchState.PREREQS,TransitionOption.WITHINDISTANCE) +" blocks");}
		}
		if (matchParams.hasOptionAt(MatchState.PREREQS,TransitionOption.SAMEWORLD)){
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
		sb.append("&eTeamSizes=&6"+params.getTeamSizeRange() + " &eTypes=&6" +params.getType());
		sb.append("&e, #Teams:&6"+params.getNTeamRange());
		sb.append("&e, #spawns:&6" +spawns.size() +"\n");
		sb.append("&eteamSpawnLocs=&b"+getSpawnLocationString()+"\n");
		if (waitroom != null) sb.append("&ewrSpawnLocs=&b"+waitroom.getSpawnLocationString()+"\n");
		if (timedSpawns != null){sb.append("&e#item/mob spawns:&6" +timedSpawns.size() +"\n");}
		return sb.toString();
	}

	/**
	 * return arena summary string (includes bukkit coloring)
	 * @return
	 */
	public String toSummaryString(){
		StringBuilder sb = new StringBuilder("&4" + name);
		if (params != null){
			sb.append("&e type=&6"+params.getType());
			sb.append(" &eTeamSizes:&6"+params.getTeamSizeRange()+"&e, nTeams:&6"+params.getNTeamRange());
		}

		sb.append("&e #spawns:&6" +spawns.size() +"&e 1stSpawn:&6");
		if (!spawns.isEmpty()){
			Location l = spawns.get(0);
			sb.append("["+l.getWorld().getName()+":"+l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ()+"] ");
		}
		if (timedSpawns != null && !timedSpawns.isEmpty())
			sb.append("&e#item/mob Spawns:&6" +timedSpawns.size());
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

	public void setWaitRoom(RoomContainer waitroom) {
		this.waitroom = waitroom;
	}

	public RoomContainer getWaitroom() {
		return waitroom;
	}

	public RoomContainer getLobby() {
		return RoomController.getLobby(getArenaType());
	}

	@Override
	public LocationType getLocationType() {
		return LocationType.ARENA;
	}

	public List<Location> getVisitorLocs() {
		return visitorRoom!=null ? visitorRoom.getSpawns() : null;
	}

	public boolean isJoinable(MatchParams mp) {
		if (!isOpen())
			return false;
		else if ( mp.needsWaitroom() && (waitroom == null || !waitroom.isOpen() || waitroom.getSpawns().isEmpty()) )
			return false;
		else if ( mp.needsLobby()){
			RoomContainer lobby = RoomController.getLobby(getArenaType());
			if (lobby == null || !lobby.isOpen() || lobby.getSpawns().isEmpty())
				return false;
		}
		return true;
	}

	public String getNotJoinableReasons(MatchParams mp) {
		if (!isOpen())
			return "&cArena is not open!";
		else if ( mp.needsWaitroom() && waitroom == null )
			return "&cYou need to create a waitroom!";
		else if ( mp.needsWaitroom() && !waitroom.isOpen() )
			return "&cWaitroom is not open!";
		else if ( mp.needsWaitroom() && waitroom.getSpawns().isEmpty() )
			return "&cYou need to set a spawn point for the waitroom!";
		else if ( mp.needsLobby() && getLobby()==null )
			return "&cYou need to create a lobby!";
		else if ( mp.needsLobby() ){
			RoomContainer lobby = getLobby();
			if (!lobby.isOpen()){
				return "&cLobby is not open!";
			} else if ( mp.needsLobby() && lobby.getSpawns().isEmpty() ){
				return "&cYou need to set a spawn point for the lobby!";}
		}
		return "";
	}

	public void setAllContainerState(PlayerContainerState state) {
		setContainerState(state);
		if (waitroom != null)
			waitroom.setContainerState(state);
		RoomContainer lobby = getLobby();
		if (lobby != null)
			lobby.setContainerState(state);
	}

	public void setContainerState(ChangeType cs, PlayerContainerState state) throws IllegalStateException{
		switch (cs){
		case LOBBY:
			RoomContainer lobby = getLobby();
			if (lobby == null)
				throw new IllegalStateException("Arena " + getName() +" does not have a Lobby");
			lobby.setContainerState(state);
			break;
		case VLOC:
			if (visitorRoom == null)
				throw new IllegalStateException("Arena " + getName() +" does not have a visitorRoom");
			visitorRoom.setContainerState(state);
			break;
		case WAITROOM:
			if (waitroom == null)
				throw new IllegalStateException("Arena " + getName() +" does not have a waitroom");
			waitroom.setContainerState(state);
			break;
		default:
			throw new IllegalStateException(cs +" can not be set to "+state);
		}
	}
}
