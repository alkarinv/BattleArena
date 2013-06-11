package mc.alk.arena.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.Permissions;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.PlayerRestoreController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.GameMode;
import org.bukkit.Location;
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
	static final HashMap<String,PlayerRestoreController> restore = new HashMap<String,PlayerRestoreController>();
	final BattleArenaController bac;

	public BAPlayerListener(BattleArenaController bac){
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
		if (restore.containsKey(event.getPlayer().getName())){
			if (restore.get(event.getPlayer().getName()).handle(event.getPlayer(),null)){
				restore.remove(event.getPlayer().getName());
			}
		}
	}

	/**
	 * Why priority highest, some other plugins try to force respawn the player in spawn(or some other loc)
	 * problem is if they have come from the creative world, their game mode gets reset to creative
	 * but the other plugin force spawns them at spawn... so they now have creative in an area they shouldnt
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event){
		if (restore.containsKey(event.getPlayer().getName())){
			if (restore.get(event.getPlayer().getName()).handle(event.getPlayer(),event)){
				restore.remove(event.getPlayer().getName());
			}
		}
	}

	private static PlayerRestoreController getOrCreateRestorer(final String name){
		if (restore.containsKey(name))
			return restore.get(name);
		PlayerRestoreController prc = new PlayerRestoreController(name);
		restore.put(name, prc);
		return prc;
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
		getOrCreateRestorer(playerName).setKill(true);
	}

	public static void clearInventoryOnReenter(String playerName) {
		getOrCreateRestorer(playerName).setClearInventory(true);
	}

	public static void teleportOnReenter(String playerName, Location destloc, Location lastloc) {
		PlayerRestoreController prc = getOrCreateRestorer(playerName);
		prc.setTp(destloc);
		prc.setLastLocs(lastloc);
	}

	public static void addMessageOnReenter(String playerName, String message) {
		getOrCreateRestorer(playerName).setMessage(message);
	}

	public static void restoreExpOnReenter(String playerName, Integer val) {
		getOrCreateRestorer(playerName).addExp(val);
	}

	public static void restoreItemsOnReenter(String playerName, PInv pinv) {
		getOrCreateRestorer(playerName).setItem(pinv);
	}

	public static void restoreMatchItemsOnReenter(String playerName, PInv pinv) {
		getOrCreateRestorer(playerName).setMatchItems(pinv);
	}

	public static void removeMatchItems(String playerName) {
		getOrCreateRestorer(playerName).removeMatchItems();
	}

	public static void clearWoolOnReenter(String playerName, int color) {
		getOrCreateRestorer(playerName).setClearWool(color);
	}

	public static void restoreGameModeOnEnter(String playerName, GameMode gamemode) {
		getOrCreateRestorer(playerName).setGamemode(gamemode);
	}

	public static void removeItemOnEnter(ArenaPlayer p, ItemStack is) {
		getOrCreateRestorer(p.getName()).addRemoveItem(is);
	}

	public static void removeItemsOnEnter(ArenaPlayer p, List<ItemStack> itemsToRemove) {
		getOrCreateRestorer(p.getName()).addRemoveItem(itemsToRemove);
	}

	public static void restoreHealthOnReenter(String playerName, Integer val) {
		getOrCreateRestorer(playerName).setHealth(val);
	}

	public static void restoreHungerOnReenter(String playerName, Integer val) {
		getOrCreateRestorer(playerName).setHunger(val);
	}

	public static void restoreMagicOnReenter(String playerName, Integer val) {
		getOrCreateRestorer(playerName).setMagic(val);
	}

	public static void killAllOnReenter(Set<String> keys) {
		if (keys==null)
			return;
		for (String name: keys){
			BAPlayerListener.killOnReenter(name);}
	}

	public static void clearInventoryOnReenter(Set<String> keys) {
		if (keys==null)
			return;
		for (String name: keys){
			BAPlayerListener.clearInventoryOnReenter(name);}
	}

	public static Map<String, PlayerRestoreController> getPlayerRestores() {
		return restore;
	}

}
