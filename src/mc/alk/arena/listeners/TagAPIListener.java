package mc.alk.arena.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.BattleArena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;


public enum TagAPIListener implements Listener {
	INSTANCE;

	final Map<String, ChatColor> playerName = new ConcurrentHashMap<String,ChatColor>();

	/**
	 * Need to be highest to override the standard renames
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNameTag(PlayerReceiveNameTagEvent event) {
		final String name = event.getNamedPlayer().getName();
		if (playerName.containsKey(name)){
			event.setTag(playerName.get(name) + name);
		}
	}

	public static void setNameColor(Player player, ChatColor teamColor) {
		/// Register ourself if we are starting to need to listen
		if (INSTANCE.playerName.isEmpty()){
			Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());}
		INSTANCE.playerName.put(player.getName(), teamColor);
		TagAPI.refreshPlayer(player);
	}

	public static void removeNameColor(Player player) {
		if (INSTANCE.playerName.remove(player.getName()) != null){
			TagAPI.refreshPlayer(player);
			/// Unregister if we aren't listening for any players
			if (INSTANCE.playerName.isEmpty()){
				HandlerList.unregisterAll(INSTANCE);}
		}
	}
}
