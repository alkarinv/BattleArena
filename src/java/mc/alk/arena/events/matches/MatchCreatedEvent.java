package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.joining.WaitingObject;

public class MatchCreatedEvent extends MatchEvent {
    WaitingObject originalObject;

    public MatchCreatedEvent(Match match, WaitingObject originalObject) {
        super(match);
        this.originalObject = originalObject;
    }

    public WaitingObject getOriginalObject() {
        return originalObject;
    }
}
