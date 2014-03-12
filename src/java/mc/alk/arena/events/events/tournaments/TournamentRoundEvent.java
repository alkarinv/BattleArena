package mc.alk.arena.events.events.tournaments;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.events.EventEvent;

/**
 * @author alkarin
 */
public class TournamentRoundEvent extends EventEvent {

    final int round;

    public TournamentRoundEvent(Event event, int round){
        super(event);
        this.round = round;
    }

    public int getRound() {
        return round;
    }

}
