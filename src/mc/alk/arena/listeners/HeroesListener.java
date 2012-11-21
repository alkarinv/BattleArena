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

import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;

public enum HeroesListener implements Listener {
	INSTANCE;

	final Set<String> cancelExpLoss = Collections.synchronizedSet(new HashSet<String>());

	/**
	 * Need to be highest to override the standard renames
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void cancelExperienceLoss(ExperienceChangeEvent event) {
		if (event.isCancelled())
			return;
		final String name = event.getHero().getName();
		if (cancelExpLoss.contains(name)){
			event.setCancelled(true);
		}
	}

	public static void setCancelExpLoss(Player player) {
		/// Register ourself if we are starting to need to listen
		if (INSTANCE.cancelExpLoss.isEmpty()){
			Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());}
		INSTANCE.cancelExpLoss.add(player.getName());
	}

	public static void removeCancelExpLoss(Player player) {
		if (INSTANCE.cancelExpLoss.remove(player.getName())){
			/// Unregister if we aren't listening for any players
			if (INSTANCE.cancelExpLoss.isEmpty()){
				HandlerList.unregisterAll(INSTANCE);}
		}
	}
}
