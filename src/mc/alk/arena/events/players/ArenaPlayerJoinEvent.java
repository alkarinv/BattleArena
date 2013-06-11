package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;

import org.bukkit.event.Cancellable;

/**
 * Signifies that the player has typed the command to Join the competition
 */
public class ArenaPlayerJoinEvent extends ArenaPlayerEvent implements Cancellable{
	boolean cancelled = false;
	String message;

	public ArenaPlayerJoinEvent(ArenaPlayer arenaPlayer) {
		super(arenaPlayer);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public void cancelWithMessage(String message) {
		this.cancelled = true;
		this.message = message;
	}

}
