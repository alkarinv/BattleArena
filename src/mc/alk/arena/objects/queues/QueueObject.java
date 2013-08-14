package mc.alk.arena.objects.queues;

import java.util.Collection;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.teams.ArenaTeam;

public abstract class QueueObject {

	protected Integer priority;

	protected MatchParams matchParams;

	final protected JoinOptions jp;

	public QueueObject(JoinOptions jp){
		this.jp = jp;
	}

	public abstract Integer getPriority();

	public abstract boolean hasMember(ArenaPlayer p);

	public abstract ArenaTeam getTeam(ArenaPlayer p);

	public abstract int size();

	public abstract Collection<ArenaTeam> getTeams();

	public abstract boolean hasTeam(ArenaTeam team);

	public long getJoinTime(){return jp.getJoinTime();}

	public MatchParams getMatchParams() {
		return matchParams;
	}

	public JoinOptions getJoinOptions() {
		return jp;
	}

}
