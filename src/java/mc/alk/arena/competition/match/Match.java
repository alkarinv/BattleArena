package mc.alk.arena.competition.match;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.ArenaAlterController.ChangeType;
import mc.alk.arena.controllers.ArenaController;
import mc.alk.arena.controllers.ListenerAdder;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.RewardController;
import mc.alk.arena.controllers.Scheduler;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.controllers.containers.GameManager;
import mc.alk.arena.controllers.joining.AbstractJoinHandler;
import mc.alk.arena.controllers.messaging.MatchMessageHandler;
import mc.alk.arena.controllers.messaging.MatchMessager;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.controllers.plugins.WorldGuardController;
import mc.alk.arena.events.EventManager;
import mc.alk.arena.events.matches.MatchCancelledEvent;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.matches.MatchOpenEvent;
import mc.alk.arena.events.matches.MatchPrestartEvent;
import mc.alk.arena.events.matches.MatchResultEvent;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.matches.MatchTimerIntervalEvent;
import mc.alk.arena.events.players.ArenaPlayerDeathEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerReadyEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.events.prizes.ArenaDrawersPrizeEvent;
import mc.alk.arena.events.prizes.ArenaLosersPrizeEvent;
import mc.alk.arena.events.prizes.ArenaPrizeEvent;
import mc.alk.arena.events.prizes.ArenaWinnersPrizeEvent;
import mc.alk.arena.events.teams.TeamDeathEvent;
import mc.alk.arena.objects.ArenaLocation;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.CompetitionSize;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.ContainerState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaControllerInterface;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.messaging.ServerChannel;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.scoreboard.ScoreboardFactory;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.NLives;
import mc.alk.arena.objects.victoryconditions.NoTeamsLeft;
import mc.alk.arena.objects.victoryconditions.OneTeamLeft;
import mc.alk.arena.objects.victoryconditions.TimeLimit;
import mc.alk.arena.objects.victoryconditions.VictoryCondition;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumLivesPerPlayer;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumTeams;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesTimeLimit;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/// TODO once I have GameLogic, split this into two matches, one for always open, one for normal
public abstract class Match extends Competition implements Runnable, ArenaController {
    public enum PlayerState{OUTOFMATCH,INMATCH}

    final MatchParams params; /// Our parameters for this match
    final Arena arena; /// The arena we are using
    final ArenaControllerInterface arenaInterface; /// Our interface to access arena methods w/o reflection

    MatchState state = MatchState.NONE;/// State of the match

    /// When did each transition occur
    final Map<MatchState, Long> times = Collections.synchronizedMap(new EnumMap<MatchState,Long>(MatchState.class));
    final List<VictoryCondition> vcs = new ArrayList<VictoryCondition>(); /// Under what conditions does a victory occur
    MatchResult matchResult; /// Results for this match
    Map<String, Location> oldlocs = null; /// Locations where the players came from before entering arena
    final Set<String> inMatch = new HashSet<String>(); /// who is still inside arena area

    final MatchTransitions tops; /// Our match options for this arena match
    final PlayerStoreController psc = new PlayerStoreController(); /// Store items and exp for players if specified

    Set<MatchState> waitRoomStates = null; /// which states are inside a waitRoom
    final CompetitionState tinState; /// which matchstat teleports players in (first one is chosen)
    Long joinCutoffTime = null; /// at what point do we cut people off from joining
    Integer currentTimer = null; /// Our current timer (for switching between states)

    Countdown startCountdown = null; /// Start Countdown

    private final Collection<ArenaPlayer> matchPlayers = new HashSet<ArenaPlayer>();

    final Set<ArenaTeam> nonEndingTeams =
            Collections.synchronizedSet(new HashSet<ArenaTeam>());
    final Map<ArenaTeam,Integer> individualTeamTimers =
            Collections.synchronizedMap(new HashMap<ArenaTeam,Integer>());
    AtomicBoolean addedVictoryConditions = new AtomicBoolean(false);
    final GameManager gameManager;
    double prizePoolMoney = 0;

    /// These get used enough or are useful enough that i'm making variables even though they can be found in match options
    final boolean needsClearInventory, clearsInventory, clearsInventoryOnDeath;
    final boolean keepsInventory;
    final boolean respawns;
    final boolean spawnsRandom;
    final boolean woolTeams, armorTeams;
    final boolean alwaysTeamNames;
    final boolean respawnsWithClass;
    final boolean cancelExpLoss;
    final boolean individualWins;
    final boolean alwaysOpen;

    final Plugin plugin; /// Convenience variable for scheduling and countdowns

    int neededTeams; /// How many teams do we need to properly start this match
    int nLivesPerPlayer = 1; /// This will change as victory conditions are added
    ArenaScoreboard scoreboard;
    MatchMessager mc; /// Our message instance
    AbstractJoinHandler joinHandler;
    ArenaObjective defaultObjective;
    ArenaPreviousState oldArenaState;

    @SuppressWarnings("unchecked")
    public Match(Arena arena, MatchParams matchParams, Collection<ArenaListener> listeners) {
        if (Defaults.DEBUG) System.out.println("ArenaMatch::" + params);
        params = ParamController.copyParams(matchParams);
        params.setName(this.getName());
        params.flatten();
        this.tops = params.getTransitionOptions();
        /// Assign variables
        this.plugin = BattleArena.getSelf();
        this.gameManager = GameManager.getGameManager(params);
        this.arena = arena;
        this.arenaInterface =new ArenaControllerInterface(arena);
        addArenaListener(arena);
        if (listeners != null)
            addArenaListeners(listeners);
        scoreboard = ScoreboardFactory.createScoreboard(this,params);

        this.mc = new MatchMessager(this);
        this.oldlocs = new HashMap<String,Location>();
        arena.setMatch(this);

        Collection<ArenaModule> modules = params.getModules();
        if (modules != null){
            for (ArenaModule am: modules){
                if (am.isEnabled())
                    addArenaListener(am);
            }
        }
        /// placed anywhere options
        boolean noEnter = tops.hasAnyOption(TransitionOption.WGNOENTER);
        if (arena.hasRegion())
            WorldGuardController.setFlag(arena.getWorldGuardRegion(), "entry", !noEnter);
        boolean noLeave = tops.hasAnyOption(TransitionOption.WGNOLEAVE);

        this.woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS) && params.getMaxTeamSize() >1 ||
                tops.hasAnyOption(TransitionOption.ALWAYSWOOLTEAMS);
        this.armorTeams = tops.hasAnyOption(TransitionOption.ARMORTEAMS);

        tinState = tops.getMatchState(TransitionOption.TELEPORTIN);
        this.spawnsRandom = tinState != null && tops.hasOptionAt(tinState, TransitionOption.RANDOMSPAWN);
        this.alwaysTeamNames = tops.hasAnyOption(TransitionOption.ALWAYSTEAMNAMES);
        this.cancelExpLoss = tops.hasAnyOption(TransitionOption.NOEXPERIENCELOSS);
        this.matchResult = new MatchResult();
        /// default Options
        this.individualWins = tops.hasOptionAt(MatchState.DEFAULTS, TransitionOption.INDIVIDUALWINS);
        /// preReq Options
        this.needsClearInventory = tops.hasOptionAt(MatchState.PREREQS, TransitionOption.CLEARINVENTORY);
        /// onComplete Options
        this.clearsInventory = tops.hasOptionAt(MatchState.ONCOMPLETE, TransitionOption.CLEARINVENTORY);
        /// onDeath options
        this.keepsInventory = tops.hasOptionAt(MatchState.ONDEATH, TransitionOption.KEEPINVENTORY);
        this.clearsInventoryOnDeath = tops.hasOptionAt(MatchState.ONDEATH, TransitionOption.CLEARINVENTORY);
        this.respawns = tops.hasOptionAt(MatchState.ONDEATH, TransitionOption.RESPAWN) ||
                tops.hasOptionAt(MatchState.ONDEATH, TransitionOption.RANDOMSPAWN);
        /// onSpawn options
        this.respawnsWithClass = tops.hasOptionAt(MatchState.ONSPAWN, TransitionOption.RESPAWNWITHCLASS);
        this.alwaysOpen = params.isAlwaysOpen();
        this.neededTeams = alwaysOpen ? 0 : params.getMinTeams();

