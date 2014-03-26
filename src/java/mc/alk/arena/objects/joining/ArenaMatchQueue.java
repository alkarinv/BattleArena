package mc.alk.arena.objects.joining;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.Permissions;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.Scheduler;
import mc.alk.arena.controllers.joining.AbstractJoinHandler;
import mc.alk.arena.controllers.joining.TeamJoinFactory;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.matches.MatchCreatedEvent;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.players.ArenaPlayerEnterQueueEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.listeners.custom.MethodController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.CompetitionSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.PlayerSave;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.exceptions.MatchCreationException;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.pairs.JoinResult.JoinStatus;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.CommandUtil;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.MinMax;
import mc.alk.arena.util.PermissionsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Map.Entry;
import static mc.alk.arena.controllers.joining.AbstractJoinHandler.TeamJoinResult;


public class ArenaMatchQueue implements ArenaListener, Listener {
    static final boolean DEBUG = false;
    static boolean disabledAllCommands;
    final private static HashSet<String> disabledCommands = new HashSet<String>();
    final private static HashSet<String> enabledCommands = new HashSet<String>();

    final List<WaitingObject> joinHandlers = new LinkedList<WaitingObject>();
    final Map<WaitingObject, IdTime> forceTimers = Collections.synchronizedMap(new HashMap<WaitingObject, IdTime>());
    final protected Map<String, WaitingObject> inQueue = new HashMap<String, WaitingObject>();

    final protected MethodController methodController = new MethodController("QC");

    final private Map<ArenaType, ArenaQueue> arenaqueue = new ConcurrentHashMap<ArenaType, ArenaQueue>();

    final Map<ArenaType,LinkedList<FoundMatch>> delayedReadyMatches = new HashMap<ArenaType, LinkedList<FoundMatch>>();
    final private Map<ArenaType, Integer> runningMatchTypes = Collections.synchronizedMap(new HashMap<ArenaType, Integer>());

    final static Map<ArenaType, Integer> inQueueForGame = new HashMap<ArenaType, Integer>();
    final static Map<Arena, Integer> inQueueForArena = new HashMap<Arena, Integer>();

    final Lock lock = new ReentrantLock();
    final Condition empty = lock.newCondition();
    final AtomicBoolean suspend = new AtomicBoolean();


