package mc.alk.arena.listeners.competition;

import mc.alk.arena.controllers.StatController;
import mc.alk.arena.events.players.ArenaPlayerKillEvent;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.arenas.ArenaListener;

public class ArenaPlayerKillListener implements ArenaListener{
	final StatController sc;
	public ArenaPlayerKillListener(MatchParams params){
		sc = new StatController(params);
	}

	public void onArenaPlayerKillEvent(ArenaPlayerKillEvent event){
		sc.addRecord(event.getPlayer(),event.getTarget(),WinLossDraw.WIN);
	}
}
