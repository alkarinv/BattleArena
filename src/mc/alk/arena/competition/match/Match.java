package mc.alk.arena.competition.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.controllers.ArenaController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.controllers.ListenerAdder;
import mc.alk.arena.controllers.LobbyController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.RewardController;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.controllers.WorldGuardController.WorldGuardFlag;
import mc.alk.arena.controllers.containers.LobbyContainer;
import mc.alk.arena.controllers.messaging.MatchMessageHandler;
import mc.alk.arena.controllers.messaging.MatchMessager;
import mc.alk.arena.events.matches.MatchCancelledEvent;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.matches.MatchOpenEvent;
import mc.alk.arena.events.matches.MatchPrestartEvent;
import mc.alk.arena.events.matches.MatchResultEvent;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.matches.MatchTimerIntervalEvent;
import mc.alk.arena.events.players.ArenaPlayerEnterMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.events.prizes.ArenaDrawersPrizeEvent;
import mc.alk.arena.events.prizes.ArenaLosersPrizeEvent;
import mc.alk.arena.events.prizes.ArenaPrizeEvent;
import mc.alk.arena.events.prizes.ArenaWinnersPrizeEvent;
import mc.alk.arena.events.teams.TeamDeathEvent;
import mc.alk.arena.listeners.competition.ItemPickupListener;
import mc.alk.arena.listeners.competition.PlayerTeleportListener;
import mc.alk.arena.listeners.competition.TeamHeadListener;
import mc.alk.arena.objects.ArenaLocation;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.TeleportDirection;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaControllerInterface;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.messaging.Channel.ServerChannel;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.queues.TeamQObject;
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
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

/// TODO once I have GameLogic, split this into two matches, one for always open, one for normal
public abstract class Match extends Competition implements Runnable, ArenaController {
	public enum PlayerState{OUTOFMATCH,INMATCH};
	static int count =0;

	final int id = count++;
	final MatchParams params; /// Our parameters for this match
	final Arena arena; /// The arena we are using
	final ArenaControllerInterface arenaInterface; /// Our interface to access arena methods w/o reflection

	List<ArenaTeam> playingTeams = new LinkedList<ArenaTeam>();
	Set<String> visitors = new HashSet<String>(); /// Who is watching
	MatchState state = MatchState.NONE;/// State of the match

	/// When did each transition occur
	final Map<MatchState, Long> times = Collections.synchronizedMap(new EnumMap<MatchState,Long>(MatchState.class));
	final List<VictoryCondition> vcs = new ArrayList<VictoryCondition>(); /// Under what conditions does a victory occur
	MatchResult matchResult; /// Results for this match
	Map<String, Location> oldlocs = null; /// Locations where the players came from before entering arena
	final Set<String> insideArena = new HashSet<String>(); /// who is still inside arena area
	final Set<String> insideWaitRoom = new HashSet<String>(); /// who is still inside the wait room
	final Set<String> insideLobby = new HashSet<String>(); /// who is still inside the wait room
	final Set<String> insideCourtyard = new HashSet<String>(); /// who is still inside the wait room

	MatchTransitions tops = null; /// Our match options for this arena match
	final PlayerStoreController psc = new PlayerStoreController(); /// Store items and exp for players if specified

//	Set<ArenaPlayer> readyPlayers = null; /// Do we have ready Players
	Set<MatchState> waitRoomStates = null; /// which states are inside a waitRoom
	final MatchState tinState; /// which matchstat teleports players in (first one is chosen)
	Long joinCutoffTime = null; /// at what point do we cut people off from joining
	Integer currentTimer = null; /// Our current timer
	Collection<ArenaTeam> originalTeams = null;
	final Set<ArenaTeam> nonEndingTeams =
			Collections.synchronizedSet(new HashSet<ArenaTeam>());
	final Map<ArenaTeam,Integer> individualTeamTimers = Collections.synchronizedMap(new HashMap<ArenaTeam,Integer>());
	boolean addedVictoryConditions = false;

	/// These get used enough or are useful enough that i'm making variables even though they can be found in match options
	final boolean needsClearInventory, clearsInventory, clearsInventoryOnDeath;
	final boolean keepsInventory;
	final boolean respawns, noLeave, noEnter;
	final boolean spawnsRandom;
	final boolean woolTeams, armorTeams;
	final boolean alwaysTeamNames;
	final boolean respawnsWithClass;
	final boolean cancelExpLoss;
	final boolean individualWins;
	final boolean alwaysOpen;

	int neededTeams; /// How many teams do we need to properly start this match
	final Plugin plugin;
	int nLivesPerPlayer = Integer.MAX_VALUE; /// This will change as victory conditions are added
	ArenaScoreboard scoreboard;
	Random rand = new Random(); /// Our randomizer
	MatchMessager mc; /// Our message instance
	TeamJoinHandler joinHandler = null;
	ArenaObjective defaultObjective = null;

