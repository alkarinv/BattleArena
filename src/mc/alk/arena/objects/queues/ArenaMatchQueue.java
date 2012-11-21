package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;
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
import mc.alk.arena.controllers.messaging.MatchMessageImpl;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.pairs.QPosTeamPair;
import mc.alk.arena.objects.queues.TeamQueue.TeamQueueComparator;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;


public class ArenaMatchQueue {
	static final boolean DEBUG = false;

	final Map<ArenaType, TeamQueue> tqs = new HashMap<ArenaType, TeamQueue>();

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

	public synchronized void add(Arena arena) {
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

	public synchronized QPosTeamPair add(final QueueObject queueObject) {
		return addToQueue(queueObject);
	}
	/**
	 * Add a matchup of teams.  They already have all required teams, so just need to wait for an open arena
	 * @param matchup
	 * @return
	 */
	public synchronized QPosTeamPair addMatchup(Matchup matchup) {
		return addToQueue(new MatchTeamQObject(matchup));
	}

	private synchronized QPosTeamPair addToQueue(final QueueObject to) {
		if (!ready_matches.isEmpty())
			notifyAll();

		TeamQueue tq = getTeamQ(to.getMatchParams());

		if (tq == null){
			return null;}

		tq.add(to);
		if (!suspend)
			notifyIfNeeded(tq);
		/// This is solely for displaying you are in position 2/8, your match will start when 8 players join
		/// So if this ever is a speed issue, remove
		QPosTeamPair qtp = null;
		synchronized(arenaqueue){
			for (Arena a : arenaqueue){
				if (a == null || !a.valid() || !a.matches(to.getMatchParams(), null))
					continue;
				MatchParams newParams = new MatchParams(to.getMatchParams());
				if (!newParams.intersect(a.getParameters()))
					continue;
				qtp = new QPosTeamPair(newParams,tq.size(),tq.getNPlayers(),to);
			}
		}
		if (qtp == null)
			qtp = new QPosTeamPair(to.getMatchParams(),tq.size(),tq.getNPlayers(),to);
		return qtp;
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
			notified |= notifyIfNeeded(tq);
		}
		return notified;
	}

	private boolean notifyIfNeeded(TeamQueue tq) {
		if (tq==null || arenaqueue.isEmpty() || tq.isEmpty())
			return false;

		Match match = findMatch(tq);
		//		System.out.println("notifyIfNeeded " + tq  + " tqsize="+tq.size() + "   match=" + match);
		if (match == null)
			return false;
		synchronized(ready_matches){
			ready_matches.addLast(match);
		}
		notifyAll();
		return true;
	}

