package mc.alk.arena.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.Permissions;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.ServerUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;


/**
 *
 * @author alkarin
 *
 */
public class BAPlayerListener implements Listener  {
	public static HashSet<String> die = new HashSet<String>();
	public static HashSet<String> clearInventory= new HashSet<String>();
	public static HashMap<String,Integer> clearWool= new HashMap<String,Integer>();
	public static HashMap<String,Location> tp = new HashMap<String,Location>();

	public static HashMap<String,Integer> expRestore = new HashMap<String,Integer>();
	public static HashMap<String,Integer> healthRestore = new HashMap<String,Integer>();
	public static HashMap<String,Integer> hungerRestore = new HashMap<String,Integer>();
	public static HashMap<String,Integer> magicRestore = new HashMap<String,Integer>();
	public static HashMap<String,PInv> itemRestore = new HashMap<String,PInv>();
	public static HashMap<String,PInv> matchItemRestore = new HashMap<String,PInv>();
	public static HashMap<String,List<ItemStack>> itemRemove = new HashMap<String,List<ItemStack>>();
	public static HashMap<String,GameMode> gamemodeRestore = new HashMap<String,GameMode>();
	public static HashMap<String,String> messagesOnRespawn = new HashMap<String,String>();

	BattleArenaController bac;

	public BAPlayerListener(BattleArenaController bac){
		die.clear();
		clearInventory.clear();
		clearWool.clear();
		tp.clear();
		expRestore.clear();
		itemRestore.clear();
		matchItemRestore.clear();
		gamemodeRestore.clear();
		messagesOnRespawn.clear();
		this.bac = bac;
	}