	@SuppressWarnings("unchecked")
	public Match(Arena arena, MatchParams params) {
		if (Defaults.DEBUG) System.out.println("ArenaMatch::" + params);
		/// Assign variables
		this.plugin = BattleArena.getSelf();
		this.params = params;

		this.arena = arena;
		this.arenaInterface =new ArenaControllerInterface(arena);
		addArenaListener(arena);
		scoreboard = ScoreboardFactory.createScoreboard(this,params);

		this.mc = new MatchMessager(this);
		this.oldlocs = new HashMap<String,Location>();
		this.tops = params.getTransitionOptions();
		arena.setMatch(this);

		Collection<ArenaModule> modules = params.getModules();
		if (modules != null){
			for (ArenaModule am: modules){
				if (am.isEnabled())
					addArenaListener(am);
			}
		}
		/// placed anywhere options
		noEnter = tops.hasAnyOption(TransitionOption.WGNOENTER);
		if (noEnter && arena.hasRegion())
			WorldGuardController.setFlag(arena.getWorldGuardRegion(), WorldGuardFlag.ENTRY, !noEnter);
		this.noLeave = tops.hasAnyOption(TransitionOption.WGNOLEAVE);
		this.woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS) && params.getMaxTeamSize() >1 ||
				tops.hasAnyOption(TransitionOption.ALWAYSWOOLTEAMS);
		this.armorTeams = tops.hasAnyOption(TransitionOption.ARMORTEAMS);
		boolean needsDamageEvents = tops.hasAnyOption(TransitionOption.PVPOFF,TransitionOption.PVPON,TransitionOption.INVINCIBLE);

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
				tops.hasOptionAt(MatchState.ONDEATH, TransitionOption.RANDOMRESPAWN);
		/// onSpawn options
		this.respawnsWithClass = tops.hasOptionAt(MatchState.ONSPAWN, TransitionOption.RESPAWNWITHCLASS);
		this.alwaysOpen = params.getAlwaysOpen();

		/// Set waitroom variables

		if (tops.hasAnyOption(TransitionOption.TELEPORTWAITROOM, TransitionOption.TELEPORTLOBBY, TransitionOption.TELEPORTCOURTYARD)){
			waitRoomStates = new HashSet<MatchState>(tops.getMatchStateRange(TransitionOption.TELEPORTWAITROOM, TransitionOption.TELEPORTIN));
			waitRoomStates.addAll(tops.getMatchStateRange(TransitionOption.TELEPORTLOBBY, TransitionOption.TELEPORTIN));
			waitRoomStates.addAll(tops.getMatchStateRange(TransitionOption.TELEPORTCOURTYARD, TransitionOption.TELEPORTIN));
			if (waitRoomStates.isEmpty()){
				waitRoomStates = null;}
		}

		/// Register the events we are listening to
		ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>();
		events.addAll(Arrays.asList(PlayerQuitEvent.class, PlayerRespawnEvent.class,
				PlayerCommandPreprocessEvent.class, PlayerDeathEvent.class, PlayerInteractEvent.class));

		if (needsDamageEvents){
			events.add(EntityDamageEvent.class);}
		if (noLeave){
			events.add(PlayerMoveEvent.class);}
		ListenerAdder.addListeners(this, tops);
		if (tops.hasAnyOption(TransitionOption.NOTELEPORT, TransitionOption.NOWORLDCHANGE) || noEnter){
			this.addArenaListener(new PlayerTeleportListener(this));}
		if (tops.hasAnyOption(TransitionOption.ITEMPICKUPOFF)){
			this.addArenaListener(new ItemPickupListener(this));}
		if (woolTeams){
			this.addArenaListener(new TeamHeadListener());}

		methodController.addSpecificEvents(this, events);
		/// add a default objective
		defaultObjective = new ArenaObjective("default","Player Kills",0);

		defaultObjective.setDisplayName(ChatColor.GOLD+"Still Alive");
		scoreboard.addObjective(defaultObjective);
		defaultObjective.setScoreBoard(scoreboard);

