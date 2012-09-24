package mc.alk.arena.controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PermissionsUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;


public class TeleportController {
	static Set<String> teleporting = Collections.synchronizedSet(new HashSet<String>());

	static Plugin plugin;

	public static void setup(Plugin plugin){
		TeleportController.plugin = plugin;
	}

	///TODO Keep the match players and show players UNTIL bukkit fixes tp invisible bug
	public static void teleportPlayer(final Player p, final Location loc, boolean in, final boolean die, final boolean wipe
			,Set<ArenaPlayer> matchPlayers){
		if (!p.isOnline() || p.isDead()){
			if (Defaults.DEBUG)Log.warn(BattleArena.getPName()+" Offline teleporting Player=" + p.getName() + " loc=" + loc + "  " + die +":"+ wipe);
			BAPlayerListener.teleportOnReenter(p.getName(),loc);
			if (wipe){
				InventoryUtil.clearInventory(p);}
			return;
		}
		teleport(p,loc);
		for(ArenaPlayer p2 : matchPlayers) {
			for (ArenaPlayer p3 : matchPlayers){
				p2.getPlayer().showPlayer(p3.getPlayer());
				p3.getPlayer().showPlayer(p2.getPlayer());
			}
		}
	}

	private static void teleporting(Player player, boolean isteleporting){
		if (isteleporting){
			teleporting.add(player.getName());
		} else {
			teleporting.remove(player.getName());
		}
	}

	/**
	 * This prevents other plugins from cancelling the teleport
	 * removes the player from the set after allowing the tp
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if (teleporting.remove(event.getPlayer().getName())){
			event.setCancelled(false);
		}
	}

	public static void teleport(final Player p, final Location loc){
		teleporting(p,true);
		/// Close their inventory so they arent taking things in/out
		InventoryUtil.closeInventory(p);
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

		/// MultiInv and Multiverse-Inventories stores/restores items when changing worlds
		/// or game states ... lets not let this happen
		PermissionsUtil.givePlayerInventoryPerms(p);			

//		/// Now move the gamemode after we have dealth with multiinv
//		p.setGameMode(GameMode.SURVIVAL);

		if (!p.teleport(loc)){
			if (Defaults.DEBUG)Log.warn("[BattleArena] Couldnt teleport player=" + p.getName() + " loc=" + loc);}
	}
}
