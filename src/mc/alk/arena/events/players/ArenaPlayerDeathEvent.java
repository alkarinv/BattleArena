package mc.alk.arena.events.players;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaPlayerDeathEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	PlayerDeathEvent event;

	public ArenaPlayerDeathEvent(Match match, ArenaPlayer arenaPlayer, ArenaTeam team) {
		super(arenaPlayer);
		this.team = team;
	}

	public ArenaTeam getArenaTeam() {
		return team;
	}

	public void setPlayerDeathEvent(PlayerDeathEvent event){
		this.event = event;
	}
	public PlayerDeathEvent getPlayerDeathEvent() {
		return event;
	}
}
