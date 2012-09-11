package mc.alk.arena.competition.match;

import java.util.ArrayList;
import java.util.Arrays;
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
import mc.alk.arena.controllers.OnMatchComplete;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.messaging.MatchMessageHandler;
import mc.alk.arena.controllers.messaging.MatchMessager;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchEventHandler;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.PVPState;
import mc.alk.arena.objects.TransitionOptions;
import mc.alk.arena.objects.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaInterface;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.objects.victoryconditions.VictoryCondition;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.WLT;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;


public class Match implements Runnable, CountdownCallback, ArenaListener, TeamHandler {
	public enum PlayerState{OUTOFMATCH,INMATCH};
	static int count =0;

	final int id = count++;
	final MatchParams mp; /// Our parameters for this match
	final Arena arena; /// The arena we are using
	final ArenaInterface arenaInterface; /// Our interface to access arena methods w/o reflection

	List<Team> teams = new ArrayList<Team>(); /// Our players
	Set<String> visitors = new HashSet<String>(); /// Who is watching
	MatchState state = MatchState.NONE;/// State of the match
	VictoryCondition vc = null; // Under what conditions does a victory occur
	Countdown timer = null; /// Timer for when victory condition is time based
	MatchResult matchResult; /// Results for this match
	Map<String, Location> oldlocs = null; /// Locations where the players came from before entering arena
	Set<String> insideArena = new HashSet<String>(); /// who is still inside arena area
	Set<String> insideWaitRoom = new HashSet<String>(); /// who is still inside the wait room
	MatchTransitions tops = null; /// Our match options for this arena match
	PlayerStoreController psc = new PlayerStoreController(); /// Store items and exp for players if specified
	OnMatchComplete matchComplete;  /// Function to call once match is complete
	List<ArenaListener> arenaListeners = new ArrayList<ArenaListener>();

	/// These get used enough or are useful enough that i'm making variables even though they can be found in match options
	final boolean needsClearInventory, clearsInventory, clearsInventoryOnDeath; 
	final boolean respawns;
	boolean woolTeams = false;
	boolean needsMobDeaths = false, needsBlockEvents = false;
	boolean needsItemPickups = false, needsInventoryClick = false;
	boolean needsDamageEvents = false;
	final Plugin plugin;

	Random rand = new Random(); /// Our randomizer

	MatchMessager mc; /// Our message instance

	public Match(Arena arena, OnMatchComplete omc, MatchParams mp) {
		if (Defaults.DEBUG) System.out.println("ArenaMatch::" + mp);
		plugin = BattleArena.getSelf();
		this.mp = mp;
		this.arena = arena;
		arenaInterface =new ArenaInterface(arena);
		arenaListeners.add(arena);
		this.matchComplete = omc;
		this.mc = new MatchMessager(this);
		this.oldlocs = new HashMap<String,Location>();
		this.teams = new ArrayList<Team>();
		this.tops = mp.getTransitionOptions();
		arena.setMatch(this);
		setVictoryCondition(VictoryType.createVictoryCondition(this));
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
		updateBukkitEvents(MatchState.ONOPEN);
		arenaInterface.onOpen();
	}

	public void run() {
		preStartMatch();
	}

	private void preStartMatch() {
		if (state == MatchState.ONCANCEL) return; /// If the match was cancelled, dont proceed
		if (Defaults.DEBUG) System.out.println("ArenaMatch::startMatch()");
		state = MatchState.ONPRESTART;
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
		if (Defaults.DEBUG) Log.info("[BattleArena] competing teams = " + competingTeams +"   allteams=" + teams +"  vc="+vc);
		boolean timeVictory = vc.hasTimeVictory();

		if (nCompetingTeams >= vc.getNeededTeams()){
			updateBukkitEvents(MatchState.ONSTART);
			PerformTransition.transition(this, state,competingTeams, true);
			arenaInterface.onStart();
			try{mc.sendOnStartMsg(teams);}catch(Exception e){e.printStackTrace();}
			if (timeVictory){				
				timer = new Countdown(plugin,vc.matchEndTime(), vc.matchUpdateInterval(), this);}
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
		/// Call the rest after a 2 tick wait to ensure the calling arenaMethods complete before the
		/// victory conditions start rolling in
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new MatchVictory(this),2L);
	}

