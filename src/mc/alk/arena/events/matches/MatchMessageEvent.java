package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.messaging.Channel;


public class MatchMessageEvent extends MatchEvent {
	final MatchState state;
	String serverMessage;
	String matchMessage;
	Channel serverChannel;

	public MatchMessageEvent(Match match, MatchState state, Channel serverChannel, String serverMessage, String matchMessage) {
		super(match);
		this.serverChannel = serverChannel;
		this.serverMessage = serverMessage;
		this.matchMessage = matchMessage;
		this.state = state;
	}

	public String getServerMessage() {
		return serverMessage;
	}

	public void setServerMessage(String serverMessage) {
		this.serverMessage = serverMessage;
	}

	public String getMatchMessage() {
		return matchMessage;
	}

	public void setMatchMessage(String matchMessage) {
		this.matchMessage = matchMessage;
	}

	public Channel getServerChannel() {
		return serverChannel == null ? Channel.NullChannel : serverChannel;
	}

	public void setServerChannel(Channel serverChannel) {
		this.serverChannel = serverChannel;
	}
	public MatchState getState(){
		return state;
	}
}
