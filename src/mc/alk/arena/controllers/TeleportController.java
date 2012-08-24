package mc.alk.arena.controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;


public class TeleportController {
	static Set<Player> teleporting = Collections.synchronizedSet(new HashSet<Player>());

	static Random rand = new Random();
	static Plugin plugin;

	public static void setup(Plugin plugin){
		TeleportController.plugin = plugin;
	}

	///TODO Keep the match players UNTIL bukkit fixes tp invisible bug
	public static void teleportPlayer(final Player p, final Location loc, boolean in, final boolean die, final boolean wipe
			,Set<ArenaPlayer> matchPlayers){
		if (!p.isOnline() || p.isDead()){
			if (Defaults.DEBUG)Log.warn("[BattleArena] Offline teleporting Player=" + p.getName() + " loc=" + loc + "  " + die +":"+ wipe);
			if (die){
				BAPlayerListener.killOnReenter(p.getName(),wipe);
			} else {
				BAPlayerListener.teleportOnReenter(p.getName(),loc,wipe);
			}
			return;
		}
		teleport(p,loc);
		for(ArenaPlayer p2 : matchPlayers) {
			if(!p2.getPlayer().canSee(p)) {
				p2.getPlayer().showPlayer(p);
				return;
			}
		}


	}

	private static void teleporting(Player player, boolean b){
		if (b){
			teleporting.add(player);
		} else {
			teleporting.remove(player);
		}
	}

	/**
	 * This prevents other plugins from cancelling the teleport
	 * removes the player from the set after allowing the tp
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if (teleporting.remove(event.getPlayer())){
			event.setCancelled(false);
		}
	}

	public static void teleport(final Player p, final Location loc){
		teleporting(p,true);
		/// Close their inventory so they arent taking things in/out
		InventoryUtil.closeInventory(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.setFireTicks(0);

		/// Deal with vehicles
		if (p.isInsideVehicle()){
			try{ p.leaveVehicle(); } catch(Exception e){}
		}

		/// Load the chunk if its not already loaded
		try {
			if(!loc.getWorld().isChunkLoaded(loc.getBlock().getChunk())){
				loc.getWorld().loadChunk(loc.getBlock().getChunk());}
		} catch (Exception e){}

		/// Multi inv stores/restores items when changing worlds... lets not let this happen
		/// If we are shutting down (aka isEnabled = false) multiinv will have shutdown before us
		/// so skip the attachment and just get players out if we can
		boolean ignoreMultiInv = Defaults.PLUGIN_MULTI_INV && BattleArena.getSelf().isEnabled() && 
				p.getWorld().getUID() != loc.getWorld().getUID();
		if (ignoreMultiInv){
			/// TeamJoinResult the multi-inv ignore this player permission node, do it for 1 tick
			p.addAttachment(plugin, Defaults.MULTI_INV_IGNORE_NODE, true, 3);
		}
		if (!p.teleport(loc)){
			if (Defaults.DEBUG)Log.warn("[BattleArena] Couldnt teleport player=" + p.getName() + " loc=" + loc);}
	}
}
