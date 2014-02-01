package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import org.bukkit.event.Cancellable;

public class MatchOpenEvent extends MatchEvent implements Cancellable {
	/// Cancel status
	boolean cancelled = false;

	public MatchOpenEvent(Match match){
		super(match);
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
