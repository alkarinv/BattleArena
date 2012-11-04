package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.pairs.QPosTeamPair;
import mc.alk.arena.objects.queues.TeamQueue.TeamQueueComparator;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;


public class ArenaMatchQueue {
	static final boolean DEBUG = false;

	Map<MatchParams, TeamQueue> tqs = new HashMap<MatchParams, TeamQueue>();

	ArenaQueue arenaqueue = new ArenaQueue();

	LinkedList<Match> ready_matches = new LinkedList<Match>();
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
		MatchParams mp = tq.getMatchParams();
		final int teamSize = mp.getMinTeamSize();
		/// The idea here is we iterate through all arenas
		/// See if one matches with the type of TeamQueue that we have been given
		/// Then we make sure those players are ready, and if not send them messages
		synchronized(arenaqueue){ synchronized(tq){
			for (Arena a : arenaqueue){
				if (a == null || !a.valid() || !a.matches(mp,null))
					continue;
				//				if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + MatchMessageImpl.decolorChat(a.toString())+
				//						"   tq=" + tq +" matches=" + ap.matches(mp) +"  mp="+mp+", --- ap="+ap +"    "+tq.size()+" <? "+ap.getMinTeams());
				/// Does our specific setting fit with this arena
				List<QueueObject> oteams = new ArrayList<QueueObject>();
				teams.clear();
				CompositeTeam cteam = null;
				for (QueueObject tto : tq){
					if (tto instanceof MatchTeamQObject){ /// we have our teams already
						MatchTeamQObject to = (MatchTeamQObject) tto;
						if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + a  +"   tq=" + tq +" matches=" + a.matches(mp,null));
						Matchup matchup = to.getMatchup();
						teams.addAll(to.getTeams());

						tq.remove(to);
						arenaqueue.remove(a);
						final Match m = new ArenaMatch(a, to.getMatchParams());
						m.onJoin(teams);
						m.addTransitionListeners(matchup.getTransitionListeners());
						matchup.addMatch(m);
						return m;
					} else {
						TeamQObject to = (TeamQObject) tto;
						Team t = to.getTeam();
						JoinOptions jp = t.getJoinPreferences();
						if (Defaults.DEBUG) System.out.println("--"+teamSize +" " + t.size() +"  t="+t.getName() +",mp="+mp +",jp="+ (jp == null ? "" : jp.matches(a)));
						if (jp != null && !jp.matches(a)){
							continue;}

						if (t.size() > mp.getMaxTeamSize()){ /// can't use this team, they are too large.  If they are too small we can merge
							continue;
						} else if (t.size()== teamSize){ /// team size is just right, add them to the teams
							teams.add(t);
							oteams.add(to);
						} else { /// we have teams joining that need to be merged together to form a correctly sized team
							if (cteam == null)
								cteam = new CompositeTeam();
							/// TODO make this a bin packing problem
							cteam.addTeam(t);
							oteams.add(to);
							if (cteam.size() == teamSize){
								teams.add(cteam);
								cteam = null;
							}
						}
						//						System.out.println("## teams = " + teams.size() +"   oteams="+oteams.size() +"  mp="+mp.getMinTeams());
						if (teams.size() == mp.getMinTeams())
							break;
					}
				}
				if (teams.size() == mp.getMinTeams()){
					for (Team t: teams){
						if (t instanceof CompositeTeam){
							((CompositeTeam) t).finish();
						}
					}
					//					System.out.println("" + tq +"   arena=" + a);
					tq.removeAll(oteams); /// remove all competing teams from the q
					arenaqueue.remove(a);
					final Match m = new ArenaMatch(a, tq.getMatchParams());
					m.onJoin(teams);
					return m;
				}
			}
		}}
		///Found nothing matching
		return null;
	}

	private TeamQueue getTeamQ(MatchParams mp) {
		final int teamSize = mp.getMinTeamSize();
		if (teamSize != ArenaParams.NONE && teamSize != ArenaParams.ANY){
			if (tqs.containsKey(mp)){
				return tqs.get(mp);
			} else {
				TeamQueue tq = new TeamQueue(mp, new TeamQueueComparator());
				tqs.put(mp, tq);
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
			for (MatchParams mp : tqs.keySet()){
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
			for (MatchParams tq : tqs.keySet()){
				for (QueueObject t: tqs.get(tq)){
					sb.append(tq.getName() + " : " + t + "\n");}}}
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