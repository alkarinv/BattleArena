package mc.alk.arena.objects.queues;

import java.util.Collection;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.teams.Team;

public abstract class QueueObject {

	protected Integer priority;

	protected MatchParams matchParams;

	protected JoinOptions jp;

	public MatchParams getMatchParams() {
		return matchParams;
	}

	public JoinOptions getJoinOptions() {
		return jp;
	}

	public void setJoinOptions(JoinOptions jp) {
		this.jp = jp;
	}

	public abstract Integer getPriority();

	public abstract boolean hasMember(ArenaPlayer p);

	public abstract Team getTeam(ArenaPlayer p);

	public abstract int size();

	public abstract Collection<Team> getTeams();

	public abstract boolean hasTeam(Team team);
}