    public ArenaMatchQueue(){
        super();
        try{
            Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());} catch(Exception e){
            /* usually only from offline testing, don't need to report */
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!Defaults.TESTSERVER && !Defaults.TESTSERVER_DEBUG) Log.printStackTrace(e);
        }
        methodController.addAllEvents(this);
    }

    public static int getPlayersInArenaQueue(Arena arena) {
        return inQueueForArena.containsKey(arena) ? inQueueForArena.get(arena) : 0;
    }


    public enum QueueType{
        GAME,ARENA
    }

    public static class IdTime{
        public Countdown c;
        public Long time;
    }

    public class FoundMatch {
        public Arena arena;
        public WaitingObject wo;
        MatchParams params;
        public AbstractJoinHandler joinHandler;

        public Match startMatch() {
            incNumberOpenMatches(params.getType());
            if (wo != null)
                removeTimer(wo); /// get rid of any timers
            final Match m = new ArenaMatch(arena, params, wo != null ? wo.getArenaListeners() : null);
            final boolean hasJoinHandler = joinHandler != null && joinHandler.getTeams() != null;
            final boolean timedStart = wo != null && wo.createsOnJoin() && wo.getParams().getForceStartTime() > 0;
            if (hasJoinHandler) {
                m.hookTeamJoinHandler(joinHandler);}
            MatchCreatedEvent mce = new MatchCreatedEvent(m, wo);
            m.callEvent(mce);

            if (hasJoinHandler) {
                addCompetition(joinHandler,m);}

            m.open();
            if (timedStart){
                m.setTimedStart(wo.getParams().getForceStartTime(), 30);}

            Scheduler.scheduleSynchronousTask(new Runnable() {
                @Override
                public void run() {
                    if (hasJoinHandler) {
                        removeFromQueue(joinHandler);}

                    if (!timedStart){ /// Start now
                        m.run();}
                }

            });
            return m;
        }

        private void addCompetition(AbstractJoinHandler joinHandler, Match m) {
            for (ArenaTeam t : joinHandler.getTeams()) {
                for (ArenaPlayer ap : t.getPlayers()) {
                    ap.addCompetition(m);
                }
            }
        }

    }


    public void add(Arena arena) {
        synchronized(arenaqueue) {
            ArenaQueue aq = arenaqueue.get(arena.getArenaType());
            if (aq == null) {
                aq = new ArenaQueue();
                arenaqueue.put(arena.getArenaType(), aq);
            }
            aq.addLast(arena);
        }
        forceStart(arena.getParams(), true);
    }



    private void addReadyMatch(JoinResult jr,  WaitingObject o, Arena arena) {
        addReadyMatch(createFoundMatch(jr, o, arena));
    }

    private void addReadyMatch(FoundMatch match) {
        if (match.params.getNConcurrentCompetitions() != CompetitionSize.MAX) {
            synchronized (delayedReadyMatches) {
                LinkedList<FoundMatch> l = delayedReadyMatches.get(match.params.getType());
                if (l == null) {
                    l = new LinkedList<FoundMatch>();
                    delayedReadyMatches.put(match.params.getType(), l);
                }

                l.add(match);

                /// do we still have room for another match
                if (getNumberOpenMatches(match.params.getType()) < match.params.getNConcurrentCompetitions()) {
                    match = l.removeFirst();
                    match.startMatch();
                }
            }
        } else{
            match.startMatch();
        }
    }

    private void checkDelayedMatches(){
        synchronized (delayedReadyMatches) {
            for (Entry<ArenaType, LinkedList<FoundMatch>> entry : delayedReadyMatches.entrySet()) {
                ArenaType at = entry.getKey();
                LinkedList<FoundMatch> l = delayedReadyMatches.get(at);
                Iterator<FoundMatch> iter = l.iterator();
                while(iter.hasNext()) {
                    FoundMatch fm = iter.next();
                    /// do we still have room for another match
                    if (getNumberOpenMatches(at) >= fm.params.getNConcurrentCompetitions()) {
                        break;
                    }
                    iter.remove();
                    fm.startMatch();
                }
            }
        }
    }

    public JoinResult join(MatchTeamQObject qo) {
        JoinResult jr = new JoinResult();
        jr.status = JoinResult.JoinStatus.CANT_FIT;
        jr.params = qo.getMatchParams();

        WaitingObject o;
        try {
            o = new WaitingObject(qo);
        } catch (NeverWouldJoinException e) {
            e.printStackTrace();
            return jr;
        }

        Arena a = reserveNextArena(qo.getMatchParams(), qo.getJoinOptions());
        if (a == null){
            synchronized (joinHandlers) {
                joinHandlers.add(o);
            }
            updateTimer(o,0L);
        } else {
            addReadyMatch(jr, o, a);
        }
        return jr;
    }

    public JoinResult join(TeamJoinObject qo) {
        JoinResult jr = new JoinResult();
        jr.status = JoinResult.JoinStatus.CANT_FIT;
        jr.params = qo.getMatchParams();
        jr.maxPlayers = qo.getMatchParams().getMaxPlayers();

        FoundMatch mf = null;
        WaitingObject o = null;
        boolean entered = false;

        synchronized (joinHandlers) {
            Iterator<WaitingObject> iter = joinHandlers.iterator();
            while (iter.hasNext()) {
                o = iter.next();
                if (!o.matches(qo)) {
                    continue;}

                TeamJoinResult r = o.join(qo);

                switch (r.status) {
                    case ADDED:
                    case ADDED_TO_EXISTING:
                    case ADDED_STILL_NEEDS_PLAYERS:
                        entered = true;
                        break;
                    case CANT_FIT:
                        continue;
                }
                /// not full, we aren't ready for a match yet
                /// but, if the force time has expired, we only need enough to start
                if (!o.isFull() && !(timeExpired(o) && o.hasEnough())) {
                    break;
                }
                /// find an arena for these teams
                Arena arena = reserveNextArena(o.getParams(), qo.getJoinOptions());
                if (arena == null){  /// No arena found
                    break;
                }
                /// create the match
                mf = createFoundMatch(jr, o, arena);
                iter.remove();
                break;
            }

            if (mf == null && !entered){
                /// Ok, we didn't match anything... let's create a WaitingObject for them
                try {
                    o = new WaitingObject(qo);
                    o.join(qo);
                    if (o.createsOnJoin()){
                        /// find an arena for these teams
                        Arena arena = getStartImmediately(o);
                        if (arena == null){  /// No arena found
                            jr.status = JoinStatus.ERROR;
                            return jr;
                        }
                        mf = createFoundMatch(jr, o, arena);
                        mf.startMatch();
                        return jr;
                    } else {
                        entered = true;
                        joinHandlers.add(o);
                        jr.status = JoinResult.JoinStatus.ADDED_TO_QUEUE;
                    }
                } catch (NeverWouldJoinException e) {
                    e.printStackTrace();
                }
            }
        }

        if (entered && o != null){
            jr.status = JoinResult.JoinStatus.ADDED_TO_QUEUE;
            /// Add count to games
            Integer i = inQueueForGame.get(o.getParams().getType());
            i = (i == null ? qo.size() : i + qo.size());

            inQueueForGame.put(o.getParams().getType(), i);
            jr.playersInQueue = i;
            jr.pos = i;


            /// Add count to arena
            if (o.getArena() != null){
                i = inQueueForArena.get(o.getArena());
                inQueueForArena.put(o.getArena(), i != null ? i + qo.size() : qo.size());
            }
            updateTimer(o);

            for (ArenaTeam t: qo.getTeams()){
                for (ArenaPlayer ap: t.getPlayers()) {
                    inQueue.put(ap.getName(), o);
                    callEvent(new ArenaPlayerEnterQueueEvent(ap,t,qo,jr));
                }
                methodController.updateEvents(MatchState.ONENTER, t.getPlayers());
            }
        }
        if (mf != null) {
            addReadyMatch(mf);}
        return jr;
    }


    public Match createMatch(Arena arena, EventOpenOptions eoo) throws MatchCreationException, NeverWouldJoinException {
        FoundMatch mf;
        mf = new FoundMatch();
        mf.arena = arena;
        mf.params = eoo.getParams();
        Match arenaMatch = mf.startMatch();
        AbstractJoinHandler jh = TeamJoinFactory.createTeamJoinHandler(eoo.getParams(), arenaMatch);
        arenaMatch.hookTeamJoinHandler(jh);
        return arenaMatch;
    }

    private Arena getStartImmediately(WaitingObject o) {
        /// do we still have room for another match
        if (getNumberOpenMatches(o.getParams().getType()) >= o.getParams().getNConcurrentCompetitions()) {
            return null;
        }
        return reserveNextArena(o.getParams(), o.getJoinOptions());
    }

    /**
     * Remove the player from the queue
     *
     * @param player ArenaPlayer
     * @return The ParamTeamPair object if the player was found.  Otherwise returns null
     */
    public boolean leave(ArenaPlayer player) {
        WaitingObject tjh = removeFromQueue(player, true);
        return tjh != null;
    }

    private void removeFromQueue(AbstractJoinHandler joinHandler) {
        for (ArenaTeam t : joinHandler.getTeams()) {
            for (ArenaPlayer ap : t.getPlayers()) {
                removeFromQueue(ap, false);
            }
        }
    }

    private WaitingObject removeFromQueue(ArenaPlayer player, boolean leaveJoinHandler) {
        WaitingObject wo = inQueue.remove(player.getName());
        if (wo != null) {
            if (leaveJoinHandler) {
                wo.jh.leave(player);
                if (wo.jh.getnPlayers() == 0) {
                    synchronized (joinHandlers) {
                        joinHandlers.remove(wo);
                    }
                }
            }
            inQueueForGame.put(wo.getParams().getType(), inQueueForGame.get(wo.getParams().getType()) - 1);
            if (wo.getArena() != null)
                inQueueForArena.put(wo.getArena(), inQueueForArena.get(wo.getArena()) - 1);
            methodController.updateEvents(MatchState.ONLEAVE, player);
            callEvent(new ArenaPlayerLeaveQueueEvent(player, wo.getParams(),wo.getArena()));
        }
        return wo;
    }

    private FoundMatch createFoundMatch(JoinResult jr,  WaitingObject o, Arena arena) {
        FoundMatch mf;
        mf = new FoundMatch();
        mf.arena = arena;
        mf.wo = o;
        mf.params = o.getParams();
        mf.joinHandler = o.jh;
        if (jr != null){
            jr.status = JoinResult.JoinStatus.STARTED_NEW_GAME;
            jr.params = o.getParams();
        }
        return mf;
    }

    public boolean forceStart(boolean respectMinimumPlayers) {
        return forceStart(null,respectMinimumPlayers);
    }

    public boolean forceStart(MatchParams params, boolean needsMinPlayers) {
        return forceStart(null, params, needsMinPlayers);
    }

    private boolean forceStart(WaitingObject wo, MatchParams params, boolean needsMinPlayers) {
        List<FoundMatch> finds = null;
        synchronized (joinHandlers) {
            Iterator<WaitingObject> iter = joinHandlers.iterator();
            WaitingObject o;
            boolean resetParams = false;
            while (iter.hasNext()) {
                o = iter.next();
                if (wo != null && !o.equals(wo)){
                    continue;}
                if (!o.hasEnough()){
                    if (needsMinPlayers) {
                        continue;}
                    resetParams = true;
                }
                if (params !=null && !o.getParams().matches(params)){
                    continue;}
                Arena arena = reserveNextArena(o.getParams(), o.getJoinOptions());
                if (arena == null) {
                    break;}
                FoundMatch mf = createFoundMatch(null, o, arena);
                iter.remove();
                if (finds == null) {
                    finds = new ArrayList<FoundMatch>();}
                if (resetParams){
                    mf.params = ParamController.copyParams(mf.params);
                    mf.params.setNTeams(new MinMax(0, ArenaSize.MAX));
                    mf.params.setTeamSize(new MinMax(0, ArenaSize.MAX));
                }
                finds.add(mf);
            }
        }
        if (finds != null) {
            for (FoundMatch mf : finds) {
                addReadyMatch(mf);
            }
        }
        checkDelayedMatches();
        return finds != null && !finds.isEmpty();
    }

    private void remove(WaitingObject wo) {
        synchronized (joinHandlers) {
            joinHandlers.remove(wo);
            removeTimer(wo);
        }
    }


    public boolean isInQue(ArenaPlayer p) {
        return inQueue.containsKey(p.getName());
    }

    public boolean isInQue(String name) {
        return inQueue.containsKey(name);
    }


    public WaitingObject getQueueObject(ArenaPlayer p) {
        return inQueue.get(p.getName());
    }

    public void stop() {
        suspend.set(true);
    }

    public void resume() {
        suspend.set(false);
    }

    public  Collection<ArenaTeam> purgeQueue(){
        List<ArenaTeam> teams = new ArrayList<ArenaTeam>();

        Map<ArenaPlayer, WaitingObject> players = new HashMap<ArenaPlayer, WaitingObject>();
        synchronized(delayedReadyMatches){
            for (List<FoundMatch> list : delayedReadyMatches.values()){
                for (FoundMatch fm: list) {
                    teams.addAll(fm.wo.jh.getTeams());
                    for (ArenaPlayer ap : fm.wo.getPlayers()) {
                        fm.wo.jh.leave(ap);
                        players.put(ap, fm.wo);
                    }
                }
            }
            delayedReadyMatches.clear();
        }
        synchronized(joinHandlers) {
            for (WaitingObject o : joinHandlers) {
                teams.addAll(o.jh.getTeams());
                for (ArenaPlayer ap : o.getPlayers()) {
                    o.jh.leave(ap);
                    players.put(ap, o);
                }
            }
            joinHandlers.clear();
        }
        for (Entry<ArenaPlayer,WaitingObject> entry : players.entrySet()) {
            callEvent(new ArenaPlayerLeaveQueueEvent(entry.getKey(),
                    entry.getValue().getParams(),entry.getValue().getArena()));
        }
        inQueue.clear();
        inQueueForGame.clear();
        inQueueForArena.clear();
        return teams;
    }

    public Arena getNextArena(MatchParams mp) {
        synchronized (arenaqueue) {
            ArenaQueue aq = arenaqueue.get(mp.getType());
            if (aq == null)
                return null;
            for (Arena a : aq){
                if (a.getArenaType() != mp.getType())
                    continue;
                if (!a.valid() || !a.matches(mp))
                    continue;
                return a;
            }
        }
        return null;
    }

    public Arena getNextArena(JoinOptions jo) {
        synchronized (arenaqueue) {
            ArenaQueue aq = arenaqueue.get(jo.getMatchParams().getType());
            if (aq == null)
                return null;
            for (Arena a : aq){
                if (!a.valid() || !a.matches(jo))
                    continue;
                return a;
            }
        }
        return null;
    }

    private Arena reserveNextArena(MatchParams mp, JoinOptions jo) {
        synchronized (arenaqueue) {
            ArenaQueue aq = arenaqueue.get(mp.getType());
            if (aq == null)
                return null;
            Iterator<Arena> iter = aq.iterator();
            Arena a;
            while(iter.hasNext()){
                a = iter.next();
                if (!a.valid() || !a.matches(jo))
                    continue;
                iter.remove();
                return a;
            }
        }
        return null;
    }

    public Arena removeArena(Arena arena) {
        synchronized (arenaqueue) {
            ArenaQueue aq = arenaqueue.get(arena.getArenaType());
            if (aq == null) {
                return null;}

            Iterator<Arena> iter = aq.iterator();
            while(iter.hasNext()) {
                Arena a = iter.next();
                if (a.getName().equalsIgnoreCase(arena.getName())) {
                    iter.remove();
                    return a;
                }
            }
        }
        return null;
    }

    public Arena reserveArena(Arena arena) {
        return removeArena(arena);
    }

    @Override
    public String toString(){
        return queuesToString() + toStringArenas();
    }

    public String toStringArenas(){
        StringBuilder sb = new StringBuilder();
        sb.append("------AMQ Arenas------- \n");
        synchronized(arenaqueue){
            for (Entry<ArenaType,ArenaQueue> entry : arenaqueue.entrySet()){
                sb.append(" ------ ").append(entry.getKey()).append("------- \n");
                for (Arena arena : entry.getValue()){
                    sb.append(arena).append("\n");
                }
            }
            return sb.toString();
        }
    }

    public String queuesToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("------game queues-------\n");
        synchronized (joinHandlers) {
            for (WaitingObject o : joinHandlers) {
                IdTime t = forceTimers.get(o);
                sb.append("  ------ o ").append(o.hashCode()).append(" - ").
                        append(o.params.getDisplayName()).append(" - ").
                        append(o.getArena() != null ? o.getArena().getName() : "null");
                if (t != null)
                    sb.append(" fs=").append((t.time - System.currentTimeMillis()) / 1000);
                sb.append("------\n");

                for (ArenaTeam at : o.jh.getTeams()) {
                    sb.append("  t ").append(at).append(" - ").append(at.getId()).append("\n");
                }
            }
        }
        sb.append("------ready matches queue-------\n");
        synchronized (delayedReadyMatches) {
            for (Entry<ArenaType, LinkedList<FoundMatch>> entry : delayedReadyMatches.entrySet()) {
                for (FoundMatch fm : entry.getValue()) {
                    sb.append("  ------ o ").append(fm.wo.hashCode()).append(" - ").
                            append(fm.wo.params.getDisplayName()).append(" - ").
                            append(fm.wo.getArena() != null ? fm.wo.getArena().getName() : "null").append("------\n");
                    for (ArenaTeam at : fm.wo.jh.getTeams()) {
                        sb.append("  t ").append(at).append(" - ").append(at.getId()).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    public Collection<ArenaPlayer> getPlayersInQueue(MatchParams params) {
        List<ArenaPlayer> players = new ArrayList<ArenaPlayer>();
        synchronized (joinHandlers) {
            for (WaitingObject o : joinHandlers) {
                if (params.matches(o.getParams())) {
                    for (ArenaTeam at : o.jh.getTeams()) {
                        players.addAll(at.getPlayers());
                    }
                }
            }
        }
        synchronized(delayedReadyMatches) {
            List<FoundMatch> list = delayedReadyMatches.get(params.getType());
            if (list != null){
                for (FoundMatch fm : list) {
                    players.addAll(fm.wo.getPlayers());
                }
            }
        }

        return players;
    }

    public Collection<ArenaPlayer> getPlayersInAllQueues() {
        List<ArenaPlayer> players = new ArrayList<ArenaPlayer>();
        synchronized (joinHandlers) {
            for (WaitingObject o : joinHandlers) {
                for (ArenaTeam at : o.jh.getTeams()) {
                    players.addAll(at.getPlayers());
                }
            }
        }
        synchronized(delayedReadyMatches){
            for (List<FoundMatch> list : delayedReadyMatches.values()){
                for (FoundMatch fm: list) {
                    players.addAll(fm.wo.getPlayers());
                }
            }
        }
        return players;
    }

    public List<String> invalidReason(WaitingObject qo){
        List<String> reasons = new ArrayList<String>();
        MatchParams params = qo.getParams();
        synchronized(arenaqueue) {
            ArenaQueue aq = arenaqueue.get(params.getType());
            for (Arena arena : aq) {
                reasons.addAll(arena.getInvalidMatchReasons(params, qo.getJoinOptions()));
            }
        }
        return reasons;
    }

    public void removeAllArenas() {
        arenaqueue.clear();
    }

    public void removeAllArenas(ArenaType arenaType) {
        synchronized(arenaqueue) {
            ArenaQueue aq = arenaqueue.get(arenaType);
            if (aq != null)
                aq.clear();
        }
    }

    public void clearTeamQueues() {
        for (IdTime idt: forceTimers.values()){
            idt.c.stop();
        }
        forceTimers.clear();
    }


    protected void callEvent(BAEvent event){
        methodController.callEvent(event);
    }

    /**
     * Get the number of players in the queue for this arena
     * @param arena Arena
     * @return player count
     */
    public int getQueueCount(Arena arena) {
        return inQueueForArena.containsKey(arena) ? inQueueForArena.get(arena) : 0;
    }

    /**
     * Get the number of players in the queue for this game type
     * @param params ArenaParams
     * @return player count
     */
    public int getQueueCount(ArenaParams params) {
        return inQueueForGame.containsKey(params.getType()) ? inQueueForGame.get(params.getType()) : 0;
    }

    @ArenaEventHandler
    public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event) {
        WaitingObject wo = removeFromQueue(event.getPlayer(), true);
        if (wo != null) {
            ArenaPlayer ap = event.getPlayer();
            event.addMessage(MessageHandler.getSystemMessage("you_left_queue", wo.getParams().getName()));
            /// They are inbetween.. but let match handle
            if (ap.getCompetition()!=null && ap.getCompetition() instanceof Match){
                return;
            }
            PlayerSave ps = ap.getMetaData().getJoinRequirements();
            if (ps!=null){
                new PlayerStoreController(ps).restoreAll(event.getPlayer());
            }
        }
    }

    @ArenaEventHandler
    public void onPlayerChangeWorld(PlayerTeleportEvent event){
        if (event.isCancelled())
            return;
        if (event.getFrom().getWorld().getUID() != event.getTo().getWorld().getUID() &&
                !event.getPlayer().hasPermission(Permissions.TELEPORT_BYPASS_PERM)){
            ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
            if (removeFromQueue(ap, true)!=null){
                MessageUtil.sendMessage(ap, "&cYou have been removed from the queue for changing worlds");
            }
        }
    }

    @ArenaEventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        if (!event.isCancelled() &&
                CommandUtil.shouldCancel(event, disabledAllCommands, disabledCommands, enabledCommands)){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED+"You cannot use that command when you are in the queue");
            if (PermissionsUtil.isAdmin(event.getPlayer())){
                MessageUtil.sendMessage(event.getPlayer(), "&cYou can set &6/bad allowAdminCommands true: &c to change");}
        }
    }

    public static void setDisabledCommands(List<String> commands) {
        if (commands == null)
            return;
        disabledCommands.clear();
        if (commands.contains("all")) {
            disabledAllCommands = true;
        } else {
            for (String s: commands){
                disabledCommands.add("/" + s.toLowerCase());}
        }
    }


    public static void setEnabledCommands(List<String> commands) {
        if (commands == null)
            return;
        enabledCommands.clear();
        for (String s: commands){
            enabledCommands.add("/" + s.toLowerCase());}
    }

    class AnnounceInterval {
        AnnounceInterval(final ArenaMatchQueue amq, final WaitingObject wo, IdTime idt, final long timeMillis){
            final Countdown c = new Countdown(BattleArena.getSelf(),
                    timeMillis/1000, 30L, new Countdown.CountdownCallback(){
                @Override
                public boolean intervalTick(int secondsRemaining) {
                    if (secondsRemaining > 0 || secondsRemaining < 0){
                        Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
                        players.addAll(wo.getPlayers());
                        String msg = BAExecutor.constructMessage(wo.getParams(), secondsRemaining * 1000L, players.size(), null);
                        MessageUtil.sendMessage(players, msg);
                    } else {
                        if (!amq.forceStart(wo, null, true) && wo.getParams().isCancelIfNotEnoughPlayers()) {
                            amq.remove(wo);
                        }
                    }
                    return true;
                }
            });
            c.setCancelOnExpire(false);
            idt.c = c;
            forceTimers.put(wo, idt);
        }

    }
    private boolean timeExpired(WaitingObject wo){
        IdTime idt = forceTimers.get(wo);
        return idt != null && System.currentTimeMillis() - idt.time >= 0;
    }

    private Long removeTimer(WaitingObject wo){
        IdTime idt = forceTimers.remove(wo);
        if (idt != null && idt.c != null){
            idt.c.stop();
            return idt.time - System.currentTimeMillis();
        }
        return null;
    }

    private IdTime updateTimer(final WaitingObject to) {
        Long time = (System.currentTimeMillis() + to.getParams().getForceStartTime()*1000);
        return updateTimer(to, time);
    }

    /**
     * Update the forceJoin timer for the following TeamQueue and the given QueueObject
     * The time will not be updated if an older timer is ongoing
     *
     * @param to QueueObject
     * @return IdTime
     */
    private IdTime updateTimer(final WaitingObject to, Long time) {
        IdTime idt = forceTimers.get(to);
        if (idt == null){
            idt = new IdTime();
            idt.time = time;
            new AnnounceInterval(this, to, idt, time-System.currentTimeMillis());
        }
        return idt;
    }


    public int getNumberOpenMatches(ArenaType type){
        Integer count = runningMatchTypes.get(type);
        if (count==null){
            count = 0;
            runningMatchTypes.put(type, count);
        }
        return count;
    }

    public int incNumberOpenMatches(ArenaType type){
        Integer count = runningMatchTypes.get(type);
        if (count==null){
            count = 0;}
        runningMatchTypes.put(type, ++count);
        return count;
    }

    private int decNumberOpenMatches(ArenaType type){
        Integer count = runningMatchTypes.get(type);
        if (count==null){
            count = 1;}
        runningMatchTypes.put(type, --count);
        return count;
    }

    @EventHandler
    public void matchFinished(MatchFinishedEvent event){
        if (Defaults.DEBUG ) Log.info("AMQ::matchFinished=" + this + ":" );
        Match am = event.getMatch();
        decNumberOpenMatches(am.getArena().getArenaType());
        forceStart(true);
    }
}
