package mc.alk.arena.objects.queues;

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

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.BlankCompetition;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinStatus;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.Scheduler;
import mc.alk.arena.controllers.messaging.MatchMessageImpl;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.JoinOptions.JoinOption;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.pairs.QueueResult;
import mc.alk.arena.objects.pairs.QueueResult.TimeStatus;
import mc.alk.arena.objects.queues.TeamQueue.TeamQueueComparator;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.util.Log;

import org.bukkit.Bukkit;


public class ArenaMatchQueue{
	static final boolean DEBUG = false;

	final Map<ArenaType, TeamQueue> tqs = new HashMap<ArenaType, TeamQueue>();
	final Map<TeamQueue, IdTime> forceTimers = Collections.synchronizedMap(new HashMap<TeamQueue, IdTime>());
	public static class IdTime{
		public int id;
		public Long time;
	}

	final ArenaQueue arenaqueue = new ArenaQueue();

	final LinkedList<Match> ready_matches = new LinkedList<Match>();

	boolean suspend = false;

	public synchronized Match getArenaMatch() {
		try{
			if (ready_matches.isEmpty())
				wait(30000); /// Technically this could wait forever, but just in case.. check occasionally
			if (!ready_matches.isEmpty() && !suspend){
				synchronized(ready_matches){
					return ready_matches.removeFirst();
				}
			}
		} catch(InterruptedException e) {
			System.err.println("InterruptedException caught");
		}
		notify();
		return null;
	}

	public synchronized void add(Arena arena, boolean checkStart) {
		synchronized(arenaqueue){
			for (Arena a : arenaqueue){
				if (a.getName().equals(arena.getName()))
					return;
			}
			arenaqueue.addLast(arena);
		}
		if (!suspend)
			notifyIfNeeded();
	}

	public synchronized QueueResult add(final QueueObject queueObject, boolean checkStart) {
		return addToQueue(queueObject,checkStart);
	}

	/**
	 * Add a matchup of teams.  They already have all required teams, so just need to wait for an open arena
	 * @param matchup
	 * @return
	 */
	public synchronized QueueResult addMatchup(Matchup matchup, boolean checkStart) {
		return addToQueue(new MatchTeamQObject(matchup), checkStart);
	}

	private synchronized QueueResult addToQueue(final QueueObject to, boolean checkStart) {
		if (!ready_matches.isEmpty() && checkStart)
			notifyAll();
		final MatchParams tomp = to.getMatchParams();
		TeamQueue tq = getTeamQ(tomp);
		tq.add(to);
		/// return if we aren't going to check for a start (aka we already have too many matches running)
		QueueResult qr = new QueueResult();
		if (!suspend && checkStart)
			qr = notifyIfNeeded(tq);
		if (to instanceof TeamQObject){
			/// If forceStart we need to track the first Team who joins, we will base how long till the force start off them
			/// we should also report to the user that the match will start in x seconds, despite the queue size
			if (Defaults.MATCH_FORCESTART_START_ONJOIN /*|| (qr != null && qr.params != null && qr.params.getMinPlayers() <= qr.playersInQueue)*/){
				IdTime idt = updateTimer(tq,to);
				if (idt != null)
					qr.time = idt.time;
			}
		}

		return qr;
	}

	/**
	 * Update the forceJoin timer for the following TeamQueue and the given QueueObject
	 * The time will not be updated if an older timer is ongoing
	 * @param tq
	 * @param to
	 * @return
	 */
	private IdTime updateTimer(final TeamQueue tq, QueueObject to) {
		JoinOptions jo = to.getJoinOptions();
		if (jo == null || jo.getJoinTime() == null)
			return null;
		IdTime idt = forceTimers.get(tq);
		if (idt == null){
			Long time = (System.currentTimeMillis() - jo.getJoinTime() + Defaults.MATCH_FORCESTART_TIME*1000);
			idt = new IdTime();
			idt.time = System.currentTimeMillis() + time;
			if (time > 0){
				idt.id = Scheduler.scheduleSynchrounousTask(new Runnable(){
					@Override
					public void run() {
						QueueResult qr = findMatch(tq,true, true);
						if (qr.match == null)
							return;
						forceTimers.remove(tq);
						addToReadyMatches(qr.match);
					}
				}, (int) (time/1000)* 20);
				forceTimers.put(tq, idt);
			}
		}
		return idt;
	}

