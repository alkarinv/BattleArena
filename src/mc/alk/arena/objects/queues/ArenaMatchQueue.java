package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.BlankCompetition;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.competition.util.TeamJoinHandler;
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
import mc.alk.arena.objects.teams.CompositeTeam;
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

	public synchronized QPosTeamPair add(final Team t1, final MatchParams mp) {
		return addToQueue(new TeamQObject(t1,mp));
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
		QPosTeamPair qtp = new QPosTeamPair(to.getMatchParams(),tq.size(),tq.getNPlayers(),to);
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
		if (Defaults.DEBUG) System.out.println("findMatch " + tq +"  " + tq.size() +"  mp=" + tq.getMatchParams());
		List<Team> teams = new ArrayList<Team>();
		final MatchParams mp = tq.getMatchParams();
		/// The idea here is we iterate through all arenas
		/// See if one matches with the type of TeamQueue that we have been given
		/// Then we make sure those players are ready, and if not send them messages
		synchronized(arenaqueue){ synchronized(tq){
			Map<Team, QueueObject> oteams = new HashMap<Team, QueueObject>();
			for (Arena a : arenaqueue){
				if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + MatchMessageImpl.decolorChat(a.toString())+
						"   tq=" + tq +" matches=" + a.getParameters().matches(mp) +"  mp="+mp+", --- ap="+a.getParameters() +"    "+tq.size()+" <? "+a.getParameters().getMinTeams());
				/// Does our specific setting fit with this arena
				if (a == null || !a.valid() || !a.matches(mp,null))
					continue;
				teams.clear();
				Competition comp = new BlankCompetition(mp);
				TeamJoinHandler tjh = null;
				try {
					tjh = TeamJoinFactory.createTeamJoinHandler(comp);
				} catch (NeverWouldJoinException e) {
					e.printStackTrace();
					continue;
				}
				for (QueueObject tto : tq){
					if (tto instanceof MatchTeamQObject){ /// we have our teams already
						MatchTeamQObject to = (MatchTeamQObject) tto;
						if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + a  +"   tq=" + tq +" matches=" + a.matches(mp,null));
						Matchup matchup = to.getMatchup();
						tq.remove(to);
						arenaqueue.remove(a);
						final Match m = new ArenaMatch(a, to.getMatchParams());
						m.setTeamJoinHandler(null); /// we don't want any custom team joining.
						m.addTransitionListeners(matchup.getTransitionListeners());
						m.onJoin(to.getTeams());
						matchup.addMatch(m);
						tjh.deconstruct();
						return m;
					} else {
						TeamQObject to = (TeamQObject) tto;
						Team t = to.getTeam();
						JoinOptions jp = t.getJoinPreferences();
						if (jp != null && !jp.matches(a)){
							continue;}
						tjh.joiningTeam(t);
						if (!oteams.containsKey(t))
							oteams.put(t, to);
					}
				}

				teams = comp.getTeams();
				if (tjh.hasEnough()){ /// If we have enough teams, remove the QueueObject's that represent them
					for (Team t: teams){
						CompositeTeam ct = (CompositeTeam) t;
						ct.finish();
						for (Team tt: ct.getOldTeams()){
							QueueObject qo = oteams.get(tt);
							tq.remove(qo);
						}
					}
					/// Get rid of the TeamJoinHandler, remove the arena from the queue, start up our match
					tjh.deconstruct();
					arenaqueue.remove(a);
					final Match m = new ArenaMatch(a, tq.getMatchParams());
					m.onJoin(teams);
					return m;
				}
				tjh.deconstruct();
			}
		}}
		///Found nothing matching
		return null;
	}

	private TeamQueue getTeamQ(MatchParams mp) {
		final int teamSize = mp.getMinTeamSize();
		if (teamSize != ArenaParams.NONE && teamSize != ArenaParams.MAX){
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

	public int addToQue(Team t, MatchParams q) {
		TeamQueue tq = getTeamQ(q);
		if (tq == null) /// no queue!!!!
			return -1;
		if (tq.contains(t)) /// already in queue
			return -1;
		tq.add(new TeamQObject(t,q)); /// TeamJoinResult team to the end of the queue
		return tq.size();
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
			//			pos = tq.indexOf(p);
			//			if (pos != -1)
			//				return new QPosTeamPair(tq.getMatchParams(),pos,tq.getNPlayers(),tq.get(pos));
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
					sb.append(tq + " : " + t + "\n");}}}
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

	public void clearTeamQueues(ArenaType arenaType) {
		synchronized(tqs){
			tqs.clear();
		}
	}


}