package mc.alk.arena.objects.queues;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.BlankCompetition;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinStatus;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.messaging.MatchMessageImpl;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.pairs.JoinResult.JoinStatus;
import mc.alk.arena.objects.pairs.JoinResult.TimeStatus;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.queues.TeamQueue.TeamQueueComparator;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamFactory;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ArenaMatchQueue implements ArenaListener{
	static final boolean DEBUG = false;

	final Map<ArenaType, TeamQueue> tqs = new HashMap<ArenaType, TeamQueue>();
	final Map<ArenaType, Map<Arena,TeamQueue>> aqs = new HashMap<ArenaType, Map<Arena,TeamQueue>>();
	final Map<ArenaType, IdTime> forceTimers = Collections.synchronizedMap(new HashMap<ArenaType, IdTime>());
	final Map<String, IdTime> arenaForceTimers = Collections.synchronizedMap(new HashMap<String, IdTime>());
	final Lock lock = new ReentrantLock();
	final Condition empty = lock.newCondition();
	final protected MethodController methodController = new MethodController("QC");

	public ArenaMatchQueue(){
		methodController.addAllEvents(this);
	}
	public enum QueueType{
		GAME,ARENA
	}

	public static class IdTime{
		public Countdown c;
		public Long time;
	}

	final ArenaQueue arenaqueue = new ArenaQueue();

	final LinkedList<Match> ready_matches = new LinkedList<Match>();

	AtomicBoolean suspend = new AtomicBoolean();

	public Match getArenaMatch() {
		lock.lock();
		try{
			while (ready_matches.isEmpty())
				empty.awaitNanos(30000000); /// Technically this could wait forever, but just in case.. check occasionally
			Match m = null;
			if (!ready_matches.isEmpty() && !suspend.get()){
				m = ready_matches.removeFirst();
			}
			return m;
		} catch(InterruptedException e) {
			System.err.println("InterruptedException caught");
		} finally{
			lock.unlock();
		}
		return null;
	}

	public void add(Arena arena, boolean checkStart) {
		synchronized(arenaqueue){
			arenaqueue.addLast(arena);
		}
		if (!suspend.get())
			notifyIfNeeded();
	}

    private JoinResult addToGameQueue(final QueueObject queueObject, boolean checkStart) {
		return addToQueue(queueObject,checkStart);
	}

	/**
	 * Add a matchup of teams.  They already have all required teams, so just need to wait for an open arena
	 * @param matchup Matchup
	 * @return JoinResult
	 */
	public JoinResult addMatchup(Matchup matchup, boolean checkStart) {
		return addToQueue(new MatchTeamQObject(matchup), checkStart);
	}

	private JoinResult addToQueue(final QueueObject to, boolean checkStart) {
		TeamCollection tq = getTeamQ(to.getMatchParams(), to.getJoinOptions().getArena());
		tq.add(to);
		IdTime idt = null;
		if (to instanceof TeamJoinObject){
			idt = updateTimer(to.getJoinOptions().getArena(),tq,to);
		}
		/// return if we aren't going to check for a start (aka we already have too many matches running)
		JoinResult jr;
		if (!suspend.get() && checkStart){
			jr = joinQueue(tq,to);
		} else {
			jr = new JoinResult();
			jr.params = tq.getMatchParams();
			jr.maxPlayers = tq.getMatchParams().getMaxPlayers();
		}

		if (idt != null)
			jr.time = idt.time;

		return jr;
	}

	class AnnounceInterval {
		AnnounceInterval(final MatchParams mp, final ArenaMatchQueue amq, final TeamCollection tq, final Arena a, final long time){
			final Countdown c = new Countdown(BattleArena.getSelf(),
					(int) time, 30, new CountdownCallback(){
				@Override
				public boolean intervalTick(int secondsRemaining) {
					if (secondsRemaining > 0 || secondsRemaining < 0){
						//								MatchParams mp = tq.getMatchParams();
						Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
						if (a!=null){
							TeamQueue tq2 = amq.getArenaTeamQ(mp, a);
							if (tq2!=null){
								players.addAll(tq2.getArenaPlayers());}
						}
						players.addAll(amq.getTeamQ(mp, null).getArenaPlayers());
						String msg = BAExecutor.constructMessage(mp, secondsRemaining*1000L, players.size(), null);
						MessageUtil.sendMessage(players, msg);
					} else {
						/// Timer expired, find a match
						JoinResult qr = findMatch(tq,true, true,a);
						if (qr.matchfind == null || qr.matchfind.match == null){
							/// we found no match.. should we cancel
							if (a != null && mp.isCancelIfNotEnoughPlayers()){
								amq.cancel(a);
								removeTimer(a,mp);
							}
						}
					}
					return true;
				}
			});
			IdTime idt = new IdTime();
			idt.time = System.currentTimeMillis() + time*1000;
			//			final ArenaMatchQueue amq = this;
			//			new AnnounceInterval(this, tq, a, params.getForceStartTime());
			//			final Countdown c = new Countdown(BattleArena.getSelf(),params.getForceStartTime())
			//					(int) (long)params.getForceStartTime(), 30, new AnnounceInterval());
			c.setCancelOnExpire(false);
			idt.c = c;
			putTimer(idt,a,tq.getMatchParams());
		}
	}
	/**
	 * Update the forceJoin timer for the following TeamQueue and the given QueueObject
	 * The time will not be updated if an older timer is ongoing
	 * @param tq TeamCollection
	 * @param to QueueObject
	 * @return IdTime
	 */
	private IdTime updateTimer(final Arena a, final TeamCollection tq, final QueueObject to) {
		JoinOptions jo = to.getJoinOptions();
		if (jo.getJoinTime() == null)
			return null;
		IdTime idt = getTime(a,tq.getMatchParams());
		if (idt == null){
			MatchParams params = a != null ? a.getParams() : tq.getMatchParams();
			long now = System.currentTimeMillis();
			Long time = (now - jo.getJoinTime() + params.getForceStartTime()*1000);
			if (time > 0){
				idt = new IdTime();
				idt.time = now + time;
				//				final ArenaMatchQueue amq = this;
				new AnnounceInterval(params, this, tq, a, params.getForceStartTime());
				//				final Countdown c = new Countdown(BattleArena.getSelf(),params.getForceStartTime())
				//						(int) (long)params.getForceStartTime(), 30, new AnnounceInterval());
				//				c.setCancelOnExpire(false);
				//				idt.c = c;
				//				putTimer(idt,a,tq.getMatchParams());
			}

		}
		return idt;
	}

	private void cancel(Arena arena) {
		Map<Arena,TeamQueue> map = aqs.get(arena.getArenaType());
		if (map == null || !map.containsKey(arena))
			return;
		TeamQueue tq = map.get(arena);
		Collection<ArenaPlayer> players = new ArrayList<ArenaPlayer>(tq.getArenaPlayers());
		final MatchParams params = tq.getMatchParams();
		for (ArenaPlayer ap: players){
			ArenaPlayerLeaveEvent event = new ArenaPlayerLeaveEvent(ap, ap.getTeam(),
					ArenaPlayerLeaveEvent.QuitReason.OTHER);
			callEvent(event);
		}
		tq.clear();
		MessageUtil.sendMessage(players,
				MessageHandler.getSystemMessage(params, "cancelled_lack_of_players"));

	}

	private void removeTimer(Arena a, MatchParams p){
		IdTime idt;
		if (a == null){
			idt = forceTimers.remove(p.getType());
		} else {
			idt= arenaForceTimers.remove(a.getName());
		}
		if (idt != null && idt.c != null){
			idt.c.stop();
		}
	}

	private void putTimer(IdTime idt, Arena a, MatchParams p){
		if (a == null){
			forceTimers.put(p.getType(),idt);
		} else {
			arenaForceTimers.put(a.getName(), idt);
		}
	}

	private IdTime getTime(Arena a, MatchParams p){
		return a == null ?
				forceTimers.get(p.getType()) :
					arenaForceTimers.get(a.getName());
	}

	public void setForcestartTime(Arena arena, MatchParams p, Long forceStartTime) {
		removeTimer(arena,p);
		TeamCollection tq = getTeamQ(p, arena);
        new AnnounceInterval(p, this, tq, arena, forceStartTime);
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
			notified |= joinQueue(tq,null).matchfind != null;
		}

		if (!aqs.isEmpty()){
			for (Map<Arena, TeamQueue> map: aqs.values()){
				for (TeamQueue tq: map.values()){
					if (tq == null || tq.isEmpty())
						continue;
					notified |= joinQueue(tq,null).matchfind != null;
				}
			}
		}

		return notified;
	}

	private JoinResult joinQueue(TeamCollection tq, QueueObject qo) {
		if (tq==null || arenaqueue.isEmpty() || tq.isEmpty()){
			JoinResult jr = new JoinResult();
            if (tq != null){
                jr.params = tq.getMatchParams();
                jr.maxPlayers = tq.getMatchParams().getMaxPlayers();
            }
			return jr;
		}

        return qo == null ?  findMatch(tq, false, true,null)
                : findMatch(tq, false, true,qo.getJoinOptions().getArena());
	}

	private void addToReadyMatches(Match match){
		//		synchronized(ready_matches){
		lock.lock();
		try {
			ready_matches.addLast(match);
			empty.signal();
		} finally {
			lock.unlock();
		}
		//		}
		//	ready_matches.notifyAll();
	}

	public void fillMatch(ArenaMatch arenaMatch) {
		TeamJoinHandler jh = arenaMatch.getTeamJoinHandler();
		MatchParams mp = arenaMatch.getParams();
		TeamCollection tq = getTeamQ(mp, arenaMatch.getArena());
		for (QueueObject qo : tq){
			if (! (qo instanceof TeamJoinObject)){
				continue;}

			if (mp.matches(qo.getMatchParams())){
				jh.joiningTeam((TeamJoinObject) qo);
			}
			if (jh.isFull())
				break;
		}
	}

	/**
	 * @param tq : Queue we are searching for matches
	 * @param forceStart : whether we are trying to start a match that usually would need more players
     * @param forceStartRespectGameSize	 * @param forceStartRespectGameSize
     * @param specificArena finding for a specific arena
     * @return Match if one can be created from the specified TeamQueue
	 */
	private JoinResult findMatch(final TeamCollection tq, boolean forceStart,
			boolean forceStartRespectGameSize, Arena specificArena) {
		JoinResult qr = new JoinResult();
		qr.params = tq.getMatchParams();
		qr.maxPlayers = tq.getMatchParams().getMaxPlayers();

		if (suspend.get())
			return qr;
		JoinResult oqr = qr;
		synchronized(tqs){ synchronized(aqs){
			if (Defaults.DEBUGQ) System.out.println("findMatch " + tq +"  " + tq.size() +
					"  mp=" + tq.getMatchParams() +"  specificArena="+specificArena);
			/// The idea here is we iterate through all arenas
			/// See if one matches with the type of TeamQueue that we have been given
			/// Then we make sure those players are ready, and if not send them messages
			final MatchParams gameParams = ParamController.getMatchParamCopy(tq.getMatchParams().getType());
			if (gameParams==null){
				Log.err("[BA Error] gameParams null for " + tq.getMatchParams().getType());
			}
			//		final MatchParams baseParams = new MatchParams(tq.getMatchParams());


			qr.status = JoinStatus.ADDED_TO_QUEUE;
			qr.pos = tq.size();
			qr.playersInQueue = tq.playerSize();
			if (gameParams != null){
				qr.maxPlayers = gameParams.getMaxPlayers();
				if (!gameParams.getMinPlayers().equals(gameParams.getMaxPlayers()))
					qr.timeStatus = TimeStatus.CANT_FORCESTART;
			}
			qr.params = gameParams;

			qr.teamsInQueue = tq.teamSize();
			IdTime idt =  getTime(specificArena, tq.getMatchParams());
			if (idt != null){
				qr.time = idt.time;
				if (idt.time <= System.currentTimeMillis()){
					forceStart = true;
					qr.timeStatus = TimeStatus.TIME_EXPIRED;
				} else {
					qr.timeStatus = TimeStatus.TIME_ONGOING;
				}
			}
			if (idt == null || idt.time == null){
				qr.timeStatus = TimeStatus.CANT_FORCESTART;
			}
			if (forceStart){
				gameParams.setMinTeamSize(forceStartRespectGameSize ? gameParams.getMinTeamSize() : 1);
				gameParams.setMinTeams(forceStartRespectGameSize ? gameParams.getMinTeams():
					Math.min(gameParams.getMinTeams(),2));
			}
			if (qr.timeStatus != TimeStatus.CANT_FORCESTART){
				qr.timeStatus = forceStart ? TimeStatus.TIME_EXPIRED : TimeStatus.TIME_ONGOING;
			}

			boolean skipNonMatched = false;

			synchronized(arenaqueue){
				for (Arena a : arenaqueue){
					if (a == null || (a.getArenaType() != gameParams.getType()) || !a.valid() || !a.isOpen())
						continue;
					if (specificArena != null && !specificArena.equals(a))
						continue;
					qr.params = a.getParams();
					MatchParams newParams = a.getParams();
					if (Defaults.DEBUGQ) Log.info("----- AMQ check matches="+ a.matches(gameParams, null) + " arena = " + a +
							"   tq=" + tq +" --- ap="+a.getParams().toPrettyString() +"    baseP="+gameParams.toPrettyString() +
							" tqparams="+tq.getMatchParams().toPrettyString());
					/// Get both the ArenaQueue and GameQueue, or just the teamQueue
					final TeamCollection iterate;
					final TeamCollection t1;
					TeamCollection t2 = null;
					if (tq instanceof CompositeTeamQueue){
						iterate = tq;
						t1 = ((CompositeTeamQueue) tq).queues[0];
						t2 = ((CompositeTeamQueue) tq).queues[1];
					} else {
						TeamQueue aq = getArenaTeamQ(gameParams, a);
						if (aq != null && tq != aq){
							iterate = new CompositeTeamQueue(aq,tq);
							t1 = aq;
							t2 = tq;
						} else {
							iterate = tq;
							t1 = tq;
						}
					}
					if (Defaults.DEBUGQ) Log.info("----- finding appropriate Match arena = " + MatchMessageImpl.decolorChat(a.toString())+
							"   tq=" + tq +" --- ap="+a.getParams().toPrettyString() +"    baseP="+gameParams.toPrettyString() +
							" newP="+newParams.toPrettyString() +"  " + newParams.getMaxPlayers() +
							" tqparams="+tq.getMatchParams().toPrettyString());
					synchronized(t1){
						if (t2 != null){
							synchronized(t2){
								qr = finding(forceStart, forceStartRespectGameSize,gameParams, qr, skipNonMatched, a, newParams, iterate);
							}
						} else {
							qr = finding(forceStart, forceStartRespectGameSize,gameParams, qr, skipNonMatched, a, newParams, iterate);
						}
					}

					if (qr == null)
						qr = oqr;
					if (qr.matchfind != null && qr.matchfind.match != null){
						arenaqueue.remove(a);
						break;
					}
					if (Defaults.USE_ARENAS_ONLY_IN_ORDER && !forceStart){ /// Only check the first valid arena
						skipNonMatched = true;}
				}
			}

		}}
		if (qr.matchfind != null && qr.matchfind.match != null){
			addToReadyMatches(qr.matchfind.match);
			qr.matchfind.sendLeaveQueueMessages();
			qr.playersInQueue = tq.playerSize();
		}
		///Found nothing matching
		return qr;
	}

	private JoinResult finding(boolean forceStart,
			boolean forceStartRespectGameSize, final MatchParams gameParams, JoinResult qr,
			boolean skipNonMatched, Arena a, MatchParams newParams,
			final TeamCollection iterate) {
		for (QueueObject qo : iterate){
			/// Check if we should only use 1 arena (skipNonMatched == true).  But for certain elements in the queue
			/// We need to ignore.
			/// Allow MatchedUp Matches to proceed like normal (this happens for tournaments and duels)
			/// Allow people that selected particular join options (like certain arenas) to get a chance
			if (skipNonMatched && qo instanceof TeamJoinObject){
				JoinOptions jpo = qo.getJoinOptions();
				if (!jpo.matches(newParams))
					continue;
			}
			try {
				MatchParams playerMatchAndArenaParams = ParamController.copyParams(newParams);
				if (forceStart && !forceStartRespectGameSize){
					playerMatchAndArenaParams.setNTeams(gameParams.getNTeams());
					playerMatchAndArenaParams.setTeamSizes(gameParams.getTeamSizes());
				}
				qr.maxPlayers = playerMatchAndArenaParams.getMaxPlayers();
				findMatch(qr, playerMatchAndArenaParams,a,iterate, forceStartRespectGameSize, forceStart);
			} catch (NeverWouldJoinException e) {
				Log.printStackTrace(e);
				continue;
			}
			if (qr.matchfind != null && qr.matchfind.match != null){
				return qr;
			}
		}
		return null;
	}

	public class MatchFind{
        Match match;
		Collection<ArenaTeam> teams;
		int tqPlayerSize;

		public void sendLeaveQueueMessages(){
			/// send out queue events now that the queue has been resized
			for (ArenaTeam t: teams){
				for (ArenaPlayer ap :t.getPlayers()){
					ParamTeamPair ptp = new ParamTeamPair(match.getParams(),t,
							QueueType.GAME, match.getArena(), tqPlayerSize);
					leaveQueue(ap, t, match.getParams(),ptp);
				}
			}
		}
        public Match getMatch() {return match;}
        public void setMatch(Match match) {this.match = match;}
    }

	/**
	 * For these parameters, go through each team to see if we can create a viable match
	 * @param result JoinResult
	 * @param params MatchParams
     * @param arena Arena
     * @param tq TeamCollection
	 * @param forceStart force start
	 * @throws NeverWouldJoinException
	 */
	private void findMatch(JoinResult result, final MatchParams params, Arena arena, final TeamCollection tq,
			boolean forceStartRespectGameSize, boolean forceStart) throws NeverWouldJoinException {
		/// Move all teams with the same team size together
		List<QueueObject> newList = new LinkedList<QueueObject>();
		List<QueueObject> delayed = new LinkedList<QueueObject>();
		int teamsInQueue = 0, playersInQueue = 0;
		boolean hasPrematched = false;
		for (QueueObject qo : tq){
			if (qo instanceof MatchTeamQObject)
				hasPrematched = true;
			MatchParams mp = qo.getMatchParams();
            if (forceStart && !mp.matchesIgnoreSizes(params)) {
                continue;
            } else if (!mp.matchesIgnoreNTeams(params)){
				continue;
            }
			if (!mp.getMinTeamSize().equals(params.getMinTeamSize())){
				delayed.add(qo);
			} else {
				newList.add(qo);
			}
			playersInQueue += qo.size();
			teamsInQueue++;
		}
		result.teamsInQueue = teamsInQueue;
		result.playersInQueue = playersInQueue;
		//		if ( (!forceStart && newParams.getMaxPlayers() > iterate.playerSize()) ||
		//		(forceStart && gameParams.getMinPlayers() > iterate.playerSize())))
		//	continue;

		if (!hasPrematched &&
				((forceStart && playersInQueue < params.getMinPlayers()) ||
						(!forceStart && playersInQueue < params.getMaxPlayers()))) /// we don't have enough players to match these params
			return;

		newList.addAll(delayed);

		Map<ArenaPlayer, QueueObject> qteams = new HashMap<ArenaPlayer, QueueObject>();
		Competition comp = new BlankCompetition(params);

		teamsInQueue = 0;
		playersInQueue = 0;
		TeamJoinHandler tjh = TeamJoinFactory.createTeamJoinHandler(params, null);
		boolean hasComp = false;
		/// Now that we have a semi sorted list, get the largest number of teams that fit in this arena
		for (QueueObject qo : newList){
			if (qo instanceof MatchTeamQObject){ /// we have our teams already
				tjh.deconstruct();
				result.status = JoinResult.JoinStatus.ADDED_TO_QUEUE;
				MatchFind mf = new MatchFind();
				mf.match = getPreMadeMatch(tq, arena, qo);
				mf.teams = mf.match.getTeams();
				result.matchfind = mf;

				return;
			} else {
				TeamJoinObject to = (TeamJoinObject) qo;
				ArenaTeam t = to.getTeam();
				JoinOptions jp = qo.getJoinOptions();
                if (jp != null && !(jp.matches(arena) && jp.matches(params) && arena.matchesIgnoreSize(params, jp))){
					continue;}

				TeamJoinResult tjr = tjh.joiningTeam(to);
				if (tjr.status == TeamJoinStatus.CANT_FIT){
					continue;}
				teamsInQueue++;
				playersInQueue += t.size();
				result.pos = playersInQueue;
				result.playersInQueue = playersInQueue;
				for (ArenaPlayer ap: t.getPlayers())
					qteams.put(ap, to);
			}
			/// If we have enough teams, mark this as a valid competition and keep going
			if (tjh.hasEnough(params.getAllowedTeamSizeDifference())){
				hasComp = true;
				List<ArenaTeam> l = new ArrayList<ArenaTeam>(tjh.getTeams().size());
				for (ArenaTeam at: tjh.getTeams()){
					ArenaTeam t = TeamFactory.createCompositeTeam();
					t.addPlayers(at.getPlayers());
					l.add(t);
				}
				comp.setTeams(l);
				if (tjh.isFull()){
					break;
				}
			}
		}

		///remove the arena from the queue, start up our match
		if (hasComp){
			result.status = JoinResult.JoinStatus.STARTED_NEW_GAME;
			result.matchfind = getMatchAndRemove(tq, tjh, comp.getTeams(), qteams, arena, params);
			result.params = params;
			return;
		}

		tjh.deconstruct();
	}

	private MatchFind getMatchAndRemove(TeamCollection tq, TeamJoinHandler tjh, List<ArenaTeam> teams,
			Map<ArenaPlayer, QueueObject> qteams, Arena arena, MatchParams params) {
		final Match m = new ArenaMatch(arena, params);
		for (ArenaTeam t: teams){
			for (ArenaPlayer ap :t.getPlayers()){
				tq.remove(qteams.get(ap));
				ap.addCompetition(m);
			}
			t.setCurrentParams(params);
		}
		MatchFind mf = new MatchFind();
		mf.tqPlayerSize = tq.playerSize();

//		if (	m.getParams().getAlwaysOpen() ||
//				m.getParams().getTransitionOptions().hasOptionAt(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN) ||
//				(m.hasWaitroom() && !tjh.isFull()) ){
			try {
				m.setTeamJoinHandler(TeamJoinFactory.createTeamJoinHandler(params, m));
			} catch (NeverWouldJoinException e) {
				Log.printStackTrace(e);
			}
//		}
		//		m.onJoin(teams);
		m.setOriginalTeams(teams);
		mf.teams = teams;
		mf.match = m;
		tjh.deconstruct(); /// now with alwaysJoin I believe this should go away
		/// reset the timer
		removeTimer(m.getArena(), m.getParams());
		//		arenaForceTimers.remove(m.getArena().getName());
		return mf;
	}

	protected void leaveQueue(ArenaPlayer ap, ArenaTeam t, MatchParams params, ParamTeamPair ptp) {
	}

	private Match getPreMadeMatch(TeamCollection tq,  Arena arena, QueueObject tto) {
		MatchTeamQObject to = (MatchTeamQObject) tto;
		if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + arena  +"   tq=" + tq);
		Matchup matchup = to.getMatchup();
		tq.remove(to);
		final Match m = new ArenaMatch(arena, to.getMatchParams());
		m.setTeamJoinHandler(null); /// we don't want any custom team joining.
		m.addArenaListeners(matchup.getArenaListeners());
		//		m.onJoin(matchup.getTeams());
		m.setOriginalTeams(matchup.getTeams());
		for (ArenaTeam at: matchup.getTeams()){
			for (ArenaPlayer ap: at.getPlayers()){
				ap.addCompetition(m);
			}
		}
		if (matchup.getMatchCreationListener() != null){
			matchup.getMatchCreationListener().matchCreated(m, matchup);
		}
		matchup.addMatch(m);
		return m;
	}

	private TeamCollection getTeamQ(MatchParams mp, Arena arena) {
		if (arena != null){
			TeamQueue tq = getOrCreateArenaTeamQ(arena);
			return new CompositeTeamQueue(tq,getOrCreateGameQ(mp));
		} else {
			return getOrCreateGameQ(mp);
		}
	}

	private TeamQueue getArenaTeamQ(MatchParams mp, Arena arena) {
		return aqs.containsKey(arena.getArenaType()) ? aqs.get(arena.getArenaType()).get(arena) : null;
	}

	private TeamQueue getOrCreateArenaTeamQ(Arena arena) {
		if (aqs.containsKey(arena.getArenaType())){
			if (aqs.get(arena.getArenaType()).containsKey(arena)){
				return aqs.get(arena.getArenaType()).get(arena);
			} else {
				TeamQueue tq = new TeamQueue(arena.getParams(), new TeamQueueComparator());
				aqs.get(arena.getArenaType()).put(arena, tq);
				return tq;
			}
		} else {
			HashMap<Arena,TeamQueue> map = new HashMap<Arena,TeamQueue>();
			aqs.put(arena.getArenaType(), map);
			TeamQueue tq = new TeamQueue(arena.getParams(), new TeamQueueComparator());
			map.put(arena, tq);
			return tq;
		}
	}

	private TeamQueue getOrCreateGameQ(ArenaParams mp) {
		if (tqs.containsKey(mp.getType())){
			return tqs.get(mp.getType());
		} else {
			TeamQueue tq = new TeamQueue(ParamController.getMatchParamCopy(mp.getType()), new TeamQueueComparator());
			synchronized(tqs){

				tqs.put(mp.getType(), tq);
			}
			return tq;
		}
	}

    public boolean isInQue(ArenaPlayer p) {
        synchronized (tqs) {
            for (TeamQueue tq : tqs.values()) {
                if (tq != null && tq.contains(p)) return true;
            }
        }
        synchronized (aqs) {
            for (Map<Arena, TeamQueue> map : aqs.values()) {
                for (TeamQueue tq : map.values()) {
                    if (tq != null && tq.contains(p)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove the player from the queue
     *
     * @param player ArenaPlayer
     * @return The ParamTeamPair object if the player was found.  Otherwise returns null
     */
    public ParamTeamPair removeFromQue(ArenaPlayer player) {
        ArenaTeam t;
        synchronized (tqs) {
            for (TeamQueue tq : tqs.values()) {
                if (tq == null)
                    continue;
                t = tq.remove(player);
                if (t != null) {
                    ParamTeamPair ptp = new ParamTeamPair(tq.getMatchParams(), t, QueueType.GAME, null, tq.playerSize());
                    leaveQueue(player, t, tq.getMatchParams(), ptp);
                    if (tq.isEmpty()) {
                        removeTimer(null, tq.getMatchParams());
                    }
                    return ptp;
                }
            }
        }
        synchronized (aqs) {
            for (Map<Arena, TeamQueue> map : aqs.values()) {
                for (Entry<Arena, TeamQueue> entry : map.entrySet()) {
                    TeamQueue tq = entry.getValue();
                    if (tq == null)
                        continue;
                    t = tq.remove(player);
                    if (t != null) {
                        ParamTeamPair ptp = new ParamTeamPair(tq.getMatchParams(), t, QueueType.GAME, entry.getKey(), tq.playerSize());
                        leaveQueue(player, t, tq.getMatchParams(), ptp);
                        if (tq.isEmpty()) {
                            removeTimer(entry.getKey(), tq.getMatchParams());
                        }
                        return ptp;
                    }
                }
            }
        }
        return null;
    }

	public QueueObject getQueueObject(ArenaPlayer p) {
		synchronized(tqs){
			for (TeamQueue tq : tqs.values()){
				for (QueueObject qo: tq){
					if (qo.hasMember(p))
						return qo;
				}
			}
		}
		synchronized(aqs){

			for (Map<Arena,TeamQueue> map : aqs.values()){
				for (TeamQueue tq : map.values()){
					for (QueueObject qo : tq){
						if (qo.hasMember(p))
							return qo;
					}
				}
			}
		}
		return null;
	}

	public  void stop() {
		suspend.set(true);
	}

	public  void resume() {
		suspend.set(false);
	}

	public  Collection<ArenaTeam> purgeQueue(){
		List<ArenaTeam> teams = new ArrayList<ArenaTeam>();
		synchronized(ready_matches){
			for (Match m: ready_matches){
				teams.addAll(m.getTeams());
				m.cancelMatch();}
			ready_matches.clear();
		}
		Map<ArenaPlayer,ParamTeamPair> ptps = new HashMap<ArenaPlayer,ParamTeamPair>();
		synchronized(tqs){
			for (TeamQueue tq : tqs.values()){
				for (ArenaTeam at: tq.getTeams()){
					teams.add(at);
					ParamTeamPair ptp = new ParamTeamPair(tq.getMatchParams(),at, QueueType.GAME,null,0);
					for (ArenaPlayer p: at.getPlayers()){
						ptps.put(p,ptp);
					}
				}
			}
			tqs.clear();
		}
		synchronized(aqs){
			for (Map<Arena,TeamQueue> map : aqs.values()){
				for (Entry<Arena,TeamQueue> entry : map.entrySet()){
					TeamQueue tq = entry.getValue();
					for (ArenaTeam at: tq.getTeams()){
						teams.add(at);
						ParamTeamPair ptp = new ParamTeamPair(tq.getMatchParams(),at, QueueType.ARENA,entry.getKey(),0);
						for (ArenaPlayer p: at.getPlayers()){
							ptps.put(p,ptp);
						}
					}
				}
				map.clear();
			}
			aqs.clear();
		}
		for (Entry<ArenaPlayer,ParamTeamPair> entry: ptps.entrySet()){
			leaveQueue(entry.getKey(), entry.getValue().team, entry.getValue().params,entry.getValue());
		}
		return teams;
	}

	public Arena getNextArena(MatchParams mp, JoinOptions jo) {
		synchronized(arenaqueue){

			for (Arena a : arenaqueue){
				if (!a.valid() || !a.matches(mp,jo))
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
        return queuesToString() + toStringArenas() + toStringReadyMatches();
	}

	public String toStringReadyMatches(){
		StringBuilder sb = new StringBuilder();
		sb.append("------ ready matches ------- \n");
		synchronized(ready_matches){
			for (Match am : ready_matches){
				sb.append(am).append("\n");}
		}
		return sb.toString();
	}

	public String toStringArenas(){
		StringBuilder sb = new StringBuilder();
		sb.append("------AMQ Arenas------- \n");
		synchronized(arenaqueue){

			for (Arena arena : arenaqueue){
				sb.append(arena).append("\n");}
		}
		return sb.toString();
	}

	public String queuesToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("------game queues-------\n");
		for (Entry<ArenaType,TeamQueue> entry : tqs.entrySet()){
			IdTime value = forceTimers.get(entry.getKey());
			String time = (value != null && value.time != null) ?
					((value.time - System.currentTimeMillis())/1000)+"" : "";
			for (QueueObject qo: entry.getValue()){
				sb.append(entry.getKey().getName()).append(" : fs=").append(time).append(" ").append(qo).append("\n");}}
		sb.append("------arena queues------- \n");
		for (Entry<ArenaType,Map<Arena,TeamQueue>> entry : aqs.entrySet()){
			for (Entry<Arena,TeamQueue> entry2: entry.getValue().entrySet()){
				IdTime value = arenaForceTimers.get(entry2.getKey().getName());
				String time = (value != null && value.time != null) ?
						((value.time - System.currentTimeMillis())/1000)+"" : "";
				for (QueueObject qo : entry2.getValue()){
					sb.append(entry.getKey().getName()).append(" : fs=").
                            append(time).append(" ").append(entry2.getKey().getName()).
                            append(" : ").append(qo).append("\n");
				}
			}
		}

		return sb.toString();
	}

	public Collection<ArenaPlayer> getPlayersInAllQueues() {
		Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		synchronized(tqs){
			for (TeamQueue tq: tqs.values()){
				players.addAll(tq.getArenaPlayers());}
		}
		synchronized(aqs){
			for (Map<Arena,TeamQueue> map : aqs.values()){
				for (TeamQueue tq : map.values()){
					players.addAll(tq.getArenaPlayers());}
			}
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
		arenaqueue.clear();
	}

	public void removeAllArenas(ArenaType arenaType) {
		synchronized(arenaqueue){

			Iterator<Arena> iter = arenaqueue.iterator();
            Arena a;
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
		synchronized(aqs){
			for (Map<Arena,TeamQueue> map : aqs.values()){
				for (TeamQueue tq : map.values()){
					tq.clear();}
			}
		}
		for (IdTime idt: forceTimers.values()){
			//			Bukkit.getScheduler().cancelTask(idt.id);
			idt.c.stop();
		}
		for (IdTime idt: arenaForceTimers.values()){
			//			Bukkit.getScheduler().cancelTask(idt.id);
			idt.c.stop();
		}
		forceTimers.clear();
		arenaForceTimers.clear();
	}

	public boolean forceStart(MatchParams mp, boolean respectMinimumPlayers) {
		TeamQueue tq = getOrCreateGameQ(mp);
		if (tq == null)
			return false;
		JoinResult qr = null;
		/// try to find it without forcing first
		if (respectMinimumPlayers){
			qr = findMatch(tq, true, true,null);}
		/// Try to find it with force option
		if (qr == null)
			qr = findMatch(tq, true, false,null);
		return qr.matchfind != null;
	}

	public Collection<ArenaPlayer> getPlayersInQueue(MatchParams params) {
		TeamQueue tq = getOrCreateGameQ(params);
		return tq == null ? null : tq.getArenaPlayers();
	}

	public boolean hasArenaQueue(Arena arena) {
		return aqs.containsKey(arena.getArenaType());
	}

	public boolean hasGameQueue(MatchParams matchParams) {
		return (tqs.containsKey(matchParams.getType()) || matchParams.hasQueue());
	}

	public JoinResult join(TeamJoinObject tqo, boolean shouldStart) {
		JoinOptions jo = tqo.getJoinOptions();
		/// can they join a specific arena queue
		if (jo.hasArena() && hasArenaQueue(jo.getArena())){
			return addToGameQueue(tqo, shouldStart);}

		/// Can they start a new game
		if (jo.hasArena() && tqo.hasStartPerms()){
		}

		/// Can they join the game queue
		if (hasGameQueue(tqo.getMatchParams())){
			return addToGameQueue(tqo, shouldStart);}

		return new JoinResult();
	}

	protected void callEvent(BAEvent event){
		methodController.callEvent(event);
	}

	public int getQueueCount(Arena arena) {
		Map<Arena,TeamQueue> map = aqs.get(arena.getArenaType());
		if (map == null || !map.containsKey(arena))
			return 0;
		return map.get(arena).playerSize();
	}

	public int getQueueCount(MatchParams params) {
		TeamQueue tq = getOrCreateGameQ(params);
		return tq == null ? 0 : tq.playerSize;
	}

	public int getAllQueueCount(ArenaParams params) {
		TeamQueue tq = getOrCreateGameQ(params);
		int size = tq == null ? 0 : tq.playerSize;
		Map<Arena,TeamQueue> map = aqs.get(params.getType());
		if (map == null)
			return size;
		synchronized(aqs){
			for (TeamQueue tq2: map.values()){
				size += tq2.playerSize();
			}
		}
		return size;
	}


}
