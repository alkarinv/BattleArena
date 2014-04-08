package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.Permissions;
import mc.alk.arena.controllers.plugins.CombatTagInterface;
import mc.alk.arena.controllers.plugins.EssentialsController;
import mc.alk.arena.controllers.plugins.VanishNoPacketInterface;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.ServerUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TeleportController implements Listener{
    final static Set<UUID> teleporting = Collections.synchronizedSet(new HashSet<UUID>());
    private static final int TELEPORT_FIX_DELAY = 15; // ticks

    public static boolean teleport(final Player player, final Location location) {
        return teleport(BattleArena.toArenaPlayer(player), location, false);
    }

    public static boolean teleport(final ArenaPlayer player, final Location location){
		return teleport(player,location,false);
	}

    public static boolean teleport(final ArenaPlayer arenaPlayer, final Location location, boolean giveBypassPerms) {
        Player player = arenaPlayer.getPlayer();
        if (Defaults.DEBUG_TRACE) Log.info("BattleArena beginning teleport player=" + player.getDisplayName());
        try {
            teleporting.add(arenaPlayer.getID());
            player.setVelocity(new Vector(0, Defaults.TELEPORT_Y_VELOCITY, 0));
            player.setFallDistance(0);
            Location loc = location.clone();
            loc.setY(loc.getY() + Defaults.TELEPORT_Y_OFFSET);
            /// Close their inventory so they arent taking things in/out
            InventoryUtil.closeInventory(player);
            player.setFireTicks(0);
            arenaPlayer.despawnMobs();


            /// Deal with vehicles
            if (player.isInsideVehicle()) {
                try {
                    player.leaveVehicle();
                } catch (Exception e) {/*ignore*/}
            }

            /// Load the chunk if its not already loaded
            try {
                if (!loc.getWorld().isChunkLoaded(loc.getBlock().getChunk())) {
                    loc.getWorld().loadChunk(loc.getBlock().getChunk());
                }
            } catch (Exception e) {/*ignore*/}

            /// MultiInv and Multiverse-Inventories stores/restores items when changing worlds
            /// or game states ... lets not let this happen
            PermissionsUtil.givePlayerInventoryPerms(player);

            /// CombatTag will prevent teleports
            if (CombatTagInterface.enabled())
                CombatTagInterface.untag(player);

            /// Give bypass perms for Teleport checks like noTeleport, and noChangeWorld
            if (giveBypassPerms && BattleArena.getSelf().isEnabled() && !Defaults.DEBUG_STRESS) {
                player.addAttachment(BattleArena.getSelf(), Permissions.TELEPORT_BYPASS_PERM, true, 1);
            }

            /// Some worlds "regenerate" which means they have the same name, but are different worlds
            /// To deal with this, reget the world
            World w = Bukkit.getWorld(loc.getWorld().getName());
            Location nl = new Location(w, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            if (!player.teleport(nl, PlayerTeleportEvent.TeleportCause.PLUGIN) ||
                    (Defaults.DEBUG_VIRTUAL && !player.isOnline())) {
                BAPlayerListener.teleportOnReenter(BattleArena.toArenaPlayer(player), nl, player.getLocation());
                //noinspection PointlessBooleanExpression,ConstantConditions
                return false;
            }
            arenaPlayer.spawnMobs();

            /// Handle the /back command from Essentials
            if (EssentialsController.enabled()) {
                Location l = BAPlayerListener.getBackLocation(player);
                if (l != null) {
                    EssentialsController.setBackLocation(player.getName(), l);
                }
            }

            if (Defaults.DEBUG_TRACE) Log.info("BattleArena ending teleport player=" + player.getDisplayName());
        } catch (Exception e) {
            if (!Defaults.DEBUG_VIRTUAL) {
                Log.err("[BA Error] teleporting player=" + player.getDisplayName() + " to " + location + " " + giveBypassPerms);
                Log.printStackTrace(e);
            }
            return false;
        }
        return true;
    }

	/**
	 * This prevents other plugins from cancelling the teleport
	 * removes the player from the set after allowing the tp.
     * Additionally as a
	 * @param event PlayerTeleportEvent
	 */
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if (teleporting.remove(PlayerUtil.getID(event.getPlayer()))){
			event.setCancelled(false);
            if (Defaults.ENABLE_TELEPORT_FIX){
				invisbleTeleportWorkaround(event.getPlayer());
			}
		}
	}

	///TODO remove these work around teleport hacks when bukkit fixes the invisibility on teleport issue
	/// modified from the teleportFix2 found online
	private void invisbleTeleportWorkaround(final Player player) {
		final Server server = Bukkit.getServer();
		final Plugin plugin = BattleArena.getSelf();
		final int visibleDistance = server.getViewDistance() * 16;
		// Fix the visibility issue one tick later
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (!player.isOnline())
					return;
				// Refresh nearby clients
				final List<Player> nearby = getPlayersWithinDistance(player, visibleDistance);
				// Hide every player
				updateEntities(player, nearby, false);
				// Then show them again
				server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						// Refresh nearby clients
						final List<Player> nearby = getPlayersWithinDistance(player, visibleDistance);
						updateEntities(player, nearby, true);
					}
				}, 2);
			}
		}, TELEPORT_FIX_DELAY);
	}

	private void updateEntities(final Player tpedPlayer, final List<Player> players, boolean visible) {
		// Hide or show every player to tpedPlayer
		// and hide or show tpedPlayer to every player.
		for (Player player : players) {
			if (!player.isOnline())
				continue;
			if (VanishNoPacketInterface.isVanished(player)) {
				if (!BattleArena.inArena(player))
					continue;
				VanishNoPacketInterface.toggleVanish(player);
			}
			if (visible){
				tpedPlayer.showPlayer(player);
				player.showPlayer(tpedPlayer);
			} else {
				tpedPlayer.hidePlayer(player);
				player.hidePlayer(tpedPlayer);
			}
		}
	}

	private List<Player> getPlayersWithinDistance(final Player player, final int distance) {
		List<Player> res = new ArrayList<Player>();
		final int d2 = distance * distance;
		final UUID uid = player.getWorld().getUID();
		for (Player p : ServerUtil.getOnlinePlayers()) {
			try{
				if (p.getWorld().getUID() == uid &&
						p != player && p.getLocation().distanceSquared(player.getLocation()) <= d2) {
					res.add(p);
				}
			} catch (IllegalArgumentException e){
				Log.info(e.getMessage());
			} catch(Exception e){
				Log.printStackTrace(e);
			}
		}
		return res;
	}

}
