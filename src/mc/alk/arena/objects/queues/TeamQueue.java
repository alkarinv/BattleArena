package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.pairs.QueueResult;
import mc.alk.arena.objects.teams.Team;


public class TeamQueue extends PriorityQueue<QueueObject>{
	private static final long serialVersionUID = 1L;
	MatchParams mp;

	public TeamQueue(MatchParams mp, TeamQueueComparator teamQueueComparator) {
		super(10,teamQueueComparator);
		this.mp = mp;
	}

	public synchronized boolean contains(ArenaPlayer p){
		for (QueueObject t: this){
			if (t.hasMember(p))
				return true;
		}
		return false;
	}

	public synchronized Team remove(ArenaPlayer p){
		for (QueueObject t: this){
			if (t.hasMember(p)){
				this.remove(t);
				return t.getTeam(p);
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
				return new QueueResult(getMatchParams(),i,getNPlayers(),t.getTeam(p), this.size());
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

	public synchronized Collection<? extends Team> getTeams() {
		List<Team> teams = new ArrayList<Team>();
		for (QueueObject team: this){
			teams.addAll(team.getTeams());
		}
		return teams;
	}
}