	/**
	 *
	 * Why priority.HIGHEST: if an exception happens after we have already set their respawn location,
	 * they relog in at a separate time and will not get teleported to the correct place.
	 * As a workaround, try to handle this event last.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		final String name = p.getName();
		if (clearInventory.remove(name)){
			Log.warn("[BattleArena] clearing inventory for quitting during a match " + p.getName());
			for (ItemStack is: p.getInventory().getContents()){
				if (is == null || is.getType()==Material.AIR)
					continue;
				//				FileLogger.log("d  itemstack="+ InventoryUtil.getItemString(is));
			}
			for (ItemStack is: p.getInventory().getArmorContents()){
				if (is == null || is.getType()==Material.AIR)
					continue;
				//				FileLogger.log("d aitemstack="+ InventoryUtil.getItemString(is));
			}
			InventoryUtil.clearInventory(p);
		}
		playerReturned(p, null);
	}

	/**
	 * Why priority highest, some other plugins try to force respawn the player in spawn(or some other loc)
	 * problem is if they have come from the creative world, their game mode gets reset to creative
	 * but the other plugin force spawns them at spawn... so they now have creative in an area they shouldnt
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player p = event.getPlayer();
		playerReturned(p, event);
	}

	private void playerReturned(final Player p, PlayerRespawnEvent event) {
		final String name = p.getName();
		final String msg = messagesOnRespawn.remove(p.getName());
		if (msg != null){
			MessageUtil.sendMessage(p, msg);
		}

		if (die.remove(name)){
			MessageUtil.sendMessage(p, "&eYou have been killed by the Arena for not being online");
			p.setHealth(0);
			return;
		}

		/// Teleport players, or set respawn point
		if (tp.containsKey(name)){
			final Location loc = tp.get(name);
			if (loc != null){
				if (event == null){
					Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
						@Override
						public void run() {
							Player pl = ServerUtil.findPlayerExact(name);
							if (pl != null){
								TeleportController.teleport(p, tp.remove(name));
							} else {
								Util.printStackTrace();
							}
						}
					});
				} else {
					PermissionsUtil.givePlayerInventoryPerms(p);
					event.setRespawnLocation(tp.remove(name));
					/// Set a timed event to check to make sure the player actually arrived
					/// Then do a teleport if needed
					/// This can happen on servers where plugin conflicts prevent the respawn (somehow!!!)
					if (HeroesController.enabled()){
						Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
							@Override
							public void run() {
								Player pl = ServerUtil.findPlayerExact(name);
								if (pl != null){
									if (pl.getLocation().getWorld().getUID()!=loc.getWorld().getUID() ||
											pl.getLocation().distanceSquared(loc) > 100){
										TeleportController.teleport(p, loc);
									}
								} else {
									Util.printStackTrace();
								}
							}
						},2L);
					}
				}
			} else { /// this is bad, how did they get a null tp loc
				Log.err(name + " respawn loc =null");
			}
		}

		/// Do these after teleports
		/// Restore game mode
		if (gamemodeRestore.containsKey(name)){
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
				@Override
				public void run() {
					PlayerStoreController.setGameMode(p, gamemodeRestore.remove(name));
				}
			});
		}

		/// Exp restore
		if (expRestore.containsKey(name)){
			final int exp = expRestore.remove(name);
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = ServerUtil.findPlayerExact(name);
					if (pl != null){
						pl.giveExp(exp);}
				}
			});
		}

		/// Health restore
		if (healthRestore.containsKey(name)){
			final int val = healthRestore.remove(name);
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = ServerUtil.findPlayerExact(name);
					if (pl != null){
						BattleArena.toArenaPlayer(pl).setHealth(val);}
				}
			});
		}

		/// Hunger restore
		if (hungerRestore.containsKey(name)){
			final int val = hungerRestore.remove(name);
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = ServerUtil.findPlayerExact(name);
					if (pl != null){
						BattleArena.toArenaPlayer(pl).setFoodLevel(val);}
				}
			});
		}

		/// Magic restore
		if (magicRestore.containsKey(name)){
			final int val = magicRestore.remove(name);
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = ServerUtil.findPlayerExact(name);
					if (pl != null){
						HeroesController.setMagicLevel(pl, val);
					}
				}
			});
		}

		/// Restore Items
		if (itemRestore.containsKey(name)){
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = ServerUtil.findPlayerExact(name);
					if (Defaults.DEBUG_STORAGE) System.out.println("### restoring items to " + name +"   pl = " + pl);
					if (pl != null){
						PInv pinv = itemRestore.remove(name);
						ArenaPlayer ap = PlayerController.toArenaPlayer(pl);
						PlayerStoreController.setInventory(ap, pinv);
					}
				}
			});
		}

		/// Restore Match Items
		if (matchItemRestore.containsKey(name)){
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = ServerUtil.findPlayerExact(name);
					if (pl != null){
						PInv pinv = matchItemRestore.remove(name);
						ArenaPlayer ap = PlayerController.toArenaPlayer(pl);
						PlayerStoreController.setInventory(ap, pinv);
					}
				}
			});
		}

		/// Remove Items
		if (itemRemove.containsKey(p.getName())){
			Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
				public void run() {
					Player pl = ServerUtil.findPlayerExact(name);
					if (pl != null){
						List<ItemStack> items = itemRemove.remove(name);
						PlayerStoreController.removeItems(BattleArena.toArenaPlayer(pl), items);
					}
				}
			});
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if (event.isCancelled() || !WorldGuardController.hasWorldGuard() ||
				WorldGuardController.regionCount() == 0 ||
				event.getPlayer().hasPermission(Permissions.TELEPORT_BYPASS_PERM))
			return;
		WorldGuardRegion region = WorldGuardController.getContainingRegion(event.getTo());
		if (region != null && !WorldGuardController.hasPlayer(event.getPlayer().getName(), region)){
			MessageUtil.sendMessage(event.getPlayer(), "&cYou can't enter the arena through teleports");
			event.setCancelled(true);
			return;
		}
	}

	public static void killOnReenter(String playerName) {
		die.add(playerName);
	}

	public static void teleportOnReenter(String playerName, Location loc) {
		tp.put(playerName,loc);
	}

	public static void addMessageOnReenter(String playerName, String string) {
		messagesOnRespawn.put(playerName, string);
	}

	public static void restoreExpOnReenter(String playerName, Integer val) {
		if (expRestore.containsKey(playerName)){
			val += expRestore.get(playerName);}
		expRestore.put(playerName, val);
	}

	public static void restoreItemsOnReenter(String playerName, PInv pinv) {
		itemRestore.put(playerName,pinv);
	}

	public static void restoreMatchItemsOnReenter(String playerName, PInv pinv) {
		matchItemRestore.put(playerName,pinv);
	}

	public static void removeMatchItems(String playerName) {
		matchItemRestore.remove(playerName);
	}

	public static void clearWoolOnReenter(String playerName, int color) {
		if (playerName==null || color == -1)
			return;
		clearWool.put(playerName, color);
	}

	public static void restoreGameModeOnEnter(String playerName, GameMode gamemode) {
		gamemodeRestore.put(playerName, gamemode);
	}

	public static void removeItemOnEnter(ArenaPlayer p, ItemStack is) {
		List<ItemStack> items = itemRemove.get(p.getName());
		if (items == null){
			items = new ArrayList<ItemStack>();
			itemRemove.put(p.getName(), items);
		}
		items.add(is);
	}

	public static void removeItemsOnEnter(ArenaPlayer p, List<ItemStack> itemsToRemove) {
		List<ItemStack> items = itemRemove.get(p.getName());
		if (items == null){
			items = new ArrayList<ItemStack>();
			itemRemove.put(p.getName(), items);
		}
		items.addAll(itemsToRemove);
	}

	public static void restoreHealthOnReenter(String playerName, Integer val) {
		healthRestore.put(playerName, val);
	}

	public static void restoreHungerOnReenter(String playerName, Integer val) {
		hungerRestore.put(playerName, val);
	}

	public static void restoreMagicOnReenter(String playerName, Integer val) {
		magicRestore.put(playerName, val);
	}

}
