package mc.alk.arena.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.HeroesController;

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
		if (!player.isOnline())
			return;
		/// Register ourself if we are starting to need to listen
		if (INSTANCE.playerName.isEmpty()){
			Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());}
		INSTANCE.playerName.put(player.getName(), teamColor);
		try{
			TagAPI.refreshPlayer(player);
		} catch (ClassCastException e){
			/* For the plugin CommandSigns which use a "ProxyPlayer" which can't be cast to
			 * a CraftPlayer, ignore the error */
		} catch (NoClassDefFoundError e){
			/* TagAPI has changed things around, Let them know of the problem
			 * But lets not crash BattleArena b/c of it */
			e.printStackTrace();
		}
	}

	public static void removeNameColor(final Player player) {
		if (!player.isOnline() || !BattleArena.getSelf().isEnabled())
			return;
		if (INSTANCE.playerName.remove(player.getName()) != null){
			if (HeroesController.enabled()){
				Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
					@Override
					public void run() {
						INSTANCE.removeName(player);
					}
				});
			} else {
				INSTANCE.removeName(player);
			}
		}
	}

	private void removeName(Player player){
		try{
			TagAPI.refreshPlayer(player);
		} catch (ClassCastException e){
			/* For the plugin CommandSigns which use a "ProxyPlayer" which can't be cast to
			 * a CraftPlayer, ignore the error */
		}
		/// Unregister if we aren't listening for any players
		if (INSTANCE.playerName.isEmpty()){
			HandlerList.unregisterAll(INSTANCE);}
	}
}
