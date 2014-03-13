package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.CompetitionResult;
import org.bukkit.event.Cancellable;

public class MatchResultEvent extends MatchEvent implements Cancellable{
    CompetitionResult matchResult;
	boolean cancelled;
	final boolean matchEnding;

	public MatchResultEvent(Match match, CompetitionResult matchResult) {
		super(match);
		this.matchResult = matchResult;
		matchEnding = !match.alwaysOpen();
	}

	public CompetitionResult getMatchResult() {
		return matchResult;
	}

	public void setMatchResult(CompetitionResult matchResult) {
		this.matchResult = matchResult;
	}

	@Override
    public boolean isCancelled() {
		return cancelled;
	}

	@Override
    public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isMatchEnding(){
		return matchEnding && !cancelled;
	}
}
