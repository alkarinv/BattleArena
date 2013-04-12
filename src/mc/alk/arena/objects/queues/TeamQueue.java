package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.pairs.QueueResult;
import mc.alk.arena.objects.teams.ArenaTeam;


public class TeamQueue extends PriorityQueue<QueueObject>{
	private static final long serialVersionUID = 1L;
	MatchParams mp;

	public TeamQueue(MatchParams mp, TeamQueueComparator teamQueueComparator) {
		super(10,teamQueueComparator);
		this.mp = mp;
	}

	public synchronized boolean contains(ArenaTeam team){
		for (QueueObject qo: this){
			if (qo.hasTeam(team))
				return true;
		}
		return false;
	}

	public synchronized boolean contains(ArenaPlayer p){
		for (QueueObject t: this){
			if (t.hasMember(p))
				return true;
		}
		return false;
	}

	public synchronized ArenaTeam remove(ArenaPlayer p){
		Iterator<QueueObject> iter = this.iterator();
		while (iter.hasNext()){
			QueueObject qo = iter.next();
			if (qo.hasMember(p)){
				iter.remove();
				return qo.getTeam(p);
			}
		}
		return null;
	}

	public synchronized ArenaTeam remove(ArenaTeam team){
		Iterator<QueueObject> iter = this.iterator();
		while (iter.hasNext()){
			QueueObject qo = iter.next();
			if (qo.hasTeam(team)){
				iter.remove();
				return team;
			}
		}
		return null;
	}

	public synchronized int indexOf(ArenaPlayer p){
		int i=0;
		for (QueueObject t: this){
			if (t.hasMember(p))
				return i;
			i++;
		}
		return -1;
	}

	public synchronized QueueResult getPos(ArenaPlayer p) {
		int i=0;
		for (QueueObject t: this){
			if (t.hasMember(p))
				return new QueueResult(null,getMatchParams(),i,getNPlayers(),t.getTeam(p), this.size());
			i++;
		}
		return null;
	}

	public MatchParams getMatchParams() {return mp;}
	public int getMinTeams() {
		return mp.getMinTeams();
	}

	public int getNPlayers(){
		ArrayList<QueueObject> teams = new ArrayList<QueueObject>(this);
		int count =0;
		for (QueueObject t: teams){
			count += t.size();
		}
		return count;
	}

	public static class TeamQueueComparator implements Comparator<QueueObject>{
		@Override
		public int compare(QueueObject arg0, QueueObject arg1) {
			Integer p1 = arg0.getPriority();
			Integer p2 = arg1.getPriority();
			return p1.compareTo(p2);
		}
	}

	public synchronized Collection<ArenaTeam> getTeams() {
		List<ArenaTeam> teams = new ArrayList<ArenaTeam>();
		for (QueueObject team: this){
			teams.addAll(team.getTeams());
		}
		return teams;
	}

	public synchronized Collection<ArenaPlayer> getArenaPlayers() {
		Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		for (QueueObject qo: this){
			for (ArenaTeam t: qo.getTeams()){
				players.addAll(t.getPlayers());
			}
		}
		return players;
	}
}
