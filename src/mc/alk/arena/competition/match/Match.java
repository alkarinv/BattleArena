package mc.alk.arena.competition.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.TransitionMethodController;
import mc.alk.arena.controllers.WorldGuardInterface;
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
import mc.alk.arena.events.matches.MatchVictoryEvent;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.listeners.TransitionListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.TransitionOptions;
import mc.alk.arena.objects.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaInterface;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;


public abstract class Match implements Runnable, ArenaListener, TeamHandler {
	public enum PlayerState{OUTOFMATCH,INMATCH};
	static int count =0;

	final int id = count++;
	final MatchParams mp; /// Our parameters for this match
	final Arena arena; /// The arena we are using
	final ArenaInterface arenaInterface; /// Our interface to access arena methods w/o reflection

	final TransitionMethodController tmc = new TransitionMethodController();

	List<Team> teams = new ArrayList<Team>(); /// Our players
	HashMap<Team,Integer> teamIndexes = new HashMap<Team,Integer>();

	Set<String> visitors = new HashSet<String>(); /// Who is watching
	MatchState state = MatchState.NONE;/// State of the match
	List<VictoryCondition> vcs = new ArrayList<VictoryCondition>(); /// Under what conditions does a victory occur
	MatchResult matchResult; /// Results for this match
	Map<String, Location> oldlocs = null; /// Locations where the players came from before entering arena
	Set<String> insideArena = new HashSet<String>(); /// who is still inside arena area
	Set<String> insideWaitRoom = new HashSet<String>(); /// who is still inside the wait room
	MatchTransitions tops = null; /// Our match options for this arena match
	PlayerStoreController psc = new PlayerStoreController(); /// Store items and exp for players if specified
	List<ArenaListener> arenaListeners = new ArrayList<ArenaListener>();

	/// These get used enough or are useful enough that i'm making variables even though they can be found in match options
	final boolean needsClearInventory, clearsInventory, clearsInventoryOnDeath; 
	final boolean respawns, noLeave, noEnter;
	boolean woolTeams = false;
	boolean needsMobDeaths = false, needsBlockEvents = false;
	boolean needsItemPickups = false, needsInventoryClick = false;
	boolean needsDamageEvents = false;
	final Plugin plugin;

	Random rand = new Random(); /// Our randomizer
	MatchMessager mc; /// Our message instance

	public Match(Arena arena, MatchParams mp) {
		if (Defaults.DEBUG) System.out.println("ArenaMatch::" + mp);
		plugin = BattleArena.getSelf();
		this.mp = mp;	
		this.arena = arena;
		arenaInterface =new ArenaInterface(arena);
		arenaListeners.add(arena);

		this.mc = new MatchMessager(this);
		this.oldlocs = new HashMap<String,Location>();
		this.teams = new ArrayList<Team>();
		this.tops = mp.getTransitionOptions();
		arena.setMatch(this);
		VictoryCondition vt = VictoryType.createVictoryCondition(this);
		if (!(vt instanceof TimeLimit))
			addVictoryCondition(new TimeLimit(this));
		addVictoryCondition(vt);
		this.noEnter = tops.hasOptions(TransitionOption.WGNOENTER);
		this.noLeave = tops.hasOptions(TransitionOption.WGNOLEAVE);
		this.woolTeams = tops.hasOptions(TransitionOption.WOOLTEAMS) && mp.getMaxTeamSize() >1;
		this.needsBlockEvents = tops.hasOptions(TransitionOption.BLOCKBREAKON,TransitionOption.BLOCKBREAKOFF,
				TransitionOption.BLOCKPLACEON,TransitionOption.BLOCKPLACEOFF);
		this.needsDamageEvents = tops.hasOptions(TransitionOption.PVPOFF,TransitionOption.PVPON,TransitionOption.INVINCIBLE);

		this.matchResult = new MatchResult();
		TransitionOptions mo = tops.getOptions(MatchState.PREREQS);
		this.needsClearInventory = mo != null ? mo.clearInventory() : false;
		mo = tops.getOptions(MatchState.ONCOMPLETE);
		this.clearsInventory = mo != null ? mo.clearInventory(): false;
		mo = tops.getOptions(MatchState.ONDEATH);
		this.clearsInventoryOnDeath = mo != null ? mo.clearInventory(): false;
		this.respawns = mo != null ? (mo.respawn() || mo.randomRespawn()): false;
	}

	private void updateBukkitEvents(MatchState matchState){
		for (ArenaListener al : arenaListeners){
			MethodController.updateMatchBukkitEvents(al, matchState, insideArena);
		}
	}

	private void updateBukkitEvents(MatchState matchState,ArenaPlayer player){
		for (ArenaListener al : arenaListeners){
			MethodController.updateAllEventListeners(al, matchState, player);
		}
	}

