package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.OnMatchComplete;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.ParamTeamPair;
import mc.alk.arena.objects.QPosTeamPair;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.util.Util;


public class ArenaMatchQueue {
	static final boolean DEBUG = true;

	TreeMap<MatchParams, TeamQueue> tqs = new TreeMap<MatchParams, TeamQueue>(Collections.reverseOrder());
	TreeMap<MatchParams, MatchedTeamQueue> mtqs = new TreeMap<MatchParams, MatchedTeamQueue>(Collections.reverseOrder());

	ArenaQueue arenaqueue = new ArenaQueue();

	LinkedList<Match> ready_matches = new LinkedList<Match>();	
	OnMatchComplete omc;
	boolean suspend = false;

	public ArenaMatchQueue(OnMatchComplete omc){
		this.omc = omc;
	}

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

	public synchronized QPosTeamPair add(Team t1, MatchParams q) {
		if (!ready_matches.isEmpty())
			notifyAll();

		TeamQueue tq = getTeamQ(q);
		//		System.out.println("tq=" + tq);

		if (tq == null){
			return null;}

		tq.addLast(t1);
		if (!suspend)
			notifyIfNeeded(tq);
		QPosTeamPair qtp = new QPosTeamPair(q,tq.size(),t1);
		return qtp;
	}

	public synchronized int addMatchup(Matchup matchup) {
		if (!ready_matches.isEmpty())
			notifyAll();

		MatchParams q = matchup.getSpecificQ();
		MatchedTeamQueue mtq = getMatchedTeamQ(q);

		if (mtq == null){
			return -1;}

		mtq.addLast(matchup);
		//		System.out.println("adding matchup ="+matchup+"  :"+ q + "   mtq " + mtq);
		if (!suspend)
			notifyIfNeeded(mtq);
		return mtq.size();
	}

	/**
	 * This is called when an arena gets readded
	 * Since an arena can match many(or all) teamqueues, need to iterate over them all
	 */
	private boolean notifyIfNeeded(){
		boolean notified = false;
		///First try to get them from our already matched up inEvent
		for (MatchedTeamQueue mtq : mtqs.values()){
			if (mtq == null || mtq.isEmpty())
				continue;
			notified |= notifyIfNeeded(mtq);
		}
		for (TeamQueue tq: tqs.values()){
			if (tq == null || tq.size() < tq.getMinTeams())
				continue;

			notified |= notifyIfNeeded(tq);
		}
		return notified;
	}

	private boolean notifyIfNeeded(MatchedTeamQueue mtq) {
		if (arenaqueue.isEmpty() || mtq.isEmpty())
			return false;

		Match match = findMatch(mtq);
		//			System.out.println("notifyIfNeeded " + tq  + " tqsize="+tq.size() + "   match=" + match);
		if (match == null)
			return false;

		synchronized(ready_matches){
			ready_matches.addLast(match);	
		}
		notifyAll();
		return true;
	}

