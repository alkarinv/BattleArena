package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.teams.ArenaTeam;


public class ReservedArenaEventMessager extends EventMessager{

	public ReservedArenaEventMessager(Event event){
		super(event);
	}

	@Override
	public void sendEventCancelledDueToLackOfPlayers(Set<ArenaPlayer> competingPlayers) {
		try{impl.sendEventCancelledDueToLackOfPlayers(getChannel(MatchState.ONCANCEL), competingPlayers);
		}catch(Exception e){e.printStackTrace();}
	}

	@Override
	public void sendEventCancelled(Collection<ArenaTeam> teams) {
		try{impl.sendEventCancelled(getChannel(MatchState.ONCANCEL), teams);}catch(Exception e){e.printStackTrace();}
	}

	public void sendTeamJoinedEvent(ArenaTeam t) {

	}


	@Override
	public void sendEventDraw(Collection<ArenaTeam> drawers, Collection<ArenaTeam> losers) {

	}

}
