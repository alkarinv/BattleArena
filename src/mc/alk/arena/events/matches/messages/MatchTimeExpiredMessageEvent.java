package mc.alk.arena.events.matches.messages;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchMessageEvent;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.messaging.Channel;

public class MatchTimeExpiredMessageEvent extends MatchMessageEvent{
	public MatchTimeExpiredMessageEvent(Match match, MatchState state, Channel serverChannel,
			String serverMessage, String matchMessage) {
		super(match, state, serverChannel, serverMessage, matchMessage);
	}
}
