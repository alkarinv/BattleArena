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
import mc.alk.arena.controllers.HeroesInterface;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.TagAPIInterface;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.WorldGuardInterface;
import mc.alk.arena.controllers.WorldGuardInterface.WorldGuardFlag;
import mc.alk.arena.controllers.messaging.MatchMessageHandler;
import mc.alk.arena.controllers.messaging.MatchMessager;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.PlayerLeftEvent;
import mc.alk.arena.events.matches.MatchCancelledEvent;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.matches.MatchFindNeededTeamsEvent;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.matches.MatchOpenEvent;
import mc.alk.arena.events.matches.MatchPrestartEvent;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.matches.MatchTimerIntervalEvent;
import mc.alk.arena.events.matches.MatchVictoryEvent;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.TransitionListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaInterface;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.options.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.queues.TeamQObject;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.objects.victoryconditions.TimeLimit;
import mc.alk.arena.objects.victoryconditions.VictoryCondition;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.WLT;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;


public abstract class Match extends Competition implements Runnable, ArenaListener, TeamHandler {
	public enum PlayerState{OUTOFMATCH,INMATCH};
	static int count =0;

	final int id = count++;
	final MatchParams params; /// Our parameters for this match
	final Arena arena; /// The arena we are using
	final ArenaInterface arenaInterface; /// Our interface to access arena methods w/o reflection

	HashMap<Team,Integer> teamIndexes = new HashMap<Team,Integer>();

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
	List<ArenaListener> arenaListeners = new ArrayList<ArenaListener>();
	Set<ArenaPlayer> readyPlayers = null; /// Do we have ready Players
	List<MatchState> waitRoomStates = null; /// which states are inside a waitRoom
	Long joinCutoffTime = null; /// at what point do we cut people off from joining
	Integer currentTimer = null; /// Our current timer
	Collection<Team> originalTeams = null;

	/// These get used enough or are useful enough that i'm making variables even though they can be found in match options
	final boolean needsClearInventory, clearsInventory, clearsInventoryOnDeath;
	final boolean respawns, noLeave;
	boolean woolTeams = false;
	final boolean alwaysTeamNames;
	final boolean respawnsWithClass;
	boolean needsMobDeaths = false, needsBlockEvents = false;
	boolean needsItemPickups = false, needsInventoryClick = false;
	final boolean needsItemDropEvents;
	final boolean stopsTeleports;
	boolean needsDamageEvents = false;
	final Plugin plugin;

	Random rand = new Random(); /// Our randomizer
	MatchMessager mc; /// Our message instance
	TeamJoinHandler joinHandler = null;

	public Match(Arena arena, MatchParams params) {
		if (Defaults.DEBUG) System.out.println("ArenaMatch::" + params);
		plugin = BattleArena.getSelf();
		this.params = params;
		this.arena = arena;
		arenaInterface =new ArenaInterface(arena);
		arenaListeners.add(arena);

		this.mc = new MatchMessager(this);
		this.oldlocs = new HashMap<String,Location>();
		this.tops = params.getTransitionOptions();
		arena.setMatch(this);
		VictoryCondition vt = VictoryType.createVictoryCondition(this);
		if (!(vt instanceof TimeLimit))
			addVictoryCondition(new TimeLimit(this));
		addVictoryCondition(vt);
		boolean noEnter = tops.hasAnyOption(TransitionOption.WGNOENTER);
		if (noEnter && arena.hasRegion())
			WorldGuardInterface.setFlag(arena.getRegionWorld(), arena.getRegion(), WorldGuardFlag.ENTRY, !noEnter);
		this.noLeave = tops.hasAnyOption(TransitionOption.WGNOLEAVE);
		this.woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS) && params.getMaxTeamSize() >1 ||
				tops.hasAnyOption(TransitionOption.ALWAYSWOOLTEAMS);
		this.needsBlockEvents = tops.hasAnyOption(TransitionOption.BLOCKBREAKON,TransitionOption.BLOCKBREAKOFF,
				TransitionOption.BLOCKPLACEON,TransitionOption.BLOCKPLACEOFF);
		this.needsDamageEvents = tops.hasAnyOption(TransitionOption.PVPOFF,TransitionOption.PVPON,TransitionOption.INVINCIBLE);
		this.needsItemDropEvents = tops.hasAnyOption(TransitionOption.DROPITEMOFF);
		this.alwaysTeamNames = tops.hasAnyOption(TransitionOption.ALWAYSTEAMNAMES);
		this.matchResult = new MatchResult();
		TransitionOptions mo = tops.getOptions(MatchState.PREREQS);
		this.needsClearInventory = mo != null ? mo.clearInventory() : false;
		mo = tops.getOptions(MatchState.ONCOMPLETE);
		this.clearsInventory = mo != null ? mo.clearInventory(): false;
		mo = tops.getOptions(MatchState.ONDEATH);
		this.clearsInventoryOnDeath = mo != null ? mo.clearInventory(): false;
		this.respawns = mo != null ? (mo.respawn() || mo.randomRespawn()): false;
		this.stopsTeleports = tops.hasAnyOption(TransitionOption.NOTELEPORT, TransitionOption.NOWORLDCHANGE);
		mo = tops.getOptions(MatchState.ONSPAWN);
		this.respawnsWithClass = mo != null ? (mo.hasOption(TransitionOption.RESPAWNWITHCLASS)): false;