	public void open(){
		state = MatchState.ONOPEN;
		MatchOpenEvent event = new MatchOpenEvent(this);
		tmc.callListeners(event); /// Call our listeners listening to only this match
		if (event.isCancelled()){
			this.cancelMatch();
			return;
		}	
		event.callEvent(); /// Call bukkit listeners for this event
		if (event.isCancelled()){
			this.cancelMatch();
			return;
		}
		updateBukkitEvents(MatchState.ONOPEN);
		arenaInterface.onOpen();
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
		state = MatchState.ONPRESTART;

		MatchPrestartEvent event= new MatchPrestartEvent(this,teams);
		notifyListeners(event);

		updateBukkitEvents(MatchState.ONPRESTART);
		PerformTransition.transition(this, MatchState.ONPRESTART, teams, true);
		arenaInterface.onPrestart();
		try{mc.sendOnPreStartMsg(teams);}catch(Exception e){e.printStackTrace();}
		/// Schedule the start of the match
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				startMatch();
			}
		}, (long) (mp.getSecondsTillMatch() * 20L * Defaults.TICK_MULT));
	}

	private void startMatch(){
		if (state == MatchState.ONCANCEL) return; /// If the match was cancelled, dont proceed
		state = MatchState.ONSTART;
		List<Team> competingTeams = new ArrayList<Team>();
		final TransitionOptions mo = tops.getOptions(state);
		for (Team t: teams){
			if (!checkReady(t,mo).isEmpty())
				competingTeams.add(t);
		}
		final int nCompetingTeams = competingTeams.size();
		if (Defaults.DEBUG) Log.info("[BattleArena] competing teams = " + competingTeams +"   allteams=" + teams +"  vcs="+vcs);

		MatchFindNeededTeamsEvent findevent = new MatchFindNeededTeamsEvent(this);
		notifyListeners(findevent);
		int neededTeams = findevent.getNeededTeams();

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
			for (Team t: competingTeams){
				t.sendMessage("&eNo team was ready to compete, choosing a random team to win");}
			if (teams.isEmpty()){
				this.cancelMatch();
			} else {
				Team victor = teams.get(rand.nextInt(teams.size()));
				setVictor(victor);				
			}
		}	
	}

	private synchronized void teamVictory() {
		/// this might be called multiple times as multiple players might meet the victory condition within a small
		/// window of time.  But only let the first one through
		if (state == MatchState.ONVICTORY || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL) 
			return;
		state = MatchState.ONVICTORY;
		/// Call the rest after a 2 tick wait to ensure the calling transitionMethods complete before the
		/// victory conditions start rolling in
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new MatchVictory(this),2L);
	}

	class MatchVictory implements Runnable{
		final Match am;
		MatchVictory(Match am){this.am = am; }

		public void run() {
			Team victor = getVictor();
			final Set<Team> losers = getLosers();
			if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am +"  victor="+ victor + "  " + losers);
			TrackerInterface bti = BTInterface.getInterface(mp);			
			if (victor != null){ /// We have a true winner
				if (bti != null && mp.isRated()){
					BTInterface.addRecord(bti,victor.getPlayers(),losers,WLT.WIN);}									
				try{mc.sendOnVictoryMsg(victor, losers);}catch(Exception e){e.printStackTrace();}
			} else { /// we have a draw
				try{mc.sendOnDrawMessage(losers);} catch(Exception e){e.printStackTrace();}
			}
			updateBukkitEvents(MatchState.ONVICTORY);
			notifyListeners(new MatchVictoryEvent());
			PerformTransition.transition(am, MatchState.ONVICTORY,teams, true);
			arenaInterface.onVictory(getResult());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, 
					new MatchCompleted(am), (long) (mp.getSecondsToLoot() * 20L * Defaults.TICK_MULT));		
		}
	}

	class MatchCompleted implements Runnable{
		final Match am;
		MatchCompleted(Match am){this.am = am;}

		public void run() {
			state = MatchState.ONCOMPLETE;
			final Team victor = am.getVictor();
			if (Defaults.DEBUG) System.out.println("Match::MatchCompleted():" + victor.getName());
			/// ONCOMPLETE can teleport people out of the arena,
			/// So the order of events is usually
			/// ONCOMPLETE(half of effects) -> ONLEAVE( and all effects) -> ONCOMPLETE( rest of effects)
			PerformTransition.transition(am, MatchState.ONCOMPLETE, teams, true);
			/// Once again, lets delay this final bit so that transitions have time to finish before
			/// Other splisteners get a chance to handle
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
				public void run() {
					/// Losers and winners get handled after the match is complete
					PerformTransition.transition(am, MatchState.LOSERS, am.getResult().getLosers(), false);
					if (victor != null) /// everyone died at once??
						PerformTransition.transition(am, MatchState.WINNER, victor, false);
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
		arenaInterface.onFinish();
		insideArena.clear();
		insideWaitRoom.clear();
		for (Team t: teams){
			TeamController.removeTeam(t, match);
			for (ArenaPlayer p: t.getPlayers()){
				stopTracking(p);
			}
		}
		notifyListeners(new MatchFinishedEvent(match));
		teams.clear();
		arenaListeners.clear();
	}

	/**
	 * Notify Bukkit Listeners and specific listeners to this match
	 * @param BAevent event
	 */
	protected void notifyListeners(BAEvent event) {
		tmc.callListeners(event); /// Call our listeners listening to only this match
		event.callEvent(); /// Call bukkit listeners for this event
	}

	/**
	 * Add to an already existing team
	 * @param p
	 * @param t
	 */
	public void playerAddedToTeam(ArenaPlayer p, Team t) {
		if (!t.hasSetName() && t.getPlayers().size() > Defaults.MAX_TEAM_NAME_APPEND){
			t.setDisplayName(TeamUtil.createTeamName(indexOf(t)));
		}
		startTracking(p);
		arenaInterface.onJoin(p,t);

		PerformTransition.transition(this, MatchState.ONJOIN, p,t, true);
	}

	public void onJoin(Collection<Team> teams){
		for (Team t: teams){
			TeamController.addTeamHandler(t, this);
			onJoin(t);}
	}

	public void onJoin(Team t) {
		//		System.out.println(" team t= " + t.getName() +"   is joining  " + q +"  mt=" + mt+ " options=" + arena.getOptions(mt));
		int index = teams.size();
		teams.add(t);
		teamIndexes.put(t, index);
		if (!t.hasSetName() && t.getPlayers().size() > Defaults.MAX_TEAM_NAME_APPEND){
			t.setDisplayName(TeamUtil.createTeamName(indexOf(t)));
		}
		startTracking(t);
		t.setAlive();
		t.resetScores();/// reset scores
		TeamController.addTeamHandler(t, this);
		for (ArenaPlayer p: t.getPlayers()){
			arenaInterface.onJoin(p,t);			
		}

		PerformTransition.transition(this, MatchState.ONJOIN, t, true);
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
	 * only call before a match is running, otherwise strange things will occur
	 * @param t
	 */
	public void onLeave(Team t) {
		PerformTransition.transition(this, MatchState.ONCANCEL, t, false);
		teams.remove(t);
		TeamController.removeTeam(t, this);
	}

	public void onLeave(ArenaPlayer p) {
		if (insideArena(p)){ /// Only leave if they haven't already left.
			Team t = getTeam(p);
			PerformTransition.transition(this, MatchState.ONCANCEL, p, t, false);
		}
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
			psc.addMember(p, arena.getRegion(), arena.getRegionWorld());}
		if (noLeave){
			MethodController.updateEventListeners(this,ms, p,PlayerMoveEvent.class);}
		if (needsBlockEvents){
			MethodController.updateEventListeners(this,ms, p,BlockBreakEvent.class, BlockPlaceEvent.class);}
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
			psc.removeMember(p, arena.getRegion(), arena.getRegionWorld());

		if (needsDamageEvents){
			MethodController.updateEventListeners(this,ms, p,EntityDamageEvent.class);}
		if (noLeave){
			MethodController.updateEventListeners(this,ms, p,PlayerMoveEvent.class);}
		if (needsBlockEvents){
			MethodController.updateEventListeners(this,ms, p,BlockBreakEvent.class, BlockPlaceEvent.class);}
		if (woolTeams || needsInventoryClick){
			MethodController.updateEventListeners(this,ms, p,InventoryClickEvent.class);}
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
		final String name = p.getName();
		insideArena.add(name);
		insideWaitRoom.add(name);
		/// Store the point at which they entered the waitroom
		if (!oldlocs.containsKey(name)) /// First teleportIn is the location we want
			oldlocs.put(name, p.getLocation());
		BTInterface.stopTracking(p);
		Team t = getTeam(p);
		PerformTransition.transition(this, MatchState.ONENTERWAITROOM, p, t, false);
		arenaInterface.onEnterWaitRoom(p,t);	
	}

	/**
	 * Player is entering arena area. Usually called from a teleportIn
	 * @param p
	 */
	@SuppressWarnings("unchecked")
	protected void enterArena(ArenaPlayer p){
		final String name = p.getName();
		insideArena.add(name);
		insideWaitRoom.remove(name);
		/// Store the point at which they entered the arena
		if (!oldlocs.containsKey(name)) /// First teleportIn is the location we want
			oldlocs.put(name, p.getLocation());
		BTInterface.stopTracking(p);
		Team t = getTeam(p);

		PerformTransition.transition(this, MatchState.ONENTER, p, t, false);
		arenaInterface.onEnter(p,t);	
		if (woolTeams){
			MethodController.updateEventListeners(this, MatchState.ONENTER, p,InventoryClickEvent.class);
			TeamUtil.setTeamHead(teams.indexOf(t), t);
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
	}


	public void setVictor(ArenaPlayer p){
		Team t = getTeam(p);
		if (t != null) setVictor(t);
	}

	public synchronized void setVictor(final Team team){
		matchResult.setVictor(team);
		ArrayList<Team> losers= new ArrayList<Team>(teams);
		if (team != null){
			losers.remove(team);
			matchResult.setResult(WinLossDraw.WIN);
		} else {
			matchResult.setResult(WinLossDraw.DRAW);
		}
		matchResult.setLosers(losers);

		teamVictory();
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

	public boolean hasAlivePlayer(ArenaPlayer p) {
		for (Team t: teams) {
			if (t.hasAliveMember(p)) return true;}
		return false;
	}
	public void setMessageHandler(MatchMessageHandler mc){this.mc.setMessageHandler(mc);}
	public MatchMessageHandler getMessageHandler(){return mc.getMessageHandler();}

	//	public void setVictoryCondition(VictoryCondition vc){
	//		this.vc = vc;
	//		addArenaListener(vc);
	//	}
	public void addVictoryCondition(VictoryCondition vc){
		vcs.add(vc);
		addArenaListener(vc);
		tmc.addListener(vc);
	}

	public void addArenaListener(ArenaListener al){
		arenaListeners.add(al);
	}
	//	public VictoryCondition getVictoryCondition(){return vc;}
	public Arena getArena() {return arena;}
	public boolean isFinished() {return state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL;}
	public boolean isWon() {return state == MatchState.ONVICTORY || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL;}
	public boolean isStarted() {return state == MatchState.ONSTART;}
	public MatchState getMatchState() {return state;}
	public MatchParams getParams() {return mp;}
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
	public MatchResult getResult(){return matchResult;}
	public Team getVictor() {return matchResult.getVictor();}
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
			boolean shouldKill = false;
			boolean online = p.isOnline();
			boolean inmatch = insideArena.contains(p.getName());
			final String pname = p.getDisplayName();

			if (Defaults.DEBUG) System.out.println(p.getName()+"  online=" + online +" isready="+tops.playerReady(p));
			if (!online){
				//				Log.warn("[BattleArena] "+p.getName()+" killed for not being online");
				t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for not being online");
				shouldKill = true;
			} else if (p.isDead()){
				//				Log.warn("[BattleArena] "+p.getName()+" killed for not being online");
				t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for being dead while the match starts");
				shouldKill = true;
			} else if (!inmatch && !tops.playerReady(p)){ /// Players are about to be teleported into arena
				t.sendToOtherMembers(p,"&4!!! &eYour teammate &6"+pname+"&e was killed for not being ready");
				MessageUtil.sendMessage(p,"&eYou were &4killed&e bc of the following. ");
				String notReady = tops.getRequiredString(p,"&eYou needed");
				MessageUtil.sendMessage(p,notReady);
				BAPlayerListener.addMessageOnReenter(p.getName(),"&eYou were &4killed&e bc of the following. "+notReady);
				//				Log.warn("[BattleArena] " + p.getName() +"  killed for " + notReady);
				shouldKill = true;
			}
			if (shouldKill){
				/// Really not sure, do we care about killing them on?
				/// But do not!! just set health to 0.  if they are offline they wont lose the inv, but the inv will drop
				/// giving them double items on reenter
			} else {
				alive.add(p);
			}
		}
		return alive;
	}

	public void sendMessage(String string) {
		for (Team t: teams){
			t.sendMessage(string);}
	}

	public String getMatchInfo() {
		TransitionOptions to = tops.getOptions(state);
		StringBuilder sb = new StringBuilder("ArenaMatch " + this.toString() +" ");
		sb.append(mp +"\n");
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

	public void timeExpired(VictoryCondition vc) {
		MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams);
		notifyListeners(event);
		try{mc.sendTimeExpired();}catch(Exception e){e.printStackTrace();}
		setVictor(event.getCurrentLeader());
	}

	public void intervalTick(VictoryCondition vc, int remaining) {
		MatchFindCurrentLeaderEvent event = new MatchFindCurrentLeaderEvent(this,teams);
		notifyListeners(event);
		try{mc.sendOnIntervalMsg(remaining, event.getCurrentLeader());}catch(Exception e){e.printStackTrace();}		
	}

	public Integer getTeamIndex(Team t) {
		return teamIndexes.get(t);
	}

	public int getID(){
		return id;
	}
}