	private Match findMatch(TeamQueue tq) {
		if (suspend)
			return null;
		if (Defaults.DEBUG) System.out.println("findMatch " + tq +"  " + tq.size() +"  mp=" + tq.getMatchParams());
		/// The idea here is we iterate through all arenas
		/// See if one matches with the type of TeamQueue that we have been given
		/// Then we make sure those players are ready, and if not send them messages
		final MatchParams baseParams = ParamController.getMatchParams(tq.getMatchParams().getType().getName());
		if (baseParams==null || tq.isEmpty())
			return null;
		boolean skipNonMatched = false;
		synchronized(arenaqueue){ synchronized(tq){
			for (Arena a : arenaqueue){
				if (a == null || !a.valid() || !a.matches(baseParams, null))
					continue;
				MatchParams newParams = new MatchParams(baseParams);
				if (!newParams.intersect(a.getParameters())){
					continue;}

				if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + MatchMessageImpl.decolorChat(a.toString())+
						"   tq=" + tq +" --- ap="+a.getParameters() +"    baseP="+baseParams +" newP="+newParams);
				for (QueueObject qo : tq){
					/// We want to allow MatchedUp Matches to proceed like normal (this happens for tournaments and duels)
					if (skipNonMatched && !(qo instanceof MatchTeamQObject)){
						continue;}
					MatchParams mp = qo.getMatchParams();
					if (!mp.matches(newParams))
						continue;
					Match m = null;
					try {
						MatchParams playerMatchAndArenaParams = new MatchParams(newParams);
						playerMatchAndArenaParams.intersect(mp);
						m = findMatch(playerMatchAndArenaParams,a,tq);
					} catch (NeverWouldJoinException e) {
						e.printStackTrace();
						continue;
					}
					if (m != null){
						arenaqueue.remove(a);
						return m;
					}
				}
				if (Defaults.USE_ARENAS_ONLY_IN_ORDER){ /// Only check the first valid arena
					skipNonMatched = true;}
			}
		}}
		///Found nothing matching
		return null;
	}

	/**
	 * For these parameters, go through each team to see if we can create a viable match
	 * @param mp
	 * @param a
	 * @param tq
	 * @return
	 * @throws NeverWouldJoinException
	 */
	private Match findMatch(final MatchParams params, Arena arena, final TeamQueue tq) throws NeverWouldJoinException {
		/// Move all teams with the same team size together
		List<QueueObject> newList = new LinkedList<QueueObject>();
		List<QueueObject> delayed = new LinkedList<QueueObject>();
		int totalSize =0;
		for (QueueObject qo : tq){
			MatchParams mp = qo.getMatchParams();
			if (!mp.matches(params))
				continue;
			if (mp.getMinTeamSize() != params.getMinTeamSize()){
				delayed.add(qo);
			} else {
				newList.add(qo);
			}
			totalSize += qo.size();
		}
		if (totalSize < params.getMinPlayers()) /// we don't have enough players to match these params
			return null;
		newList.addAll(delayed);

		Map<ArenaPlayer, QueueObject> qteams = new HashMap<ArenaPlayer, QueueObject>();
		Competition comp = new BlankCompetition(params);
		//		Competition lastValidComp = null;

		TeamJoinHandler tjh = TeamJoinFactory.createTeamJoinHandler(params, null);
		boolean hasComp = false;
		/// Now that we have a semi sorted list, get the largest number of teams that fit in this arena
		for (QueueObject qo : newList){
			if (qo instanceof MatchTeamQObject){ /// we have our teams already
				tjh.deconstruct();
				return getPreMadeMatch(tq, arena, qo);
			} else {
				TeamQObject to = (TeamQObject) qo;
				Team t = to.getTeam();
				JoinOptions jp = qo.getJoinOptions();
				if (jp != null &&
						!(jp.matches(arena) && jp.matches(params) && arena.matches(params, jp))){
					continue;
				}
				TeamJoinResult tjr = tjh.joiningTeam(to);
				if (tjr.status == TeamJoinStatus.CANT_FIT){
					continue;
				}
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

		/// Get rid of the TeamJoinHandler, remove the arena from the queue, start up our match
		tjh.deconstruct();
		if (hasComp){
			return getMatchAndRemove(tq, comp.getTeams(), qteams, arena, params);
		}
		return null;
	}

	private Match getMatchAndRemove(TeamQueue tq, List<Team> teams, Map<ArenaPlayer, QueueObject> oteams, Arena a, MatchParams params) {
		Set<Team> originalTeams = new HashSet<Team>();
		for (Team t: teams){
			originalTeams.add(t);
			for (ArenaPlayer ap :t.getPlayers()){
				Team originalTeam = oteams.get(ap).getTeam(ap);
				originalTeams.add(originalTeam);
				tq.remove(oteams.get(ap));
			}
		}
		final Match m = new ArenaMatch(a, params);
		m.onJoin(teams);
		m.setOriginalTeams(originalTeams);
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
		matchup.addMatch(m);
		return m;
	}

	private TeamQueue getTeamQ(MatchParams mp) {
		final int teamSize = mp.getMinTeamSize();
		if (teamSize != ArenaParams.MAX){
			if (tqs.containsKey(mp.getType())){
				return tqs.get(mp.getType());
			} else {
				TeamQueue tq = new TeamQueue(mp, new TeamQueueComparator());
				tqs.put(mp.getType(), tq);
				return tq;
			}
		}
		return null;
	}

	public synchronized boolean isInQue(ArenaPlayer p) {
		for (TeamQueue tq : tqs.values()){
			if (tq != null && tq.contains(p)) return true;
		}
		return false;
	}

	public synchronized ParamTeamPair removeFromQue(ArenaPlayer p) {
		for (TeamQueue tq : tqs.values()){
			if (tq != null && tq.contains(p)){
				Team t = tq.remove(p);
				return new ParamTeamPair(tq.getMatchParams(),t);
			}
		}
		return null;
	}

	public synchronized ParamTeamPair removeFromQue(Team t) {
		for (TeamQueue tq : tqs.values()){
			if (tq.remove(t)){
				return new ParamTeamPair(tq.getMatchParams(),t);}
		}
		return null;
	}

	public QPosTeamPair getQuePos(ArenaPlayer p) {
		//		int pos;
		for (TeamQueue tq : tqs.values()){
			QPosTeamPair qtp = tq.getPos(p);
			if (qtp != null)
				return qtp;
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

	public synchronized Collection<Team> purgeQueue(){
		List<Team> teams = new ArrayList<Team>();
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
		sb.append("------queues------- \n");
		synchronized(tqs){
			for (ArenaType tq : tqs.keySet()){
				for (QueueObject t: tqs.get(tq)){
					sb.append(tq + " : " + t +"\n");}}}
		sb.append("------AMQ Arenas------- \n");
		synchronized(arenaqueue){
			for (Arena arena : arenaqueue){
				sb.append(arena +"\n");}}
		sb.append("------ ready matches ------- \n");
		synchronized(ready_matches){
			for (Match am : ready_matches){
				sb.append(am +"\n");}}
		return sb.toString();
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
	}
}