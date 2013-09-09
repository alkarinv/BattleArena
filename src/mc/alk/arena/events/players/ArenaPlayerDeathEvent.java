package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaPlayerDeathEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	PlayerDeathEvent event;
	boolean exiting = false;

	public ArenaPlayerDeathEvent(ArenaPlayer arenaPlayer, ArenaTeam team) {
		super(arenaPlayer);
		this.team = team;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public void setPlayerDeathEvent(PlayerDeathEvent event){
		this.event = event;
	}
	public PlayerDeathEvent getPlayerDeathEvent() {
		return event;
	}

	public boolean isTrueDeath() {
		return event != null;
	}

	public boolean isExiting() {
		return exiting;
	}

	public void setExiting(boolean exiting) {
		this.exiting = exiting;
	}

}
