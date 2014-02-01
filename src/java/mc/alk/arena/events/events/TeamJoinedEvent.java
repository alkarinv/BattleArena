package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.objects.teams.ArenaTeam;
import org.bukkit.event.Cancellable;

public class TeamJoinedEvent extends EventEvent implements Cancellable {
	final ArenaTeam team;
	/// Cancel status
	boolean cancelled = false;

	public TeamJoinedEvent(Event event,ArenaTeam team) {
		super(event);
		this.team = team;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
