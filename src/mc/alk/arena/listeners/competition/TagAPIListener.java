package mc.alk.arena.listeners.competition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.players.ArenaPlayerEnterEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;
import org.kitteh.tag.api.TagAPIException;



public enum TagAPIListener implements Listener {
	INSTANCE;

	final Map<String, ChatColor> playerName = new ConcurrentHashMap<String,ChatColor>();

	public static void enable(boolean enable) {
		Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());
	}

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

	@EventHandler
	public void onArenaPlayerEnterEvent(ArenaPlayerEnterEvent event){
		Player player = event.getPlayer().getPlayer();
		if (!player.isOnline() || !BattleArena.getSelf().isEnabled())
			return;
		ArenaTeam team = event.getPlayer().getTeam();
		playerName.put(player.getName(), team.getTeamChatColor());
		try{
			TagAPI.refreshPlayer(player);
		} catch (ClassCastException e){
			/* For the plugin CommandSigns which use a "ProxyPlayer" which can't be cast to
			 * a CraftPlayer, ignore the error */
		} catch (NoClassDefFoundError e){
			/* TagAPI has changed things around, Let them know of the problem
			 * But lets not crash BattleArena b/c of it */
			Log.printStackTrace(e);
		}
	}

	@EventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		Player player = event.getPlayer().getPlayer();
		if (!player.isOnline() || !BattleArena.getSelf().isEnabled())
			return;
		playerName.remove(player.getName());
		try{
			TagAPI.refreshPlayer(player);
		} catch (ClassCastException e){
			/* For the plugin CommandSigns which use a "ProxyPlayer" which can't be cast to
			 * a CraftPlayer, ignore the error */
		} catch (TagAPIException e){
			/* Do nothing.
			 * Bukkit can sometimes have OfflinePlayers that are not caught by isOnline()*/
		}
	}

}
