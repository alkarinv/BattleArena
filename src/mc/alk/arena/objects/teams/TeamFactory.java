package mc.alk.arena.objects.teams;

import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;

public class TeamFactory {
	public static Team createTeam(ArenaPlayer p){
		return new Team(p);
	}
	public static Team createTeam(Set<ArenaPlayer> players){
		return new Team(players);
	}

	public static CompositeTeam createCompositeTeam(Set<ArenaPlayer> players) {
		return new CompositeTeam(players);
	}

	public static CompositeTeam createCompositeTeam() {
		return new CompositeTeam();
	}
	public static CompositeTeam createCompositeTeam(Team team) {
		return new CompositeTeam(team);
	}

}
