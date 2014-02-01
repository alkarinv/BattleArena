package mc.alk.arena.listeners.competition.plugins;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.players.ArenaPlayerEnterMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveMatchEvent;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public enum TagAPIListener implements Listener, ArenaListener {
	INSTANCE;

	final Map<String, ChatColor> playerName = new ConcurrentHashMap<String,ChatColor>();

	public static void enable() {
		Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());
	}

	/**
	 * Need to be highest to override the standard renames
	 * @param event AsyncPlayerReceiveNameTagEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNameTag(AsyncPlayerReceiveNameTagEvent event) {
		final String name = event.getNamedPlayer().getName();
		if (playerName.containsKey(name)){
			event.setTag(playerName.get(name) + name);
		}
	}

	@ArenaEventHandler
	public void onArenaPlayerEnterEvent(ArenaPlayerEnterMatchEvent event){
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
            Log.err("TagAPI has made class changes");
			Log.printStackTrace(e);
		}
	}

	@ArenaEventHandler
	public void onArenaPlayerLeaveMatchEvent(ArenaPlayerLeaveMatchEvent event){
		Player player = event.getPlayer().getPlayer();
		if (!player.isOnline() || !BattleArena.getSelf().isEnabled())
			return;
		if (playerName.remove(player.getName()) != null){
			try{
				TagAPI.refreshPlayer(player);
			} catch (ClassCastException e){
				/* For the plugin CommandSigns which use a "ProxyPlayer" which can't be cast to
				 * a CraftPlayer, ignore the error */
			} catch (Exception e){
				/* Do nothing.
				 * Bukkit can sometimes have OfflinePlayers that are not caught by isOnline()*/
			}
		}

	}

}
