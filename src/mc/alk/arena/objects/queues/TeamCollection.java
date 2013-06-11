package mc.alk.arena.objects.queues;

import mc.alk.arena.objects.MatchParams;

public interface TeamCollection extends Iterable<QueueObject>{

	int size();

	MatchParams getMatchParams();

	boolean remove(QueueObject queueObject);

	boolean isEmpty();

	boolean add(QueueObject to);

}
