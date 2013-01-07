package mc.alk.arena.listeners;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.BattleArena;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.massivecraft.factions.event.PowerLossEvent;

public enum FactionsListener implements Listener{
	INSTANCE;

	final Set<String> players = Collections.synchronizedSet(new HashSet<String>());

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFactionLoss(PowerLossEvent event){
		if (INSTANCE.players.contains(event.getPlayer().getName())){
			event.setMessage("&2You lost no power &d%d / %d");
			event.setCancelled(true);
		}
	}

	public static boolean addPlayer(Player player) {
		if (INSTANCE.players.isEmpty()){
			Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());}

		return INSTANCE.players.add(player.getName());
	}

	public static boolean removePlayer(Player player) {
		boolean removed = INSTANCE.players.remove(player.getName());
		if (removed && INSTANCE.players.isEmpty()){
			HandlerList.unregisterAll(INSTANCE);}
		return removed;
	}

	public static boolean hasPowerLoss() {
		try {
			Class.forName("com.massivecraft.factions.event.PowerLossEvent");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
