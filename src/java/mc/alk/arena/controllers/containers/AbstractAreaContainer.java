package mc.alk.arena.controllers.containers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.TransitionController;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.listeners.custom.MethodController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.ContainerState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.StateOption;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.TeamUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractAreaContainer implements PlayerHolder, TeamHandler{
    public static final AbstractAreaContainer HOMECONTAINER = new AbstractAreaContainer("home"){
        @Override
        public LocationType getLocationType() {return LocationType.HOME;}
        @Override
        public ArenaTeam getTeam(ArenaPlayer player) {return null;}
    };

    protected String name;

    protected String displayName;

    protected MatchParams params;

    ContainerState state = ContainerState.OPEN;

    boolean disabledAllCommands;
    Set<String> disabledCommands;
    Set<String> enabledCommands;

    private final MethodController methodController;

    final protected Set<UUID> players = new HashSet<UUID>();

    /** Spawn points */
    final protected List<List<SpawnLocation>> spawns = new ArrayList<List<SpawnLocation>>();

    protected List<SpawnLocation> allSpawns;

    /** Main Spawn is different than the normal spawns.  It is specified by Defaults.MAIN_SPAWN */
    SpawnLocation mainSpawn = null;

    /** Our teams */
    final protected List<ArenaTeam> teams = Collections.synchronizedList(new ArrayList<ArenaTeam>());

    /** our values for the team index, only used if the Team.getIndex is null*/
    final Map<ArenaTeam,Integer> teamIndexes = new ConcurrentHashMap<ArenaTeam,Integer>();

    final static Random r = new Random();

    public AbstractAreaContainer(String name){
        methodController = new MethodController("AAC " + name);
        methodController.addAllEvents(this);
        try{
            Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());}catch(Exception e){
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!Defaults.TESTSERVER && !Defaults.TESTSERVER_DEBUG) Log.printStackTrace(e);
        }
        this.name = name;
    }

    @Override
    public void callEvent(BAEvent event){
        methodController.callEvent(event);
    }

    public void playerLeaving(ArenaPlayer player){
        methodController.updateEvents(MatchState.ONLEAVE, player);
    }

    protected void playerJoining(ArenaPlayer player){
        methodController.updateEvents(MatchState.ONENTER, player);
    }

    protected void updateBukkitEvents(MatchState matchState,ArenaPlayer player){
        methodController.updateEvents(matchState, player);
    }

    protected void teamLeaving(ArenaTeam team){
        if (teams.remove(team)){
            methodController.updateEvents(MatchState.ONLEAVE, team.getPlayers());
        }
    }

    public boolean teamJoining(ArenaTeam team){
        teams.add(team);
        teamIndexes.put(team, teams.size());
        for (ArenaPlayer ap: team.getPlayers()){
            doTransition(MatchState.ONJOIN, ap,team, true);
        }

        return true;
    }

    /**
     * Tekkit Servers don't get the @EventHandler methods (reason unknown) so have this method be
     * redundant.  Possibly can now simplify to just the @ArenaEventHandler
     * @param event ArenaPlayerLeaveEvent
     */
    @ArenaEventHandler
    public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event) {
        _onArenaPlayerLeaveEvent(event);
    }

    @EventHandler
    public void _onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
        if (players.remove(event.getPlayer().getID())){
            updateBukkitEvents(MatchState.ONLEAVE, event.getPlayer());
            callEvent(new ArenaPlayerLeaveLobbyEvent(event.getPlayer(),event.getTeam()));
            event.addMessage(MessageHandler.getSystemMessage("you_left_competition", this.params.getName()));
            event.getPlayer().reset();
        }
    }

    protected void doTransition(MatchState state, ArenaPlayer player, ArenaTeam team, boolean onlyInMatch){
        if (player != null){
            TransitionController.transition(this, state, player, team, onlyInMatch);
        } else {
            TransitionController.transition(this, state, team, onlyInMatch);
        }
    }

    @Override
    public boolean canLeave(ArenaPlayer p) {
        return false;
    }

    @Override
    public boolean leave(ArenaPlayer p) {
        return players.remove(p.getID());
    }

    @Override
    public void addArenaListener(ArenaListener arenaListener) {
        this.methodController.addListener(arenaListener);
    }

    @Override
    public boolean removeArenaListener(ArenaListener arenaListener) {
        return this.methodController.removeListener(arenaListener);
    }

    @Override
    public MatchParams getParams() {
        return params;
    }

    public void setParams(MatchParams mp) {
        this.params= mp;
    }

    @Override
    public MatchState getMatchState() {
        return MatchState.INLOBBY;
    }

    @Override
    public boolean isHandled(ArenaPlayer player) {
        return players.contains(player.getID());
    }

    @Override
    public CompetitionState getState() {
        return MatchState.NONE;
    }

    @Override
    public boolean checkReady(ArenaPlayer player, ArenaTeam team, StateOptions mo, boolean b) {
        return params.getStateGraph().playerReady(player, null);
    }

    @Override
    public SpawnLocation getSpawn(int index, boolean random) {
        if (index == Defaults.MAIN_SPAWN)
            return mainSpawn != null ? mainSpawn :
                    (spawns.size()==1 ? spawns.get(0).get(0) : null);
        if (random){
            if (allSpawns == null) {
                buildAllSpawns();}
            return allSpawns ==null? null : allSpawns.get(r.nextInt(allSpawns.size()));
        } else {
            List<SpawnLocation> l = index >= spawns.size() ? spawns.get(index % spawns.size()) : spawns.get(index);
            return l.get(r.nextInt(l.size()));
        }
    }

    public SpawnLocation getSpawn(int teamIndex, int spawnIndex) {
        List<SpawnLocation> l = teamIndex >= spawns.size() ? null : spawns.get(teamIndex);
        return l==null || spawnIndex >= l.size() ? null : l.get(spawnIndex);
    }

    private void buildAllSpawns(){
        if (spawns.isEmpty()){
            return;
        }
        allSpawns = new ArrayList<SpawnLocation>();
        for (List<SpawnLocation> spawn: spawns) {
            allSpawns.addAll(spawn);
        }
    }

    /**
     * Set the spawn location for the team with the given index
     * @param teamIndex index of which team to add this spawn to
     * @param spawnIndex which spawn to set
     * @param loc SpawnLocation
     */
    public void setSpawnLoc(int teamIndex, int spawnIndex, SpawnLocation loc) throws IllegalStateException{
        if (teamIndex == Defaults.MAIN_SPAWN){
            mainSpawn = loc;
        } else if (spawns.size() > teamIndex) {
            List<SpawnLocation> list = spawns.get(teamIndex);
            if (list.size() > spawnIndex) {
                list.set(spawnIndex, loc);
            } else if (list.size() == spawnIndex){
                list.add(loc);
            } else {
                throw new IllegalStateException("You must set team spawn " + (list.size()+1) + " first");
            }
        } else if (spawns.size() == teamIndex) {
            ArrayList<SpawnLocation> list = new ArrayList<SpawnLocation>();
            if (list.size() < spawnIndex){
                throw new IllegalStateException("You must set spawn #" + (list.size()+1) +
                        " for the "+ TeamUtil.getTeamName(teamIndex)+" team first");}
            list.add(loc);
            spawns.add(list);
        } else {
            throw new IllegalStateException("You must set spawn " + (spawns.size()+1) + " first");
        }
    }
    public boolean validIndex(int index){
        return spawns.size() < index;
    }

    public List<List<SpawnLocation>> getSpawns(){
        return spawns;
    }
    public SpawnLocation getMainSpawn(){
        return mainSpawn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName == null ? name : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    @Override
    public void onPreJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostEnter(ArenaPlayer player,ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    public void setContainerState(ContainerState state) {
        this.state = state;
    }

    public ContainerState getContainerState() {
        return this.state;
    }

    public boolean isOpen() {
        return state.isOpen();
    }

    public boolean isClosed() {
        return state.isClosed();
    }


    public String getContainerMessage() {
        return state.getMsg();
    }

    @Override
    public boolean hasOption(StateOption option) {
        return getParams().hasOptionAt(getState(),option);
    }
}