        /// Set waitroom variables
        if (tops.hasAnyOption(TransitionOption.TELEPORTWAITROOM, TransitionOption.TELEPORTLOBBY,
                TransitionOption.TELEPORTMAINWAITROOM, TransitionOption.TELEPORTMAINLOBBY,
                TransitionOption.TELEPORTCOURTYARD)){
            waitRoomStates = new HashSet<MatchState>(tops.getMatchStateRange(TransitionOption.TELEPORTWAITROOM, TransitionOption.TELEPORTIN));
            waitRoomStates.addAll(tops.getMatchStateRange(TransitionOption.TELEPORTWAITROOM, TransitionOption.TELEPORTIN));
            waitRoomStates.addAll(tops.getMatchStateRange(TransitionOption.TELEPORTMAINWAITROOM, TransitionOption.TELEPORTIN));
            waitRoomStates.addAll(tops.getMatchStateRange(TransitionOption.TELEPORTLOBBY, TransitionOption.TELEPORTIN));
            waitRoomStates.addAll(tops.getMatchStateRange(TransitionOption.TELEPORTMAINLOBBY, TransitionOption.TELEPORTIN));
            waitRoomStates.addAll(tops.getMatchStateRange(TransitionOption.TELEPORTCOURTYARD, TransitionOption.TELEPORTIN));
            if (waitRoomStates.isEmpty()){
                waitRoomStates = null;}
        }

        /// Register the events we are listening to
        ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>();
        events.addAll(Arrays.asList(ArenaPlayerLeaveEvent.class, PlayerRespawnEvent.class,
                PlayerCommandPreprocessEvent.class, PlayerDeathEvent.class,
                PlayerInteractEvent.class, ArenaPlayerDeathEvent.class, ArenaPlayerReadyEvent.class));
        if (noLeave){
            events.add(PlayerMoveEvent.class);}
        ListenerAdder.addListeners(this, tops);
        methodController.addSpecificEvents(this, events);
        EventManager.registerEvents(this, BattleArena.getSelf());
        /// add a default objective
        defaultObjective = scoreboard.createObjective("default",
                "Player Kills", "&6Still Alive", SAPIDisplaySlot.SIDEBAR, 100);
        if (params.getMaxTeamSize() <= 2){
            defaultObjective.setDisplayTeams(false);}


