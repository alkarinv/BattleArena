package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaPlayerKillEvent extends ArenaPlayerEvent{
	final ArenaPlayer target;
	final ArenaTeam team;
	PlayerDeathEvent event;

	public ArenaPlayerKillEvent(ArenaPlayer arenaPlayer, ArenaTeam team, ArenaPlayer target) {
		super(arenaPlayer);
		this.team = team;
		this.target = target;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public ArenaPlayer getTarget(){
		return target;
	}

	public void setPlayerDeathEvent(PlayerDeathEvent event){
		this.event = event;
	}
	public PlayerDeathEvent getPlayerDeathEvent() {
		return event;
	}

	public int getPoints() {
		return 1;
	}
}
