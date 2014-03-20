package mc.alk.arena.objects.arenas;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.match.PerformTransition;
import mc.alk.arena.controllers.ArenaAlterController.ChangeType;
import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.SpawnController;
import mc.alk.arena.controllers.containers.AreaContainer;
import mc.alk.arena.controllers.containers.RoomContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionTransition;
import mc.alk.arena.objects.ContainerState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.StateGraph;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.regions.PylamoRegion;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.Persist;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Arena extends AreaContainer {

    /// If this is not null, this is where distance will be based off of, otherwise it's an area around the spawns
    protected Location joinloc;

    protected Map<Long, TimedSpawn> timedSpawns; /// Item/mob/other spawn events

    protected SpawnController spawnController;

    protected Match match;

    protected RoomContainer spectate;

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
        try{onEnterWaitRoom(player, team);}catch(Exception e){Log.printStackTrace(e);}
    }

    /**
     * private Arena onEnterWaitRoom events, calls onEnterWaitRoom for subclasses to be able to override
     */
    void privateOnEnterSpectate(ArenaPlayer player, ArenaTeam team){
        try{onEnterSpectate(player, team);}catch(Exception e){Log.printStackTrace(e);}
    }

    /**
     * private Arena onJoin events, calls onJoin for subclasses to be able to override
     * Happens when a player joins a team
     */
    void privateOnJoin(ArenaPlayer player, ArenaTeam team){
        try{onJoin(player, team);}catch(Exception e){Log.printStackTrace(e);}
    }

    /**
     * private Arena onLeave events, calls onLeave for subclasses to be able to override
     * Happens when a player leaves a team
     */
    void privateOnLeave(ArenaPlayer player, ArenaTeam team){
        try{onLeave(player, team);}catch(Exception e){Log.printStackTrace(e);}
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
     * @param player the player
     * @param team the team they are on
     */
    @SuppressWarnings("unused")
    protected void onJoin(ArenaPlayer player, ArenaTeam team){}

    /**
     * Called when a player is leaving the match ( via typing a command usually) ,
     * but its still acceptable to leave(usually before the match starts)
     * @param player the player
     * @param team the team they were on
     */
    @SuppressWarnings("unused")
    protected void onLeave(ArenaPlayer player, ArenaTeam team) {}

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
     * @param result Result of the Match
     */
    @SuppressWarnings("unused")
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
     * @param player ArenaPlayer
     * @param team : the team they were in
     */
    @SuppressWarnings("unused")
    protected void onEnter(ArenaPlayer player, ArenaTeam team) {}

    /**
     * Called if a player is teleported into a waiting room before a match
     * @param player ArenaPlayer
     * @param team: the team they are in
     */
    @SuppressWarnings("unused")
    protected void onEnterWaitRoom(ArenaPlayer player, ArenaTeam team) {}

    /**
     * Called if a player is teleported into a spectator room before a match
     * @param player ArenaPlayer
     * @param team: the team they are in
     */
    @SuppressWarnings("unused")
    protected void onEnterSpectate(ArenaPlayer player, ArenaTeam team) {}

    /**
     * Called when a player is exiting the match (usually through a death)
     * @param player ArenaPlayer
     * @param team : the team they were in
     */
    @SuppressWarnings("unused")
    protected void onExit(ArenaPlayer player, ArenaTeam team) {}

    /**
     * Returns the spawn location of this index
     * @param index index
     * @return location
     */
    public SpawnLocation getWaitRoomSpawnLoc(int index){
        return waitroom.getSpawn(index,false);
    }

    /**
     * returns a random spawn location
     * @return location
     */
    public SpawnLocation getRandomWaitRoomSpawnLoc(){
        return waitroom.getSpawn(-1,true);
    }

    /**
     * Return the visitor location (if any)
     * @return location
     */
    public SpawnLocation getVisitorLoc(int teamIndex, boolean random) {
        return visitorRoom != null ? visitorRoom.getSpawn(teamIndex,random) : null;}

    /**
     * Set the wait room spawn location
     * @param teamIndex index of team
     * @param spawnIndex index of spawn
     * @param loc location
     */
    public void setWaitRoomSpawnLoc(int teamIndex, int spawnIndex, SpawnLocation loc) {
        waitroom.setSpawnLoc(teamIndex, spawnIndex, loc);
    }

    /**
     * Return the spot where players need to add close to
     * @return location
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
     * @param arenaName name
     */
    @Override
    public void setName(String arenaName) {this.name = arenaName;}

    /**
     * Get the name of this arena
     * @return name
     */
    @Override
    public String getName() {return name;}

    /**
     * Return the waitroom spawn locations
     * @return list of location
     */
    public List<List<SpawnLocation>> getWaitRoomSpawnLocs(){return waitroom != null ? waitroom.getSpawns() : null;}

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
     * @return true if the arena is valid
     */
    public boolean valid() {
        return (!(name == null || spawns.isEmpty() || spawns.get(0) == null || !params.valid() ));
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
     * @param regionWorld World name
     * Deprecated: use setWorldGuardRegion instead
     */
    @Deprecated
    public void addWorldGuardRegion(String regionWorld, String regionName) {
        wgRegion = new WorldGuardRegion(regionWorld, regionName);
    }

    /**
     * Set the worldguard region for this arena (only available with worldguard)
     * @param regionWorld World name
     * @param regionName region name
     */
    public void setWorldGuardRegion(String regionWorld, String regionName) {
        wgRegion = new WorldGuardRegion(regionWorld, regionName);
    }

    /**
     * Set the worldguard region for this arena (only available with worldguard)
     * @param region WorldGuardRegion
     */
    public void setWorldGuardRegion(WorldGuardRegion region) {
        wgRegion = region;
    }

    /**
     * does this arena have a worldguard wgRegionName attached
     * @return true or false if region is found and valid
     */
    public boolean hasRegion() {
        return wgRegion != null && wgRegion.valid();
    }

    /**
     * Get the worldguard wgRegionName for this arena
     * @return region
     */
    public WorldGuardRegion getWorldGuardRegion() {
        return wgRegion;
    }

    /**
     * Return the timed spawns for this arena
     * @return the timed spawns
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
     * @return TimedSpawn
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
     * @param team ArenaTeam
     */
    protected void setWinner(ArenaTeam team) {
        match.setVictor(team);
    }

    /**
     * set the winning player, this will also cause the match to be ended
     * @param player ArenaPlayer that will win
     */
    protected void setWinner(ArenaPlayer player) {
        match.setVictor(player);
    }

    /**
     * Get the current state of the match
     * @return current match state
     */
    @Override
    public MatchState getMatchState(){
        return match.getState();
    }

    /**
     * return a list of teams inside this match
     * @return list of teams
     */
    public List<ArenaTeam> getTeams(){
        return match == null ? null : match.getTeams();
    }

    /**
     * Return a list of live teams inside this match
     * @return List of alive teams
     */
    public List<ArenaTeam> getAliveTeams(){
        return match == null ? null : match.getAliveTeams();
    }

    /**
     * Return a list of living arena players inside this match
     * @return list of alive players
     */
    public Set<ArenaPlayer> getAlivePlayers(){
        return match == null ? null : match.getAlivePlayers();
    }

    /**
     * Return a list of alive bukkit players inside this match
     * @return list of alive bukkit players
     */
    public Set<Player> getAliveBukkitPlayers(){
        return match == null ? null : BattleArena.toPlayerSet(match.getAlivePlayers());
    }

    /**
     * Return the team of this player
     * @return the team or null if player isn't in match
     */
    @Override
    public ArenaTeam getTeam(ArenaPlayer p){
        return match == null ? null : match.getTeam(p);
    }

    /**
     * Return the team of this player
     * @return the team or null if player isn't in match
     */
    public ArenaTeam getTeam(Player p){
        return match == null ? null : match.getTeam(BattleArena.toArenaPlayer(p));
    }

    /**
     * Return the team with this index
     * @return the team or null if index isn't valid
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

    public boolean matches(final JoinOptions jp) {
        Arena a = jp.getArena();
        if (a != null && !a.matches(this))
            return false;
        MatchParams matchParams = jp.getMatchParams();
        if (!matches(matchParams)) {
            return false;
        }
        if (matchParams.hasOptionAt(MatchState.PREREQS, TransitionOption.WITHINDISTANCE)) {
            if (!jp.nearby(this, matchParams.getDoubleOption(MatchState.PREREQS, TransitionOption.WITHINDISTANCE))) {
                return false;
            }
        }
        if (matchParams.hasOptionAt(MatchState.PREREQS, TransitionOption.SAMEWORLD)) {
            if (!jp.sameWorld(this)) {
                return false;
            }
        }

        return true;
    }


    @SuppressWarnings("SimplifiableIfStatement")
    public boolean matches(Arena arena) {
        if (arena == null)
            return false;
        if (this == arena)
            return true;
        if (arena.name == null || this.name==null)
            return false;
        return this.name.equals(arena.name);
    }

    /**
     * Checks to see whether this arena has paramaters that match the given matchparams
     * @param params params
     * @return true if arena matches the params
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean matches(MatchParams params) {
        if (!getParams().matches(params)) {
            return false;}
        if ((waitroom == null || !waitroom.hasSpawns()) && params.needsWaitroom())
            return false;
        if ((spectate == null || !spectate.hasSpawns()) && params.needsSpectate())
            return false;
        return true;
    }



    public List<String> getInvalidMatchReasons(Arena arena) {
        List<String> reasons = new ArrayList<String>();
        if (arena == null){
            reasons.add("Arena is null");
        } else if (this == arena) {
            return reasons;
        } else if (arena.name == null || this.name==null){
            reasons.add("Arena name is null or this.name is null");
        } else if (!this.name.equals(arena.name)){
            reasons.add("This arena '"+this.getName()+"' isn't '" + arena.getName()+"'");
        }
        return reasons;
    }
    public boolean withinDistance(Location location, double distance){
        for (List<SpawnLocation> list: spawns){
            for (SpawnLocation l: list){
                if (location.getWorld().getUID() == l.getLocation().getWorld().getUID() &&
                        location.distance(l.getLocation()) < distance)
                    return true;
            }
        }
        return false;
    }

    public List<String> getInvalidMatchReasons(MatchParams matchParams, JoinOptions jp) {
        List<String> reasons = new ArrayList<String>();
        reasons.addAll(getParams().getInvalidMatchReasons(matchParams));
        final StateGraph tops = matchParams.getStateGraph();
        if (tops != null){
            if (matchParams.needsWaitroom() && (waitroom == null || !waitroom.hasSpawns()))
                reasons.add("Needs a waitroom but none has been provided");
            if (matchParams.needsSpectate() && (spectate == null || !spectate.hasSpawns()))
                reasons.add("Needs a spectator room but none has been provided");
            if (matchParams.needsLobby() && (!RoomController.hasLobby(matchParams.getType())))
                reasons.add("Needs a lobby but none has been provided");
        }
        if (jp == null)
            return reasons;

        if (jp.getArena() != null ) {
            reasons.addAll(this.getInvalidMatchReasons(jp.getArena()));
        }

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
     * @return detailed info
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public String toDetailedString(){
        StringBuilder sb = new StringBuilder("&6" + name+" &e");
        sb.append("&eTeamSizes=&6"+params.getTeamSizes() + " &eTypes=&6" +params.getType());
        sb.append("&e, #Teams:&6"+params.getNTeams());
        sb.append("&e, #spawns:&6" +spawns.size() +"\n");
        sb.append("&eteamSpawnLocs=&b"+getSpawnLocationString()+"\n");
        if (waitroom != null) sb.append("&ewrSpawnLocs=&b"+waitroom.getSpawnLocationString()+"\n");
        if (spectate != null) sb.append("&espectateSpawnLocs=&b"+spectate.getSpawnLocationString()+"\n");
        if (timedSpawns != null){sb.append("&e#item/mob spawns:&6" +timedSpawns.size() +"\n");}
        return sb.toString();
    }


    private ChatColor getColor(Object o) {
        return o == null ? ChatColor.GOLD : ChatColor.WHITE;
    }

    /**
     * return arena summary string (includes bukkit coloring)
     * @return summary
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public String toSummaryString(){
        StringBuilder sb = new StringBuilder("&4" + name);
        if (params != null){
//            sb.append("&2 type=&f").append(params.getType());
            sb.append(" &2TeamSizes:" + getColor(params.getThisTeamSize()) + params.getTeamSizes() +
                    "&2, nTeams:"+getColor(params.getThisNTeams()) + params.getNTeams());
        }

        sb.append("&2 #spawns:&f" +spawns.size() +"&2 1stSpawn:&f");
        if (!spawns.isEmpty()){
            SpawnLocation l = spawns.get(0).get(0);
            sb.append("["+ Util.getLocString(l)+"] ");
        }
        if (timedSpawns != null && !timedSpawns.isEmpty())
            sb.append("&2#item/mob Spawns:&f" +timedSpawns.size());
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

    public void setSpectate(RoomContainer spectate) {
        this.spectate = spectate;
    }

    public RoomContainer getWaitroom() {
        return waitroom;
    }

    public RoomContainer getSpectatorRoom() {
        return spectate;
    }

    public RoomContainer getLobby() {
        return RoomController.getLobby(getArenaType());
    }

    @Override
    public LocationType getLocationType() {
        return LocationType.ARENA;
    }

    public List<List<SpawnLocation>> getVisitorLocs() {
        return visitorRoom!=null ? visitorRoom.getSpawns() : null;
    }

    public boolean isJoinable(MatchParams mp) {
        if (!isOpen())
            return false;
        else if ( mp.needsWaitroom() && (waitroom == null || !waitroom.isOpen() || waitroom.getSpawns().isEmpty()) )
            return false;
        else if ( mp.needsSpectate() && (spectate== null || !spectate.isOpen() || spectate.getSpawns().isEmpty()) )
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
            return waitroom.getContainerMessage()!= null ?
                    waitroom.getContainerMessage() :
                    "&cWaitroom is not open!";
        else if ( mp.needsWaitroom() && waitroom.getSpawns().isEmpty() )
            return "&cYou need to set a spawn point for the waitroom!";
        else if ( mp.needsSpectate() && spectate == null )
            return "&cYou need to create a spectator area!";
        else if ( mp.needsSpectate() && spectate.getSpawns().isEmpty() )
            return "&cYou need to set a spawn point for the spectate area!";
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

    public void setAllContainerState(ContainerState state) {
        setContainerState(state);
        if (waitroom != null)
            waitroom.setContainerState(state);
        RoomContainer lobby = getLobby();
        if (lobby != null)
            lobby.setContainerState(state);
    }

    public void setContainerState(ChangeType cs, ContainerState state) throws IllegalStateException{
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

    public int getQueueCount() {
        return BattleArena.getBAController().getArenaMatchQueue().getQueueCount(this);
    }

    /**
     * Perform a player transition while in a game
     * @param transition the transition type to perform
     * @param player the player to perform the transition on
     */
    protected void performTransition(CompetitionTransition transition, ArenaPlayer player) {
        PerformTransition.transition((match != null ? match : this), transition, player, player.getTeam(), false);
    }

    /**
     * Perform a team transition while in a game
     * @param transition the transition type to perform
     * @param team the team to perform the transition on
     */
    protected void performTransition(CompetitionTransition transition, ArenaTeam team) {
        PerformTransition.transition((match != null ? match : this), transition, team, false);
    }
}
