package mc.alk.arena.objects.teams;

import java.util.Set;

import mc.alk.arena.controllers.TeamController;

import org.bukkit.entity.Player;

public class TeamFactory {
	static TeamController tc ;
	
	public static Team createTeam(Player p){
		return new Team(p);	
	}
	public static Team createTeam(Set<Player> players){
		return new Team(players);	
	}

	public static CompositeTeam createCompositeTeam(Set<Player> players) {
		return new CompositeTeam(players);
	}

}
