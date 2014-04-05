package mc.alk.arena.competition.events;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.joining.AbstractJoinHandler;
import mc.alk.arena.controllers.joining.TeamJoinFactory;
import mc.alk.arena.controllers.messaging.EventMessageImpl;
import mc.alk.arena.controllers.messaging.EventMessager;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.events.EventCancelEvent;
import mc.alk.arena.events.events.EventCompletedEvent;
import mc.alk.arena.events.events.EventFinishedEvent;
import mc.alk.arena.events.events.EventOpenEvent;
import mc.alk.arena.events.events.EventResultEvent;
import mc.alk.arena.events.events.EventStartEvent;
import mc.alk.arena.events.events.TeamJoinedEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionResult;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.EventState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.joining.TeamJoinObject;
import mc.alk.arena.objects.messaging.EventMessageHandler;
import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.ServerUtil;
import mc.alk.arena.util.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class Event extends Competition implements CountdownCallback, ArenaListener {
    final String name; /// Name of this event

    protected EventParams eventParams; /// The parameters for this event

    EventMessager mc; /// Our message handler

    Countdown timer; /// Timer till Event starts

    protected AbstractJoinHandler joinHandler; /// Specify how teams are allocated

    protected EventState state; /// The current state of this event

    /// When did each transition occur
    final Map<EventState, Long> times = new EnumMap<EventState,Long>(EventState.class);

    /**
     * Create our event from the specified paramaters
     * @param params EventParams
     */
    public Event(EventParams params) throws NeverWouldJoinException {
        this.eventParams = params;
        transitionTo(EventState.CLOSED);
        this.name = params.getName();
        joinHandler = TeamJoinFactory.createTeamJoinHandler(params, this);
        if (mc == null)
            mc = new EventMessager(this);
        mc.setMessageHandler(new EventMessageImpl(this));
    }

    public void openEvent() {
        teams.clear();
        EventOpenEvent event = new EventOpenEvent(this);
        callEvent(event);
        if (event.isCancelled())
            return;

        stopTimer();
        transitionTo(EventState.OPEN);
        mc.sendEventOpenMsg();
    }

    public void autoEvent(){
        openEvent();
        TimeUtil.testClock();
        mc.sendCountdownTillEvent(eventParams.getSecondsTillStart());
        timer = new Countdown(BattleArena.getSelf(),(long)eventParams.getSecondsTillStart(),
                (long)eventParams.getAnnouncementInterval(), this);
    }

    public void addAllOnline() {
        Player[] online = ServerUtil.getOnlinePlayers();

        for (Player p: online){
            if (PermissionsUtil.isAdmin(p)) { /// skip admins (they are doin' importantz thingz)
                continue;}
            ArenaTeam t = TeamController.createTeam(eventParams, BattleArena.toArenaPlayer(p));
            TeamJoinObject tqo = new TeamJoinObject(t,eventParams,null);
            this.joining(tqo);
        }
    }

    /**
     * Add an arena listener for this competition
     * @param arenaListener ArenaListener
     */
    @Override
    public void addArenaListener(ArenaListener arenaListener){
        methodController.addListener(arenaListener);
    }

    /**
     * Remove an arena listener for this competition
     * @param arenaListener ArenaListener
     */
    @Override
    public boolean removeArenaListener(ArenaListener arenaListener){
        return methodController.removeListener(arenaListener);
    }

    public void startEvent() {
        List<ArenaTeam> improper = joinHandler.removeImproperTeams();
        for (ArenaTeam t: improper){
            t.sendMessage("&cYour team has been excluded to having an improper team size");
        }
        /// TODO rebalance teams
        Set<ArenaPlayer> excludedPlayers = getExcludedPlayers();
        for (ArenaPlayer p : excludedPlayers){
            p.sendMessage(Log.colorChat(eventParams.getPrefix()+
                    "&6 &5There werent enough players to create a &6" + eventParams.getMinTeamSize() +"&5 person team"));
        }
        transitionTo(EventState.RUNNING);

        callEvent(new EventStartEvent(this,teams));
    }

    protected void setEventResult(CompetitionResult result, boolean announce) {
        if (announce){
            if (result.hasVictor()){
                mc.sendEventVictory(result.getVictors(), result.getLosers());
            } else {
                mc.sendEventDraw(result.getDrawers(), result.getLosers());
            }
        }
        callEvent(new EventResultEvent(this,result));
    }

    public void stopTimer(){
        if (timer != null){
            timer.stop();
            timer = null;
        }
    }

    public void cancelEvent() {
        eventCancelled();
    }

    public void eventCompleted(){
        callEvent(new EventCompletedEvent(this));
        endEvent();
    }

    protected void eventCancelled(){
        stopTimer();
        List<ArenaTeam> newTeams = new ArrayList<ArenaTeam>(teams);
        callEvent(new EventCancelEvent(this));
        mc.sendEventCancelled(newTeams);
        endEvent();
    }

    protected void endEvent() {
        if (state == EventState.CLOSED)
            return;
        transitionTo(EventState.CLOSED);
        if (Defaults.DEBUG_TRACE) Log.info("BAEvent::endEvent");
        stopTimer();

        removeAllTeams();
        teams.clear();
        joinHandler = null;
        callEvent(new EventFinishedEvent(this));
        HandlerList.unregisterAll(this);
    }

    public boolean canJoin(){
        return isOpen();
    }

    @SuppressWarnings("unused")
    public boolean canJoin(ArenaTeam t){
        return isOpen();
    }

    @Override
    public abstract boolean canLeave(ArenaPlayer p);

    @Override
    public EventState getState() {return state;}

    @Override
    protected void transitionTo(CompetitionState state){
        this.state = (EventState) state;
        times.put(this.state, System.currentTimeMillis());
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public Long getTime(CompetitionState state){
        return times.get(state);
    }

    /**
     * Called when a player leaves minecraft.. we cant stop them so deal with it
     */
    @Override
    public boolean leave(ArenaPlayer p) {
        ArenaTeam t = getTeam(p);
        p.removeCompetition(this);
        if (eventParams.needsLobby()){
            RoomController.leaveLobby(eventParams, p);
        }
        if (t==null) /// they arent in this Event
            return false;
        t.playerLeft(p);
        return true;
    }

    public void removeAllTeams(){

        for (ArenaTeam t: teams){
            for (ArenaPlayer p: t.getPlayers()){
                p.removeCompetition(this);
            }
        }
        teams.clear();
    }

    @Override
    public boolean removedTeam(ArenaTeam team){
        if (teams.remove(team)){
            for (ArenaPlayer p: team.getPlayers()){
                p.removeCompetition(this);
            }
            return true;
        }
        return false;
    }


    @Override
    public boolean addedTeam(ArenaTeam team){
        if (teams.contains(team)) /// adding a team twice is bad mmkay
            return true;
        callEvent(new TeamJoinedEvent(this,team));
        return teams.add(team);
    }

    /**
     * Called when a team wants to add
     * @param tqo TeamJoinObject that is joining
     * @return where the team ended up
     */
    public JoinResult.JoinStatus joining(TeamJoinObject tqo){
        JoinResult.JoinStatus js;
        ArenaTeam team = tqo.getTeam();
        if (joinHandler == null) {
            js = JoinResult.JoinStatus.NOTOPEN;
            return js;
        }
        AbstractJoinHandler.TeamJoinResult tjr = joinHandler.joiningTeam(tqo);
        switch(tjr.status){
            case ADDED_TO_EXISTING: /* drop down into added */
            case ADDED:
                for (ArenaPlayer player: tqo.getTeam().getPlayers()){
                    player.addCompetition(this);}
                mc.sendTeamJoined(tqo.getTeam());
                break;
            case ADDED_STILL_NEEDS_PLAYERS:
                mc.sendWaitingForMorePlayers(team, tjr.remaining);
                for (ArenaPlayer player: tqo.getTeam().getPlayers()){
                    player.addCompetition(this);}
                break;
            case CANT_FIT:
                mc.sendCantFitTeam(team);
                break;
        }

        return null;
    }

    @Override
    public String getName(){
        return name;
    }

    public String getCommand(){return eventParams.getCommand();}
    public String getDisplayName() {
        return getName();
    }

    public boolean isRunning() {return state == EventState.RUNNING;}
    public boolean isOpen() {return state == EventState.OPEN;}
    public boolean isClosed() {return state == EventState.CLOSED;}
    public boolean isFinished() {return state== EventState.FINISHED;}

    @Override
    public EventParams getParams() {return eventParams;}

    public int getNTeams() {
        int size = 0;
        for (ArenaTeam t: teams){
            if (t.size() > 0)
                size++;
        }
        return size;
    }

    public void setTeamJoinHandler(AbstractJoinHandler tjh){
        this.joinHandler = tjh;
    }

    /**
     * Set a Message handler to override default Event messages
     * @param handler EventMessageHandler
     */
    public void setMessageHandler(EventMessageHandler handler){
        this.mc.setMessageHandler(handler);
    }

    /**
     * Return the Message Handler for this Event
     * @return EventMessageHandler
     */
    public EventMessageHandler getMessageHandler(){
        return mc.getMessageHandler();
    }

    public abstract String getResultString();

    public static class TeamSizeComparator implements Comparator<ArenaTeam>{
        @Override
        public int compare(ArenaTeam arg0, ArenaTeam arg1) {
            if (arg0.size() == arg1.size() ) return 0;
            return (arg0.size() < arg1.size()) ? -1 : 1;
        }
    }


    protected Set<ArenaPlayer> getExcludedPlayers() {
        return joinHandler == null ? null :  joinHandler.getExcludedPlayers();
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        if (eventParams != null){
            boolean rated = eventParams.isRated();
            sb.append(rated ? "&4Rated" : "&aUnrated").append("&e ").append(name).append(". ");
            sb.append("&e(&6").append(state).append("&e)");
            sb.append("&eTeam size=").append(eventParams.getTeamSize());
            sb.append("&e Teams=&6 ").append(teams.size());
        }
        if (state == EventState.OPEN && joinHandler != null){
            sb.append("\n&eJoiningTeams: ").append(MessageUtil.joinPlayers(joinHandler.getExcludedPlayers(), ", "));
        }
        return sb.toString();
    }

    public String getInfo() {
        return StateOptions.getInfo(eventParams, eventParams.getName());
    }

    public boolean canLeaveTeam(ArenaPlayer p) {return canLeave(p);}

    /**
     * Broadcast to all players in the Event
     */
    public void broadcast(String msg){for (ArenaTeam t : teams){t.sendMessage(msg);}}

    public Long getTimeTillStart() {
        if (timer == null)
            return null;
        return timer.getTimeRemaining();
    }

    @Override
    public boolean intervalTick(int remaining){
        if (!isOpen())
            return false;
        if (remaining == 0){
            if (this.hasEnough() ){
                startEvent();
            } else {
                mc.sendEventCancelledDueToLackOfPlayers(getPlayers());
                cancelEvent();
            }
        } else {
            mc.sendCountdownTillEvent(remaining);
        }
        return true;
    }

    /**
     * Get all players in the Event
     * if Event is open will return those players still waiting for a team as well
     * @return players
     */
    @Override
    public Set<ArenaPlayer> getPlayers() {
        Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
        for (ArenaTeam t: getTeams()){
            players.addAll(t.getPlayers());}
        if (isOpen() && joinHandler != null){
            players.addAll(joinHandler.getExcludedPlayers());
        }
        return players;
    }

    public void setSilent(boolean silent) {
        mc.setSilent(silent);
    }

    @Override
    public String toString(){
        return "[" + getName()+":"+id+"]";
    }

    public boolean waitingToJoin(ArenaPlayer p) {
        return joinHandler != null && joinHandler.getExcludedPlayers().contains(p);
    }

    public boolean hasEnoughTeams() {
        return getNTeams() >= eventParams.getMinTeams();
    }

    public boolean hasEnough() {
        return joinHandler != null && joinHandler.hasEnough(Integer.MAX_VALUE);
    }

    @Override
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> players) {/* do nothing */}

    @Override
    public void addedToTeam(ArenaTeam team, ArenaPlayer player) {/* do nothing */}

    @Override
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> players) {/* do nothing */}

    @Override
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {/* do nothing */}

    @Override
    public int getID(){
        return id;
    }

    @Override
    public void onPreJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        player.removeCompetition(this);
    }

    @Override
    public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostEnter(ArenaPlayer player,ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @Override
    public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {/* do nothing */}

    @EventHandler(priority=EventPriority.MONITOR)
    public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
        if (hasPlayer(event.getPlayer())) {
            event.addMessage(MessageHandler.getSystemMessage("you_left_event", this.getName()));
            leave(event.getPlayer());
        }
    }

}