		LobbyContainer lc = LobbyController.getLobby(params.getType());
		if (lc != null){
			insideArena.addAll(lc.getInsidePlayers());
		}
	}

	private void updateBukkitEvents(MatchState matchState){
		final ArrayList<String> players = new ArrayList<String>(insideArena);
		methodController.updateEvents(matchState, players);
	}

	private void updateBukkitEvents(MatchState matchState,ArenaPlayer player){
		methodController.updateEvents(matchState, player);
	}

	/**
	 * As this gets calls Arena's and events which can call bukkit events
	 * this should be done in a synchronous fashion
	 */
	public void open(){
		final Match match = this;
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
			@Override
			public void run() {
				transitionTo(MatchState.ONOPEN);
				MatchOpenEvent event = new MatchOpenEvent(match);

				callEvent(event);
				if (event.isCancelled()){
					match.cancelMatch();
					return;
				}
				updateBukkitEvents(MatchState.ONOPEN);
				arenaInterface.onOpen();
			}});
	}

	public void run() {
		preStartMatch();
	}

	public void setTeamJoinHandler(TeamJoinHandler teamJoinHandler){
		this.joinHandler = teamJoinHandler;
	}

	//	private void beginMatch() {
	//		if (state == MatchState.ONCANCEL) return; /// If the match was cancelled, dont proceed
	//		if (Defaults.DEBUG) System.out.println("ArenaMatch::beginMatch()");
	//		state = MatchState.ONBEGIN;
	//
	//		MatchBeginEvent event= new MatchBeginEvent(this,teams);
	//		notifyListeners(event);
	//		updateBukkitEvents(MatchState.ONBEGIN);
	//		PerformTransition.transition(this, MatchState.ONBEGIN, teams, true);
	//		arenaInterface.onBegin();
	//		try{mc.sendOnBeginMsg(teams);}catch(Exception e){e.printStackTrace();}
	//		/// Schedule the start of the match
	//		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	//			public void run() {
	//				preStartMatch();
	//			}
	//		}, (long) (mp.getSecondsTillMatch() * 20L * Defaults.TICK_MULT));
	//	}
	private void preStartTeams(List<ArenaTeam> teams, boolean matchPrestarting){
		TransitionOptions ts = tops.getOptions(MatchState.ONPRESTART);
		/// If we will teleport them into the arena for the first time, check to see they are ready first
		if (ts != null && ts.teleportsIn()){
			for (ArenaTeam t: teams){
				checkReady(t,tops.getOptions(MatchState.PREREQS));	}
		}
		PerformTransition.transition(this, MatchState.ONPRESTART, teams, true);
		/// Send messages to teams and server, or just to the teams
		if (matchPrestarting){
			mc.sendOnPreStartMsg(teams);
		} else {
			mc.sendOnPreStartMsg(teams, ServerChannel.NullChannel);
		}
	}

	private void preStartMatch() {
		if (state == MatchState.ONCANCEL) return; /// If the match was cancelled, dont proceed
		if (Defaults.DEBUG) System.out.println("ArenaMatch::startMatch()");
		transitionTo(MatchState.ONPRESTART);

		updateBukkitEvents(MatchState.ONPRESTART);
		callEvent(new MatchPrestartEvent(this,teams));

		preStartTeams(teams,true);
		arenaInterface.onPrestart();
		/// Schedule the start of the match
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				startMatch();
			}
		}, (long) (params.getSecondsTillMatch() * 20L * Defaults.TICK_MULT));
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
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				startMatch();
			}
		}, (long) (10 * 20L * Defaults.TICK_MULT));
	}

	private void startMatch(){
		if (state == MatchState.ONCANCEL) return; /// If the match was cancelled, dont proceed
		transitionTo(MatchState.ONSTART);
		if (!addedVictoryConditions){
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
			PerformTransition.transition(this, state,competingTeams, true);
			arenaInterface.onStart();
			try{mc.sendOnStartMsg(teams);}catch(Exception e){Log.printStackTrace(e);}
			/// At this point every team and player should be inside.. if they aren't mark them dead
			nCompetingTeams = checkInside(teams);
		}
		checkEnoughTeams(competingTeams, nCompetingTeams, neededTeams);
	}

	private void checkEnoughTeams(List<ArenaTeam> competingTeams, int nCompetingTeams, int neededTeams) {
		if (nCompetingTeams >= neededTeams){
			return;
		} else if (nCompetingTeams < neededTeams && nCompetingTeams==1){
			ArenaTeam victor = competingTeams.get(0);
			victor.sendMessage("&4WIN!!!&eThe other team was offline or didnt meet the entry requirements.");
			setVictor(victor);
		} else { /// Seriously, no one showed up?? Well, one of them won regardless, but scold them
			if (competingTeams.isEmpty()){
				this.cancelMatch();
			} else {
				setDraw();
			}
		}
	}

	private int checkInside(List<ArenaTeam> teams) {
		int alive= 0;
		for (ArenaTeam t: teams){
			for (ArenaPlayer ap : t.getPlayers()){
				if (!insideArena(ap)){
					t.killMember(ap);}
			}
			if (!t.isDead())
				alive++;
		}
		return alive;
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
		int timerid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
				new NonEndingMatchVictory(this,result),2L);
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
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new MatchVictory(this),2L);
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
			if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am +"  victors="+ victors + "  " + losers+"  "+drawers +" " + matchResult);
			if (params.isRated()){
				StatController sc = new StatController(params);
				sc.addRecord(victors,losers,drawers,result.getResult());
			}

			if (result.hasVictor()){ /// We have a true winner
				try{mc.sendOnVictoryMsg(victors, losers);}catch(Exception e){Log.printStackTrace(e);}
			} else { /// we have a draw
				try{mc.sendOnDrawMessage(drawers,losers);} catch(Exception e){Log.printStackTrace(e);}
			}

			PerformTransition.transition(am, MatchState.ONVICTORY,teams, true);
			int timerid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
					new NonEndingMatchCompleted(am, result, teams),
					(long) (params.getSecondsToLoot() * 20L * Defaults.TICK_MULT));
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
			if (params.isRated()){
				StatController sc = new StatController(params);
				sc.addRecord(victors,losers,drawers,am.getResult().getResult());
			}
			if (matchResult.hasVictor()){ /// We have a true winner
				try{mc.sendOnVictoryMsg(victors, losers);}catch(Exception e){Log.printStackTrace(e);}
			} else { /// we have a draw
				try{mc.sendOnDrawMessage(drawers,losers);} catch(Exception e){Log.printStackTrace(e);}
			}

			updateBukkitEvents(MatchState.ONVICTORY);
			PerformTransition.transition(am, MatchState.ONVICTORY,teams, true);
			currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
					new MatchCompleted(am), (long) (params.getSecondsToLoot() * 20L * Defaults.TICK_MULT));
		}
	}
	class NonEndingMatchCompleted implements Runnable{
		final Match am;
		final MatchResult result;
		final List<ArenaTeam> teams;

		NonEndingMatchCompleted(Match am, MatchResult result, List<ArenaTeam> teams){
			this.am = am;this.result = result;
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
			int timerid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
				public void run() {
					/// Losers and winners get handled after the match is complete
					if (result.getLosers() != null){
						PerformTransition.transition(am, MatchState.LOSERS, result.getLosers(), false);
						ArenaPrizeEvent event = new ArenaLosersPrizeEvent(am, result.getLosers());
						callEvent(event);
						new RewardController(event,psc).giveRewards();
					}
					if (result.getDrawers() != null){
						PerformTransition.transition(am, MatchState.LOSERS, result.getDrawers(), false);
						ArenaPrizeEvent event = new ArenaDrawersPrizeEvent(am, result.getDrawers());
						callEvent(event);
						new RewardController(event,psc).giveRewards();
					}
					if (result.getVictors() != null){
						PerformTransition.transition(am, MatchState.WINNER, result.getVictors(), false);
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
			final Collection<ArenaTeam> victors = am.getVictors();

			if (Defaults.DEBUG) System.out.println("Match::MatchCompleted(): " + victors);
			/// ONCOMPLETE can teleport people out of the arena,
			/// So the order of events is usually
			/// ONCOMPLETE(half of effects) -> ONLEAVE( and all effects) -> ONCOMPLETE( rest of effects)
			PerformTransition.transition(am, MatchState.ONCOMPLETE, teams, true);
			/// Once again, lets delay this final bit so that transitions have time to finish before
			/// Other splisteners get a chance to handle
			currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
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
						PerformTransition.transition(am, MatchState.LOSERS, am.getDrawers(), false);
						ArenaPrizeEvent event = new ArenaDrawersPrizeEvent(am, am.getDrawers());
						callEvent(event);
						new RewardController(event,psc).giveRewards();
					}
					if (am.getVictors() != null){
						PerformTransition.transition(am, MatchState.WINNER, am.getVictors(), false);
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
			PerformTransition.transition(this, MatchState.ONCANCEL,t,true);
		}
		/// For players that were in the process of joining when cancel happened
		for (Entry<ArenaTeam,Integer> entry : individualTeamTimers.entrySet()){
			Bukkit.getScheduler().cancelTask(entry.getValue());
			if (!teams.contains(entry.getKey()))
				PerformTransition.transition(this, MatchState.ONCANCEL,entry.getKey(),true);
		}
		callEvent(new MatchCancelledEvent(this));
		updateBukkitEvents(MatchState.ONCANCEL);
		deconstruct();
	}

	private void nonEndingDeconstruct(List<ArenaTeam> teams){
		final Match match = this;
		for (ArenaTeam t: teams){
			TeamController.removeTeamHandler(t, match);

			PerformTransition.transition(this, MatchState.ONFINISH,t,true);
			for (ArenaPlayer p: t.getPlayers()){
				stopTracking(p);
				p.removeCompetition(this);
				insideArena.remove(p.getName());
				insideWaitRoom.remove(p.getName());
				insideLobby.remove(p.getName());
				insideCourtyard.remove(p.getName());
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
			TeamController.removeTeamHandler(t, match);
			PerformTransition.transition(this, MatchState.ONFINISH,t,true);
			for (ArenaPlayer p: t.getPlayers()){
				stopTracking(p);
				p.removeCompetition(this);
			}
			scoreboard.removeTeam(t);
		}
		/// For players that were in the process of joining when deconstruct happened
		for (Entry<ArenaTeam,Integer> entry : individualTeamTimers.entrySet()){
			Bukkit.getScheduler().cancelTask(entry.getValue());
			if (!teams.contains(entry.getKey())){
				TeamController.removeTeamHandler(entry.getKey(), match);
				PerformTransition.transition(this, MatchState.ONCANCEL,entry.getKey(),true);
				for (ArenaPlayer p: entry.getKey().getPlayers()){
					stopTracking(p);
					p.removeCompetition(this);
				}
			}
		}
		scoreboard.clear();
		arenaInterface.onFinish();
		insideArena.clear();
		insideWaitRoom.clear();
		insideLobby.clear();
		insideCourtyard.clear();
		teams.clear();
		methodController.deconstruct();
		//		arenaListeners.clear();
		if (joinHandler != null){
			joinHandler.deconstruct();}
	}

	@Override
	public boolean addTeam(ArenaTeam team){
		if (this.isFinished())
			return false;
		if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" addTeam("+team.getName()+":"+team.getId()+")");

		teamIndexes.put(team, teams.size());
		teams.add(team);
		team.reset();/// reset scores, set alive
		TeamController.addTeamHandler(team, this);
		int index = indexOf(team);
		team.setTeamChatColor(TeamUtil.getTeamChatColor(index));
		String name = TeamUtil.createTeamName(index);
		if ( alwaysTeamNames || (!team.hasSetName() && team.getDisplayName().length() > Defaults.MAX_TEAM_NAME_APPEND)){
			team.setDisplayName(name);
		}
		team.setScoreboardDisplayName(name.length() > Defaults.MAX_SCOREBOARD_NAME_SIZE ?
				name.substring(0,Defaults.MAX_SCOREBOARD_NAME_SIZE) : name);
		team.setArenaObjective(defaultObjective);
		scoreboard.addTeam(team);

		for (ArenaPlayer p: team.getPlayers()){
			privateAddedToTeam(team,p);}
		joiningOngoing(team, null);
		return true;
	}

	/** Called during both, addTeam and addedToTeam */
	private void privateAddedToTeam(ArenaTeam team, ArenaPlayer player){
		leftPlayers.remove(player.getName()); /// remove players from the list as they are now joining again
		insideArena.remove(player.getName());
		team.setAlive(player);
		player.reset();
		player.addCompetition(this);
		arenaInterface.onJoin(player,team);
	}

	@Override
	public boolean removeTeam(ArenaTeam team){
		if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" removeTeam("+team.getName()+":"+team.getId()+")");

		if (teams.contains(team)){
			onLeave(team);
			scoreboard.removeTeam(team);
			return true;
		}
		return false;
	}

	/**
	 * Add to an already existing team
	 * @param p
	 * @param t
	 */
	@Override
	public boolean addedToTeam(final ArenaTeam team, final ArenaPlayer player) {
		Log.info(getID()+" addedToTeam("+team.getName()+":"+team.getId()+", " + player.getName()+") inside="+insideArena.contains(player.getName()));
		if (isEnding())
			return false;
		if (Defaults.DEBUG_MATCH_TEAMS)
			Log.info(getID()+" addedToTeam("+team.getName()+":"+team.getId()+", " + player.getName()+") inside="+insideArena.contains(player.getName()));

		if (!team.hasSetName() && team.getDisplayName().length() > Defaults.MAX_TEAM_NAME_APPEND){
			team.setDisplayName(TeamUtil.createTeamName(indexOf(team)));}
		privateAddedToTeam(team,player);
		scoreboard.addedToTeam(team, player);

		mc.sendAddedToTeam(team,player);
		//		team.sendToOtherMembers(player, MessageController.);

		joiningOngoing(team, player);
		return true;
	}

	private static void doTransition(Match match, MatchState state, ArenaPlayer player, ArenaTeam team, boolean onlyInMatch){
		if (player != null){
			PerformTransition.transition(match, state, player,team, onlyInMatch);
		} else {
			PerformTransition.transition(match, state, team, onlyInMatch);
		}
	}

	private void joiningOngoing(final ArenaTeam team, final ArenaPlayer player) {
		final Match match = this;
		/// onJoin Them
		doTransition(match, MatchState.ONJOIN, player,team, true);
		/// onPreStart Them
		if (state.ordinal() >= MatchState.ONPRESTART.ordinal()){
			doTransition(match, MatchState.ONPRESTART, player,team, true);

			/// onStart Them
			if (state.ordinal() >= MatchState.ONSTART.ordinal()){
				int timerid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						doTransition(match, MatchState.ONSTART, player,team, true);
					}
				}, (long) (params.getSecondsTillMatch() * 20L * Defaults.TICK_MULT));
				for (ArenaTeam t: teams)
					individualTeamTimers.put(t, timerid);
			}
		}
	}

	/**
	 * Add to an already existing team
	 * @param p
	 * @param t
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

	public void onJoin(Collection<ArenaTeam> teams){
		for (ArenaTeam t: teams){
			onJoin(t);}
	}

	public void onJoin(ArenaTeam team) {
		if (joinHandler != null){
			TeamQObject tqo = new TeamQObject(team,params,null);
			joinHandler.joiningTeam(tqo);
		} else {
			addTeam(team);
		}
	}

	/**
	 * TeamHandler override
	 * Players can always leave, they just might be killed for doing so
	 */
	@Override
	public boolean canLeave(ArenaPlayer p) {
		return true;
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
	 * @param team
	 */
	public void onLeave(ArenaTeam team) {
		for (ArenaPlayer ap: team.getPlayers()){
			privateOnLeave(ap,team);}
		privateRemoveTeam(team);
	}

	public boolean onLeave(ArenaPlayer p) {
		/// remove them from the match, they don't want to be here
		ArenaTeam t = getTeam(p);
		if (t==null) /// really? trying to make a player leave who isnt in the match
			return false;
		privateOnLeave(p,t);
		return true;
	}

	private void privateOnLeave(ArenaPlayer ap, ArenaTeam team){
		if (insideArena(ap)){ /// Only leave if they haven't already left.
			/// The onCancel should teleport them out, and call leaveArena(ap)
			PerformTransition.transition(this, MatchState.ONCANCEL, ap, team, false);
		}
		scoreboard.leaving(team,ap);
		team.playerLeft(ap);
		leftPlayers.add(ap.getName());
		ap.removeCompetition(this);
		checkAndHandleIfTeamDead(team);
	}

	protected void checkAndHandleIfTeamDead(ArenaTeam team){
		if (team.isDead()){
			callEvent(new TeamDeathEvent(team));}
	}

	private void privateRemovedFromTeam(ArenaTeam team,ArenaPlayer ap){
		if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" removedFromTeam("+team.getName()+":"+team.getId()+")"+ap.getName());
		HeroesController.removedFromTeam(team, ap.getPlayer());
		scoreboard.removedFromTeam(team,ap);
	}

	private void privateRemoveTeam(ArenaTeam team){
		teams.remove(team);
		HeroesController.removeTeam(team);
		TeamController.removeTeamHandler(team, this);
	}

	/**
	 * Called when a player or team joins the arena
	 * @param p
	 */
	protected void startTracking(final ArenaPlayer p){
		final MatchState ms = MatchState.ONENTER;
		updateBukkitEvents(ms,p);
		if (WorldGuardController.hasWorldGuard() && arena.hasRegion()){
			psc.addMember(p, arena.getWorldGuardRegion());}
		p.reset();
	}

	protected void stopTracking(final ArenaPlayer p){
		final MatchState ms = MatchState.ONLEAVE;
		updateBukkitEvents(ms,p);
		p.reset(); /// reset their isReady status, chosen class, etc.
		if (WorldGuardController.hasWorldGuard() && arena.hasRegion())
			psc.removeMember(p, arena.getWorldGuardRegion());
	}

	/**
	 * Player is entering arena area. Usually called from a teleportIn
	 * @param p
	 */
	private void enterArena(ArenaLocation src, ArenaPlayer p, ArenaTeam team){
		preEnter(p);
		/// If they werent in the wait room then they are entering for the first time
		/// so call the ONENTER, otherwise we have already done all the options
		boolean inwr = insideWaitRoom.remove(p.getName());
		boolean inlobby = src.getType() == LocationType.LOBBY || insideLobby.contains(p.getName());
//		boolean inlobby = insideLobby.remove(p.getName());
		boolean incy = insideCourtyard.remove(p.getName());
		if (!inwr && !inlobby && !incy){
			PerformTransition.transition(this, MatchState.ONENTER, p, team, false);}
		postEnter(p,team);
		arenaInterface.onEnter(p,team);
	}
	/**
	 * Player is entering arena area. Usually called from a teleportIn
	 * @param p
	 */
	private void enterWaitroom(ArenaLocation src, ArenaPlayer p, ArenaTeam team){
		preEnter(p);
		/// If they werent in the wait room then they are entering for the first time
		/// so call the ONENTER, otherwise we have already done all the options
		insideWaitRoom.add(p.getName());
		boolean inlobby = src.getType() == LocationType.LOBBY;
		if (!inlobby){
			PerformTransition.transition(this, MatchState.ONENTER, p, team, false);}
		postEnter(p,team);
		arenaInterface.onEnter(p,team);
	}

	private void enterLobby(ArenaLocation src, ArenaPlayer p, ArenaTeam team){
		insideArena.add(p.getName());
		insideLobby.add(p.getName());
		arenaInterface.onEnter(p,team);
	}

	private void enterCourtyard(ArenaLocation src, ArenaPlayer p, ArenaTeam team){
		insideArena.add(p.getName());
		insideCourtyard.add(p.getName());
		arenaInterface.onEnter(p,team);
	}

	private void preEnter(ArenaPlayer p){
		final String name = p.getName();
		insideArena.add(p.getName());
		startTracking(p);
		if (!params.getUseTrackerPvP())
			StatController.stopTracking(p);
		/// Store the point at which they entered the arena
		if (!oldlocs.containsKey(name) || oldlocs.get(name) == null) /// First teleportIn is the location we want
			oldlocs.put(name, p.getLocation());
	}

	private void postEnter(ArenaPlayer p, ArenaTeam t){
		insideArena.add(p.getName());

		p.setCurLocation(LocationType.ARENA);
		callEvent(new ArenaPlayerEnterMatchEvent(p,t));

		if (cancelExpLoss){
			psc.cancelExpLoss(p,true);}
	}

	/**
	 * Signfies that player has left the arena
	 * This can happen when the player was teleported out, kicked from server,
	 * the player disconnected, or player died and wasnt respawned in arena
	 * @param p: Leaving player
	 */
	private void leaveArena(ArenaPlayer p, ArenaTeam t){
		PerformTransition.transition(this, MatchState.ONLEAVE, p, t, false);
		insideArena.remove(p.getName());
		insideWaitRoom.remove(p.getName());
		insideLobby.remove(p.getName());
		insideCourtyard.remove(p.getName());
		stopTracking(p);
		t.killMember(p);
		scoreboard.setDead(t,p);
		if (!params.getUseTrackerPvP())
			StatController.resumeTracking(p);

		arenaInterface.onLeave(p,t);
		callEvent(new ArenaPlayerLeaveMatchEvent(p,t));
		p.setCurLocation(LocationType.HOME);
		if (cancelExpLoss){
			psc.cancelExpLoss(p,false);}
	}

	public void setMessageHandler(MatchMessageHandler mc){this.mc.setMessageHandler(mc);}
	public MatchMessageHandler getMessageHandler(){return mc.getMessageHandler();}

	private void addVictoryConditions(){
		addedVictoryConditions = true;
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
	 * @param vc
	 */
	public void addVictoryCondition(VictoryCondition vc){
		vcs.add(vc);
		addArenaListener(vc);
		if (vc instanceof DefinesNumTeams){
			neededTeams = Math.max(neededTeams, ((DefinesNumTeams)vc).getNeededNumberOfTeams().max);}
		if (vc instanceof DefinesNumLivesPerPlayer){
			if (nLivesPerPlayer== Integer.MAX_VALUE) nLivesPerPlayer = 1;
			nLivesPerPlayer = Math.max(nLivesPerPlayer, ((DefinesNumLivesPerPlayer)vc).getLivesPerPlayer());}
		if (vc instanceof ScoreTracker){
			((ScoreTracker)vc).setScoreBoard(scoreboard);
		}
		updateBukkitEvents(MatchState.ONOPEN); /// register all match events
		if (!insideArena.isEmpty()){
			updateBukkitEvents(MatchState.ONENTER); /// register all current inside players with the vc
		}
	}

	/**
	 * Remove a victory condition from this match
	 * @param vc
	 */
	public void removeVictoryCondition(VictoryCondition vc){
		vcs.remove(vc);
		removeArenaListener(vc);
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
		if (state == tinState){
			addVictoryConditions();}
		times.put(this.state, System.currentTimeMillis());
	}

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
		return random ? arena.getRandomSpawnLoc(): arena.getSpawnLoc(teams.indexOf(team));
	}
	public Location getTeamSpawn(int index, boolean random){
		return random ? arena.getRandomSpawnLoc(): arena.getSpawnLoc(index);
	}
	public Location getWaitRoomSpawn(ArenaTeam team, boolean random){
		return random ? arena.getRandomWaitRoomSpawnLoc(): arena.getWaitRoomSpawnLoc(teams.indexOf(team));
	}
	public Location getWaitRoomSpawn(int index, boolean random){
		return random ? arena.getRandomWaitRoomSpawnLoc(): arena.getWaitRoomSpawnLoc(index);
	}

