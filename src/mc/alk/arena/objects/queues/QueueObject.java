package mc.alk.arena.objects.queues;

import java.util.Collection;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;

public abstract class QueueObject {
	Integer priority;
	MatchParams mp;

	public abstract Integer getPriority();

	public MatchParams getMatchParams() {
		return mp;
	}

	public abstract boolean hasMember(ArenaPlayer p);

	public abstract Team getTeam(ArenaPlayer p);

	public abstract int size();

	public abstract Collection<Team> getTeams();

	public abstract boolean hasTeam(Team team);
}
