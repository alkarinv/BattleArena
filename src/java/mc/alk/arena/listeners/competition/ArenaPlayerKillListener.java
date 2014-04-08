package mc.alk.arena.listeners.competition;

import mc.alk.arena.controllers.plugins.TrackerController;
import mc.alk.arena.events.players.ArenaPlayerKillEvent;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.WinLossDraw;
import mc.alk.arena.objects.arenas.ArenaListener;

public class ArenaPlayerKillListener implements ArenaListener{
	final TrackerController sc;
	public ArenaPlayerKillListener(MatchParams params){
		sc = new TrackerController(params);
	}

	public void onArenaPlayerKillEvent(ArenaPlayerKillEvent event){
		sc.addRecord(event.getPlayer(),event.getTarget(), WinLossDraw.WIN);
	}
}
