package mc.alk.arena.controllers.containers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.PerformTransition;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.listeners.competition.InArenaListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.ContainerState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.util.CommandUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

    Set<String> disabledCommands;

    private final MethodController methodController;

    protected Set<String> players = new HashSet<String>();

    /** Spawn points */
    protected List<Location> spawns = new ArrayList<Location>();

    /** Main Spawn is different than the normal spawns.  It is specified by Defaults.MAIN_SPAWN */
    Location mainSpawn = null;

    /** Our teams */
    protected List<ArenaTeam> teams = Collections.synchronizedList(new ArrayList<ArenaTeam>());

    /** our values for the team index, only used if the Team.getIndex is null*/
    final Map<ArenaTeam,Integer> teamIndexes = new ConcurrentHashMap<ArenaTeam,Integer>();

    static Random r = new Random();

    public AbstractAreaContainer(String name){
        methodController = new MethodController("AAC " + name);
        methodController.addAllEvents(this);
        try{Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());}catch(Exception e){
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!Defaults.TESTSERVER && !Defaults.TESTSERVER_DEBUG) Log.printStackTrace(e);
        }
        this.name = name;
    }

    public void callEvent(BAEvent event){
        methodController.callEvent(event);
    }

    public void playerLeaving(ArenaPlayer player){
        methodController.updateEvents(MatchState.ONLEAVE, player);
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
            players.add(ap.getName());}

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
        if (players.remove(event.getPlayer().getName())){
            updateBukkitEvents(MatchState.ONLEAVE, event.getPlayer());
            callEvent(new ArenaPlayerLeaveLobbyEvent(event.getPlayer(),event.getTeam()));
            event.addMessage(MessageHandler.getSystemMessage("you_left_competition", this.params.getName()));
            event.getPlayer().reset();
        }
    }

    /**
     * Tekkit Servers don't get the @EventHandler methods (reason unknown) so have this method be
     * redundant.  Possibly can now simplify to just the @ArenaEventHandler
     * @param event PlayerCommandPreprocessEvent
     */
    @ArenaEventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        _onPlayerCommandPreprocess(event);
    }

    @EventHandler
    public void _onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        if (disabledCommands == null)
            return;
        if (!event.isCancelled() && InArenaListener.inQueue(event.getPlayer().getName()) &&
                CommandUtil.shouldCancel(event, disabledCommands)){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED+"You cannot use that command when you are in the "+displayName);
            if (PermissionsUtil.isAdmin(event.getPlayer())){
                MessageUtil.sendMessage(event.getPlayer(),"&cYou can set &6/bad allowAdminCommands true: &c to change");}
        }
    }

    public void setDisabledCommands(List<String> commands) {
        if (disabledCommands == null)
            disabledCommands = new HashSet<String>();
        for (String s: commands){
            disabledCommands.add("/" + s.toLowerCase());}
    }

    protected void doTransition(MatchState state, ArenaPlayer player, ArenaTeam team, boolean onlyInMatch){
        if (player != null){
            PerformTransition.transition(this, state, player,team, onlyInMatch);
        } else {
            PerformTransition.transition(this, state, team, onlyInMatch);
        }
    }

    @Override
    public boolean canLeave(ArenaPlayer p) {
        return false;
    }

    @Override
    public boolean leave(ArenaPlayer p) {
        return players.remove(p.getName());
    }

    @Override
    public void addArenaListener(ArenaListener arenaListener) {
        this.methodController.addListener(arenaListener);
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
        return players.contains(player.getName());
    }

    @Override
    public CompetitionState getState() {
        return null;
    }

    @Override
    public boolean checkReady(ArenaPlayer player, ArenaTeam team, TransitionOptions mo, boolean b) {
        return params.getTransitionOptions().playerReady(player, null);
    }

    @Override
    public Location getSpawn(int index, boolean random) {
        if (index == Defaults.MAIN_SPAWN)
            return mainSpawn != null ? mainSpawn : (spawns.size()==1 ? spawns.get(0) : null);
        if (random){
            return spawns.get(r.nextInt(spawns.size()));
        } else{
            return index >= spawns.size() ? spawns.get(index % spawns.size()) : spawns.get(index);
        }
    }

    @Override
    public Location getSpawn(ArenaPlayer player, boolean random) {
        return null;
    }

    /**
     * Set the spawn location for the team with the given index
     * @param index index of spawn
     * @param loc location
     */
    public void setSpawnLoc(int index, Location loc) throws IllegalStateException{
        if (index == Defaults.MAIN_SPAWN){
            mainSpawn = loc;
        } else if (spawns.size() > index){
            spawns.set(index, loc);
        } else if (spawns.size() == index){
            spawns.add(loc);
        } else {
            throw new IllegalStateException("You must set spawn " + (spawns.size()+1) + " first");
        }
    }
    public boolean validIndex(int index){
        return spawns != null && spawns.size() < index;
    }

    public List<Location> getSpawns(){
        return spawns;
    }
    public Location getMainSpawn(){
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
}
