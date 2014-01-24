package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchResult;

import org.bukkit.event.Cancellable;

public class MatchResultEvent extends MatchEvent implements Cancellable{
	MatchResult matchResult;
	boolean cancelled;
	final boolean matchEnding;

	public MatchResultEvent(Match match, MatchResult matchResult) {
		super(match);
		this.matchResult = matchResult;
		matchEnding = !match.alwaysOpen();
	}

	public MatchResult getMatchResult() {
		return matchResult;
	}
	public void setMatchResult(MatchResult matchResult) {
		this.matchResult = matchResult;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isMatchEnding(){
		return matchEnding && !cancelled;
	}
}