	private Match findMatch(MatchedTeamQueue mtq) {
		if (Defaults.DEBUG) System.out.println("findMatch " + mtq);

		/// The idea here is we iterate through all arenas
		/// See if one matches with the type of TeamQueue that we have been given
		/// Then we make sure those inEvent are ready, and if not send them messages
		synchronized(arenaqueue){ synchronized(mtq){
			final MatchParams mp = mtq.getMatchParams();
			for (Arena a : arenaqueue){
				if (!a.valid() || !a.matches(mp))
					continue;
				if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + a  +"   mtq=" + mtq +" matches=" + a.matches(mp));

				List<Team> teams = new ArrayList<Team>();
				Matchup matchup = mtq.getFirst();
				teams.addAll(matchup.getTeams());

				mtq.remove(matchup);
				arenaqueue.remove(a);
				final Match m = new Match(ArenaType.createArena(a), omc, matchup.getSpecificQ());
				m.onJoin(teams);
				return m;
			}
		}}
		return null;
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
		//		final int neededPlayers = mp.getMinTeams() * teamSize; /// minTeams*minPlayers
		/// The idea here is we iterate through all arenas
		/// See if one matches with the type of TeamQueue that we have been given
		/// Then we make sure those inEvent are ready, and if not send them messages
		synchronized(arenaqueue){ synchronized(tq){
			for (Arena a : arenaqueue){
				if (!a.valid() || !a.matches(mp))
					continue;
				//				final ArenaParams ap = a.getParameters();
				//				if (Defaults.DEBUG) System.out.println("----- finding appropriate Match arena = " + MessageController.decolorChat(a.toString())+
				//						"   tq=" + tq +" matches=" + ap.matches(mp) +"  mp="+mp+", --- ap="+ap +"    "+tq.size()+" <? "+ap.getMinTeams());
				/// Does our specific setting fit with this arena
				List<Team> qteams = null;
				List<Team> oteams = new ArrayList<Team>();
				if (mp.getMinTeamSize() > 1){
					qteams = tq.sortBySize();
				} else { 
					qteams = tq;
				}
				teams.clear();
				CompositeTeam cteam = null;
				for (Team t : qteams){
					if (Defaults.DEBUG) System.out.println("--"+teamSize +" " + t.size() +"  t="+t.getName() +",mp="+mp);
					if (t.size() > mp.getMaxTeamSize()){ /// can't use this team, they are too large.  If they are too small we can merge
						continue;
					} else if (t.size()== teamSize){ /// team size is just right, add them to the inEvent
						teams.add(t);
						oteams.add(t);
					} else { /// we have inEvent joining that need to be merged together to form a correctly sized team
						if (cteam == null)
							cteam = new CompositeTeam();
						/// TODO make this a bin packing problem
						cteam.addTeam(t);
						oteams.add(t);
						if (cteam.size() == teamSize){
							teams.add(cteam);
							cteam = null;
						}
					}
//					System.out.println("## teams = " + teams.size() );
					if (teams.size() == mp.getMinTeams())
						break;
				}
				if (teams.size() == mp.getMinTeams()){
					for (Team t: teams){
						if (t instanceof CompositeTeam){
							((CompositeTeam) t).finish();
						}
					}
					tq.removeAll(oteams); /// remove all competing inEvent from the q
					arenaqueue.remove(a);
					final Match m = new Match(ArenaType.createArena(a), omc, tq.getMatchParams());
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
		//		System.out.println("getTeamQ mp=" + mp +"  hash = " +mp.hashCode());
		if (teamSize != ArenaParams.NONE && teamSize != ArenaParams.ANY){
			if (tqs.containsKey(mp)){
				return tqs.get(mp);
			} else {
				TeamQueue tq = new TeamQueue(mp);
				tqs.put(mp, tq);
				return tq;
			}
		}
		return null;
	}

	private MatchedTeamQueue getMatchedTeamQ(MatchParams q) {
		final int teamSize = q.getMinTeamSize();
		if (teamSize != ArenaParams.NONE && teamSize != ArenaParams.ANY){
			if (mtqs.containsKey(q)){
				return mtqs.get(q);
			} else {
				MatchedTeamQueue tq = new MatchedTeamQueue(q);
				mtqs.put(q, tq);
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
		tq.push(t); /// TeamJoinResult team to the end of the queue
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
		int pos; 
		for (TeamQueue tq : tqs.values()){
			pos = tq.indexOf(p);
			if (pos != -1)
				return new QPosTeamPair(tq.getMatchParams(),pos,tq.get(pos));
		}
		return new QPosTeamPair();
	}

	public synchronized void stop() {
		suspend = true;
		notifyAll();
	}

	public synchronized void resume() {
		suspend = false;
		notifyAll();
	}

	public Arena getNextArena(MatchParams mp) {
		synchronized(arenaqueue){ 
			for (Arena a : arenaqueue){
				if (!a.valid() || !a.matches(mp))
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

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("------queues------- \n");
		synchronized(tqs){
			for (MatchParams tq : tqs.keySet()){
				for (Team t: tqs.get(tq)){
					sb.append(tq + " : " + t + "\n");}}}
		sb.append("------Matched Queues------- \n");
		synchronized(mtqs){
			for (MatchParams mtq : mtqs.keySet()){
				for (Matchup t: mtqs.get(mtq)){
					sb.append(mtq + " : " + t + "\n");}}}
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
		synchronized(ready_matches){
			ready_matches.clear();
		}
	}


}