	class MatchVictory implements Runnable{
		final Match am;
		MatchVictory(Match am){this.am = am; }

		public void run() {
			cancelTimers();
			Team victor = getVictor();
			final Set<Team> losers = getLosers();
			if (Defaults.DEBUG) System.out.println("Match::MatchVictory():"+ am +"  victor="+ victor + "  " + losers);
			if (victor != null){
				TrackerInterface bti = BTInterface.getInterface(mp);
				//		System.out.println("vicotry and my pi = " + q);
				if (bti != null && mp.isRated()){
					BTInterface.addRecord(bti,victor.getPlayers(),losers,WLT.WIN);}				
				try{mc.sendOnVictoryMsg(victor, losers);}catch(Exception e){e.printStackTrace();}
			}
			updateBukkitEvents(MatchState.ONVICTORY);
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
			cancelTimers();
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
					matchComplete.matchComplete(am); /// Call BattleArenaController and say we are completely done
					updateBukkitEvents(MatchState.ONCOMPLETE);
					deconstruct();	
				}
			});
		}
	}

	public void cancelMatch(){
		state = MatchState.ONCANCEL;
		arenaInterface.onCancel();
		cancelTimers();
		for (Team t : teams){
			PerformTransition.transition(this, MatchState.ONCANCEL,t,true);
		}
		updateBukkitEvents(MatchState.ONCANCEL);
		matchComplete.matchComplete(this); /// Call BattleArenaController and say we are completely done
		deconstruct();
	}

	private void deconstruct(){
		insideArena.clear();
		insideWaitRoom.clear();
		for (Team t: teams){
			TeamController.removeTeam(t, this);
			for (ArenaPlayer p: t.getPlayers()){
				stopTracking(p);
			}
		}
		teams.clear();
		arenaListeners.clear();
	}

	private void cancelTimers() {
		if (timer != null){
			timer.stop();
			timer =null;
		}		
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
		for (Team team : teams){
			System.out.println("team1 = " + team +"    " + t);
			
		}

		PerformTransition.transition(this, MatchState.ONJOIN, p,t, true);
	}

	public void onJoin(Collection<Team> teams){
		for (Team t: teams){
			TeamController.addTeamHandler(t, this);
			onJoin(t);}
	}

	public void onJoin(Team t) {
		//		System.out.println(" team t= " + t.getName() +"   is joining  " + q +"  mt=" + mt+ " options=" + arena.getOptions(mt));
		teams.add(t);
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
		//		System.out.println("ArenaMatch::onLeave(Team t)=" + t +"   mo =" + tops.getOptionString());
		teams.remove(t);
		TeamController.removeTeam(t, this);
		for (ArenaPlayer p: t.getPlayers()){
			stopTracking(p);
			arenaInterface.onLeave(p,t);
			vc.playerLeft(p);
		}
		PerformTransition.transition(this, MatchState.ONCANCEL, t, false);
	}

	public void onLeave(ArenaPlayer p) {
		//		System.out.println("ArenaMatch::onLeave(Team t)=" + t +"   mo =" + tops.getOptionString());
		Team t = getTeam(p);
		stopTracking(p);
		arenaInterface.onLeave(p,t);
		PerformTransition.transition(this, MatchState.ONCANCEL, t, false);
		vc.playerLeft(p);
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
		if (needsDamageEvents){
			MethodController.updateEventListeners(this,ms, p,EntityDamageEvent.class);			
		}
		if (needsBlockEvents){
			MethodController.updateEventListeners(this,ms, p,BlockBreakEvent.class, BlockPlaceEvent.class);
		}
		updateBukkitEvents(ms,p);
		/// if (woolTeams) /*do nothing*/ Wool Teams Inventory click listener is added when they get their wool team.. not here
	}

	@SuppressWarnings("unchecked")
	protected void stopTracking(final ArenaPlayer p){
		final MatchState ms = MatchState.ONLEAVE;
		MethodController.updateEventListeners(this,ms, p,PlayerQuitEvent.class,PlayerRespawnEvent.class);
		MethodController.updateEventListeners(this, ms,p, PlayerCommandPreprocessEvent.class);
		MethodController.updateEventListeners(this,ms, p,PlayerDeathEvent.class);
		if (needsDamageEvents){
			MethodController.updateEventListeners(this,ms, p,EntityDamageEvent.class);			
		}
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
	}

	@MatchEventHandler
	public void onPlayerJoin(PlayerJoinEvent event, ArenaPlayer player){
		//// TODO I feel like I should do something here
	}

	@MatchEventHandler
	public void onPlayerQuit(PlayerQuitEvent event, ArenaPlayer player){
//		System.out.println(this+"onPlayerQuit = " + player.getName() + "  " +matchResult.matchComplete()  +" :" + state + insideArena.contains(player.getName()));
		if (woolTeams)
			BAPlayerListener.clearWoolOnReenter(player.getName(), teams.indexOf(getTeam(player)));
		/// If they are just in the arena waiting for match to start, or they havent joined yet
		if (state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL || 
				state == MatchState.ONOPEN || !insideArena.contains(player.getName())){ 
			return;}
		/// kill player will teleport them out, which makes them leaveArena(p)
		/// This ensures that onExit is called for both player kicked, quit, and player disconnected  
//		killPlayer(player,clearsInventory);
		Team t = getTeam(player);
		PerformTransition.transition(this, MatchState.ONCOMPLETE, player, t, true);
		
		vc.playerLeft(player);
	}

	@MatchEventHandler(suppressCastWarnings=true)
	public void onPlayerDeath(PlayerDeathEvent event, ArenaPlayer target){
//		System.out.println(this+"!!!!! onPlayerDeath = " + target.getName() + "  complete=" +matchResult.matchComplete()  +
//				": inside=" + insideArena.contains(target.getName()) +"   clearInv?=" + clearsInventoryOnDeath +"   " + isWon());
		if (state == MatchState.ONCANCEL || state == MatchState.ONCOMPLETE || !insideArena.contains(target.getName())){
			return;}
		//		target.getLastDamageCause();
//		ArenaPlayer killer = null;
		Team t = getTeam(target);
		/// Handle Drops from bukkitEvent
		if (clearsInventoryOnDeath){ /// Very important for deathmatches.. otherwise tons of items on floor
			try {event.getDrops().clear();} catch (Exception e){}
		} else if (woolTeams){  /// Get rid of the wool from teams so it doesnt drop
			int color = teams.indexOf(t);
			//				System.out.println("resetting wool team " + target.getName() +" color  ");
			List<ItemStack> items = event.getDrops();
			for (ItemStack is : items){
				if (is.getType() == Material.WOOL && color == is.getData().getData()){
					final int amt = is.getAmount();
					if (amt > 1)
						is.setAmount(amt-1);
					else 
						is.setType(Material.AIR);
					break;
				}
			}
		}
		if (!respawns){
			PerformTransition.transition(this, MatchState.ONCOMPLETE, target, t, true);			
		}
	}

	@MatchEventHandler(suppressCastWarnings=true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		ArenaPlayer target = BattleArena.toArenaPlayer((Player) event.getEntity());
		TransitionOptions to = tops.getOptions(state);
		if (to == null)
			return;
		final PVPState pvp = to.getPVP();
		if (pvp == null)
			return;
		if (pvp == PVPState.INVINCIBLE){
			/// all damage is cancelled
			target.setFireTicks(0);
			event.setDamage(0);
			event.setCancelled(true);
			return;
		}
		if (!(event instanceof EntityDamageByEntityEvent)){
			return;}
		
		Entity damagerEntity = ((EntityDamageByEntityEvent)event).getDamager();

		ArenaPlayer damager=null;
		switch(pvp){
		case ON:
			Team targetTeam = getTeam(target);
			if (targetTeam == null || !targetTeam.hasAliveMember(target)) /// We dont care about dead players
				return;
			damager = DmgDeathUtil.getPlayerCause(damagerEntity);
			if (damager == null){ /// damage from some source, its not pvp though. so we dont care
				return;}
			Team t = getTeam(damager);
			if (t != null && t.hasMember(target)){ /// attacker is on the same team
				event.setCancelled(true);
				event.setDamage(0);
			} else {/// different teams... lets make sure they can actually hit
				event.setCancelled(false);
			}
			break;
		case OFF:
			damager = DmgDeathUtil.getPlayerCause(damagerEntity);
			if (damager != null){ /// damage done from a player
				event.setDamage(0);
				event.setCancelled(true);
			}
			break;
		}
	}	

	@MatchEventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event, final ArenaPlayer p){
		if (isWon()){ 
			return;}
		final TransitionOptions mo = tops.getOptions(MatchState.ONDEATH);
		if (mo == null)
			return;

		if (respawns){
			Location loc = getTeamSpawn(getTeam(p), mo.randomRespawn());
			event.setRespawnLocation(loc);
			/// For some reason, the player from onPlayerRespawn Event isnt the one in the main thread, so we need to 
			/// resync before doing any effects
			final Match am = this;
			Plugin plugin = BattleArena.getSelf();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					try{
						PerformTransition.transition(am, MatchState.ONDEATH, p, getTeam(p), false);
						PerformTransition.transition(am, MatchState.ONSPAWN, p, getTeam(p), false);
						if (woolTeams){
							Team t= getTeam(p);
							TeamUtil.setTeamHead(teams.indexOf(t), t);
						}
					} catch(Exception e){}
				}
			});
		} else { /// This player is now out of the system now that we have given the ondeath effects
			Location l = oldlocs.get(p.getName());
			if (l != null)
				event.setRespawnLocation(l);
			stopTracking(p);
		}
	}

	@MatchEventHandler
	public void onPlayerBlockBreak(BlockBreakEvent event, ArenaPlayer p){
		TransitionOptions to = tops.getOptions(state);
		if (to==null)
			return;
		if (to.blockBreakOff() == true){
			event.setCancelled(true);
		}
	}

	@MatchEventHandler
	public void onPlayerBlockPlace(BlockPlaceEvent event, ArenaPlayer p){
		TransitionOptions to = tops.getOptions(state);
		if (to==null)
			return;
		if (to.blockPlaceOff() == true){
			event.setCancelled(true);
		}
	}

	@MatchEventHandler
	public void onPlayerInventoryClick(InventoryClickEvent event, ArenaPlayer p) {
		if (woolTeams && event.getSlot() == 39/*Helm Slot*/)
			event.setCancelled(true);
	}


	/// TODO where should this go
	public static final HashSet<String> disabled = 
			new HashSet<String>(Arrays.asList( "/home", "/spawn", "/trade", "/paytrade", "/payhome", 
					"/warp","/watch", "/sethome","/inf", "/va","/survival","/ma","/mob","/ctp","/chome","/csethome"));

	@MatchEventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (event.isCancelled() || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL){
			return;}
		final Player p = event.getPlayer();
		if (event.getPlayer().isOp())
			return;

		String msg = event.getMessage();
		final int index = msg.indexOf(' ');
		if (index != -1){
			msg = msg.substring(0, index);
		}
		if(disabled.contains(msg)){
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED+"You cannot use that command when you are in a match");
		}
	}

	public void setVictor(ArenaPlayer p){
		Team t = getTeam(p);
		if (t != null) setVictor(t);
	}

	public synchronized void setVictor(final Team team){
		matchResult.setVictor(team);
		ArrayList<Team> losers= new ArrayList<Team>(teams);
		losers.remove(team);
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

	public void setVictoryCondition(VictoryCondition vc){
		this.vc = vc;
		addArenaListener(vc);
	}
	public void addArenaListener(ArenaListener al){
		arenaListeners.add(al);
	}
	public VictoryCondition getVictoryCondition(){return vc;}
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

	public boolean intervalTick(int remaining){
		if (remaining <= 0){
			vc.timeExpired();
			try{mc.sendTimeExpired();}catch(Exception e){e.printStackTrace();}
		} else {
			vc.timeInterval(remaining);
			try{mc.sendOnIntervalMsg(remaining);}catch(Exception e){e.printStackTrace();}
		}	
		return true;
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


}
