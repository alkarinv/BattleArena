package mc.alk.arena.events.matches.messages;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchMessageEvent;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.messaging.Channel;

public class MatchIntervalMessageEvent extends MatchMessageEvent{
	final int timeRemaining;

	public MatchIntervalMessageEvent(Match match, MatchState state, Channel serverChannel,
			String serverMessage, String matchMessage, int remainingTime) {
		super(match, state, serverChannel, serverMessage, matchMessage);
		this.timeRemaining = remainingTime;
	}
	public int getTimeRemaining(){
		return timeRemaining;
	}
}