		final Map<Integer,Location> wr = arena.getWaitRoomSpawnLocs();
		if (wr != null && !wr.isEmpty()){
			waitRoomStates = tops.getMatchStateRange(TransitionOption.TELEPORTWAITROOM, TransitionOption.TELEPORTIN);
			if (waitRoomStates.isEmpty()){
				waitRoomStates = null;}
		}

		/// Try and make a joinhandler out of our current params, but we don't care if it's null
		//		try {joinHandler = TeamJoinFactory.createTeamJoinHandler(params,this);} catch (NeverWouldJoinException e) {}
	}

	private void updateBukkitEvents(MatchState matchState){
		for (ArenaListener al : arenaListeners){
			MethodController.updateMatchBukkitEvents(al, matchState, new ArrayList<String>(insideArena));
		}
	}

	private void updateBukkitEvents(MatchState matchState,ArenaPlayer player){
		for (ArenaListener al : arenaListeners){
			MethodController.updateAllEventListeners(al, matchState, player);
		}
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
				tmc.callListeners(event); /// Call our listeners listening to only this match
				if (event.isCancelled()){
					match.cancelMatch();
					return;
				}
				event.callEvent(); /// Call bukkit listeners for this event
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

	public void addTransitionListener(TransitionListener transitionListener){
		tmc.addListener(transitionListener);
	}
	public void addTransitionListeners(Collection<TransitionListener> transitionListeners){
		for (TransitionListener tl: transitionListeners){
			tmc.addListener(tl);}
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
				checkReady(t,tops.getOptions(MatchState.PREREQS));				}
		}

		notifyListeners(new MatchPrestartEvent(this,teams));

		updateBukkitEvents(MatchState.ONPRESTART);
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
				checkReady(t,tops.getOptions(MatchState.PREREQS));				}
		}
		for (Team t: teams){
			if (!t.isDead()){
				competingTeams.add(t);}
		}
		final int nCompetingTeams = competingTeams.size();

		MatchFindNeededTeamsEvent findevent = new MatchFindNeededTeamsEvent(this);
		notifyListeners(findevent);
		int neededTeams = findevent.getNeededTeams();
		if (Defaults.DEBUG) Log.info("[BattleArena] competing teams = " + competingTeams +":"+neededTeams+"   allteams=" + teams);

		if (nCompetingTeams >= neededTeams){
			MatchStartEvent event= new MatchStartEvent(this,teams);
			notifyListeners(event);
			updateBukkitEvents(MatchState.ONSTART);
			PerformTransition.transition(this, state,competingTeams, true);
			arenaInterface.onStart();
			try{mc.sendOnStartMsg(teams);}catch(Exception e){e.printStackTrace();}
		} else if (nCompetingTeams==1){
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

	private synchronized void teamVictory() {
		/// this might be called multiple times as multiple players might meet the victory condition within a small
		/// window of time.  But only let the first one through
		if (state == MatchState.ONVICTORY || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL)
			return;
		transitionTo(MatchState.ONVICTORY);
		/// Call the rest after a 2 tick wait to ensure the calling transitionMethods complete before the
		/// victory conditions start rolling in
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new MatchVictory(this),2L);
	}

	class MatchVictory implements Runnable{
		final Match am;
		MatchVictory(Match am){this.am = am; }

		public void run() {
			final Set<Team> victors = matchResult.getVictors();
			final Set<Team> losers = matchResult.getLosers();
			final Set<Team> drawers = matchResult.getDrawers();
			if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am +"  victors="+ victors + "  " + losers);
			if (matchResult.hasVictor()){ /// We have a true winner
				TrackerInterface bti = BTInterface.getInterface(params);
				if (bti != null && params.isRated()){
					try{BTInterface.addRecord(bti,victors,losers,drawers,WLT.WIN);}catch(Exception e){e.printStackTrace();}
				}
				try{mc.sendOnVictoryMsg(victors, losers);}catch(Exception e){e.printStackTrace();}
			} else { /// we have a draw
				try{mc.sendOnDrawMessage(drawers,losers);} catch(Exception e){e.printStackTrace();}
			}
			updateBukkitEvents(MatchState.ONVICTORY);
			notifyListeners(new MatchVictoryEvent(am,matchResult));
			PerformTransition.transition(am, MatchState.ONVICTORY,teams, true);
			arenaInterface.onVictory(getResult());
			currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
					new MatchCompleted(am), (long) (params.getSecondsToLoot() * 20L * Defaults.TICK_MULT));
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
					/// Losers and winners get handled after the match is complete
					if (am.getLosers() != null)
						PerformTransition.transition(am, MatchState.LOSERS, am.getLosers(), false);
					if (am.getDrawers() != null)
						PerformTransition.transition(am, MatchState.LOSERS, am.getDrawers(), false);
					if (am.getVictors() != null)
						PerformTransition.transition(am, MatchState.WINNER, am.getVictors(), false);
					arenaInterface.onComplete();
					notifyListeners(new MatchCompletedEvent(am));
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
			PerformTransition.transition(this, MatchState.ONCANCEL,t,true);
		}
		updateBukkitEvents(MatchState.ONCANCEL);
		notifyListeners(new MatchCancelledEvent(this));
		deconstruct();
	}

	private void deconstruct(){
		/// Teleport out happens 1 tick after oncancel/oncomplete, we also must wait 1 tick
		final Match match = this;
		updateBukkitEvents(MatchState.ONFINISH);
		notifyListeners(new MatchFinishedEvent(match));
		for (Team t: teams){
			TeamController.removeTeamHandler(t, match);
			PerformTransition.transition(this, MatchState.ONFINISH,t,true);
			for (ArenaPlayer p: t.getPlayers()){
				stopTracking(p);
			}
		}
		arenaInterface.onFinish();
		insideArena.clear();
		insideWaitRoom.clear();
		teams.clear();
		arenaListeners.clear();
		if (joinHandler != null){
			joinHandler.deconstruct();}
		joinHandler = null;
	}

	/**
	 * Notify Bukkit Listeners and specific listeners to this match
	 * @param BAevent event
	 */
	protected void notifyListeners(BAEvent event) {
		tmc.callListeners(event); /// Call our listeners listening to only this match
		event.callEvent(); /// Call bukkit listeners for this event
	}

	@Override
	public void addTeam(Team team){
		teams.add(team);
		teamIndexes.put(team, teams.size());
		startTracking(team);
		team.setAlive();
		team.resetScores();/// reset scores
		TeamController.addTeamHandler(team, this);
		for (ArenaPlayer p: team.getPlayers()){
			arenaInterface.onJoin(p,team);}
		if ( alwaysTeamNames || (!team.hasSetName() && team.getPlayers().size() > Defaults.MAX_TEAM_NAME_APPEND)){
			team.setDisplayName(TeamUtil.createTeamName(indexOf(team)));}
		PerformTransition.transition(this, MatchState.ONJOIN, team, true);
		HeroesInterface.createTeam(team);
	}

	@Override
	public boolean removeTeam(Team team){
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
		if (!team.hasSetName() && team.getPlayers().size() > Defaults.MAX_TEAM_NAME_APPEND){
			team.setDisplayName(TeamUtil.createTeamName(indexOf(team)));
		}
		team.setAlive(player);
		startTracking(player);
		arenaInterface.onJoin(player,team);
		HeroesInterface.addedToTeam(team, player.getPlayer());
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
			removedFromTeam(team,ap);
	}

	@Override
	public void removedFromTeam(Team team, ArenaPlayer player) {
		for (ArenaPlayer p: team.getPlayers()){
			arenaInterface.onLeave(p,team);
			HeroesInterface.removedFromTeam(team, player.getPlayer());
		}
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
		return true;
	}

	/**
	 *
	 * @param team
	 */
	public void onLeave(Team team) {
		for (ArenaPlayer ap: team.getPlayers()){
			onLeave(ap,team);
		}
		privateRemoveTeam(team);
	}

	public void onLeave(ArenaPlayer p) {
		/// remove them from the match, they don't want to be here
		Team t = getTeam(p);
		onLeave(p,t);
	}

	private void onLeave(ArenaPlayer ap, Team team){
		team.killMember(ap);
		team.playerLeft(ap);
		leftPlayers.add(ap.getName());
		if (insideArena(ap)){ /// Only leave if they haven't already left.
			/// The onCancel should teleport them out, and call leaveArena(ap)
			PerformTransition.transition(this, MatchState.ONCANCEL, ap, team, false);
		}
		if (team.size()==1){
			privateRemoveTeam(team);}
	}

	private void privateRemoveTeam(Team team){
		teams.remove(team);
		HeroesInterface.removeTeam(team);
		TeamController.removeTeamHandler(team, this);
	}
	/**
	 * Called when a team joins the arena
	 * @param team
	 */
	protected void startTracking(final Team team){
		for (ArenaPlayer p: team.getPlayers()){
			startTracking(p);}
	}

	/**
	 * Called when a player or team joins the arena
	 * @param p
	 */
	@SuppressWarnings("unchecked")
	protected void startTracking(final ArenaPlayer p){
		final MatchState ms = MatchState.ONENTER;
		MethodController.updateEventListeners(this, ms,p,PlayerQuitEvent.class,PlayerRespawnEvent.class);
		MethodController.updateEventListeners(this, ms,p, PlayerCommandPreprocessEvent.class);
		MethodController.updateEventListeners(this,ms, p,PlayerDeathEvent.class);
		MethodController.updateEventListeners(this,ms, p,PlayerInteractEvent.class); /// for sign clicks
		if (needsDamageEvents){
			MethodController.updateEventListeners(this,ms, p,EntityDamageEvent.class);}
		if (WorldGuardInterface.hasWorldGuard() && arena.getRegion() != null){
			psc.addMember(p, arena.getRegionWorld(),arena.getRegion());}
		if (noLeave){
			MethodController.updateEventListeners(this,ms, p,PlayerMoveEvent.class);}
		if (needsBlockEvents){
			MethodController.updateEventListeners(this,ms, p,BlockBreakEvent.class, BlockPlaceEvent.class);}
		if (needsItemDropEvents){
			MethodController.updateEventListeners(this,ms, p,PlayerDropItemEvent.class);}
		if (stopsTeleports){
			MethodController.updateEventListeners(this,ms, p,PlayerTeleportEvent.class);
		}
		updateBukkitEvents(ms,p);
		p.setChosenClass(null);
	}

	@SuppressWarnings("unchecked")
	protected void stopTracking(final ArenaPlayer p){
		final MatchState ms = MatchState.ONLEAVE;
		MethodController.updateEventListeners(this,ms, p,PlayerQuitEvent.class,PlayerRespawnEvent.class);
		MethodController.updateEventListeners(this, ms,p, PlayerCommandPreprocessEvent.class);
		MethodController.updateEventListeners(this,ms, p,PlayerDeathEvent.class);
		MethodController.updateEventListeners(this,ms, p,PlayerInteractEvent.class); /// for sign clicks
		if (WorldGuardInterface.hasWorldGuard() && arena.getRegion() != null)
			psc.removeMember(p, arena.getRegionWorld(), arena.getRegion());

		if (needsDamageEvents){
			MethodController.updateEventListeners(this,ms, p,EntityDamageEvent.class);}
		if (noLeave){
			MethodController.updateEventListeners(this,ms, p,PlayerMoveEvent.class);}
		if (needsBlockEvents){
			MethodController.updateEventListeners(this,ms, p,BlockBreakEvent.class, BlockPlaceEvent.class);}
		if (needsItemDropEvents){
			MethodController.updateEventListeners(this,ms, p,PlayerDropItemEvent.class);}
		if (woolTeams || needsInventoryClick){
			MethodController.updateEventListeners(this,ms, p,InventoryClickEvent.class);}
		if (stopsTeleports){
			MethodController.updateEventListeners(this,ms, p,PlayerTeleportEvent.class);
		}
		p.setChosenClass(null);
		BTInterface.resumeTracking(p);
		updateBukkitEvents(ms,p);
	}

	public void setNeedsItemPickupEvents(boolean b) {
		if (b != needsItemPickups){
			this.needsItemPickups = b;}
	}

	/**
	 * Player is entering arena area. Usually called from a teleportIn
	 * @param p
	 */
	protected void enterWaitRoom(ArenaPlayer p){
		preEnter(p);
		Team t = getTeam(p);
		PerformTransition.transition(this, MatchState.ONENTERWAITROOM, p, t, false);
		insideWaitRoom.add(p.getName());
		postEnter(p,t);
		arenaInterface.onEnterWaitRoom(p,t);
	}

	/**
	 * Player is entering arena area. Usually called from a teleportIn
	 * @param p
	 */
	protected void enterArena(ArenaPlayer p){
		preEnter(p);
		Team t = getTeam(p);
		PerformTransition.transition(this, MatchState.ONENTER, p, t, false);
		insideWaitRoom.remove(p.getName());
		postEnter(p,t);
		arenaInterface.onEnter(p,t);
	}

	private void preEnter(ArenaPlayer p){
		final String name = p.getName();
		BTInterface.stopTracking(p);
		/// Store the point at which they entered the arena
		if (!oldlocs.containsKey(name) || oldlocs.get(name) == null) /// First teleportIn is the location we want
			oldlocs.put(name, p.getLocation());
	}

	@SuppressWarnings("unchecked")
	private void postEnter(ArenaPlayer p, Team t){
		insideArena.add(p.getName());
		Integer index = null;
		if (woolTeams){
			index = teams.indexOf(t);
			MethodController.updateEventListeners(this, MatchState.ONENTER, p,InventoryClickEvent.class);
			TeamUtil.setTeamHead(index, t);
		}
		if (TagAPIInterface.enabled()){
			if (index == null) index = teams.indexOf(t);
			psc.setNameColor(p,TeamUtil.getTeamColor(index));
		}
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
		notifyListeners(new PlayerLeftEvent(p));
		if (woolTeams)
			PlayerStoreController.removeItem(p, TeamUtil.getTeamHead(getTeamIndex(t)));
		if (TagAPIInterface.enabled()){
			psc.removeNameColor(p);
		}
	}

	public Team getTeam(ArenaPlayer p) {
		for (Team t: teams) {
			if (t.hasMember(p)) return t;}
		return null;
	}

	public boolean hasPlayer(ArenaPlayer p) {
		for (Team t: teams) {
			if (t.hasMember(p)) return true;}
		return false;
	}

	public Set<ArenaPlayer> getPlayers() {
		HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		for (Team t: teams){
			players.addAll(t.getPlayers());
		}
		return players;
	}

	public boolean hasAlivePlayer(ArenaPlayer p) {
		for (Team t: teams) {
			if (t.hasAliveMember(p)) return true;}
		return false;
	}
	public void setMessageHandler(MatchMessageHandler mc){this.mc.setMessageHandler(mc);}
	public MatchMessageHandler getMessageHandler(){return mc.getMessageHandler();}

	public void addVictoryCondition(VictoryCondition vc){
		vcs.add(vc);
		addArenaListener(vc);
		tmc.addListener(vc);
	}
	public void removeVictoryCondition(VictoryCondition vc){
		vcs.remove(vc);
		tmc.removeListener(vc);
		arenaListeners.remove(vc);
	}

	public void addArenaListener(ArenaListener al){
		arenaListeners.add(al);
	}
	//	public VictoryCondition getVictoryCondition(){return vc;}
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
	public void endMatchWithResult(MatchResult result){
		this.matchResult = result;
		teamVictory();
	}

	public void setVictor(ArenaPlayer p){
		Team t = getTeam(p);
		if (t != null) setVictor(t);
	}

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
	public MatchResult getResult(){return matchResult;}
	public Set<Team> getVictors() {return matchResult.getVictors();}
	public Set<Team> getLosers() {return matchResult.getLosers();}
	public Set<Team> getDrawers() {return matchResult.getDrawers();}
	public Map<String,Location> getOldLocations() {return oldlocs;}
	public int indexOf(Team t){return teams.indexOf(t);}
	/// For debugging events
	public void addKill(ArenaPlayer player) {
		Team t = getTeam(player);
		t.addKill(player);
	}

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
		StringBuilder sb = new StringBuilder("[Match:"+id+":" + arena.getName() +" ,");
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
		notifyListeners(event);
		try{mc.sendTimeExpired();}catch(Exception e){e.printStackTrace();}
		List<Team> leaders = event.getCurrentLeaders();
		if (leaders != null)
			setVictor(leaders);
		else
			setDraw();
	}

	public void intervalTick(int remaining) {
		MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams);
		notifyListeners(event);
		try{mc.sendOnIntervalMsg(remaining, event.getCurrentLeaders());}catch(Exception e){e.printStackTrace();}
		notifyListeners(new MatchTimerIntervalEvent(this, remaining));
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
		return isInWaitRoomState() && (joinCutoffTime == null || System.currentTimeMillis() < joinCutoffTime);
	}

	public void setReady(ArenaPlayer ap) {
		if (readyPlayers == null)
			readyPlayers = new HashSet<ArenaPlayer>();
		readyPlayers.add(ap);
	}

	@Override
	public int getID(){
		return id;
	}

	public void setOriginalTeams(Collection<Team> originalTeams) {
		this.originalTeams = originalTeams;
	}
	public Collection<Team> getOriginalTeams(){
		return originalTeams;
	}
}