package mc.alk.arena.competition.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.controllers.FactionsController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.RewardController;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.controllers.TagAPIController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.controllers.WorldGuardController.WorldGuardFlag;
import mc.alk.arena.controllers.messaging.MatchMessageHandler;
import mc.alk.arena.controllers.messaging.MatchMessager;
import mc.alk.arena.events.PlayerLeftEvent;
import mc.alk.arena.events.matches.MatchCancelledEvent;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.matches.MatchOpenEvent;
import mc.alk.arena.events.matches.MatchPrestartEvent;
import mc.alk.arena.events.matches.MatchResultEvent;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.matches.MatchTimerIntervalEvent;
import mc.alk.arena.events.prizes.ArenaDrawersPrizeEvent;
import mc.alk.arena.events.prizes.ArenaLosersPrizeEvent;
import mc.alk.arena.events.prizes.ArenaPrizeEvent;
import mc.alk.arena.events.prizes.ArenaWinnersPrizeEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaControllerInterface;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.queues.TeamQObject;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.victoryconditions.NLives;
import mc.alk.arena.objects.victoryconditions.NoTeamsLeft;
import mc.alk.arena.objects.victoryconditions.OneTeamLeft;
import mc.alk.arena.objects.victoryconditions.TimeLimit;
import mc.alk.arena.objects.victoryconditions.VictoryCondition;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumLivesPerPlayer;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumTeams;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesTimeLimit;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public abstract class Match extends Competition implements Runnable {
	public enum PlayerState{OUTOFMATCH,INMATCH};
	static int count =0;

	final int id = count++;
	final MatchParams params; /// Our parameters for this match
	final Arena arena; /// The arena we are using
	final ArenaControllerInterface arenaInterface; /// Our interface to access arena methods w/o reflection

	Map<Team,Integer> teamIndexes = Collections.synchronizedMap(new HashMap<Team,Integer>());

	Set<String> visitors = new HashSet<String>(); /// Who is watching
	MatchState state = MatchState.NONE;/// State of the match

	/// When did each transition occur
	final Map<MatchState, Long> times = Collections.synchronizedMap(new EnumMap<MatchState,Long>(MatchState.class));
	final List<VictoryCondition> vcs = new ArrayList<VictoryCondition>(); /// Under what conditions does a victory occur
	MatchResult matchResult; /// Results for this match
	Map<String, Location> oldlocs = null; /// Locations where the players came from before entering arena
	final Set<String> insideArena = new HashSet<String>(); /// who is still inside arena area
	final Set<String> insideWaitRoom = new HashSet<String>(); /// who is still inside the wait room
	MatchTransitions tops = null; /// Our match options for this arena match
	final PlayerStoreController psc = new PlayerStoreController(); /// Store items and exp for players if specified

	Set<ArenaPlayer> readyPlayers = null; /// Do we have ready Players
	List<MatchState> waitRoomStates = null; /// which states are inside a waitRoom
	Long joinCutoffTime = null; /// at what point do we cut people off from joining
	Integer currentTimer = null; /// Our current timer
	Collection<Team> originalTeams = null;

	/// These get used enough or are useful enough that i'm making variables even though they can be found in match options
	final boolean needsClearInventory, clearsInventory, clearsInventoryOnDeath;
	final boolean keepsInventory;
	final boolean respawns, noLeave, noEnter;
	final boolean spawnsRandom;
	final boolean woolTeams, armorTeams;
	final boolean alwaysTeamNames;
	final boolean respawnsWithClass;
	final boolean cancelExpLoss;

	int neededTeams; /// How many teams do we need to properly start this match
	final Plugin plugin;
	int nLivesPerPlayer = Integer.MAX_VALUE; /// This will change as victory conditions are added

	Random rand = new Random(); /// Our randomizer
	MatchMessager mc; /// Our message instance
	TeamJoinHandler joinHandler = null;

	@SuppressWarnings("unchecked")
	public Match(Arena arena, MatchParams params) {
		if (Defaults.DEBUG) System.out.println("ArenaMatch::" + params);
		this.plugin = BattleArena.getSelf();
		this.params = params;
		this.arena = arena;
		this.arenaInterface =new ArenaControllerInterface(arena);
		addArenaListener(arena);

		this.mc = new MatchMessager(this);
		this.oldlocs = new HashMap<String,Location>();
		this.tops = params.getTransitionOptions();
		arena.setMatch(this);
		addVictoryConditions();
		noEnter = tops.hasAnyOption(TransitionOption.WGNOENTER);
		if (noEnter && arena.hasRegion())
			WorldGuardController.setFlag(arena.getWorldGuardRegion(), WorldGuardFlag.ENTRY, !noEnter);
		this.noLeave = tops.hasAnyOption(TransitionOption.WGNOLEAVE);
		this.woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS) && params.getMaxTeamSize() >1 ||
				tops.hasAnyOption(TransitionOption.ALWAYSWOOLTEAMS);
		this.armorTeams = tops.hasAnyOption(TransitionOption.ARMORTEAMS);
		boolean needsBlockEvents = tops.hasAnyOption(TransitionOption.BLOCKBREAKON,TransitionOption.BLOCKBREAKOFF,
				TransitionOption.BLOCKPLACEON,TransitionOption.BLOCKPLACEOFF);
		boolean needsDamageEvents = tops.hasAnyOption(TransitionOption.PVPOFF,TransitionOption.PVPON,TransitionOption.INVINCIBLE);
		boolean needsItemDropEvents = tops.hasAnyOption(TransitionOption.ITEMDROPOFF);
		boolean needsPotionEvents = tops.hasAnyOption(TransitionOption.POTIONDAMAGEON);
		MatchState tinState = tops.getMatchState(TransitionOption.TELEPORTIN);
		this.spawnsRandom = tinState != null && tops.hasOptionAt(tinState, TransitionOption.RANDOMSPAWN);
		this.alwaysTeamNames = tops.hasAnyOption(TransitionOption.ALWAYSTEAMNAMES);
		this.cancelExpLoss = tops.hasAnyOption(TransitionOption.NOEXPERIENCELOSS);
		this.matchResult = new MatchResult();
		TransitionOptions mo = tops.getOptions(MatchState.PREREQS);
		this.needsClearInventory = mo != null ? mo.clearInventory() : false;
		mo = tops.getOptions(MatchState.ONCOMPLETE);
		this.clearsInventory = mo != null ? mo.clearInventory(): false;
		mo = tops.getOptions(MatchState.ONDEATH);
		this.keepsInventory = mo != null ? mo.hasOption(TransitionOption.KEEPINVENTORY) : false;
		this.clearsInventoryOnDeath = mo != null ? mo.clearInventory(): false;
		this.respawns = mo != null ? (mo.respawn() || mo.randomRespawn()): false;
		boolean stopsTeleports = tops.hasAnyOption(TransitionOption.NOTELEPORT, TransitionOption.NOWORLDCHANGE);
		mo = tops.getOptions(MatchState.ONSPAWN);
		this.respawnsWithClass = mo != null ? (mo.hasOption(TransitionOption.RESPAWNWITHCLASS)): false;

		final Map<Integer,Location> wr = arena.getWaitRoomSpawnLocs();
		if (wr != null && !wr.isEmpty()){
			waitRoomStates = tops.getMatchStateRange(TransitionOption.TELEPORTWAITROOM, TransitionOption.TELEPORTIN);
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
		if (needsBlockEvents){
			events.add(BlockBreakEvent.class);
			events.add(BlockPlaceEvent.class);
		}
		if (needsItemDropEvents){
			events.add(PlayerDropItemEvent.class);}
		if (stopsTeleports || noEnter){
			events.add(PlayerTeleportEvent.class);}
		if (woolTeams){
			events.add(InventoryClickEvent.class);}
		if (needsPotionEvents){
			events.add(PotionSplashEvent.class);}
		methodController.addSpecificEvents(this, events);

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

	private void preStartMatch() {
		if (state == MatchState.ONCANCEL) return; /// If the match was cancelled, dont proceed
		if (Defaults.DEBUG) System.out.println("ArenaMatch::startMatch()");
		transitionTo(MatchState.ONPRESTART);
		/// If we will teleport them into the arena for the first time, check to see they are ready first
		TransitionOptions ts = tops.getOptions(state);
		if (ts != null && ts.teleportsIn()){
			for (Team t: teams){
				checkReady(t,tops.getOptions(MatchState.PREREQS));	}
		}

		updateBukkitEvents(MatchState.ONPRESTART);
		callEvent(new MatchPrestartEvent(this,teams));

		PerformTransition.transition(this, MatchState.ONPRESTART, teams, true);
		arenaInterface.onPrestart();
		try{mc.sendOnPreStartMsg(teams);}catch(Exception e){e.printStackTrace();}
		/// Schedule the start of the match
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				startMatch();
			}
		}, (long) (params.getSecondsTillMatch() * 20L * Defaults.TICK_MULT));
		if (waitRoomStates != null){
			joinCutoffTime = System.currentTimeMillis() + (params.getSecondsTillMatch()- Defaults.JOIN_CUTOFF_TIME)*1000;}
	}

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
		List<Team> competingTeams = new ArrayList<Team>();
		/// If we will teleport them into the arena for the first time, check to see they are ready first
		TransitionOptions ts = tops.getOptions(state);
		if (ts != null && ts.teleportsIn()){
			for (Team t: teams){
				checkReady(t,tops.getOptions(MatchState.PREREQS));}
		}
		for (Team t: teams){
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
			try{mc.sendOnStartMsg(teams);}catch(Exception e){e.printStackTrace();}
			/// At this point every team and player should be inside.. if they aren't mark them dead
			nCompetingTeams = checkInside(teams);
		}
		checkEnoughTeams(competingTeams, nCompetingTeams, neededTeams);
	}

	private void checkEnoughTeams(List<Team> competingTeams, int nCompetingTeams, int neededTeams) {
		if (nCompetingTeams >= neededTeams){
			return;
		} else if (nCompetingTeams < neededTeams && nCompetingTeams==1){
			Team victor = competingTeams.get(0);
			victor.sendMessage("&4WIN!!!&eThe other team was offline or didnt meet the entry requirements.");
			setVictor(victor);
		} else { /// Seriously, no one showed up?? Well, one of them won regardless, but scold them
			if (teams.isEmpty()){
				this.cancelMatch();
			} else {
				setDraw();
			}
		}
	}

	private int checkInside(List<Team> teams) {
		int alive= 0;
		for (Team t: teams){
			for (ArenaPlayer ap : t.getPlayers()){
				if (!insideArena(ap)){
					t.killMember(ap);}
			}
			if (!t.isDead())
				alive++;
		}
		return alive;
	}

	private synchronized void nonEndingMatchWinLossOrDraw(MatchResult result){
		MatchResultEvent event = new MatchResultEvent(this,result);
		callEvent(event);
		if (event.isCancelled()){
			return;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
				new NonEndingMatchVictory(this,result),2L);
	}

	private synchronized void matchWinLossOrDraw() {
		/// this might be called multiple times as multiple players might meet the victory condition within a small
		/// window of time.  But only let the first one through
		if (state == MatchState.ONVICTORY || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL)
			return;

		MatchResultEvent event = new MatchResultEvent(this,matchResult);
		callEvent(event);
		if (event.isCancelled()){
			return;
		}
		transitionTo(MatchState.ONVICTORY);
		arenaInterface.onVictory(matchResult);
		/// Call the rest after a 2 tick wait to ensure the calling transitionMethods complete before the
		/// victory conditions start rolling in
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new MatchVictory(this),2L);
	}

	class NonEndingMatchVictory implements Runnable{
		final Match am;
		final MatchResult result;
		NonEndingMatchVictory(Match am, MatchResult result){this.am = am; this.result = result;}

		public void run() {
			List<Team> teams = new ArrayList<Team>();
			final Set<Team> victors = result.getVictors();
			final Set<Team> losers = result.getLosers();
			final Set<Team> drawers = result.getDrawers();
			teams.addAll(victors);
			teams.addAll(losers);
			teams.addAll(drawers);
			if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am +"  victors="+ victors + "  " + losers+"  "+drawers +" " + matchResult);
			if (params.isRated()){
				StatController sc = new StatController(params);
				sc.addRecord(victors,losers,drawers,result.getResult());
			}
			if (result.hasVictor()){ /// We have a true winner
				try{mc.sendOnVictoryMsg(victors, losers);}catch(Exception e){e.printStackTrace();}
			} else { /// we have a draw
				try{mc.sendOnDrawMessage(drawers,losers);} catch(Exception e){e.printStackTrace();}
			}

			PerformTransition.transition(am, MatchState.ONVICTORY,teams, true);
			currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
					new NonEndingMatchCompleted(am, result, teams),
					(long) (params.getSecondsToLoot() * 20L * Defaults.TICK_MULT));
		}
	}

	class MatchVictory implements Runnable{
		final Match am;
		MatchVictory(Match am){this.am = am; }

		public void run() {
			final Set<Team> victors = matchResult.getVictors();
			final Set<Team> losers = matchResult.getLosers();
			final Set<Team> drawers = matchResult.getDrawers();
			if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am +"  victors="+ victors + "  " + losers+"  "+drawers +" " + matchResult);
			if (params.isRated()){
				StatController sc = new StatController(params);
				sc.addRecord(victors,losers,drawers,matchResult.getResult());
			}
			if (matchResult.hasVictor()){ /// We have a true winner
				try{mc.sendOnVictoryMsg(victors, losers);}catch(Exception e){e.printStackTrace();}
			} else { /// we have a draw
				try{mc.sendOnDrawMessage(drawers,losers);} catch(Exception e){e.printStackTrace();}
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
		final List<Team> teams;

		NonEndingMatchCompleted(Match am, MatchResult result, List<Team> teams){
			this.am = am;this.result = result;
			this.teams = teams;
		}
		public void run() {
			final Collection<Team> victors = result.getVictors();

			if (Defaults.DEBUG) System.out.println("Match::NonEndingMatchCompleted(): " + victors);
			/// ONCOMPLETE can teleport people out of the arena,
			/// So the order of events is usually
			/// ONCOMPLETE(half of effects) -> ONLEAVE( and all effects) -> ONCOMPLETE( rest of effects)
			PerformTransition.transition(am, MatchState.ONCOMPLETE, teams, true);
			/// Once again, lets delay this final bit so that transitions have time to finish before
			/// Other splisteners get a chance to handle
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
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
		}
	}
	class MatchCompleted implements Runnable{
		final Match am;

		MatchCompleted(Match am){this.am = am;}
		public void run() {
			transitionTo(MatchState.ONCOMPLETE);
			final Collection<Team> victors = am.getVictors();

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

	public void cancelMatch(){
		state = MatchState.ONCANCEL;
		arenaInterface.onCancel();
		for (Team t : teams){
			if (t == null) ///What is going on with teams!??? TODO
				continue;
			PerformTransition.transition(this, MatchState.ONCANCEL,t,true);
		}
		callEvent(new MatchCancelledEvent(this));
		updateBukkitEvents(MatchState.ONCANCEL);
		deconstruct();
	}

	private void nonEndingDeconstruct(List<Team> teams){
		final Match match = this;
		for (Team t: teams){
			TeamController.removeTeamHandler(t, match);
			PerformTransition.transition(this, MatchState.ONFINISH,t,true);
			for (ArenaPlayer p: t.getPlayers()){
				stopTracking(p);
				p.removeCompetition(this);
				insideArena.remove(p.getName());
				insideWaitRoom.remove(p.getName());
				if (joinHandler != null)
					joinHandler.leave(p);
			}
		}
	}

	private void deconstruct(){
		/// Teleport out happens 1 tick after oncancel/oncomplete, we also must wait 1 tick
		final Match match = this;
		callEvent(new MatchFinishedEvent(match));
		updateBukkitEvents(MatchState.ONFINISH);
		for (Team t: teams){
			TeamController.removeTeamHandler(t, match);
			PerformTransition.transition(this, MatchState.ONFINISH,t,true);
			for (ArenaPlayer p: t.getPlayers()){
				stopTracking(p);
				p.removeCompetition(this);
			}
		}
		arenaInterface.onFinish();
		insideArena.clear();
		insideWaitRoom.clear();
		teams.clear();
		methodController.deconstruct();
		//		arenaListeners.clear();
		if (joinHandler != null){
			joinHandler.deconstruct();}
		joinHandler = null;
	}

	@Override
	public void addTeam(Team team){
		if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" addTeam("+team.getName()+":"+team.getId()+")");
		teamIndexes.put(team, teams.size());
		teams.add(team);
		team.reset();/// reset scores, set alive
		TeamController.addTeamHandler(team, this);
		if ( alwaysTeamNames || (!team.hasSetName() && team.getPlayers().size() > Defaults.MAX_TEAM_NAME_APPEND)){
			team.setDisplayName(TeamUtil.createTeamName(indexOf(team)));}
		for (ArenaPlayer p: team.getPlayers()){
			privateAddPlayer(team,p);}
		PerformTransition.transition(this, MatchState.ONJOIN, team, true);
	}

	/** Called during both, addTeam and addedToTeam */
	private void privateAddPlayer(Team team, ArenaPlayer player){
		leftPlayers.remove(player.getName()); /// remove players from the list as they are now joining again
		insideArena.remove(player.getName());
		team.setAlive(player);
		player.reset();
		player.addCompetition(this);
		startTracking(player);
		arenaInterface.onJoin(player,team);
	}

	@Override
	public boolean removeTeam(Team team){
		if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" removeTeam("+team.getName()+":"+team.getId()+")");

		if (teams.contains(team)){
			onLeave(team);
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
	public void addedToTeam(Team team, ArenaPlayer player) {
		if (Defaults.DEBUG_MATCH_TEAMS)
			Log.info(getID()+" addedToTeam("+team.getName()+":"+team.getId()+", " + player.getName()+") inside="+insideArena.contains(player.getName()));

		if (!team.hasSetName() && team.getPlayers().size() > Defaults.MAX_TEAM_NAME_APPEND){
			team.setDisplayName(TeamUtil.createTeamName(indexOf(team)));}
		privateAddPlayer(team,player);
		PerformTransition.transition(this, MatchState.ONJOIN, player,team, true);
	}

	/**
	 * Add to an already existing team
	 * @param p
	 * @param t
	 */
	@Override
	public void addedToTeam(Team team, Collection<ArenaPlayer> players) {
		for (ArenaPlayer ap: players)
			addedToTeam(team,ap);
	}

	@Override
	public void removedFromTeam(Team team, Collection<ArenaPlayer> players) {
		for (ArenaPlayer ap:players)
			privateRemovedFromTeam(ap,team);
	}

	@Override
	public void removedFromTeam(Team team, ArenaPlayer player) {
		privateRemovedFromTeam(player, team);
	}

	public void onJoin(Collection<Team> teams){
		for (Team t: teams){
			onJoin(t);}
	}

	public void onJoin(Team team) {
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
	public void onLeave(Team team) {
		for (ArenaPlayer ap: team.getPlayers()){
			privateOnLeave(ap,team);}
		privateRemoveTeam(team);
	}

	public boolean onLeave(ArenaPlayer p) {
		/// remove them from the match, they don't want to be here
		Team t = getTeam(p);
		if (t==null) /// really? trying to make a player leave who isnt in the match
			return false;
		privateOnLeave(p,t);
		return true;
	}

	private void privateOnLeave(ArenaPlayer ap, Team team){
		if (insideArena(ap)){ /// Only leave if they haven't already left.
			/// The onCancel should teleport them out, and call leaveArena(ap)
			PerformTransition.transition(this, MatchState.ONCANCEL, ap, team, false);
		}
		team.playerLeft(ap);
		leftPlayers.add(ap.getName());
		ap.removeCompetition(this);
	}

	private void privateRemovedFromTeam(ArenaPlayer ap, Team team){
		if (Defaults.DEBUG_MATCH_TEAMS) Log.info(getID()+" removedFromTeam("+team.getName()+":"+team.getId()+")"+ap.getName());
		HeroesController.removedFromTeam(team, ap.getPlayer());
	}

	private void privateRemoveTeam(Team team){
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
	protected void enterWaitRoom(ArenaPlayer p){
		preEnter(p);
		Team t = getTeam(p);
		PerformTransition.transition(this, MatchState.ONENTER, p, t, false);
		insideWaitRoom.add(p.getName());
		postEnter(p,t);
		arenaInterface.onEnterWaitRoom(p,t);
	}

	/**
	 * Player is entering arena area. Usually called from a teleportIn
	 * @param p
	 */
	protected void enterArena(ArenaPlayer p, Team team){
		preEnter(p);
		/// If they werent in the wait room then they are entering for the first time
		/// so call the ONENTER, otherwise we have already done all the options
		if (!insideWaitRoom.remove(p.getName())){
			PerformTransition.transition(this, MatchState.ONENTER, p, team, false);}
		postEnter(p,team);
		arenaInterface.onEnter(p,team);
	}

	private void preEnter(ArenaPlayer p){
		final String name = p.getName();
		if (params.getOverrideBattleTracker())
			StatController.stopTracking(p);
		/// Store the point at which they entered the arena
		if (!oldlocs.containsKey(name) || oldlocs.get(name) == null) /// First teleportIn is the location we want
			oldlocs.put(name, p.getLocation());
	}

	private void postEnter(ArenaPlayer p, Team t){
		insideArena.add(p.getName());
		if (FactionsController.enabled()){
			FactionsController.addPlayer(p.getPlayer());}

		Integer index = null;
		if (woolTeams){
			index = teams.indexOf(t);
			if (index != -1){ /// TODO Really I would like to know how this is -1.  Should I remove the team now?
				TeamUtil.setTeamHead(index, p);}
		}
		if (TagAPIController.enabled()){
			if (index == null) index = teams.indexOf(t);
			if (index != -1) /// Same, how did this get -1
				psc.setNameColor(p,TeamUtil.getTeamChatColor(index));
		}
		if (cancelExpLoss){
			psc.cancelExpLoss(p,true);}
		HeroesController.addedToTeam(t, p.getPlayer());
		if (HeroesController.enabled()){
			HeroesController.enterArena(p.getPlayer());}
	}

	/**
	 * Signfies that player has left the arena
	 * This can happen when the player was teleported out, kicked from server,
	 * the player disconnected, or player died and wasnt respawned in arena
	 * @param p: Leaving player
	 */
	protected void leaveArena(ArenaPlayer p){
		insideArena.remove(p.getName());
		insideWaitRoom.remove(p.getName());
		stopTracking(p);
		Team t = getTeam(p);
		PerformTransition.transition(this, MatchState.ONLEAVE, p, t, false);
		arenaInterface.onLeave(p,t);
		if (params.getOverrideBattleTracker())
			StatController.resumeTracking(p);

		callEvent(new PlayerLeftEvent(p));
		if (FactionsController.enabled()){
			FactionsController.removePlayer(p.getPlayer());}

		if (woolTeams){
			int index = getTeamIndex(t);
			if (index != -1)
				PlayerStoreController.removeItem(p, TeamUtil.getTeamHead(index));
		}
		if (TagAPIController.enabled()){
			psc.removeNameColor(p);}
		if (cancelExpLoss){
			psc.cancelExpLoss(p,false);}
		if (HeroesController.enabled()){
			HeroesController.leaveArena(p.getPlayer());}

	}

	public void setMessageHandler(MatchMessageHandler mc){this.mc.setMessageHandler(mc);}
	public MatchMessageHandler getMessageHandler(){return mc.getMessageHandler();}

	private void addVictoryConditions(){
		VictoryCondition vt = VictoryType.createVictoryCondition(this);
		/// Add a time limit unless one is provided by default
		if (!(vt instanceof DefinesTimeLimit) && params.getMatchTime() > 0){
			addVictoryCondition(new TimeLimit(this));
		}

		/// set the number of lives
		Integer nLives = params.getNLives();
		if (nLives != null && nLives > 0 && !hasVictoryCondition(NLives.class)){
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

	private boolean hasVictoryCondition(Class<NLives> clazz) {
		for (VictoryCondition vc: vcs){
			if (vc.getClass() == clazz)
				return true;
		}
		return false;
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

	public boolean isFinished() {return state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL;}
	public boolean isWon() {return state == MatchState.ONVICTORY || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL;}
	public boolean isStarted() {return state == MatchState.ONSTART;}
	public boolean isInWaitRoomState() {return waitRoomStates != null && waitRoomStates.contains(state);}

	@Override
	public MatchState getState() {return state;}

	@Override
	protected void transitionTo(CompetitionState state){
		this.state = (MatchState) state;
		times.put(this.state, System.currentTimeMillis());
	}

	@Override
	public Long getTime(CompetitionState state){
		return times.get(state);
	}

	@Override
	public MatchParams getParams() {return params;}

	@Override
	public List<Team> getTeams() {return teams;}
	public List<Team> getAliveTeams() {
		List<Team> alive = new ArrayList<Team>();
		for (Team t: teams){
			if (t.isDead())
				continue;
			alive.add(t);
		}
		return alive;
	}

	public Set<ArenaPlayer> getAlivePlayers() {
		HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		for (Team t: teams){
			if (t.isDead())
				continue;
			players.addAll(t.getLivingPlayers());
		}
		return players;
	}

	public Location getTeamSpawn(Team t, boolean random){
		return random ? arena.getRandomSpawnLoc(): arena.getSpawnLoc(teams.indexOf(t));
	}
	public Location getTeamSpawn(int index, boolean random){
		return random ? arena.getRandomSpawnLoc(): arena.getSpawnLoc(index);
	}
	public Location getWaitRoomSpawn(int index, boolean random){
		return random ? arena.getRandomWaitRoomSpawnLoc(): arena.getWaitRoomSpawnLoc(index);
	}
	public void setMatchResult(MatchResult result){
		this.matchResult = result;
	}

	public void endMatchWithResult(MatchResult result){
		this.matchResult = result;
		matchWinLossOrDraw();
	}

	public void setVictor(ArenaPlayer p){
		Team t = getTeam(p);
		if (t != null) setVictor(t);
	}

	/**
	 * Alias for setVictor
	 * @param team
	 */
	public synchronized void setWinner(final Team team){
		setVictor(team);
	}

	/**
	 * Set the victor of this match
	 * @param team
	 */
	public synchronized void setVictor(final Team team){
		setVictor(new ArrayList<Team>(Arrays.asList(team)));
	}

	public synchronized void setVictor(final Collection<Team> winningTeams){
		matchResult.setVictors(winningTeams);
		ArrayList<Team> losers= new ArrayList<Team>(teams);
		losers.removeAll(winningTeams);
		matchResult.addLosers(losers);
		matchResult.setResult(WinLossDraw.WIN);
		endMatchWithResult(matchResult);
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


	public synchronized void setNonEndingVictor(final Team team){
		MatchResult result = new MatchResult();
		result.setVictor(team);
		nonEndingMatchWinLossOrDraw(result);
	}

	public MatchResult getResult(){return matchResult;}
	public Set<Team> getVictors() {return matchResult.getVictors();}
	public Set<Team> getLosers() {return matchResult.getLosers();}
	public Set<Team> getDrawers() {return matchResult.getDrawers();}
	public Map<String,Location> getOldLocations() {return oldlocs;}
	public int indexOf(Team t){return teams.indexOf(t);}

	public boolean insideArena(ArenaPlayer p){
		return insideArena.contains(p.getName());
	}

	protected Set<ArenaPlayer> checkReady(final Team t, TransitionOptions mo) {
		Set<ArenaPlayer> alive = new HashSet<ArenaPlayer>();
		for (ArenaPlayer p : t.getPlayers()){
			if (checkReady(p,t,mo,true)){
				alive.add(p);}
		}
		return alive;
	}

	protected boolean checkReady(ArenaPlayer p, final Team t, TransitionOptions mo, boolean announce) {
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
		for (Team t: teams){
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
		List<Team> deadTeams = new ArrayList<Team>();
		List<Team> aliveTeams = new ArrayList<Team>();
		for (Team t: teams){
			if (t.isDead())
				deadTeams.add(t);
			else
				aliveTeams.add(t);
		}
		for (Team t: aliveTeams) sb.append(t.getTeamInfo(insideArena) +"\n");
		for (Team t: deadTeams) sb.append(t.getTeamInfo(insideArena) +"\n");
		return sb.toString();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("[Match:"+id+":" + (arena != null ? arena.getName():"none") +" ,");
		for (Team t: teams){
			sb.append("["+t.getDisplayName() + "] ,");}
		sb.append("]");
		return sb.toString();
	}

	public boolean hasTeam(Team team) {
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
		try{mc.sendTimeExpired();}catch(Exception e){e.printStackTrace();}
		endMatchWithResult(result);
	}

	public void intervalTick(int remaining) {
		MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams);
		callEvent(event);
		callEvent(new MatchTimerIntervalEvent(this, remaining));
		try{mc.sendOnIntervalMsg(remaining, event.getCurrentLeaders());}catch(Exception e){e.printStackTrace();}
	}

	public TeamJoinHandler getTeamJoinHandler() {
		return joinHandler;
	}

	public Integer getTeamIndex(Team t) {
		return teamIndexes.get(t);
	}

	public boolean hasWaitroom() {
		return waitRoomStates != null;
	}
	public boolean canStillJoin() {
		return joinHandler != null &&
				(tops.hasOptionAt(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN) ||
				((hasWaitroom() && !joinHandler.isFull()) &&
				(isInWaitRoomState() && (joinCutoffTime == null || System.currentTimeMillis() < joinCutoffTime)) ));
	}

	public void setReady(ArenaPlayer ap) {
		if (readyPlayers == null)
			readyPlayers = new HashSet<ArenaPlayer>();
		readyPlayers.add(ap);
		ap.setReady(true);
	}

	@Override
	public int getID(){
		return id;
	}

	@Override
	public String getName(){
		return params.getName();
	}

	public void setOriginalTeams(Collection<Team> originalTeams) {
		this.originalTeams = originalTeams;
	}

	public Collection<Team> getOriginalTeams(){
		return originalTeams;
	}

	public Set<String> getInsidePlayers(){
		Set<String> inside = new HashSet<String>(insideArena);
		return inside;
	}


	public boolean allTeamsReady() {
		for (Team t: teams){
			if (!t.isReady())
				return false;
		}
		return true;
	}
}