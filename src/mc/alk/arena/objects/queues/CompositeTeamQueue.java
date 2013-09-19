package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;


public class CompositeTeamQueue implements  TeamCollection{
	final TeamCollection[] queues;
	final MatchParams mp;

	public CompositeTeamQueue(TeamCollection ... queues) {
		this.queues = queues;
		this.mp = queues[0].getMatchParams();
	}

	public boolean add(QueueObject to) {
		queues[0].add(to);
		return true;
	}

	public int size() {
		int size = 0;
		for (TeamCollection tq: this.queues){
			size += tq.size();}
		return size;
	}

	public MatchParams getMatchParams() {
		return mp;
	}

	@Override
	public Iterator<QueueObject> iterator() {
		return new QOIterator(queues);
	}

	public boolean isEmpty() {
		return this.size() == 0;
	}

	@Override
	public boolean remove(QueueObject queueObject) {
		for (TeamCollection tq: queues){
			if (tq.remove(queueObject)){
				return true;
			}
		}
		return false;
	}

	class QOIterator implements Iterator<QueueObject>{
		TeamCollection[] queues;
		Iterator<QueueObject> iter;
		int cur = 0;
		public QOIterator(TeamCollection[] queues) {
			this.queues = queues;
			iter = queues[0].iterator();
		}

		@Override
		public boolean hasNext() {
			if (!iter.hasNext()){
				if (++cur >= queues.length){
					return false;
				} else {
					iter = queues[cur].iterator();
					return iter.hasNext();
				}
			} else {
				return true;
			}
		}

		@Override
		public QueueObject next() {
			if (iter.hasNext()) {
				return iter.next();
			} else {
				if (++cur >= queues.length){
					return null;
				} else {
					iter = queues[cur].iterator();
					return iter.next();
				}
			}
		}

		@Override
		public void remove() {
			iter.remove();
		}
	}

	@Override
	public int playerSize() {
		int size = 0;
		for (TeamCollection tq: this.queues){
			size += tq.playerSize();}
		return size;
	}

	@Override
	public Collection<? extends ArenaPlayer> getArenaPlayers() {
		List<ArenaPlayer> players = new ArrayList<ArenaPlayer>();
		for (TeamCollection tq: this.queues){
			players.addAll(tq.getArenaPlayers());}
		return players;
	}
}
