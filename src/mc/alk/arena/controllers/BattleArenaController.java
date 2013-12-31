package mc.alk.arena.controllers;

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
import java.util.concurrent.CopyOnWriteArraySet;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.controllers.ArenaAlterController.ChangeType;
import mc.alk.arena.controllers.containers.GameManager;
import mc.alk.arena.controllers.containers.RoomContainer;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.listeners.SignUpdateListener;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.ContainerState;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaControllerInterface;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.objects.options.EventOpenOptions.EventOpenOption;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.queues.ArenaMatchQueue;
import mc.alk.arena.objects.queues.ArenaQueue;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.queues.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MinMax;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.ServerUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BattleArenaController implements Runnable, /*TeamHandler, */ ArenaListener, Listener{

    private boolean stop = false;
    private boolean running = false;

    final private Set<Match> running_matches = Collections.synchronizedSet(new CopyOnWriteArraySet<Match>());
    final private Map<String, Integer> runningMatchTypes = Collections.synchronizedMap(new HashMap<String, Integer>());
    final private Map<ArenaType,LinkedList<Match>> unfilled_matches = Collections.synchronizedMap(new ConcurrentHashMap<ArenaType,LinkedList<Match>>());
    private Map<String, Arena> allarenas = new ConcurrentHashMap<String, Arena>();
    private Map<ArenaType, ArenaQueue> nextArena = new ConcurrentHashMap<ArenaType, ArenaQueue>();
    final private Map<Match, OldMatchContainerState> oldArenaStates = new HashMap<Match, OldMatchContainerState>();
    final private Map<ArenaType,OldLobbyState> oldLobbyState = new HashMap<ArenaType,OldLobbyState>();
    private final ArenaMatchQueue amq = new QueueController();
    final SignUpdateListener signUpdateListener;

    public class OldLobbyState{
        ContainerState pcs;
        Set<Match> running = new HashSet<Match>();
        public boolean isEmpty() {return running.isEmpty();}
        public void add(Match am){running.add(am);}
        public boolean remove(Match am) {return running.remove(am);}
    }

    public class OldMatchContainerState{
        final ContainerState waitroomCS;
        final ContainerState arenaCS;
        MatchParams params = null;

        public OldMatchContainerState(Arena arena) {
            waitroomCS = (arena.getWaitroom()!=null) ? arena.getWaitroom().getContainerState() : null;
            arenaCS = arena.getContainerState();
        }

        public void revert(Arena arena) {
            if(params != null)
                arena.setParams(params);
            arena.setContainerState(arenaCS);
            if (waitroomCS != null && arena.getWaitroom()!=null){
                arena.getWaitroom().setContainerState(waitroomCS);}
        }
    }

    public BattleArenaController(SignUpdateListener signUpdateListener){
        MethodController methodController = new MethodController("BAC");
        methodController.addAllEvents(this);
        try{Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());}catch(Exception e){/* keep on truckin'*/}
        this.signUpdateListener = signUpdateListener;
    }

    /// Run is Thread Safe
    public void run() {
        running = true;
        Match match;
        while (!stop){
            match = amq.getArenaMatch();
            if (match != null){
                preOpenChanges(match);
                Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new OpenAndStartMatch(match));
            }
        }
        running = false;
    }

    private void preOpenChanges(Match match) {
        addLast(match.getArena());
        if (match.hasWaitroom() && match.getParams().isWaitroomClosedWhenRunning()){
            saveStates(match, match.getArena());
            match.getArena().setContainerState(ChangeType.WAITROOM,
                    new ContainerState(ContainerState.AreaContainerState.CLOSED,
                            "&cA match is already in progress in arena "+
                                    match.getArena().getName()));
        }
    }

    /**
     * opens and starts the match
     */
    private class OpenAndStartMatch implements Runnable{
        Match match;
        public OpenAndStartMatch(Match match){
            this.match = match;
        }
        @Override
        public void run() {
            openMatch(match);
            match.run();
        }
    }

    public int getNumberOpenMatches(String type){
        Integer count = runningMatchTypes.get(type);
        if (count==null){
            count = 0;
            runningMatchTypes.put(type, count);
        }
        return count;
    }

    public int incNumberOpenMatches(String type){
        Integer count = runningMatchTypes.get(type);
        if (count==null){
            count = 0;}
        runningMatchTypes.put(type, ++count);
        return count;
    }

    public int decNumberOpenMatches(String type){
        Integer count = runningMatchTypes.get(type);
        if (count==null){
            count = 1;}
        runningMatchTypes.put(type, --count);
        return count;
    }

    public Match createMatch(Arena arena, EventOpenOptions eoo) throws NeverWouldJoinException {
        final ArenaMatch arenaMatch = new ArenaMatch(arena, eoo.getParams());
        TeamJoinHandler jh = TeamJoinFactory.createTeamJoinHandler(eoo.getParams(), arenaMatch);
        arenaMatch.setTeamJoinHandler(jh);
        Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
            @Override
            public void run() {
                openMatch(arenaMatch);
                amq.fillMatch(arenaMatch);
            }
        });
        return arenaMatch;
    }


    public Match createAndAutoMatch(Arena arena, EventOpenOptions eoo)
            throws NeverWouldJoinException, IllegalStateException {
        /// eoo
        MatchParams mp = eoo.getParams();
        OldMatchContainerState old = new OldMatchContainerState(arena);
        old.params = arena.getParams();

        mp.setForceStartTime((long) eoo.getSecTillStart());
        ArenaParams parent = mp.getParent();
        mp.setParent(null);

        MinMax nTeams = mp.getNTeams();
        MinMax teamSize = mp.getTeamSizes();
        if( nTeams == null){
            mp.setNTeams(new MinMax(ArenaSize.MAX));}
        if( teamSize == null){
            mp.setTeamSizes(new MinMax(1));}
        mp.setParent(parent);
        amq.setForcestartTime(arena, mp, mp.getForceStartTime());

        arena.setParams(mp);
        Match m = createMatch(arena,eoo);
        oldArenaStates.put(m, old);
        preOpenChanges(m);
        saveStates(m,arena);
        arena.setAllContainerState(ContainerState.OPEN);
        m.setTimedStart(eoo.getSecTillStart(), eoo.getInterval());

//		/// Since we want people to join this event, add this arena as the next
        nextArena.get(arena.getArenaType()).addFirst(arena);

        if (eoo.hasOption(EventOpenOption.FORCEJOIN)){
            addAllOnline(m.getParams(), arena);}

        return m;
    }

    private void addAllOnline(MatchParams mp, Arena arena) {
        Player[] online = ServerUtil.getOnlinePlayers();
        String cmd = mp.getCommand() +" join "+arena.getName();
        for (Player p: online){
            PlayerUtil.doCommand(p, cmd);
        }
    }

    private OldMatchContainerState getOrCreateSavedState(Match m, Arena arena) {
        OldMatchContainerState old = oldArenaStates.get(m);
        if(old == null){
            old = new OldMatchContainerState(arena);
            oldArenaStates.put(m,old);
        }
        return old;
    }
    private void saveStates(Match m, Arena arena) {
        /// save the old states to put back after the match
        getOrCreateSavedState(m, arena);
        if (RoomController.hasLobby(arena.getArenaType())){
            RoomContainer pc = RoomController.getLobby(arena.getArenaType());
            OldLobbyState ols = oldLobbyState.get(arena.getArenaType());
            if (ols == null){
                ols = new OldLobbyState();
                ols.pcs = pc.getContainerState();
                oldLobbyState.put(arena.getArenaType(), ols);
            }
            ols.add(m);
        }
    }

    private void openMatch(Match match){
        match.addArenaListener(this);
        synchronized(running_matches){
            running_matches.add(match);
        }
        incNumberOpenMatches(match.getParams().getType().getName());
        match.open();
        if (match.isJoinablePostCreate()){
            LinkedList<Match> matches = unfilled_matches.get(match.getParams().getType());
            if (matches == null){
                matches = new LinkedList<Match>();
                unfilled_matches.put(match.getParams().getType(), matches);
            }
            matches.addFirst(match);
        }
    }

    private void restoreStates(Match am, Arena arena){
        if (arena == null)
            arena = am.getArena();
        OldLobbyState ols = oldLobbyState.get(arena.getArenaType());
        if (ols != null){ /// we only put back the lobby state if its the last autoed match finishing
            if (ols.remove(am) && ols.isEmpty()){
                RoomController.getLobby(am.getArena().getArenaType()).setContainerState(ols.pcs);
            }
        }
        OldMatchContainerState states = oldArenaStates.remove(am);
        if (states != null){
            states.revert(arena);}
    }

    public void startMatch(Match arenaMatch) {
        /// arenaMatch run calls.... broadcastMessage ( which unfortunately is not thread safe)
        /// So we have to schedule a sync task... again
        Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), arenaMatch);
    }


    @ArenaEventHandler
    public void matchFinished(MatchFinishedEvent event){
        if (Defaults.DEBUG ) Log.info("BattleArenaController::matchFinished=" + this + ":" );
        Match am = event.getMatch();
        removeMatch(am); /// handles removing running match from the BArenaController
        decNumberOpenMatches(am.getArena().getArenaType().getName());

        //		unhandle(am);/// unhandle any teams that were added during the match
        final Arena arena = allarenas.get(am.getArena().getName().toUpperCase());
        /// put back old states if it was autoed
        restoreStates(am, arena);
        if (am.getParams().hasOptionAt(MatchState.ONCOMPLETE, TransitionOption.REJOIN)){
            MatchParams mp = am.getParams();
            List<ArenaPlayer> players = am.getNonLeftPlayers();
            String[] args = {};
            for (ArenaPlayer ap: players){
                BattleArena.getBAExecutor().join(ap, mp, args);
            }
        }
        /// isEnabled to check to see if we are shutting down
        if (arena != null && BattleArena.getSelf().isEnabled()){
            Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
                @Override
                public void run() {
                    amq.add(arena,shouldStart(arena)); /// add it back into the queue
                }
            }, am.getParams().getArenaCooldown()*20L);
        }
    }

    public void updateArena(Arena arena) {
        allarenas.put(arena.getName().toUpperCase(), arena);
        if (amq.removeArena(arena) != null){ /// if its not being used
            amq.add(arena,shouldStart(arena));}
        addLast(arena);
    }

    public void addArena(Arena arena) {
        allarenas.put(arena.getName().toUpperCase(), arena);
        addLast(arena);
        amq.add(arena, shouldStart(arena));
    }

    private LinkedList<Arena> addLast(Arena arena) {
        ArenaType arenaType = arena.getArenaType();
        ArenaQueue list = nextArena.get(arenaType);
        if (list == null){
            list = new ArenaQueue();
            nextArena.put(arenaType, list);
        }
        list.addLast(arena);
        return list;
    }

    public Arena getNextArena(ArenaType arenaType){
        LinkedList<Arena> list = nextArena.get(arenaType);
        return list == null || list.isEmpty() ? null : list.getFirst();
    }

    public Map<String, Arena> getArenas(){return allarenas;}

    /**
     * Add the TeamQueueing object to the queue
     * @param tqo the TeamQueueing object to the queue
     * @return JoinResult
     */
    public JoinResult wantsToJoin(TeamJoinObject tqo) throws IllegalStateException{
        /// Can they join an existing Game
        if (joinExistingMatch(tqo)){
            JoinResult qr = new JoinResult();
            qr.status = JoinResult.JoinStatus.ADDED_TO_EXISTING_MATCH;
            return qr;
        }
        /// Add a default arena if they havent specified
        if (!tqo.getJoinOptions().hasArena()){
            tqo.getJoinOptions().setArena(getNextArena(tqo.getMatchParams().getType()));
        }
        JoinResult jr = amq.join(tqo,shouldStart(tqo.getMatchParams()));
        MatchParams mp = tqo.getMatchParams();
        /// who is responsible for doing what
        if (Defaults.DEBUG)Log.info(" Join status = " + jr.status +"    " + tqo.getTeam() + "   " + tqo.getTeam().getId() +" --"
                + ", haslobby="+mp.needsLobby() +"  ,wr="+(mp.hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTWAITROOM))+"  "+
                "   --- hasArena=" + tqo.getJoinOptions().hasArena());
        if (tqo.getJoinOptions().hasArena()){
            Arena a = tqo.getJoinOptions().getArena();
            if (mp.hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTIN) && BattleArena.getBAController().getMatch(a) != null){
                throw new IllegalStateException("&cThe arena " + a.getDisplayName() +"&c is currently in use");
            }
        }
        switch(jr.status){
            case ADDED_TO_ARENA_QUEUE:
            case ADDED_TO_QUEUE:
                break;
            case NONE:
                break;
            case ERROR:
            case ADDED_TO_EXISTING_MATCH:
            case STARTED_NEW_GAME:
                return jr;
            default:
                break;
        }
        if (mp.needsLobby()){
            if (!RoomController.hasLobby(mp.getType())){
                throw new IllegalStateException("&cLobby is not set for the "+mp.getName());}
            RoomController.getLobby(mp.getType()).teamJoining(tqo.getTeam());
        }
        if (tqo.getJoinOptions().hasArena()){
            Arena a = tqo.getJoinOptions().getArena();
            if (mp.hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTWAITROOM) ||
                    mp.hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTMAINWAITROOM)){
                if (a.getWaitroom()== null){
                    throw new IllegalStateException("&cWaitroom is not set for this arena");}
                a.getWaitroom().teamJoining(tqo.getTeam());
            } else if (mp.hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTSPECTATE)){
                if (a.getSpectatorRoom()== null){
                    throw new IllegalStateException("&cSpectate is not set for this arena");}
                a.getSpectatorRoom().teamJoining(tqo.getTeam());
            } else if (mp.hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTIN)){
                tqo.getJoinOptions().getArena().teamJoining(tqo.getTeam());
            }
        }
        return jr;
    }

    private boolean shouldStart(MatchParams matchParams) {
        return shouldStart(matchParams.getType().getName(), matchParams);
    }

    private boolean shouldStart(Arena arena) {
        final String arenaType = arena.getArenaType().getName();
        return shouldStart(arenaType, ParamController.getMatchParams(arenaType));
    }

    private boolean shouldStart(String type, MatchParams mp){
        return type == null || mp == null || getNumberOpenMatches(type) < mp.getNConcurrentCompetitions();
    }


    private boolean joinExistingMatch(TeamJoinObject tqo) {
        if (unfilled_matches.isEmpty()){
            return false;}
        MatchParams params = tqo.getMatchParams();
        synchronized(unfilled_matches){
            List<Match> matches = unfilled_matches.get(params.getType());
            if (matches == null)
                return false;
            for (Match match : matches) {
                /// We dont want people joining in a non waitroom state
                if (!match.canStillJoin()) {
                    continue;
                }
                if (match.getParams().matches(params)) {
                    TeamJoinHandler tjh = match.getTeamJoinHandler();
                    if (tjh == null)
                        continue;
                    if (!JoinOptions.matches(tqo.getJoinOptions(), match))
                        continue;
                    boolean result = false;
                    TeamJoinResult tjr = tjh.joiningTeam(tqo);
                    switch (tjr.status) {
                        case ADDED_TO_EXISTING:
                        case ADDED:
                            result = true;
                        default:
                            break;
                    }
                    return result;
                }
            }
        }
        return false;
    }

    public boolean isInQue(ArenaPlayer p) {return amq.isInQue(p);}

    public void addMatchup(Matchup m) {amq.addMatchup(m, shouldStart(m.getMatchParams()));}
    public Arena reserveArena(Arena arena) {return amq.reserveArena(arena);}
    public Arena getArena(String arenaName) {return allarenas.get(arenaName.toUpperCase());}

    public Arena removeArena(Arena arena) {
        Arena a = amq.removeArena(arena);
        if (a != null){
            allarenas.remove(arena.getName().toUpperCase());}
        ArenaQueue aq = nextArena.get(arena.getArenaType());
        if (aq != null){
            aq.remove(arena);
        }
        return a;
    }

    public void deleteArena(Arena arena) {
        removeArena(arena);
        ArenaControllerInterface ai = new ArenaControllerInterface(arena);
        ai.delete();
    }

    public void arenaChanged(Arena arena){
        try{
            if (removeArena(arena) != null){
                addArena(arena);}
        } catch (Exception e){
            Log.printStackTrace(e);
        }
    }

    public Arena nextArenaByMatchParams(MatchParams mp){
        return amq.getNextArena(mp,null);
    }

    public Arena getArenaByMatchParams(MatchParams mp, JoinOptions jp) {
        for (Arena a : allarenas.values()){
            if (a.valid() && a.matches(mp,jp)){
                return a;}
        }
        return null;
    }

    public List<Arena> getArenas(MatchParams mp) {
        List<Arena> arenas = new ArrayList<Arena>();
        for (Arena a : allarenas.values()){
            if (a.valid() && a.matches(mp,null)){
                arenas.add(a);}
        }
        return arenas;
    }

    public Arena getArenaByNearbyMatchParams(MatchParams mp, JoinOptions jp) {
        Arena possible = null;
        int sizeDif = Integer.MAX_VALUE;
        int m1 = mp.getMinTeamSize();
        for (Arena a : allarenas.values()){
            if (!a.valid() || a.getArenaType() != mp.getType())
                continue;
            if (a.matchesIgnoreSize(mp,jp)){
                return a;}
            int m2 = a.getParams().getMinTeamSize();
            if (m2 > m1 && m2 -m1 < sizeDif){
                sizeDif = m2 - m1;
                possible = a;
            }
        }
        return possible;
    }


    public Map<Arena,List<String>> getNotMachingArenaReasons(MatchParams mp, JoinOptions jp) {
        Map<Arena,List<String>> reasons = new HashMap<Arena, List<String>>();
        for (Arena a : allarenas.values()){
            if (a.getArenaType() != mp.getType()){
                continue;
            }
            if (jp != null && !jp.matches(a))
                continue;
            if (!a.valid()){
                reasons.put(a, a.getInvalidReasons());}
            if (!a.matches(mp,jp)){
                List<String> rs = reasons.get(a);
                if (rs == null){
                    reasons.put(a, a.getInvalidMatchReasons(mp, jp));
                } else {
                    rs.addAll(a.getInvalidMatchReasons(mp, jp));
                }
            }
        }
        return reasons;
    }

    //	/**
    //	 * We dont care if they leave queues
    //	 */
    //	@Override
    //	public boolean canLeave(ArenaPlayer p) {
    //		return true;
    //	}

    public boolean hasArenaSize(int i) {return getArenaBySize(i) != null;}
    public Arena getArenaBySize(int i) {
        for (Arena a : allarenas.values()){
            if (a.getParams().matchesTeamSize(i)){
                return a;}
        }
        return null;
    }

    private void removeMatch(Match am){
        synchronized(running_matches){
            running_matches.remove(am);
        }
        synchronized(unfilled_matches){
            List<Match> matches = unfilled_matches.get(am.getParams().getType());
            if (matches != null){
                matches.remove(am);
            }
        }
    }

    public synchronized void stop() {
        if (stop)
            return;
        stop = true;
        amq.stop();
        amq.purgeQueue();
        synchronized(running_matches){
            for (Match am: running_matches){
                cancelMatch(am);
                Arena a = am.getArena();
                if (a != null){
                    Arena arena = allarenas.get(a.getName().toUpperCase());
                    if (arena != null)
                        amq.add(arena, false);
                }
            }
            running_matches.clear();
        }
    }

    public void resume() {
        stop = false;
        amq.resume();
        if (!running){
            new Thread(this).start();
        }
    }

    //	/**
    //	 * If they are in a queue, take them out
    //	 */
    //	public boolean leave(ArenaPlayer p) {
    //		ParamTeamPair ptp = removeFromQue(p);
    //		if (ptp != null){
    ////			unhandle(p,ptp.team,ptp.q);
    //			ptp.team.sendMessage("&cYour team has left the queue b/c &6"+p.getDisplayName()+"c has left");
    //			return true;
    //		}
    //		return false;
    //	}

    public boolean cancelMatch(Arena arena) {
        synchronized(running_matches){
            for (Match am: running_matches){
                if (am.getArena().getName().equals(arena.getName())){
                    cancelMatch(am);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean cancelMatch(ArenaPlayer p) {
        Match am = getMatch(p);
        if (am==null)
            return false;
        cancelMatch(am);
        return true;
    }

    public boolean cancelMatch(ArenaTeam team) {
        Set<ArenaPlayer> ps = team.getPlayers();
        return !ps.isEmpty() && cancelMatch(ps.iterator().next());
    }

    public void cancelMatch(Match am){
        am.cancelMatch();
        for (ArenaTeam t: am.getTeams()){
            t.sendMessage("&4!!!&e This arena match has been cancelled");
        }
    }

    public Match getArenaMatch(Arena a) {
        synchronized(running_matches){
            for (Match am: running_matches){
                if (am.getArena().getName().equals(a.getName())){
                    return am;
                }
            }
        }
        return null;
    }

    public boolean insideArena(ArenaPlayer p) {
        synchronized(running_matches){
            for (Match am: running_matches){
                if (am.isHandled(p)){
                    return true;
                }
            }
        }
        return false;
    }

    public Match getMatch(ArenaPlayer p) {
        synchronized(running_matches){
            for (Match am: running_matches){
                if (am.hasPlayer(p)){
                    return am;
                }
            }
        }
        return null;
    }

    public Match getMatch(Arena arena) {
        synchronized(running_matches){
            for (Match am: running_matches){
                if (am.getArena().equals(arena)){
                    return am;
                }
            }
        }
        return null;
    }

    @Override
    public String toString(){
        return "[BAC]";
    }

    public String toStringQueuesAndMatches(){
        StringBuilder sb = new StringBuilder();
        sb.append(amq.toStringReadyMatches());
        sb.append("------ running  matches -------\n");
        synchronized(running_matches){
            for (Match am : running_matches)
                sb.append(am).append("\n");
        }
        return sb.toString();
    }

    public String toStringArenas(){
        StringBuilder sb = new StringBuilder();
        sb.append(amq.toStringArenas());
        sb.append("------ arenas -------\n");
        for (Arena a : allarenas.values()){
            sb.append(a).append("\n");
        }
        return sb.toString();
    }


    public void removeAllArenas() {
        synchronized(running_matches){
            for (Match am: running_matches){
                am.cancelMatch();
            }
        }

        amq.stop();
        amq.removeAllArenas();
        allarenas.clear();
        amq.resume();
        nextArena.clear();
    }

    public void removeAllArenas(ArenaType arenaType) {
        synchronized(running_matches){
            for (Match am: running_matches){
                if (am.getArena().getArenaType() == arenaType)
                    am.cancelMatch();
            }
        }

        amq.stop();
        amq.removeAllArenas(arenaType);
        Iterator<Arena> iter = allarenas.values().iterator();
        while (iter.hasNext()){
            Arena a = iter.next();
            if (a != null && a.getArenaType() == arenaType){
                iter.remove();}
        }
        nextArena.remove(arenaType);
        amq.resume();
    }

    public void cancelAllArenas() {
        amq.stop();
        amq.clearTeamQueues();
        synchronized(running_matches){
            for (Match am: running_matches){
                am.cancelMatch();
            }
        }
        GameManager.cancelAll();
        amq.resume();
    }


    public Collection<ArenaTeam> purgeQueue() {
        Collection<ArenaTeam> teams = amq.purgeQueue();
        amq.purgeQueue();
        return teams;
    }

    public boolean hasRunningMatches() {
        return !running_matches.isEmpty();
    }
    public QueueObject getQueueObject(ArenaPlayer player) {
        return amq.getQueueObject(player);
    }
    public List<String> getInvalidQueueReasons(QueueObject qo) {
        return amq.invalidReason(qo);
    }

    public boolean forceStart(MatchParams mp, boolean respectMinimumPlayers) {
        return amq.forceStart(mp, respectMinimumPlayers);
    }

    public Collection<ArenaPlayer> getPlayersInAllQueues(){
        return amq.getPlayersInAllQueues();
    }
    public Collection<ArenaPlayer> getPlayersInQueue(MatchParams params){
        return amq.getPlayersInQueue(params);
    }

    public String queuesToString() {
        return amq.queuesToString();
    }

    public boolean isQueueEmpty() {
        Collection<ArenaPlayer> col = getPlayersInAllQueues();
        return col == null || col.isEmpty();
    }

    public ArenaMatchQueue getArenaMatchQueue() {
        return amq;
    }


    public List<Match> getRunningMatches(MatchParams params){
        List<Match> list = new ArrayList<Match>();
        synchronized(running_matches){
            for (Match m: running_matches){
                if (m.getParams().getType() == params.getType()){
                    list.add(m);
                }
            }
            return list;
        }

    }

    public Match getSingleMatch(MatchParams params) {
        Match match = null;
        synchronized(running_matches){
            for (Match m: running_matches){
                if (m.getParams().getType() == params.getType()){
                    if (match != null)
                        return null;
                    match = m;
                }
            }
        }
        return match;
    }

    public void openAll(MatchParams mp) {
        for (Arena arena : getArenas(mp)) {
            arena.setAllContainerState(ContainerState.OPEN);
        }
    }

}
