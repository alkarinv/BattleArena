package mc.alk.arena.listeners;

import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.matches.MatchFinishedEvent;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SignUpdateListener implements Listener{
	MapOfSet<String,ArenaCommandSign> arenaSigns = new MapOfSet<String, ArenaCommandSign>();

	private String getMatchState(String str){
		if (str != null && (str.startsWith("\\d") || str.indexOf(' ') > 0 )){
			int index = str.indexOf(' ');
			return index != -1 ? str.substring(0, index) : str;
		} else {
			return "Open";
		}
	}

	private String getQCount(String str){
		if (str != null && (str.startsWith("\\d") || str.indexOf(' ') > 0 )){
			int index = str.indexOf(' ');
			return index != -1 ? str.substring(index+1, str.length()) : str;
		} else {
			return "";
		}
	}

	private void setPeopleInQueue(Arena arena, int playersInQueue, int neededPlayers) {
		Set<ArenaCommandSign> signLocs = arenaSigns.getSafer(arena.getName());
		if (signLocs == null || signLocs.isEmpty()){
			return;
		}
		final String strcount = (neededPlayers == ArenaSize.MAX) ?
				playersInQueue +"&6/\u221E" : playersInQueue+"&6/"+neededPlayers;
		for (ArenaCommandSign l : signLocs){
			Sign s = l.getSign();
			if (s == null)
				continue;
			s.setLine(3, MessageUtil.colorChat(getMatchState(s.getLine(3))+" " + strcount));
			s.update();
//			Log.debug("  line now " + s.getLine(3));
		}
	}

	private void setMatchState(Arena arena, String state) {
		Set<ArenaCommandSign> signLocs = arenaSigns.getSafer(arena.getName());
		if (signLocs == null || signLocs.isEmpty()){
			return;
		}
		for (ArenaCommandSign l : signLocs){
			Sign s = l.getSign();
			if (s == null)
				continue;
			s.setLine(3, MessageUtil.colorChat(state + " "+ getQCount(s.getLine(3))));
			s.update();
//			Log.debug("  line now " + s.getLine(3));

		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onMatchStartEvent(MatchStartEvent event){
		setMatchState(event.getMatch().getArena(), "Active&4");
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onMatchFinishedEvent(MatchFinishedEvent event){
		setMatchState(event.getMatch().getArena(), "Open&2");
	}

	@EventHandler
	public void onArenaPlayerEnterQueueEvent(ArenaPlayerEnterQueueEvent event){
//		Log.debug("onArenaPlayerEnterQueueEvent === "+ event.getPlayer().getName() +"   " +
//				event.getQueueResult().playersInQueue +" / " + event.getQueueResult().maxPlayers
//				+"  arena="+ event.getArena());
		if (event.getArena() == null) return;
		int size = event.getQueueResult().playersInQueue;
		setPeopleInQueue(event.getArena(), size, event.getQueueResult().maxPlayers);
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onArenaPlayerLeaveQueueEvent(ArenaPlayerLeaveQueueEvent event){
//		Log.debug("onArenaPlayerLeaveQueueEvent === "+ event.getPlayer().getName() +"   " +
//				event.getNPlayers() +" / " + event.getParams().getMinPlayers() +"  " + event.getParams().getMaxPlayers());

		if (event.getArena() == null) return;
		int size = event.getNPlayers();
		setPeopleInQueue(event.getArena(), size,event.getParams().getMaxPlayers());
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