        /// Save the arena state if we are changing it
        if (hasWaitroom() && params.isWaitroomClosedWhenRunning()){
            oldArenaState = new ArenaPreviousState(arena);
            arena.setContainerState(ChangeType.WAITROOM,
                    new ContainerState(ContainerState.AreaContainerState.CLOSED,
                            "&cA match is already in progress in arena " + arena.getName()));
        }
        transitionTo(MatchState.ONCREATE);
        updateBukkitEvents(MatchState.ONCREATE);
    }


    private void updateBukkitEvents(MatchState matchState){
        final ArrayList<String> players = new ArrayList<String>(inMatch);
        methodController.updateEvents(null,matchState, players);
    }

    private void updateBukkitEvents(MatchState matchState,ArenaPlayer player){
        methodController.updateEvents(matchState, player);
    }

    private void updateBukkitEvents(MatchState matchState, ArenaListener listener){
        final ArrayList<String> players = new ArrayList<String>(inMatch);
        methodController.updateEvents(listener, matchState, players);
    }

    /**
     * As this gets calls Arena's and events which can call bukkit events
     * this should be done in a synchronous fashion
     */
    public void open(){
        transitionTo(MatchState.ONOPEN);

        MatchOpenEvent event = new MatchOpenEvent(this);

        callEvent(event);
        if (event.isCancelled()){
            cancelMatch();
            return;
        }
        updateBukkitEvents(MatchState.ONOPEN);
        arenaInterface.onOpen();
        onJoin(teams);
    }

    public void run() {
        preStartMatch();
    }

    public boolean isTimedStart() {
        return startCountdown != null;
    }

    public void setTimedStart(int seconds, Integer interval){
        if (startCountdown != null){
            this.startCountdown.stop();
            startCountdown = null;
        }
        if (seconds > 0){
            mc.sendCountdownTillPrestart(seconds);
            this.startCountdown = new Countdown(BattleArena.getSelf(), seconds, interval, new CountdownCallback(){
                @Override
                public boolean intervalTick(int remaining) {
                    if (state != MatchState.ONOPEN)
                        return false;
                    if (remaining == 0){
                        if (checkEnoughTeams(getTeams(), neededTeams))
                            preStartMatch();
                    } else {
                        mc.sendCountdownTillPrestart(remaining);
                    }
                    return true;
                }
            });
        } else {
            preStartMatch();
        }
    }

    public void hookTeamJoinHandler(AbstractJoinHandler teamJoinHandler){
        teamJoinHandler.setCompetition(this);
        this.joinHandler = teamJoinHandler;
        this.teams = this.joinHandler.getTeams();
        for (int i=0;i<teams.size();i++){
            this.teams.get(i).setIndex(i);
            for (ArenaPlayer ap : this.teams.get(i).getPlayers()) {
                joiningOngoing(this.teams.get(i),ap);
            }


        }
        matchPlayers.addAll(joinHandler.getPlayers());
    }
    public List<ArenaTeam> getNonEmptyTeams() {
        List<ArenaTeam> teams = new ArrayList<ArenaTeam>();
        for (ArenaTeam at: this.teams) {
            if (at != null && at.size() > 0) {
                teams.add(at);}
        }
        return teams;
    }
    private void preStartTeams(List<ArenaTeam> teams, boolean matchPrestarting){
        TransitionOptions ts = tops.getOptions(MatchState.ONPRESTART);
        /// If we will teleport them into the arena for the first time, check to see they are ready first
        if (ts != null && ts.teleportsIn()){
            for (ArenaTeam t: teams){
                checkReady(t,tops.getOptions(MatchState.PREREQS));	}
        }
        PerformTransition.transition(this, MatchState.ONPRESTART, teams, true);
        /// Send messages to teams and server, or just to the teams
        if (matchPrestarting) {
            mc.sendOnPreStartMsg(getNonEmptyTeams());
        } else {
            mc.sendOnPreStartMsg(getNonEmptyTeams(), ServerChannel.NullChannel);
        }
    }

    private void preStartMatch() {
        if (state == MatchState.ONCANCEL || state.ordinal() >= MatchState.ONPRESTART.ordinal())
            return; /// If the match was cancelled, or we are already prestarted dont proceed
        if (Defaults.DEBUG) System.out.println("ArenaMatch::startMatch()");
        transitionTo(MatchState.ONPRESTART);

        updateBukkitEvents(MatchState.ONPRESTART);
        callEvent(new MatchPrestartEvent(this,teams));

        preStartTeams(teams,true);
        arenaInterface.onPrestart();
        new Countdown(plugin, params.getSecondsTillMatch(), 1,
                new CountdownCallback(){
                    @Override
                    public boolean intervalTick(int remaining) {
                        SObjective obj = scoreboard.getObjective(SAPIDisplaySlot.SIDEBAR);
                        if (obj != null){
                            if (remaining == 0 && params.getMatchTime()== CompetitionSize.MAX){
                                obj.setDisplayNameSuffix("");
                            } else {
                                obj.setDisplayNameSuffix(" &e("+remaining+")");
                            }
                        }
                        return (currentTimer!=null);
                    }
                });
        /// Schedule the start of the match

        currentTimer = Scheduler.scheduleSynchronousTask(plugin, new Runnable() {
            public void run() {
                startMatch();
            }
        }, (int) (params.getSecondsTillMatch() * 20L * Defaults.TICK_MULT));

        if (waitRoomStates != null){
            joinCutoffTime = System.currentTimeMillis() + (params.getSecondsTillMatch()- Defaults.JOIN_CUTOFF_TIME)*1000;}
    }

    /**
     * If the match is already in the preStart phase, just start it
     */
    public void start() {
        if (state != MatchState.ONPRESTART)
            return;
        if (currentTimer != null){
            Bukkit.getScheduler().cancelTask(currentTimer);}
        currentTimer = Scheduler.scheduleSynchronousTask(plugin, new Runnable() {
            public void run() {
                startMatch();
            }
        }, (int) (10 * 20L * Defaults.TICK_MULT));
    }

    private void startMatch(){
        if (state == MatchState.ONCANCEL) return; /// If the match was cancelled, dont proceed
        transitionTo(MatchState.ONSTART);
        if (!addedVictoryConditions.get()){
            addVictoryConditions();
        }
        List<ArenaTeam> competingTeams = new ArrayList<ArenaTeam>();
        /// If we will teleport them into the arena for the first time, check to see they are ready first
        TransitionOptions ts = tops.getOptions(state);
        if (ts != null && ts.teleportsIn()){
            for (ArenaTeam t: teams){
                checkReady(t,tops.getOptions(MatchState.PREREQS));}
        }
        for (ArenaTeam t: teams){
            if (!t.isDead()){
                competingTeams.add(t);}
        }
        int nCompetingTeams = competingTeams.size();

        if (Defaults.DEBUG) Log.info("[BattleArena] competing teams = " + competingTeams +":"+neededTeams+"   allteams=" + teams);

        if (nCompetingTeams >= neededTeams){
            MatchStartEvent event= new MatchStartEvent(this,teams);
            updateBukkitEvents(MatchState.ONSTART);
            callEvent(event);
            PerformTransition.transition(this, state, competingTeams, true);
            arenaInterface.onStart();
            try{mc.sendOnStartMsg(getNonEmptyTeams());}catch(Exception e){Log.printStackTrace(e);}
        }
        checkEnoughTeams(competingTeams, neededTeams);
    }

    private boolean checkEnoughTeams(List<ArenaTeam> competingTeams, int neededTeams) {
        final int nCompetingTeams = competingTeams.size();
        if (nCompetingTeams >= neededTeams){
            return true;
        } else if (nCompetingTeams < neededTeams && nCompetingTeams==1){
            ArenaTeam victor = competingTeams.get(0);
            victor.sendMessage("&4WIN!!!&eThe other team was offline or didnt meet the entry requirements.");
            setVictor(victor);
            return false;
        } else { /// Seriously, no one showed up?? Well, one of them won regardless, but scold them
            if (competingTeams.isEmpty()){
                this.cancelMatch();
            } else {
                setDraw();
            }
            return false;
        }
    }

    private synchronized void matchWinLossOrDraw(MatchResult result) {
        if (alwaysOpen){
            this.nonEndingMatchWinLossOrDraw(result);
        } else {
            this.endingMatchWinLossOrDraw(result);
        }
    }

    private synchronized void nonEndingMatchWinLossOrDraw(MatchResult result){
        List<ArenaTeam> remove = new ArrayList<ArenaTeam>();
        for (ArenaTeam t: result.getLosers())
            if (!nonEndingTeams.add(t) || t.size()==0)
                remove.add(t); /// they are already being handled
        result.removeLosers(remove);
        remove.clear();
        for (ArenaTeam t: result.getDrawers())
            if (!nonEndingTeams.add(t)|| t.size()==0)
                remove.add(t); /// they are already being handled
        result.removeDrawers(remove);
        remove.clear();
        for (ArenaTeam t: result.getVictors())
            if (!nonEndingTeams.add(t)|| t.size()==0)
                remove.add(t); /// they are already being handled
        result.removeVictors(remove);

        if (result.getLosers().isEmpty() && result.getDrawers().isEmpty() && result.getVictors().isEmpty())
            return;

        MatchResultEvent event = new MatchResultEvent(this,result);
        callEvent(event);
        if (event.isCancelled()){
            return;
        }
        int timerid = Scheduler.scheduleSynchronousTask(plugin,
                new NonEndingMatchVictory(this,result),(int)2L);
        for (ArenaTeam t: teams)
            individualTeamTimers.put(t, timerid);
    }

    private synchronized void endingMatchWinLossOrDraw(MatchResult result) {
        /// this might be called multiple times as multiple players might meet the victory condition within a small
        /// window of time.  But only let the first one through
        if (state == MatchState.ONVICTORY || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL)
            return;
        this.matchResult = result;
        MatchResultEvent event = new MatchResultEvent(this,result);
        callEvent(event);
        if (event.isCancelled()){
            return;
        }
        transitionTo(MatchState.ONVICTORY);
        arenaInterface.onVictory(result);
        /// Call the rest after a 2 tick wait to ensure the calling transitionMethods complete before the
        /// victory conditions start rolling in
        currentTimer = Scheduler.scheduleSynchronousTask(plugin, new MatchVictory(this),(int)2L);
    }

    class NonEndingMatchVictory implements Runnable{
        final Match am;
        final MatchResult result;
        NonEndingMatchVictory(Match am, MatchResult result){this.am = am; this.result = result;}

        public void run() {
            List<ArenaTeam> teams = new ArrayList<ArenaTeam>();
            final Set<ArenaTeam> victors = result.getVictors();
            final Set<ArenaTeam> losers = result.getLosers();
            final Set<ArenaTeam> drawers = result.getDrawers();
            teams.addAll(victors);
            teams.addAll(losers);
            teams.addAll(drawers);
            if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am +"  victors="+ victors + "  losers=" + losers+"  drawers="+drawers +" " + matchResult);
            if (params.isRated()){
                StatController sc = new StatController(params);
                sc.addRecord(victors,losers,drawers,result.getResult(), params.isTeamRating());
            }

            if (result.hasVictor()){ /// We have a true winner
                try{mc.sendOnVictoryMsg(victors, losers);}catch(Exception e){Log.printStackTrace(e);}
            } else { /// we have a draw
                try{mc.sendOnDrawMessage(drawers,losers);} catch(Exception e){Log.printStackTrace(e);}
            }

            PerformTransition.transition(am, MatchState.ONVICTORY, teams, true);
            int timerid = Scheduler.scheduleSynchronousTask(plugin,
                    new NonEndingMatchCompleted(am, result, teams),
                    (int) (params.getSecondsToLoot() * 20L * Defaults.TICK_MULT));
            for (ArenaTeam t: teams)
                individualTeamTimers.put(t, timerid);
        }
    }

    class MatchVictory implements Runnable{
        final Match am;
        MatchVictory(Match am){this.am = am; }

        public void run() {
            final Set<ArenaTeam> victors = matchResult.getVictors();
            final Set<ArenaTeam> losers = matchResult.getLosers();
            final Set<ArenaTeam> drawers = matchResult.getDrawers();
            if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am.getID() +"  victors="+ victors + "  losers=" + losers+"  drawers="+drawers +" " + matchResult);

            if (params.isRated()){
                StatController sc = new StatController(params);
                sc.addRecord(victors,losers,drawers,am.getResult().getResult(), params.isTeamRating());
            }
            if (matchResult.hasVictor()){ /// We have a true winner
                try{mc.sendOnVictoryMsg(victors, losers);}catch(Exception e){Log.printStackTrace(e);}
            } else { /// we have a draw
                try{mc.sendOnDrawMessage(drawers,losers);} catch(Exception e){Log.printStackTrace(e);}
            }

            updateBukkitEvents(MatchState.ONVICTORY);
            PerformTransition.transition(am, MatchState.ONVICTORY, teams, true);
            currentTimer = Scheduler.scheduleSynchronousTask(plugin,
                    new MatchCompleted(am), (int) (params.getSecondsToLoot() * 20L * Defaults.TICK_MULT));
        }
    }
    class NonEndingMatchCompleted implements Runnable{
        final Match am;
        final MatchResult result;
        final List<ArenaTeam> teams;

        NonEndingMatchCompleted(Match am, MatchResult result, List<ArenaTeam> teams){
            this.am = am;
            this.result = result;
            this.teams = teams;
        }

        public void run() {
            final Collection<ArenaTeam> victors = result.getVictors();
            if (Defaults.DEBUG) System.out.println("Match::NonEndingMatchCompleted(): " + victors);
            /// ONCOMPLETE can teleport people out of the arena,
            /// So the order of events is usually
            /// ONCOMPLETE(half of effects) -> ONLEAVE( and all effects) -> ONCOMPLETE( rest of effects)
            PerformTransition.transition(am, MatchState.ONCOMPLETE, teams, true);
            /// Once again, lets delay this final bit so that transitions have time to finish before
            /// Other splisteners get a chance to handle
            int timerid = Scheduler.scheduleSynchronousTask(plugin, new Runnable(){
                public void run() {
                    /// Losers and winners get handled after the match is complete
                    if (result.getLosers() != null){
                        PerformTransition.transition(am, MatchState.LOSERS, result.getLosers(), false);
                        ArenaPrizeEvent event = new ArenaLosersPrizeEvent(am, result.getLosers());
                        callEvent(event);
                        new RewardController(event,psc).giveRewards();
                    }
                    if (result.getDrawers() != null){
                        PerformTransition.transition(am, MatchState.DRAWERS, result.getDrawers(), false);
                        ArenaPrizeEvent event = new ArenaDrawersPrizeEvent(am, result.getDrawers());
                        callEvent(event);
                        new RewardController(event,psc).giveRewards();
                    }
                    if (result.getVictors() != null){
                        PerformTransition.transition(am, MatchState.WINNERS, result.getVictors(), false);
                        ArenaPrizeEvent event = new ArenaWinnersPrizeEvent(am, result.getVictors());
                        callEvent(event);
                        new RewardController(event,psc).giveRewards();
                    }
                    nonEndingDeconstruct(teams);
                }
            });
            for (ArenaTeam t: teams)
                individualTeamTimers.put(t, timerid);
        }
    }

    class MatchCompleted implements Runnable{
        final Match am;

        MatchCompleted(Match am){this.am = am;}
        public void run() {
            transitionTo(MatchState.ONCOMPLETE);
            if (Defaults.DEBUG) System.out.println("Match::MatchCompleted(): " + am.getResult());
            /// ONCOMPLETE can teleport people out of the arena,
            /// So the order of events is usually
            /// ONCOMPLETE(half of effects) -> ONLEAVE( and all effects) -> ONCOMPLETE( rest of effects)
            PerformTransition.transition(am, MatchState.ONCOMPLETE, teams, true);
            /// Once again, lets delay this final bit so that transitions have time to finish before
            /// Other splisteners get a chance to handle
            currentTimer = Scheduler.scheduleSynchronousTask(plugin, new Runnable(){
                public void run() {
                    callEvent(new MatchCompletedEvent(am));
                    arenaInterface.onComplete();
                    /// Losers and winners get handled after the match is complete

                    if (am.getLosers() != null){
                        PerformTransition.transition(am, MatchState.LOSERS, am.getLosers(), false);
                        ArenaPrizeEvent event = new ArenaLosersPrizeEvent(am, am.getLosers());
                        callEvent(event);
                        new RewardController(event,psc).giveRewards();
                    }

                    if (am.getDrawers() != null){
                        PerformTransition.transition(am, MatchState.DRAWERS, am.getDrawers(), false);
                        ArenaPrizeEvent event = new ArenaDrawersPrizeEvent(am, am.getDrawers());
                        callEvent(event);
                        new RewardController(event,psc).giveRewards();
                    }

                    if (am.getVictors() != null){
                        PerformTransition.transition(am, MatchState.WINNERS, am.getVictors(), false);
                        ArenaPrizeEvent event = new ArenaWinnersPrizeEvent(am, am.getVictors());
                        callEvent(event);
                        new RewardController(event,psc).giveRewards();
                    }
                    updateBukkitEvents(MatchState.ONCOMPLETE);
                    deconstruct();
                }
            });
        }
    }

    public synchronized void cancelMatch(){
        if (state == MatchState.ONCANCEL)
            return;
        state = MatchState.ONCANCEL;
        arenaInterface.onCancel();
        for (ArenaTeam t : teams){
            if (t == null)
                continue;
            PerformTransition.transition(this, MatchState.ONCANCEL, t, true);
        }
        /// For players that were in the process of joining when cancel happened
        for (Entry<ArenaTeam,Integer> entry : individualTeamTimers.entrySet()){
            Scheduler.cancelTask(entry.getValue());
            if (!teams.contains(entry.getKey()))
                PerformTransition.transition(this, MatchState.ONCANCEL, entry.getKey(), true);
        }
        callEvent(new MatchCancelledEvent(this));
        updateBukkitEvents(MatchState.ONCANCEL);
        deconstruct();
    }

    private void nonEndingDeconstruct(List<ArenaTeam> teams){
        for (ArenaTeam t: teams){
            PerformTransition.transition(this, MatchState.ONFINISH, t, true);
            for (ArenaPlayer p: t.getPlayers()){
                p.removeCompetition(this);
                if (joinHandler != null)
                    joinHandler.leave(p);
            }
            individualTeamTimers.remove(t);
            scoreboard.removeTeam(t);
        }
        nonEndingTeams.removeAll(teams);
        this.teams.removeAll(teams);
    }

    private void deconstruct(){
        /// Teleport out happens 1 tick after oncancel/oncomplete, we also must wait 1 tick
        final Match match = this;
        callEvent(new MatchFinishedEvent(match));
        updateBukkitEvents(MatchState.ONFINISH);
        for (ArenaTeam t: teams){
            PerformTransition.transition(this, MatchState.ONFINISH, t, true);
            for (ArenaPlayer p: t.getPlayers()){
                p.removeCompetition(this);
            }
            scoreboard.removeTeam(t);
        }
        /// For players that were in the process of joining when deconstruct happened
        for (Entry<ArenaTeam,Integer> entry : individualTeamTimers.entrySet()){
            Scheduler.cancelTask(entry.getValue());
            if (!teams.contains(entry.getKey())){
                PerformTransition.transition(this, MatchState.ONCANCEL, entry.getKey(), true);
                for (ArenaPlayer p: entry.getKey().getPlayers()){
                    p.removeCompetition(this);
                }
            }
        }
        if (oldArenaState != null){
            oldArenaState.revert(arena);}
        scoreboard.clear();
        arenaInterface.onFinish();
        inMatch.clear();
        teams.clear();
        methodController.deconstruct();
        HandlerList.unregisterAll(this);
    }

    private boolean _addedTeam(ArenaTeam team){
        if (this.isFinished())
            return false;
        if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" addedTeam("+team.getName()+":"+team.getId()+")");

        TeamUtil.initTeam(team, params);
        team.setArenaObjective(defaultObjective);
        scoreboard.addTeam(team);
        for (ArenaPlayer p: team.getPlayers()){
            if (p == null)
                continue;
            _addedToTeam(team, p);
            if (!this.isHandled(p))
                joiningOngoing(team, p);
        }
        return true;
    }


    @Override
    public boolean addedTeam(ArenaTeam team) {
        return _addedTeam(team);
    }

    /** Called during both, addedTeam and addedToTeam */
    private void _addedToTeam(ArenaTeam team, ArenaPlayer player){
        leftPlayers.remove(player.getName()); /// remove players from the list as they are now joining again
        matchPlayers.add(player);
        if (params.getTransitionOptions().hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTIN)
                && (state == MatchState.NONE || state == MatchState.ONCREATE))
            inMatch.add(player.getName());
        else
            inMatch.remove(player.getName()); /// when they "enter" which should happen after this, inMatch will get them
        team.setAlive(player);
        player.addCompetition(this);
        if (params.hasEntranceFee()){
            prizePoolMoney += params.getEntranceFee();}
        arenaInterface.onJoin(player,team);
        if (state == MatchState.ONOPEN && joinHandler != null && joinHandler.isFull()){
            Scheduler.scheduleSynchronousTask(BattleArena.getSelf(), this);
        }
        defaultObjective.setPoints(player, 0);
        scoreboard.addedToTeam(team, player);
        if (nLivesPerPlayer != 1 && nLivesPerPlayer != ArenaSize.MAX) {
            player.getMetaData().setLivesLeft(nLivesPerPlayer);
            SEntry e = scoreboard.getEntry(player.getPlayer());
            if (e!=null)
                scoreboard.setEntryNameSuffix(e, "(" + nLivesPerPlayer + ")");
        }
    }

    @Override
    public boolean removedTeam(ArenaTeam team){
        if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" removedTeam("+team.getName()+":"+team.getId()+")");

        if (teams.contains(team)){
            onLeave(team);
            scoreboard.removeTeam(team);
            return true;
        }
        return false;
    }

    /**
     * Add to an already existing team
     * @param team ArenaTeam
     * @param player player
     */
    @Override
    public void addedToTeam(final ArenaTeam team, final ArenaPlayer player) {
        if (isEnding())
            return;
        if (Defaults.DEBUG_MATCH_TEAMS)
            Log.info(getID()+" addedToTeam("+team.getName()+":"+team.getId()+", " + player.getName()+") inside="+inMatch.contains(player.getName()));

        if (!team.hasSetName() && team.getDisplayName().length() > Defaults.MAX_TEAM_NAME_APPEND){
            team.setDisplayName(TeamUtil.getTeamName(team.getIndex()));}
        _addedToTeam(team, player);

        mc.sendAddedToTeam(team,player);
        if (!this.isHandled(player))
            joiningOngoing(team, player);
    }

    private static void doTransition(Match match, MatchState state, ArenaPlayer player, ArenaTeam team, boolean onlyInMatch){
        if (player != null){
            PerformTransition.transition(match, state, player, team, onlyInMatch);
        } else {
            PerformTransition.transition(match, state, team, onlyInMatch);
        }
    }

    private void joiningOngoing(final ArenaTeam team, final ArenaPlayer player) {
        final Match match = this;

        if (!gameManager.hasPlayer(player)){
            /// onJoin Them
            doTransition(match, MatchState.ONJOIN, player,team, true);
        } else if ((state== MatchState.NONE || state==MatchState.ONCREATE)  &&
                params.hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTIN)){
            /// This is a bit strange.. but in instances where people have teleported in
            /// before the match was created, we need to add them in as if they were teleporting
            /// These are the 2 methods called from the teleport in events
            //			player.setCurLocation(this.getLocationType());
            //			ArenaLocation l = player.getCurLocation();
            ArenaLocation nl = new ArenaLocation(this,
                    player.getCurLocation().getLocation(), this.getLocationType());
            player.setCurLocation(nl);
            this.preFirstJoin(player);
            this.postFirstJoin(player);
        }
        /// onPreStart Them
        if (state.ordinal() >= MatchState.ONPRESTART.ordinal()){
            doTransition(match, MatchState.ONPRESTART, player,team, true);

            /// onStart Them
            if (state.ordinal() >= MatchState.ONSTART.ordinal()){
                int timerid = Scheduler.scheduleSynchronousTask(plugin, new Runnable() {
                    public void run() {
                        doTransition(match, MatchState.ONSTART, player,team, true);
                    }
                }, (int) (params.getSecondsTillMatch() * 20L * Defaults.TICK_MULT));
                for (ArenaTeam t: teams)
                    individualTeamTimers.put(t, timerid);
            }
        }
    }

    /**
     * Add to an already existing team
     * @param team team
     * @param players players
     */
    @Override
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> players) {
        for (ArenaPlayer ap: players)
            addedToTeam(team,ap);
    }

    @Override
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> players) {
        for (ArenaPlayer ap:players)
            privateRemovedFromTeam(team,ap);
    }

    @Override
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
        privateRemovedFromTeam(team, player);
    }

    protected void onJoin(Collection<ArenaTeam> teams){
        if (teams == null)
            return;
        for (ArenaTeam t: teams){
            _addedTeam(t);}
    }

    /**
     * TeamHandler override
     * Players can always leave, they just might be killed for doing so
     */
    @Override
    public boolean canLeave(ArenaPlayer p) {
        return !tops.hasOptionAt(state, TransitionOption.NOLEAVE);
    }

    /**
     * TeamHandler override
     * We already handle leaving in other methods.
     */
    @Override
    public boolean leave(ArenaPlayer p) {
        return onLeave(p);
    }

    /**
     *
     * @param team team
     */
    public void onLeave(ArenaTeam team) {
        for (ArenaPlayer ap: team.getPlayers()){
            privateQuitting(ap,team);}
        privateRemoveTeam(team);
    }

    public boolean onLeave(ArenaPlayer p) {
        if (Defaults.DEBUG_TRACE) Log.info(p.getName() + " -onLeave  t=" + p.getTeam());

        /// remove them from the match, they don't want to be here
        ArenaTeam t = getTeam(p);
        if (t==null) /// really? trying to make a player leave who isnt in the match
            return false;
        privateQuitting(p,t);
        /// Should this be a removePlayer or killMember... really depends on what we should do with them on the team
        //			t.removePlayer(p);
        t.killMember(p);
        Competition comp;
        while ((comp = p.getCompetition()) != null){
            p.removeCompetition(comp);
        }
        return true;
    }

    protected void checkAndHandleIfTeamDead(ArenaTeam team){
        if (team.isDead()){
            callEvent(new TeamDeathEvent(team));}
    }

    private void privateRemovedFromTeam(ArenaTeam team,ArenaPlayer ap){
        if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" removedFromTeam("+team.getName()+":"+team.getId()+")"+ap.getName());
        HeroesController.removedFromTeam(team, ap.getPlayer());
        if (state.ordinal() < MatchState.ONSTART.ordinal() && params.hasEntranceFee()){
            this.prizePoolMoney -= params.getEntranceFee();
        }
        scoreboard.removedFromTeam(team,ap);
    }

    private void privateRemoveTeam(ArenaTeam team){
        teams.remove(team);
        HeroesController.removeTeam(team);
    }

    private void preFirstJoin(ArenaPlayer player){
        ArenaTeam team = getTeam(player);

        final String name = player.getName();
        inMatch.add(player.getName());
        if (WorldGuardController.hasWorldGuard() && arena.hasRegion()){
            psc.addMember(player, arena.getWorldGuardRegion());}
        if (!params.getUseTrackerPvP()){
            StatController.stopTracking(player);
            StatController.stopTrackingMessages(player);
        }
        /// Store the point at which they entered the arena
        if (!oldlocs.containsKey(name) || oldlocs.get(name) == null) /// First teleportIn is the location we want
            oldlocs.put(name, player.getLocation());

        updateBukkitEvents(MatchState.ONENTER,player);
        PerformTransition.transition(this, MatchState.ONENTERARENA, player, team, false);
        arenaInterface.onEnter(player,team);
    }

    private void postFirstJoin(ArenaPlayer player){
        ArenaTeam team = getTeam(player);
        inMatch.add(player.getName());
        if (woolTeams && team !=null && team.getIndex()!=-1){
            TeamUtil.setTeamHead(team.getIndex(), player); // give wool heads
        }
        if (cancelExpLoss){
            psc.cancelExpLoss(player,true);}
    }

    @Override
    public void onPreJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        preFirstJoin(player);
    }

    @Override
    public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        postFirstJoin(player);
    }


    protected void privateQuitting(ArenaPlayer ap, ArenaTeam team){
        if (isHandled(ap)){ /// Only leave if they haven't already left.
            /// The onCancel should teleport them out, and call leaveArena(ap)
            PerformTransition.transition(this, MatchState.ONCANCEL, ap, team, false);
        }
    }

    @Override
    public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        ArenaTeam t = getTeam(player);
        if (t == null) /// we already handled the quit
            return;
        try {
            scoreboard.leaving(t,player);
        } catch (Exception e){
            Log.printStackTrace(e);
        }

    }

    /**
     * Only do this when players are not in the competition yet
     * @param event ArenaPlayerLeaveEvent
     */
    @EventHandler
    public void onArenaPlayerLeaveEventGlobal(ArenaPlayerLeaveEvent event){
        if (Defaults.DEBUG_TRACE) MessageUtil.sendMessage(event.getPlayer(), " -onArenaPlayerLeaveEventGlobal  t=" + event.getPlayer().getTeam());

        if (leftPlayers.contains(event.getPlayer().getName()) ||
                (matchPlayers.contains(event.getPlayer()) && event.getPlayer().getCurLocation().getType() == LocationType.ARENA)||
                !isHandled(event.getPlayer()))
            return;
        ArenaPlayer player = event.getPlayer();
        if (Defaults.DEBUG_TRACE) MessageUtil.sendMessage(player, " -onArenaPlayerLeaveEventGlobal  t="+player.getTeam());
        player.removeCompetition(this);
        player.reset(); /// reset the players
        ArenaTeam t = getTeam(player);
        if (t == null) {
            /// we already handled them
            return;
        }
        try {
            scoreboard.setDead(t,player);
            scoreboard.leaving(t,player);
        } catch (Exception e){
            /// something bad happened, but lets not crash b/c of it
            /// Possibly scoreboard set wrong? player removed after team?
            Log.printStackTrace(e);
        }
        t.playerLeft(player);
        leftPlayers.add(player.getName());
        if (this.getState().ordinal() < MatchState.ONVICTORY.ordinal())
            checkAndHandleIfTeamDead(t);
        inMatch.remove(player.getName());
        matchPlayers.remove(player);

        event.addMessage(MessageHandler.getSystemMessage("you_left_competition", this.params.getName()));
    }

    @ArenaEventHandler
    public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
        ArenaPlayer player = event.getPlayer();
        if (Defaults.DEBUG_TRACE) MessageUtil.sendMessage(player, " -onArenaPlayerLeaveEvent  t="+player.getTeam());
        if (!isHandled(player))
            return;
        quitting(event, player);
    }

    private void quitting(ArenaPlayerLeaveEvent event, ArenaPlayer player) {
        if (Defaults.DEBUG_TRACE) Log.info(player.getName() + " -quitting  t=" + player.getTeam());
        event.addMessage(MessageHandler.getSystemMessage("you_left_competition", this.params.getName()));
        if (params.hasOptionAt(MatchState.DEFAULTS, TransitionOption.DROPITEMS)) {
            InventoryUtil.dropItems(player.getPlayer());
            InventoryUtil.clearInventory(player.getPlayer());
        }
        ArenaTeam t = getTeam(player);
        PerformTransition.transition(this, MatchState.ONCANCEL, player, t, false);
    }

    @Override
    public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        if (Defaults.DEBUG_TRACE) Log.info(player.getName() + " -onPostQuit  t=" + player.getTeam());
        ArenaTeam t = getTeam(player);
        PerformTransition.transition(this, MatchState.ONLEAVEARENA, player, t, false);
        updateBukkitEvents(MatchState.ONLEAVE,player);
        if (WorldGuardController.hasWorldGuard() && arena.hasRegion())
            psc.removeMember(player, arena.getWorldGuardRegion());
        inMatch.remove(player.getName());
        player.removeCompetition(this);
        player.reset(); /// reset the players
        if (!params.getUseTrackerPvP()){
            StatController.resumeTracking(player);
            StatController.resumeTrackingMessages(player);
        }
        if (t != null){
            if (this.woolTeams)
                TeamUtil.removeTeamHead(t.getIndex(), player.getPlayer());
            t.killMember(player);
            if (this.getState().ordinal() < MatchState.ONVICTORY.ordinal())
                checkAndHandleIfTeamDead(t);
            scoreboard.setDead(t, player);

        }

        arenaInterface.onLeave(player,t);
        if (alwaysOpen){ /// free the team up for more players
            joinHandler.leave(player);
            matchPlayers.remove(player);
        }
        if (cancelExpLoss){
            psc.cancelExpLoss(player,false);}
    }

    @Override
    public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        if (!inMatch.contains(player.getName())){
            this.preFirstJoin(player);
            player.getMetaData().setJoining(true);
        }
    }

    @Override
    public void onPostEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        if (player.getMetaData().isJoining()){
            this.postFirstJoin(player);
            player.getMetaData().setJoining(false);
        }
        inMatch.add(player.getName());
    }

    @Override
    public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        //		Log.debug(" onPreLeave !!!!!!! " + player.getName() +"   " + apte.getArenaType());
        if (Defaults.DEBUG_TRACE) Log.info(player.getName() + " -onPreLeave  t=" + player.getTeam());
        inMatch.remove(player.getName());
    }

    @Override
    public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        if (Defaults.DEBUG_TRACE) Log.info(player.getName() + " -onPostLeave  t=" + player.getTeam());

    }

    public void setMessageHandler(MatchMessageHandler mc){this.mc.setMessageHandler(mc);}
    public MatchMessageHandler getMessageHandler(){return mc.getMessageHandler();}

    private synchronized void addVictoryConditions(){
        addedVictoryConditions.set(true);
        VictoryCondition vt = VictoryType.createVictoryCondition(this);

        /// Add a time limit unless one is provided by default
        if (!(vt instanceof DefinesTimeLimit) &&
                (params.getMatchTime() != null && params.getMatchTime() > 0 &&
                        params.getMatchTime() != Integer.MAX_VALUE)){
            addVictoryCondition(new TimeLimit(this));
        }

        /// set the number of lives
        Integer nLives = params.getNLives();
        if (nLives != null && nLives > 0 && !(vt instanceof DefinesNumLivesPerPlayer)){
            addVictoryCondition(new NLives(this,nLives));
        }

        /// Add a default number of teams unless the specified victory condition handles it
        Integer nTeams = params.getMinTeams();
        if (!(vt instanceof DefinesNumTeams)){
            if (nTeams <= 0){
				/* do nothing.  They want this event to be open even with no teams*/
            } else if (nTeams == 1){
                addVictoryCondition(new NoTeamsLeft(this));
            } else {
                addVictoryCondition(new OneTeamLeft(this));
            }
        }
        addVictoryCondition(vt);
    }

    /**
     * Add Another victory condition to this match
     * @param victoryCondition Victory condition to add
     */
    public void addVictoryCondition(VictoryCondition victoryCondition){
        if (Defaults.DEBUG_TRACE) System.out.println(this.getArena().getName() + " adding vc=" + victoryCondition);
        vcs.add(victoryCondition);
        addArenaListener(victoryCondition);
        if (!alwaysOpen && victoryCondition instanceof DefinesNumTeams){
            neededTeams = Math.max(neededTeams, ((DefinesNumTeams)victoryCondition).getNeededNumberOfTeams().max);}
        if (victoryCondition instanceof DefinesNumLivesPerPlayer){
            nLivesPerPlayer = Math.max(nLivesPerPlayer, ((DefinesNumLivesPerPlayer)victoryCondition).getLivesPerPlayer());
            for (ArenaPlayer ap : matchPlayers){
                if (nLivesPerPlayer != 1 && nLivesPerPlayer != ArenaSize.MAX) {
                    ap.getMetaData().setLivesLeft(nLivesPerPlayer);
                    scoreboard.setEntryNameSuffix(ap.getName(),"&4("+nLivesPerPlayer+")");
                }
            }

        }
        if (victoryCondition instanceof ScoreTracker){
            if (params.getMaxTeamSize() <= 2){
                ((ScoreTracker)victoryCondition).setDisplayTeams(false);
            }
            ((ScoreTracker)victoryCondition).setScoreBoard(scoreboard);
        }
        /// update listener to ONOPEN if we are already opened
        if (state.ordinal() >= MatchState.ONOPEN.ordinal())
            updateBukkitEvents(MatchState.ONOPEN,victoryCondition); /// register all match events
        if (!inMatch.isEmpty()){
            updateBukkitEvents(MatchState.ONENTER,victoryCondition); /// register all current inside players with the vc
        }
    }

    /**
     * Remove a victory condition from this match
     * @param victoryCondition VictoryCondition to remove
     */
    public void removeVictoryCondition(VictoryCondition victoryCondition){
        vcs.remove(victoryCondition);
        removeArenaListener(victoryCondition);
    }


    public VictoryCondition getVictoryCondition(Class<? extends VictoryCondition> clazz) {
        for (VictoryCondition vc : vcs){
            if (vc.getClass() == clazz)
                return vc;
        }
        return null;
    }

    /**
     * Gets the arena currently being used by this match
     * @return arena or null if not in use
     */
    public Arena getArena() {return arena;}

    public boolean isEnding() {return isWon() || isFinished();}
    public boolean isFinished() {return state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL;}
    public boolean isWon() {return state == MatchState.ONVICTORY || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL;}
    public boolean isStarted() {return state == MatchState.ONSTART;}
    public boolean isInWaitRoomState() {return state.ordinal() < MatchState.ONSTART.ordinal();}

    @Override
    public MatchState getState() {return state;}
    @Override
    public MatchState getMatchState(){return state;}

    @Override
    protected void transitionTo(CompetitionState state){
        this.state = (MatchState) state;
        if (!addedVictoryConditions.get() && state == tinState){
            addVictoryConditions();}
        times.put(this.state, System.currentTimeMillis());
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public Long getTime(CompetitionState state){
        return times.get(state);
    }

    @Override
    public MatchParams getParams() {return params;}

    @Override
    public List<ArenaTeam> getTeams() {return teams;}
    public List<ArenaTeam> getAliveTeams() {
        List<ArenaTeam> alive = new ArrayList<ArenaTeam>();
        for (ArenaTeam t: teams){
            if (t.isDead())
                continue;
            alive.add(t);
        }
        return alive;
    }

    public Set<ArenaPlayer> getAlivePlayers() {
        HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>();
        for (ArenaTeam t: teams){
            if (t.isDead())
                continue;
            players.addAll(t.getLivingPlayers());
        }
        return players;
    }

    public Location getTeamSpawn(ArenaTeam team, boolean random){
        return random ? arena.getSpawn(-1,true): arena.getSpawn(team.getIndex(),false);
    }
    public Location getTeamSpawn(int index, boolean random){
        return random ? arena.getSpawn(-1,true): arena.getSpawn(index,false);
    }
    public Location getWaitRoomSpawn(ArenaTeam team, boolean random){
        return random ? arena.getRandomWaitRoomSpawnLoc(): arena.getWaitRoomSpawnLoc(team.getIndex());
    }
    public Location getWaitRoomSpawn(int index, boolean random){
        return random ? arena.getRandomWaitRoomSpawnLoc(): arena.getWaitRoomSpawnLoc(index);
    }

    public void setMatchResult(MatchResult result){
        this.matchResult = result;
    }

    public void endMatchWithResult(MatchResult result){
        this.matchResult = result;
        matchWinLossOrDraw(result);
    }

    public void setVictor(ArenaPlayer p){
        ArenaTeam t = getTeam(p);
        if (t != null) setVictor(t);
    }

    /**
     * Alias for setVictor
     * @param team ArenaTeam
     */
    public synchronized void setWinner(final ArenaTeam team){
        setVictor(team);
    }

    /**
     * Set the victor of this match
     * @param team ArenaTeam
     */
    public synchronized void setVictor(final ArenaTeam team){
        setVictor(new ArrayList<ArenaTeam>(Arrays.asList(team)));
    }

    public synchronized void setVictor(final Collection<ArenaTeam> winningTeams){
        if (individualWins){
            MatchResult result = new MatchResult();
            result.setVictors(winningTeams);
            endMatchWithResult(result);
        } else {
            matchResult.setVictors(winningTeams);
            ArrayList<ArenaTeam> losers= new ArrayList<ArenaTeam>(teams);
            losers.removeAll(winningTeams);
            matchResult.addLosers(losers);
            matchResult.setResult(WinLossDraw.WIN);
            endMatchWithResult(matchResult);
        }
    }

    public synchronized void setDraw(){
        matchResult.setResult(WinLossDraw.DRAW);
        matchResult.setDrawers(teams);
        endMatchWithResult(matchResult);
    }

    public synchronized void setLosers(){
        matchResult.setResult(WinLossDraw.LOSS);
        matchResult.setLosers(teams);
        endMatchWithResult(matchResult);
    }


    public synchronized void setNonEndingVictor(final ArenaTeam team){
        MatchResult result = new MatchResult();
        result.setVictor(team);
        nonEndingMatchWinLossOrDraw(result);
    }

    public MatchResult getResult(){return matchResult;}
    public Set<ArenaTeam> getVictors() {return matchResult.getVictors();}
    public synchronized Set<ArenaTeam> getLosers() {return matchResult.getLosers();}
    public Set<ArenaTeam> getDrawers() {return matchResult.getDrawers();}
    public Map<String,Location> getOldLocations() {return oldlocs;}

    public int indexOf(ArenaTeam t){return teams.indexOf(t);}

    public boolean isHandled(ArenaPlayer player) {
        GameManager gm = GameManager.getGameManager(params);
        boolean b = gm.isHandled(player);
        return inMatch.contains(player.getName()) || b;
    }

    @Deprecated
    /**
     * use isHandled instead
     */
    public boolean insideArena(ArenaPlayer p){
        return isHandled(p);
    }

    public boolean isInMatch(ArenaPlayer player) {
        return inMatch.contains(player.getName());
    }

    protected Set<ArenaPlayer> checkReady(final ArenaTeam t, TransitionOptions mo) {
        Set<ArenaPlayer> alive = new HashSet<ArenaPlayer>();
        for (ArenaPlayer p : t.getPlayers()){
            if (checkReady(p,t,mo,true)){
                alive.add(p);}
        }
        return alive;
    }

    public boolean checkReady(ArenaPlayer p, final ArenaTeam t, TransitionOptions mo, boolean announce) {
        boolean online = p.isOnline();
        boolean inBed = p.getPlayer().isSleeping();
        boolean inmatch = inMatch.contains(p.getName());
        final String pname = p.getDisplayName();
        boolean ready = true;
        World w = arena.getSpawn(0,false).getWorld();
        if (Defaults.DEBUG) System.out.println(p.getName()+"  online=" + online +" isready="+tops.playerReady(p,w));
        if (!online){
            t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for not being online");
            ready = false;
        } else if (inBed){
            t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for being in bed while the match starts");
            ready = false;
        } else if (p.isDead()){
            t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for being dead while the match starts");
            ready = false;
        } else if (!inmatch && !tops.playerReady(p,w)){ /// Players are about to be teleported into arena
            t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for not being ready");
            String notReady = tops.getRequiredString(p,w,"&eYou needed");
            MessageUtil.sendMessage(p,"&eYou didn't compete because of the following.");
            MessageUtil.sendMessage(p,notReady);
            ready = false;
        }
        if (!ready){
            t.killMember(p);
        }
        return ready;
    }

    public void sendMessage(String string) {
        for (ArenaTeam t: teams){
            t.sendMessage(string);}
    }

    public String getMatchInfo() {
        TransitionOptions to = tops.getOptions(state);
        StringBuilder sb = new StringBuilder("ArenaMatch " + this.toString() +" ");
        sb.append(params).append("\n");
        sb.append("state=&6").append(state).append("\n");
        sb.append("pvp=&6").append(to != null ? to.getPVP() : "on").append("\n");
        //		sb.append("playersInMatch=&6"+inMatch.get(p)+"\n");
        sb.append("result=&e(").append(matchResult).append("&e)\n");
        List<ArenaTeam> deadTeams = new ArrayList<ArenaTeam>();
        List<ArenaTeam> aliveTeams = new ArrayList<ArenaTeam>();
        for (ArenaTeam t: teams){
            if (t.size() == 0)
                continue;
            if (t.isDead())
                deadTeams.add(t);
            else
                aliveTeams.add(t);
        }
        for (ArenaTeam t: aliveTeams) sb.append(t.getTeamInfo(inMatch)).append("\n");
        for (ArenaTeam t: deadTeams) sb.append(t.getTeamInfo(inMatch)).append("\n");
        return sb.toString();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("[Match:"+id+":" + (arena != null ? arena.getName():"none") +" ,");
        for (ArenaTeam t: teams){
            sb.append("[").append(t.getDisplayName()).append("] ,");}
        sb.append("]");
        return sb.toString();
    }

    public boolean hasTeam(ArenaTeam team) {
        return teams.contains(team);
    }

    public List<VictoryCondition> getVictoryConditions() {
        return vcs;
    }

    public void timeExpired() {
        MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams, true);
        callEvent(event);
        MatchResult result = event.getResult();
        /// No one has an opinion of how this match ends... so declare it a draw
        if (result.isUnknown() || (result.getDrawers().isEmpty() &&
                result.getLosers().isEmpty() && result.getVictors().isEmpty())){
            result = defaultObjective.getMatchResult(this);
        }

        try{mc.sendTimeExpired();}catch(Exception e){Log.printStackTrace(e);}
        this.endingMatchWinLossOrDraw(result);
    }

    public void intervalTick(int remaining) {
        MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams,false);
        callEvent(event);
        callEvent(new MatchTimerIntervalEvent(this, remaining));
        try{mc.sendOnIntervalMsg(remaining, event.getCurrentLeaders());}catch(Exception e){Log.printStackTrace(e);}
    }

    public void secondTick(int remaining) {
        SObjective obj = scoreboard.getObjective(SAPIDisplaySlot.SIDEBAR);
        if (obj != null){
            obj.setDisplayNameSuffix(" &e("+remaining+")");}
    }

    public AbstractJoinHandler getTeamJoinHandler() {
        return joinHandler;
    }

    public boolean hasWaitroom() {
        return arena.getWaitroom() != null;
    }

    public boolean hasSpectatorRoom() {
        return arena.getSpectatorRoom()!= null;
    }

    public boolean isJoinablePostCreate(){
        return joinHandler != null &&
                (alwaysOpen || tops.hasOptionAt(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN) ||
                        !joinHandler.isFull());
    }

    public boolean canStillJoin() {
        return joinHandler != null &&
                (alwaysOpen || tops.hasOptionAt(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN) ||
                        ((hasWaitroom() || hasSpectatorRoom()) && !joinHandler.isFull() && (isInWaitRoomState() &&
                                (joinCutoffTime == null || System.currentTimeMillis() < joinCutoffTime))));
    }

    @Override
    public int getID(){
        return id;
    }

    @Override
    public String getName(){
        return params.getName();
    }


    public void killTeam(int teamIndex) {
        if (teams.size() <= teamIndex)
            return;
        ArenaTeam t = teams.get(teamIndex);
        if (t == null)
            return;
        killTeam(t);
    }

    public void killTeam(ArenaTeam t){
        for (ArenaPlayer ap: t.getLivingPlayers()){
            ArenaPlayerDeathEvent apde = new ArenaPlayerDeathEvent(ap,t);
            apde.setExiting(true);
            callEvent(apde);
        }
    }

    public Set<String> getInsidePlayers(){
        return new HashSet<String>(inMatch);
    }


    public boolean allTeamsReady() {
        for (ArenaTeam t: teams){
            if (!t.isReady())
                return false;
        }
        return true;
    }

    public boolean alwaysOpen() {
        return alwaysOpen;
    }

    public ArenaScoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public Location getSpawn(int index, boolean random) {
        return this.getTeamSpawn(index, random);
    }

    @Override
    public Location getSpawn(ArenaPlayer player, boolean random) {
        return player.getLocation();
    }

    @Override
    public LocationType getLocationType() {
        return LocationType.ARENA;
    }

    public List<ArenaPlayer> getNonLeftPlayers() {
        List<ArenaPlayer> players = new ArrayList<ArenaPlayer>();
        for (ArenaTeam at: this.getTeams()){
            for (ArenaPlayer ap: at.getPlayers()){
                if (at.hasLeft(ap))
                    continue;
                players.add(ap);
            }
        }
        return players;
    }

    class ArenaPreviousState{
        final ContainerState waitroomCS;
        final ContainerState arenaCS;
        MatchParams params;

        public ArenaPreviousState(Arena arena) {
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
    public void setOldArenaParams(MatchParams oldArenaParams) {
        if (oldArenaState == null) {
            oldArenaState = new ArenaPreviousState(arena);}
        oldArenaState.params = oldArenaParams;
    }

}
