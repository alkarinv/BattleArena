package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.CancellableEvent;
import mc.alk.arena.objects.teams.ArenaTeam;

public class TeamJoinedEvent extends EventEvent implements CancellableEvent {
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
