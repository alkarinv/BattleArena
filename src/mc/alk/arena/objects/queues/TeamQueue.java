package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.teams.ArenaTeam;


public class TeamQueue extends PriorityQueue<QueueObject> implements TeamCollection{
	private static final long serialVersionUID = 1L;
	final MatchParams mp;

	final Lock lock = new ReentrantLock();
	final Condition modifiying  = lock.newCondition();
	int playerSize = 0;

	public TeamQueue(MatchParams mp, TeamQueueComparator teamQueueComparator) {
		super(10,teamQueueComparator);
		this.mp = mp;
	}

	@Override
	public synchronized boolean add(QueueObject to){
		if (to == null)
			return false;
		playerSize += to.getNumPlayers();
		return super.add(to);
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
				playerSize--;
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
				playerSize -= qo.getNumPlayers();
				return team;
			}
		}
		return null;
	}


	@Override
	public boolean remove(QueueObject queueObject) {

		if (super.remove(queueObject)){
			playerSize -= queueObject.getNumPlayers();
			return true;
		}
		return false;
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

	public synchronized JoinResult getPos(ArenaPlayer p) {
		int i=0;
		for (QueueObject t: this){
			if (t.hasMember(p))
				return new JoinResult(null,getMatchParams(),i,playerSize(),t.getTeam(p), this.size());
			i++;
		}
		return null;
	}

	public MatchParams getMatchParams() {return mp;}
	public int getMinTeams() {
		return mp.getMinTeams();
	}

	public static class TeamQueueComparator implements Comparator<QueueObject>{
		@Override
		public int compare(QueueObject arg0, QueueObject arg1) {
			Integer p1 = arg0.getPriority();
			Integer p2 = arg1.getPriority();
			int c = p1.compareTo(p2);
			if (c != 0)
				return c;
			return new Long(arg0.getJoinTime()).compareTo(arg1.getJoinTime());
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
		List<ArenaPlayer> players = new ArrayList<ArenaPlayer>();
		for (QueueObject qo: this){
			for (ArenaTeam t: qo.getTeams()){
				players.addAll(t.getPlayers());
			}
		}
		return players;
	}

	@Override
	public synchronized int playerSize() {
		return playerSize;
	}

}
