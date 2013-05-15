package mc.alk.arena.listeners;

import mc.alk.arena.controllers.SignController;
import mc.alk.arena.events.players.ArenaPlayerEnterQueueEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;
import mc.alk.arena.objects.events.ArenaEventHandler;

public class SignUpdateListener {
	SignController sc;
	public SignUpdateListener(SignController sc){
		this.sc = sc;
	}

	@ArenaEventHandler
	public void onArenaPlayerEnterQueueEvent(ArenaPlayerEnterQueueEvent event){

	}

	@ArenaEventHandler
	public void onArenaPlayerLeaveQueueEvent(ArenaPlayerLeaveQueueEvent event){

	}
}