//	public Location getLobbySpawn(ArenaTeam team, boolean random){
//		//		MatchParams.
//		return random ? arena.getRandomWaitRoomSpawnLoc(): arena.getWaitRoomSpawnLoc(teams.indexOf(team));
//	}

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
	 * @param team
	 */
	public synchronized void setWinner(final ArenaTeam team){
		setVictor(team);
	}

	/**
	 * Set the victor of this match
	 * @param team
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
	public Set<ArenaTeam> getLosers() {return matchResult.getLosers();}
	public Set<ArenaTeam> getDrawers() {return matchResult.getDrawers();}
	public Map<String,Location> getOldLocations() {return oldlocs;}
	public int indexOf(ArenaTeam t){return teams.indexOf(t);}

	public boolean isHandled(ArenaPlayer player){
		return insideArena.contains(player.getName());
	}

	@Deprecated
	public boolean insideArena(ArenaPlayer p){
		return isHandled(p);
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
		boolean inmatch = insideArena.contains(p.getName());
		final String pname = p.getDisplayName();
		boolean ready = true;
		World w = arena.getSpawnLoc(0).getWorld();
		if (Defaults.DEBUG) System.out.println(p.getName()+"  online=" + online +" isready="+tops.playerReady(p,w));
		if (!online){
			//				Log.warn("[BattleArena] "+p.getName()+" killed for not being online");
			t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for not being online");
			ready = false;
		} else if (p.isDead()){
			//				Log.warn("[BattleArena] "+p.getName()+" killed for not being online");
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
		sb.append(params +"\n");
		sb.append("state=&6"+state +"\n");
		sb.append("pvp=&6"+ (to != null ? to.getPVP(): "on" ) +"\n");
		//		sb.append("playersInMatch=&6"+inMatch.get(p)+"\n");
		sb.append("result=&e("+matchResult +"&e)\n");
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
		for (ArenaTeam t: aliveTeams) sb.append(t.getTeamInfo(insideArena) +"\n");
		for (ArenaTeam t: deadTeams) sb.append(t.getTeamInfo(insideArena) +"\n");
		return sb.toString();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("[Match:"+id+":" + (arena != null ? arena.getName():"none") +" ,");
		for (ArenaTeam t: teams){
			sb.append("["+t.getDisplayName() + "] ,");}
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
		MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams);
		callEvent(event);
		MatchResult result = event.getResult();
		/// No one has an opinion of how this match ends... so declare it a draw
		if (result.isUnknown()){
			result.setDrawers(teams);}
		try{mc.sendTimeExpired();}catch(Exception e){Log.printStackTrace(e);}
		this.endingMatchWinLossOrDraw(result);
	}

	public void intervalTick(int remaining) {
		MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams);
		callEvent(event);
		callEvent(new MatchTimerIntervalEvent(this, remaining));
		try{mc.sendOnIntervalMsg(remaining, event.getCurrentLeaders());}catch(Exception e){Log.printStackTrace(e);}
	}

	public TeamJoinHandler getTeamJoinHandler() {
		return joinHandler;
	}

	public Integer getTeamIndex(ArenaTeam t) {
		return teamIndexes.get(t);
	}

	public boolean hasWaitroom() {
		return waitRoomStates != null;
	}

	public boolean isJoinablePostCreate(){
		return joinHandler != null && (alwaysOpen || tops.hasOptionAt(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN) ||
				(hasWaitroom() && !joinHandler.isFull()));
	}

	public boolean canStillJoin() {
		if (joinHandler == null)
			return false;
		return alwaysOpen || tops.hasOptionAt(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN) ||
				( hasWaitroom() && !joinHandler.isFull() &&
						(isInWaitRoomState() && (joinCutoffTime == null || System.currentTimeMillis() < joinCutoffTime)));
	}

//	public void setReady(ArenaPlayer ap) {
//		if (readyPlayers == null)
//			readyPlayers = new HashSet<ArenaPlayer>();
//		readyPlayers.add(ap);
//		ap.setReady(true);
//	}

	@Override
	public int getID(){
		return id;
	}

	@Override
	public String getName(){
		return params.getName();
	}

	public void setOriginalTeams(Collection<ArenaTeam> originalTeams) {
		this.originalTeams = originalTeams;
	}

	public Collection<ArenaTeam> getOriginalTeams(){
		return originalTeams;
	}

	public Set<String> getInsidePlayers(){
		Set<String> inside = new HashSet<String>(insideArena);
		return inside;
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


	@ArenaEventHandler
	public void onArenaPlayerTeleportEvent(ArenaPlayerTeleportEvent event){
		LocationType loc = event.getDestType();
		TeleportDirection in = event.getDirection();
		if (Defaults.DEBUG_TRANSITIONS)Log.debug(" -- onArenaPlayerTeleportEvent " + getID() +", dest="+loc +"  in=" + in +
				 "  from="+ event.getSrcLocation());
		if (in == null)
			return;
		ArenaPlayer p = event.getPlayer();
		ArenaTeam team = getTeam(p);
		if (team == null){
			Log.err("[BA Error] calling teleport with null team" );
			Util.printStackTrace();
			return;
		}
		if (in == TeleportDirection.IN){
			switch(loc){
			case COURTYARD:
				enterCourtyard(event.getSrcLocation(), p,team);
				break;
			case LOBBY:
				enterLobby(event.getSrcLocation(),p,team);
				break;
			case WAITROOM:
				enterWaitroom(event.getSrcLocation(),p,team);
				break;
			case ARENA:
			case CUSTOM:
				enterArena(event.getSrcLocation(), p,team);
				break;
			default:
			}
		} else {
			switch(loc){
			case HOME:
			case CUSTOM:
				leaveArena(p,team);
				break;
			default:
			}
		}
	}

	@Override
	public Location getSpawn(int index, LocationType type, boolean random) {

		switch(type){
		case ARENA: return this.getTeamSpawn(index, random);
		case WAITROOM: return this.getWaitRoomSpawn(index, random);
		case LOBBY: return LobbyController.getLobbySpawn(index, params.getType(), random);
		default:
			break;
		}
		return null;
	}
	@Override
	public Location getSpawn(ArenaPlayer player, LocationType type, boolean random) {
		switch(type){
		case HOME: return oldlocs.get(player.getName());
		default:
			break;
		}
		return null;
	}

	@Override
	public LocationType getLocationType() {
		return LocationType.ARENA;
	}
}