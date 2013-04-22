package mc.alk.arena.listeners.competition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.players.ArenaPlayerEnterEvent;
import mc.alk.arena.events.players.ArenaPlayerEnterQueueEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public enum InArenaListener implements Listener {
	INSTANCE;

	final Set<String> players = Collections.synchronizedSet(new HashSet<String>());
	final Set<String> qplayers = Collections.synchronizedSet(new HashSet<String>());
	final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	boolean registered = false;

	private InArenaListener(){
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
	}

	@EventHandler
	public void onArenaPlayerEnterQueueEvent(ArenaPlayerEnterQueueEvent event){
		if (qplayers.isEmpty() && players.isEmpty()){
			registered = true;
			for (Listener l: listeners){
				Bukkit.getPluginManager().registerEvents(l, BattleArena.getSelf());
			}
		}
		qplayers.add(event.getPlayer().getName());
	}

	@EventHandler
	public void onArenaPlayerLeaveQueueEvent(ArenaPlayerLeaveQueueEvent event){
		if (qplayers.remove(event.getPlayer().getName()) && qplayers.isEmpty() && players.isEmpty()){
			registered = false;
			for (Listener l: listeners){
				HandlerList.unregisterAll(l);
			}
		}
	}

	@EventHandler
	public void onArenaPlayerEnterEvent(ArenaPlayerEnterEvent event){
		if (players.isEmpty() && qplayers.isEmpty()){
			registered = true;
			for (Listener l: listeners){
				Bukkit.getPluginManager().registerEvents(l, BattleArena.getSelf());
			}
		}
		players.add(event.getPlayer().getName());
	}

	@EventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		if (players.remove(event.getPlayer().getName()) && players.isEmpty()){
			registered = false;
			for (Listener l: listeners){
				HandlerList.unregisterAll(l);
			}
		}
	}

	public boolean isPlayerInQueue(String name) {
		return qplayers.contains(name);
	}

	public boolean isPlayerInArena(String name) {
		return players.contains(name);
	}

	public static boolean inArena(String name) {
		return INSTANCE.players.contains(name);
	}

	public static boolean inQueue(String name) {
		return INSTANCE.qplayers.contains(name);
	}

	public static void addListener(Listener listener){
		INSTANCE.listeners.add(listener);
	}

}
