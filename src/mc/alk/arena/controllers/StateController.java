package mc.alk.arena.controllers;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.queues.ArenaMatchQueue;
import mc.alk.arena.objects.queues.TeamJoinObject;
import mc.alk.arena.util.Log;

public class StateController {
	private final ArenaMatchQueue amq;
	private LobbyController lobbies = LobbyController.INSTANCE;
	private LobbyController waitrooms = LobbyController.INSTANCE;


	public StateController(ArenaMatchQueue amq){
		this.amq = amq;
	}

	private void callEvent(BAEvent event){
//		methodController.callEvent(event);
		event.callEvent();
	}

	public JoinResult join(TeamJoinObject tqo, boolean shouldStart) {
		JoinResult jr = amq.join(tqo,shouldStart );

		MatchParams mp = tqo.getMatchParams();
		/// who is responsible for doing what
		Log.debug(" Join status = " + jr.status +"    " + tqo.getTeam() + "   " + tqo.getTeam().getId() +" --"
				+ ", haslobby="+mp.hasLobby() +"  ,wr="+(mp.getTransitionOptions().hasOptionAt(MatchState.ONJOIN, TransitionOption.TELEPORTWAITROOM))+"  "+
				"   --- hasArena=" + tqo.getJoinOptions().hasArena());
		if (mp.hasLobby()){
			lobbies.joinLobby(tqo.getMatchParams().getType(), tqo.getTeam());
		}
		if (tqo.getJoinOptions().hasArena()){
			waitrooms.joinWaitroom(tqo.getJoinOptions().getArena(), tqo.getTeam());
		}
		return jr;
	}
}
