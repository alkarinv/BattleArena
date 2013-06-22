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
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaControllerInterface;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.queues.ArenaMatchQueue;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.queues.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.util.CommandUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BattleArenaController implements Runnable, /*TeamHandler, */ ArenaListener, Listener{

	private boolean stop = false;
	private boolean running = false;

	private static HashSet<String> disabledCommands = new HashSet<String>();
	final private Set<Match> running_matches = Collections.synchronizedSet(new CopyOnWriteArraySet<Match>());
	final private Map<String, Integer> runningMatchTypes = Collections.synchronizedMap(new HashMap<String,Integer>());
	final private Map<ArenaType,List<Match>> unfilled_matches = Collections.synchronizedMap(new ConcurrentHashMap<ArenaType,List<Match>>());
	private Map<String, Arena> allarenas = new ConcurrentHashMap<String, Arena>();
	private final MethodController methodController;

	private final ArenaMatchQueue amq = new QueueController();
	private StateController sc = new StateController(amq);
	final SignController signController;

	public BattleArenaController(SignController signController){
		methodController = new MethodController();
		methodController.addAllEvents(this);
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
		this.signController = signController;
	}

	/// Run is Thread Safe
	public void run() {
		running = true;
		Match match = null;
		while (!stop){
			match = amq.getArenaMatch();
			if (match != null){
				Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new OpenAndStartMatch(match));
			}
		}
		running = false;
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
			startMatch(match);
		}
	}

	public Integer getNumberOpenMatches(String type){
		Integer count = runningMatchTypes.get(type);
		if (count==null){
			count = 0;
			runningMatchTypes.put(type, count);
		}
		return count;
	}

	public Integer incNumberOpenMatches(String type){
		Integer count = runningMatchTypes.get(type);
		if (count==null){
			count = 0;}
		runningMatchTypes.put(type, ++count);
		return count;
	}

	public Integer decNumberOpenMatches(String type){
		Integer count = runningMatchTypes.get(type);
		if (count==null){
			count = 1;}
		runningMatchTypes.put(type, --count);
		return count;
	}

	public Match createMatch(Arena arena, EventOpenOptions eoo) throws NeverWouldJoinException {
		ArenaMatch arenaMatch = new ArenaMatch(arena, eoo.getParams());
		TeamJoinHandler jh = TeamJoinFactory.createTeamJoinHandler(eoo.getParams(), arenaMatch);
		arenaMatch.setTeamJoinHandler(jh);
		openMatch(arenaMatch);
		amq.fillMatch(arenaMatch);
		return arenaMatch;
	}

	public Match createAndAutoMatch(Arena arena, EventOpenOptions eoo) throws NeverWouldJoinException {
		Match m = createMatch(arena,eoo);
		m.setTimedStart(eoo.getSecTillStart(), eoo.getInterval());
		return m;
	}

	private void openMatch(Match match){
		match.addArenaListener(this);
		synchronized(running_matches){
			running_matches.add(match);
		}
		incNumberOpenMatches(match.getParams().getType().getName());
		match.open();
		if (match.isJoinablePostCreate()){
			List<Match> matches = unfilled_matches.get(match.getParams().getType());
			if (matches == null){
				matches = new LinkedList<Match>();
				unfilled_matches.put(match.getParams().getType(), matches);
			}
			((LinkedList<Match>)matches).addFirst(match);
		}
	}

	public void startMatch(Match arenaMatch) {
		/// arenaMatch run calls.... broadcastMessage ( which unfortunately is not thread safe)
		/// So we have to schedule a sync task... again
		//		unhandle(arenaMatch); /// Since teams and players can join between onOpen and onStart.. re unhandle
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), arenaMatch);
	}

	@ArenaEventHandler
	public void matchFinished(MatchFinishedEvent event){
		//		if (Defaults.DEBUG ) System.out.println("BattleArenaController::matchComplete=" + am + ":" );
		Match am = event.getMatch();
		removeMatch(am); /// handles removing running match from the BArenaController
		decNumberOpenMatches(am.getArena().getArenaType().getName());
		//		unhandle(am);/// unhandle any teams that were added during the match
		Arena arena = allarenas.get(am.getArena().getName().toUpperCase());
		if (arena != null)
			amq.add(arena,shouldStart(arena)); /// add it back into the queue
	}

	public void updateArena(Arena arena) {
		allarenas.put(arena.getName().toUpperCase(), arena);
		if (amq.removeArena(arena) != null){ /// if its not being used
			amq.add(arena,shouldStart(arena));}
	}

	public void addArena(Arena arena) {
		allarenas.put(arena.getName().toUpperCase(), arena);
		amq.add(arena, shouldStart(arena));
	}

	public Map<String, Arena> getArenas(){return allarenas;}

	/**
	 * Add the TeamQueueing object to the queue
	 * @param Add the TeamQueueing object to the queue
	 * @return
	 */
	public JoinResult wantsToJoin(TeamJoinObject tqo) {
		/// Can they join an existing Game
		if (joinExistingMatch(tqo)){
			JoinResult qr = new JoinResult();
			qr.status = JoinResult.JoinStatus.ADDED_TO_EXISTING_MATCH;
			return qr;
		}
		JoinResult jr = sc.join(tqo, shouldStart(tqo.getMatchParams()));
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
		if (type == null || mp == null)
			return true;
		return getNumberOpenMatches(type) < mp.getNConcurrentCompetitions();
	}


	private boolean joinExistingMatch(TeamJoinObject tqo) {
		Log.debug("unfilled = " + unfilled_matches.size());
		if (unfilled_matches.isEmpty()){
			return false;}
		MatchParams params = tqo.getMatchParams();
		synchronized(unfilled_matches){
			List<Match> matches = unfilled_matches.get(params.getType());
			Log.debug("matches = " + matches);
			if (matches == null)
				return false;
			Iterator<Match> iter = matches.iterator();
			while (iter.hasNext()){
				Match match = iter.next();
				/// We dont want people joining in a non waitroom state
				Log.debug("matches = " + match +"   stilljoin=" + match.canStillJoin() +"   matches="+
						match.getParams().matches(params));
				if (!match.canStillJoin()){
					continue;}
				if (match.getParams().matches(params)){
					TeamJoinHandler tjh = match.getTeamJoinHandler();
					if (tjh == null)
						continue;
					if (!JoinOptions.matches(tqo.getJoinOptions(), match))
						continue;
					boolean result = false;
					TeamJoinResult tjr = tjh.joiningTeam(tqo);
					switch(tjr.status){
					case ADDED_TO_EXISTING: case ADDED: result = true;
					default: break;
					}
					return result;
				}
			}
		}
		return false;
	}

	public boolean isInQue(ArenaPlayer p) {return amq.isInQue(p);}

	public JoinResult getCurrentQuePos(ArenaPlayer p) {return amq.getQueuePos(p);}

	public void addMatchup(Matchup m) {amq.addMatchup(m, shouldStart(m.getMatchParams()));}
	public Arena reserveArena(Arena arena) {return amq.reserveArena(arena);}
	public Arena getArena(String arenaName) {return allarenas.get(arenaName.toUpperCase());}

	public Arena removeArena(Arena arena) {
		Arena a = amq.removeArena(arena);
		if (a != null){
			allarenas.remove(arena.getName().toUpperCase());}
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
		return amq.getNextArena(mp);
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
			if (a.valid() && a.matches(mp,jp)){
				return a;}
			if (a.getArenaType() != mp.getType())
				continue;
			int m2 = a.getParameters().getMinTeamSize();
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
			if (a.getParameters().matchesTeamSize(i)){
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
		for (ArenaPlayer p : ps){
			return cancelMatch(p);
		}
		return false;
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
				if (am.insideArena(p)){
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
				sb.append(am + "\n");
		}
		return sb.toString();
	}

	public String toStringArenas(){
		StringBuilder sb = new StringBuilder();
		sb.append(amq.toStringArenas());
		sb.append("------ arenas -------\n");
		for (Arena a : allarenas.values()){
			sb.append(a +"\n");
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
		signController.clearQueues();
		amq.resume();
	}


	public Collection<ArenaTeam> purgeQueue() {
		signController.clearQueues();
		Collection<ArenaTeam> teams = amq.purgeQueue();
		amq.purgeQueue();
		//		TeamController.removeTeams(teams, this);
		//		for (ArenaTeam t: teams){
		//			unhandle(t);
		//			t.sendMessage("&cYou have been removed from the queue");
		//		}
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
	public int getMatchingQueueSize(MatchParams mp) {
		return amq.getMatchingQueueSize(mp);
	}
	public boolean forceStart(MatchParams mp, boolean respectMinimumPlayers) {
		return amq.forceStart(mp, respectMinimumPlayers);
	}
	public static void setDisabledCommands(List<String> commands) {
		for (String s: commands){
			disabledCommands.add("/" + s.toLowerCase());}
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

	@ArenaEventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (!event.isCancelled() && CommandUtil.shouldCancel(event, disabledCommands)){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED+"You cannot use that command when you are in the queue");
			if (PermissionsUtil.isAdmin(event.getPlayer())){
				MessageUtil.sendMessage(event.getPlayer(),"&cYou can set &6/bad allowAdminCommands true: &c to change");}
		}
	}

	@ArenaEventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.isCancelled() || !Defaults.ENABLE_PLAYER_READY_BLOCK)
			return;
		if (event.getClickedBlock().getType().equals(Defaults.READY_BLOCK)) {
			final ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
			if (ap.isReady()) /// they are already ready
				return;
			JoinResult qtp = amq.getQueuePos(ap);
			if (qtp == null){
				return;}
			final Action action = event.getAction();
			if (action == Action.LEFT_CLICK_BLOCK){ /// Dont let them break the block
				event.setCancelled(true);}
			MessageUtil.sendMessage(ap, "&2You ready yourself for the arena");
			this.forceStart(qtp.params, true);
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


}