	/**
	 * This is called when an arena gets readded
	 * Since an arena can match many(or all) teamqueues, need to iterate over them all
	 */
	private boolean notifyIfNeeded(){
		boolean notified = false;
		///First try to get them from our already matched up players
		for (TeamQueue tq: tqs.values()){
			if (tq == null || tq.isEmpty())
				continue;
			notified |= notifyIfNeeded(tq).match != null;
		}
		return notified;
	}

	private QueueResult notifyIfNeeded(TeamQueue tq) {
		if (tq==null || arenaqueue.isEmpty() || tq.isEmpty())
			return new QueueResult();

		QueueResult qr = findMatch(tq, false, true);
		if (qr.match == null)
			return qr;
		addToReadyMatches(qr.match);
		return qr;
	}

	private synchronized void addToReadyMatches(Match match){
		synchronized(ready_matches){
			ready_matches.addLast(match);
		}
		notifyAll();
	}

	/**
	 * @param TeamQueue : Queue we are searching for matches
	 * @param forceStart : whether we are trying to start a match that usually would need more players
	 * @return Match if one can be created from the specified TeamQueue
	 */
	private QueueResult findMatch(final TeamQueue tq, boolean forceStart, boolean forceStartRespectMinimumPlayers) {
		if (suspend)
			return new QueueResult();
		if (Defaults.DEBUG) System.out.println("findMatch " + tq +"  " + tq.size() +"  mp=" + tq.getMatchParams());
		/// The idea here is we iterate through all arenas
		/// See if one matches with the type of TeamQueue that we have been given
		/// Then we make sure those players are ready, and if not send them messages
		final MatchParams baseParams = ParamController.getMatchParamCopy(tq.getMatchParams().getType().getName());

		QueueResult qr = new QueueResult();
		qr.pos = tq.size();
		qr.playersInQueue = tq.size();
		if (baseParams != null){
			qr.neededPlayers = baseParams.getMaxPlayers();
			if (baseParams.getMinPlayers() == baseParams.getMaxPlayers())
				qr.timeStatus = TimeStatus.CANT_FORCESTART;
		}
		qr.params = baseParams;
		qr.teamsInQueue = tq.size();
		/// With a force start we are trying to be as permissive as possible,
		/// this means changing the minimum teamsize and teams.  It also means later on we will do the same
		/// with the arena params
		//		if (Defaults.MATCH_FORCESTART_ENABLED){
		IdTime idt = forceTimers.get(tq);
		if (idt != null && idt.time <= System.currentTimeMillis()){
			qr.time = idt.time;
			forceStart = true;
		}
		//		}
		if (idt == null || idt.time == null){
			qr.timeStatus = TimeStatus.CANT_FORCESTART;
		}
		if (forceStart){
			baseParams.setMinTeamSize(forceStartRespectMinimumPlayers ? baseParams.getMinTeamSize() : 1);
			baseParams.setMinTeams(forceStartRespectMinimumPlayers ? baseParams.getMinTeams():
				Math.min(baseParams.getMinTeams(),2));
		}
		if (qr.timeStatus != TimeStatus.CANT_FORCESTART){
			qr.timeStatus = forceStart ? TimeStatus.TIME_EXPIRED : TimeStatus.TIME_ONGOING;
		}

		if (baseParams==null){
			qr.status = QueueResult.QueueStatus.ERROR;
			return qr;
		}


		boolean skipNonMatched = false;
		synchronized(arenaqueue){ synchronized(tq){
			for (Arena a : arenaqueue){
				if (a == null || !a.valid() || (!a.matches(baseParams, null) && !forceStart))
					continue;
				MatchParams newParams = new MatchParams(baseParams);
				if (!forceStart && !newParams.intersect(a.getParameters())){ /// only intersect if not forceStart
					continue;}

				if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + MatchMessageImpl.decolorChat(a.toString())+
						"   tq=" + tq +" --- ap="+a.getParameters() +"    baseP="+baseParams +" newP="+newParams +"  " + newParams.getMaxPlayers() +
						" tqparams="+tq.mp);
				for (QueueObject qo : tq){
					/// Check if we should only use 1 arena (skipNonMatched == true).  But for certain elements in the queue
					/// We need to ignore.
					/// Allow MatchedUp Matches to proceed like normal (this happens for tournaments and duels)
					/// Allow people that selected particular join options (like certain arenas) to get a chance
					if (skipNonMatched && qo instanceof TeamQObject){
						JoinOptions jpo = qo.getJoinOptions();
						if (jpo == null || !jpo.isSpecific())
							continue;
					}
					MatchParams qomp = qo.getMatchParams();
					/// Check to see if the team matches these params
					if (!qomp.matches(newParams)){
						if (!forceStart){ /// continue to next queue object
							continue;
						} else { /// since we are trying to force a start... how bad does this player not match?
							JoinOptions jpo = qo.getJoinOptions();
							if (jpo != null && jpo.isSpecific()) /// alright, they want a particular place/size we aren't doing
								continue;
						}
					}

					try {
						MatchParams playerMatchAndArenaParams = new MatchParams(newParams);
						qr.neededPlayers = playerMatchAndArenaParams.getMaxPlayers();
						findMatch(qr, playerMatchAndArenaParams,a,tq, forceStart);
					} catch (NeverWouldJoinException e) {
						Log.printStackTrace(e);
						continue;
					}
					if (qr.match != null){
						arenaqueue.remove(a);
						return qr;
					}
				}
				if (Defaults.USE_ARENAS_ONLY_IN_ORDER && !forceStart){ /// Only check the first valid arena
					skipNonMatched = true;}
			}
		}}
		///Found nothing matching
		return qr;
	}

	/**
	 * For these parameters, go through each team to see if we can create a viable match
	 * @param MatchParams
	 * @param Arena
	 * @param TeamQueue
	 * @param forceStart
	 * @return
	 * @throws NeverWouldJoinException
	 */
	private void findMatch(QueueResult qr, final MatchParams params, Arena arena, final TeamQueue tq,
			boolean forceStart) throws NeverWouldJoinException {
		/// Move all teams with the same team size together
		List<QueueObject> newList = new LinkedList<QueueObject>();
		List<QueueObject> delayed = new LinkedList<QueueObject>();
		int teamsInQueue = 0, playersInQueue = 0;
		boolean hasPrematched = false;
		for (QueueObject qo : tq){
			if (qo instanceof MatchTeamQObject)
				hasPrematched = true;
			MatchParams mp = qo.getMatchParams();
			if (!mp.matches(params))
				continue;
			if (mp.getMinTeamSize() != params.getMinTeamSize()){
				delayed.add(qo);
			} else {
				newList.add(qo);
			}
			playersInQueue += qo.size();
			teamsInQueue++;
		}
		qr.teamsInQueue = teamsInQueue;
		qr.playersInQueue = playersInQueue;

		if (!hasPrematched &&
				(playersInQueue < params.getMinPlayers() || (!forceStart && playersInQueue < params.getMaxPlayers()))) /// we don't have enough players to match these params
			return;

		newList.addAll(delayed);

		Map<ArenaPlayer, QueueObject> qteams = new HashMap<ArenaPlayer, QueueObject>();
		Competition comp = new BlankCompetition(params);
		//		Competition lastValidComp = null;

		teamsInQueue = 0;
		playersInQueue = 0;
		TeamJoinHandler tjh = TeamJoinFactory.createTeamJoinHandler(params, null);
		boolean hasComp = false;
		/// Now that we have a semi sorted list, get the largest number of teams that fit in this arena
		for (QueueObject qo : newList){
			if (qo instanceof MatchTeamQObject){ /// we have our teams already
				tjh.deconstruct();
				qr.status = QueueResult.QueueStatus.ADDED_TO_QUEUE;
				qr.match = getPreMadeMatch(tq, arena, qo);
				return;
			} else {
				TeamQObject to = (TeamQObject) qo;
				ArenaTeam t = to.getTeam();
				JoinOptions jp = qo.getJoinOptions();
				if (jp != null && !(jp.matches(arena) && jp.matches(params) && arena.matches(params, jp))){
					continue;}

				TeamJoinResult tjr = tjh.joiningTeam(to);
				if (tjr.status == TeamJoinStatus.CANT_FIT){
					continue;}
				teamsInQueue++;
				playersInQueue += t.size();
				qr.pos = playersInQueue;
				qr.playersInQueue = playersInQueue;
				for (ArenaPlayer ap: t.getPlayers())
					qteams.put(ap, to);
			}
			if (tjh.hasEnough(false)){ /// If we have enough teams, mark this as a valid competition and keep going
				hasComp = true;
				comp.setTeams(tjh.getTeams());
				if (tjh.isFull()){
					break;
				}
			}
		}
		qr.teamsInQueue = teamsInQueue;
		qr.playersInQueue = playersInQueue;

		///remove the arena from the queue, start up our match
		if (hasComp){
			qr.status = QueueResult.QueueStatus.ADDED_TO_QUEUE;
			qr.match = getMatchAndRemove(tq, tjh, comp.getTeams(), qteams, arena, params);
			return;
		}

		tjh.deconstruct();
		return;
	}

	private Match getMatchAndRemove(TeamQueue tq, TeamJoinHandler tjh, List<ArenaTeam> teams,
			Map<ArenaPlayer, QueueObject> oteams, Arena a, MatchParams params) {
		Set<ArenaTeam> originalTeams = new HashSet<ArenaTeam>();
		for (ArenaTeam t: teams){
			originalTeams.add(t);
			for (ArenaPlayer ap :t.getPlayers()){
				ArenaTeam originalTeam = oteams.get(ap).getTeam(ap);
				originalTeams.add(originalTeam);
				tq.remove(oteams.get(ap));
			}
		}
		final Match m = new ArenaMatch(a, params);
		if (	m.getParams().getAlwaysOpen() ||
				m.getParams().getTransitionOptions().hasOptionAt(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN) ||
				(m.hasWaitroom() && !tjh.isFull()) ){
			try {
				m.setTeamJoinHandler(TeamJoinFactory.createTeamJoinHandler(params, m));
			} catch (NeverWouldJoinException e) {
				Log.printStackTrace(e);
			}
		}
		m.onJoin(teams);
		m.setOriginalTeams(originalTeams);

		tjh.deconstruct(); /// now with alwaysJoin I believe this should go away
		/// For forcestart we need to reset the next timer
		if (Defaults.MATCH_FORCESTART_ENABLED){
			forceTimers.remove(tq);
			for (QueueObject qo : tq){
				if (qo instanceof MatchTeamQObject)
					continue;
				JoinOptions jo = qo.getJoinOptions();
				if (jo == null)
					continue;
				updateTimer(tq,qo);
				break;
			}
		}
		return m;
	}

	private Match getPreMadeMatch(TeamQueue tq,  Arena arena, QueueObject tto) {
		MatchTeamQObject to = (MatchTeamQObject) tto;
		if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + arena  +"   tq=" + tq);
		Matchup matchup = to.getMatchup();
		tq.remove(to);
		final Match m = new ArenaMatch(arena, to.getMatchParams());
		m.setTeamJoinHandler(null); /// we don't want any custom team joining.
		m.addArenaListeners(matchup.getArenaListeners());
		m.onJoin(matchup.getTeams());
		if (matchup.getMatchCreationListener() != null){
			matchup.getMatchCreationListener().matchCreated(m, matchup);
		}
		matchup.addMatch(m);
		return m;
	}

	private TeamQueue getTeamQ(MatchParams mp) {
		if (tqs.containsKey(mp.getType())){
			return tqs.get(mp.getType());
		} else {
			TeamQueue tq = new TeamQueue(mp, new TeamQueueComparator());
			tqs.put(mp.getType(), tq);
			return tq;
		}
	}

	public synchronized boolean isInQue(ArenaPlayer p) {
		for (TeamQueue tq : tqs.values()){
			if (tq != null && tq.contains(p)) return true;
		}
		return false;
	}

	/**
	 * Remove the player from the queue
	 * @param player
	 * @return The ParamTeamPair object if the player was found.  Otherwise returns null
	 */
	public synchronized ParamTeamPair removeFromQue(ArenaPlayer player) {
		for (TeamQueue tq : tqs.values()){
			if (tq != null && tq.contains(player)){
				ArenaTeam t = tq.remove(player);
				return new ParamTeamPair(tq.getMatchParams(),t);
			}
		}
		return null;
	}

	public synchronized ParamTeamPair removeFromQue(ArenaTeam team) {
		for (TeamQueue tq : tqs.values()){
			if (tq.remove(team) != null){
				return new ParamTeamPair(tq.getMatchParams(),team);}
		}
		return null;
	}

	public QueueResult getQueuePos(ArenaPlayer p) {
		for (TeamQueue tq : tqs.values()){
			QueueResult qtp = tq.getPos(p);
			if (qtp != null)
				return qtp;
		}
		return null;
	}

	public QueueObject getQueueObject(ArenaPlayer p) {
		for (TeamQueue tq : tqs.values()){
			for (QueueObject qo: tq){
				if (qo.hasMember(p))
					return qo;
			}
		}
		return null;
	}
	public synchronized void stop() {
		suspend = true;
		notifyAll();
	}

	public synchronized void resume() {
		suspend = false;
		notifyAll();
	}

	public synchronized Collection<ArenaTeam> purgeQueue(){
		List<ArenaTeam> teams = new ArrayList<ArenaTeam>();
		synchronized(ready_matches){
			for (Match m: ready_matches){
				teams.addAll(m.getTeams());
				m.cancelMatch();}
			ready_matches.clear();
		}
		synchronized(tqs){
			for (ArenaType mp : tqs.keySet()){
				TeamQueue tq = tqs.get(mp);
				teams.addAll(tq.getTeams());
			}
			tqs.clear();
		}
		return teams;
	}

	public Arena getNextArena(MatchParams mp) {
		synchronized(arenaqueue){
			for (Arena a : arenaqueue){
				if (!a.valid() || !a.matches(mp,null))
					continue;
				arenaqueue.remove(a);
				return a;
			}
		}
		return null;
	}

	public Arena removeArena(Arena arena) {
		synchronized(arenaqueue){
			for (Arena a : arenaqueue){
				if (a.getName().equalsIgnoreCase(arena.getName())){
					arenaqueue.remove(a);
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
		StringBuilder sb = new StringBuilder();
		sb.append(queuesToString());
		sb.append(toStringArenas());
		sb.append(toStringReadyMatches());
		return sb.toString();
	}

	public String toStringReadyMatches(){
		StringBuilder sb = new StringBuilder();
		sb.append("------ ready matches ------- \n");
		synchronized(ready_matches){
			for (Match am : ready_matches){
				sb.append(am +"\n");}}
		return sb.toString();
	}

	public String toStringArenas(){
		StringBuilder sb = new StringBuilder();
		sb.append("------AMQ Arenas------- \n");
		synchronized(arenaqueue){
			for (Arena arena : arenaqueue){
				sb.append(arena +"\n");}}
		return sb.toString();
	}

	public String queuesToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("------queues------- \n");
		synchronized(tqs){
			for (ArenaType tq : tqs.keySet()){
				for (QueueObject t: tqs.get(tq)){
					sb.append(tq + " : " + t +"\n");}}}
		return sb.toString();
	}

	public Collection<ArenaPlayer> getPlayersInAllQueues() {
		Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		synchronized(tqs){
			for (TeamQueue tq: tqs.values()){
				players.addAll(tq.getArenaPlayers());}
		}
		return players;
	}
	public List<String> invalidReason(QueueObject qo){
		List<String> reasons = new ArrayList<String>();
		MatchParams params = qo.getMatchParams();
		synchronized(arenaqueue){
			for (Arena arena : arenaqueue){
				reasons.addAll(arena.getInvalidMatchReasons(params, qo.getJoinOptions()));
			}
		}
		return reasons;
	}

	public void removeAllArenas() {
		synchronized(arenaqueue){
			arenaqueue.clear();
		}
	}

	public void removeAllArenas(ArenaType arenaType) {
		synchronized(arenaqueue){
			Iterator<Arena> iter = arenaqueue.iterator();
			Arena a = null;
			while (iter.hasNext()){
				a = iter.next();
				if (a.getArenaType() == arenaType)
					iter.remove();
			}
		}
	}

	public void clearTeamQueues() {
		synchronized(tqs){
			tqs.clear();
		}
		for (IdTime idt: forceTimers.values()){
			Bukkit.getScheduler().cancelTask(idt.id);}
		forceTimers.clear();
	}

	public int getMatchingQueueSize(MatchParams mp) {
		TeamQueue tq = getTeamQ(mp);
		if (tq == null)
			return -1;
		int size = 0;
		synchronized(tq){
			for (QueueObject qo: tq){
				JoinOptions jo = qo.getJoinOptions();
				if (qo instanceof MatchTeamQObject || (jo != null && jo.hasOption(JoinOption.ARENA)))
					continue;
				size += qo.getTeams().size();
			}
		}
		return size;
	}

	public boolean forceStart(MatchParams mp, boolean force) {
		TeamQueue tq = getTeamQ(mp);
		if (tq == null)
			return false;
		QueueResult qr = null;
		/// try to find it without forcing first
		if (force){
			qr = findMatch(tq, true, false);}
		/// Try to find it with force option
		if (qr == null)
			qr = findMatch(tq, true, force);
		if (qr.match == null)
			return false;

		addToReadyMatches(qr.match);
		return true;
	}

	public Collection<ArenaPlayer> getPlayersInQueue(MatchParams params) {
		TeamQueue tq = getTeamQ(params);
		return tq == null ? null : tq.getArenaPlayers();
	}

}