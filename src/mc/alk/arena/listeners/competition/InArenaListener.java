package mc.alk.arena.listeners.competition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.events.players.ArenaPlayerEnterMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerEnterQueueEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;
import mc.alk.arena.util.Log;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public enum InArenaListener implements Listener {
	INSTANCE;

	final Set<String> inArena = Collections.synchronizedSet(new HashSet<String>());
	final Set<String> inGame = Collections.synchronizedSet(new HashSet<String>());
	final Set<String> inQueue = Collections.synchronizedSet(new HashSet<String>());
	final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	boolean registered = false;


	@EventHandler
	public void onArenaPlayerEnterQueueEvent(ArenaPlayerEnterQueueEvent event){
		if (Defaults.DEBUG_TRACE)Log.info("  onArenaPlayerEnterQueueEvent " + event.getPlayer().getName());
		if (!registered && BattleArena.getSelf().isEnabled()){
			registered = true;
			for (Listener l: listeners){
				Bukkit.getPluginManager().registerEvents(l, BattleArena.getSelf());
			}
		}
		inQueue.add(event.getPlayer().getName());
	}

	@EventHandler
	public void onArenaPlayerLeaveQueueEvent(ArenaPlayerLeaveQueueEvent event){
		if (Defaults.DEBUG_TRACE)Log.info( "  onArenaPlayerLeaveQueueEvent " + event.getPlayer().getName());
		if (inQueue.remove(event.getPlayer().getName()) && inQueue.isEmpty() && inArena.isEmpty()){
			registered = false;
			for (Listener l: listeners){
				HandlerList.unregisterAll(l);
			}
		}
	}

	@EventHandler
	public void onArenaPlayerEnterEvent(ArenaPlayerEnterMatchEvent event){
		if (Defaults.DEBUG_TRACE)Log.info( "  - onArenaPlayerEnterEvent " + event.getPlayer().getName());

		if (!registered){
			registered = true;
			for (Listener l: listeners){
				Bukkit.getPluginManager().registerEvents(l, BattleArena.getSelf());
			}
		}
		inArena.add(event.getPlayer().getName());
	}

	@EventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveMatchEvent event){
		if (Defaults.DEBUG_TRACE)Log.info( "  - onArenaPlayerLeaveEvent " + event.getPlayer().getName());

		if (inArena.remove(event.getPlayer().getName()) && inArena.isEmpty()){
			registered = false;
			for (Listener l: listeners){
				HandlerList.unregisterAll(l);
			}
		}
	}

	public boolean isPlayerInQueue(String name) {
		return inQueue.contains(name);
	}

	public boolean isPlayerInArena(String name) {
		return inArena.contains(name);
	}

	public static boolean inArena(String name) {
		return INSTANCE.inArena.contains(name);
	}

	public static boolean inQueue(String name) {
		return INSTANCE.inQueue.contains(name);
	}

	public static void addListener(Listener listener){
		INSTANCE.listeners.add(listener);
	}


}
