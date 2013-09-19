package mc.alk.arena.listeners;

import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.players.ArenaPlayerEnterQueueEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.signs.ArenaCommandSign;
import mc.alk.arena.util.MapOfSet;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SignUpdateListener implements Listener{
	MapOfSet<String,ArenaCommandSign> arenaSigns = new MapOfSet<String, ArenaCommandSign>();

	@EventHandler
	public void onArenaPlayerEnterQueueEvent(ArenaPlayerEnterQueueEvent event){
		//		Log.debug("onArenaPlayerEnterQueueEvent === "+ event.getPlayer().getName() +"   " +
		//				event.getQueueResult().playersInQueue +" / " + event.getQueueResult().maxPlayers);

		Arena arena = event.getArena();
		if (arena == null) return;
		int size = event.getQueueResult().playersInQueue;
		setPeopleInQueue(arena, size, event.getQueueResult().maxPlayers);
	}

	private void setPeopleInQueue(Arena arena, int playersInQueue, int neededPlayers) {
		Set<ArenaCommandSign> signLocs = arenaSigns.getSafer(arena.getName());
		if (signLocs == null || signLocs.isEmpty()){
			return;
		}
		String needed = neededPlayers == ArenaSize.MAX ? "inf" : neededPlayers+"";
		for (ArenaCommandSign l : signLocs){
			Sign s = l.getSign();
			if (s == null)
				continue;
			final String newLine;
			//				if (s.getLine(3) != null){
			//
			//					if (s.getLine(3).contains(" ")){
			//						String[] split = s.getLine(3).split(" ");
			//						newLine = playersInQueue+"/&6"+neededPlayers+" &8" + split[1];
			//					} else {
			//						newLine = playersInQueue+"/&6"+neededPlayers+" &80";
			//					}
			//				} else {
			//					newLine = playersInQueue+"/&6"+neededPlayers+" &80";
			//				}
			newLine = playersInQueue+"/&6"+needed;
			//				Log.debug("newLine === "+ newLine);
			s.setLine(3, MessageUtil.colorChat(newLine));
			s.update();
		}
	}

	private void setMatchState(Arena arena, String state) {
		Set<ArenaCommandSign> signLocs = arenaSigns.getSafer(arena.getName());
	}

	@EventHandler
	public void onMatchStartEvent(MatchStartEvent event){

	}
	@EventHandler
	public void onArenaPlayerLeaveQueueEvent(ArenaPlayerLeaveQueueEvent event){
		//		Log.debug("onArenaPlayerLeaveQueueEvent === "+ event.getPlayer().getName() +"   " +
		//				event.getNPlayers() +" / " + event.getParams().getMinPlayers() +"  " + event.getParams().getMaxPlayers());

		Arena arena = event.getArena();
		if (arena ==null) return;
		int size = event.getNPlayers();
		setPeopleInQueue(arena, size,event.getParams().getMinPlayers());
	}

	public void addSign(ArenaCommandSign acs) {
		if (acs.getSign() == null || acs.getOption1() == null){
			return;}
		Arena a = BattleArena.getBAController().getArena(acs.getOption1());
		if (a == null)
			return;
		arenaSigns.add(a.getName(), acs);
	}

	public void updateAllSigns() {
	}

	public MapOfSet<String, ArenaCommandSign> getStatusSigns() {
		return arenaSigns;
	}

}
