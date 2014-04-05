package mc.alk.arena.listeners;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Permissions;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.PlayerRestoreController;
import mc.alk.arena.controllers.plugins.EssentialsController;
import mc.alk.arena.controllers.plugins.WorldGuardController;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 *
 * @author alkarin
 *
 */
public class BAPlayerListener implements Listener  {
	static final HashMap<UUID,PlayerRestoreController> restore = new HashMap<UUID,PlayerRestoreController>();
	final BattleArenaController bac;

	public BAPlayerListener(BattleArenaController bac){
		this.bac = bac;
	}

	/**
	 * Why priority highest, some other plugins try to force respawn the player in spawn(or some other loc)
	 * problem is if they have come from the creative world, their game mode gets reset to creative
	 * but the other plugin force spawns them at spawn... so they now have creative in an area they shouldnt
	 * @param event PlayerRespawnEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event){
        UUID id = PlayerUtil.getID(event.getPlayer());
        if (restore.containsKey(id)){
			if (!restore.get(id).handle(event.getPlayer(),event)){
				restore.remove(id);
			}
		}
	}

	/**
	 * This method is just used to handle essentials and the /back command
	 * @param event PlayerDeathEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event){
		if (!EssentialsController.enabled() || !PlayerController.hasArenaPlayer(event.getEntity()))
			return;
        ArenaPlayer ap = BattleArena.toArenaPlayer(event.getEntity());
        if (!restore.containsKey(ap.getID()))
                return;

        PlayerRestoreController prc = getOrCreateRestorer(ap);
		Location loc = prc.getBackLocation();
		if (loc != null){
			EssentialsController.setBackLocation(event.getEntity().getName(), loc);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event){
		if (!PlayerController.hasArenaPlayer(event.getPlayer()))
			return;
		ArenaPlayer p = PlayerController.getArenaPlayer(event.getPlayer());
		ArenaPlayerLeaveEvent aple = new ArenaPlayerLeaveEvent(p, p.getTeam(),
				ArenaPlayerLeaveEvent.QuitReason.QUITMC);
		aple.callEvent();
	}

	private static PlayerRestoreController getOrCreateRestorer(ArenaPlayer player) {
        final UUID id = player.getID();
        if (restore.containsKey(id)) {
            return restore.get(id);
        }
        PlayerRestoreController prc = new PlayerRestoreController(player);
        restore.put(id, prc);
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
		}
	}

	public static void killOnReenter(ArenaPlayer player) {
		getOrCreateRestorer(player).setKill(true);
	}

	public static void clearInventoryOnReenter(ArenaPlayer player) {
		getOrCreateRestorer(player).setClearInventory(true);
	}

	public static void teleportOnReenter(ArenaPlayer player, Location destloc, Location lastloc) {
		PlayerRestoreController prc = getOrCreateRestorer(player);
		prc.setTp(destloc);
		prc.setLastLocs(lastloc);
	}

	public static void addMessageOnReenter(ArenaPlayer player, String message) {
		getOrCreateRestorer(player).setMessage(message);
	}

	public static void restoreExpOnReenter(ArenaPlayer player, Integer val) {
		getOrCreateRestorer(player).addExp(val);
	}

	public static void restoreItemsOnReenter(ArenaPlayer player, PInv pinv) {
		getOrCreateRestorer(player).setItem(pinv);
	}

	public static void restoreMatchItemsOnReenter(ArenaPlayer player, PInv pinv) {
		getOrCreateRestorer(player).setMatchItems(pinv);
	}

	public static void removeMatchItems(ArenaPlayer player) {
		getOrCreateRestorer(player).removeMatchItems();
	}

	public static void clearWoolOnReenter(ArenaPlayer player, int color) {
		getOrCreateRestorer(player).setClearWool(color);
	}

	public static void restoreGameModeOnEnter(ArenaPlayer player, GameMode gamemode) {
		getOrCreateRestorer(player).setGamemode(gamemode);
	}

	public static void removeItemOnEnter(ArenaPlayer p, ItemStack is) {
		getOrCreateRestorer(p).addRemoveItem(is);
	}

	public static void removeItemsOnEnter(ArenaPlayer p, List<ItemStack> itemsToRemove) {
		getOrCreateRestorer(p).addRemoveItem(itemsToRemove);
	}

	public static void restoreHealthOnReenter(ArenaPlayer player, Double val) {
		getOrCreateRestorer(player).setHealth(val);
	}

	public static void restoreHungerOnReenter(ArenaPlayer player, Integer val) {
		getOrCreateRestorer(player).setHunger(val);
	}

	public static void restoreMagicOnReenter(ArenaPlayer player, Integer val) {
		getOrCreateRestorer(player).setMagic(val);
	}

	public static void deEnchantOnEnter(ArenaPlayer player) {
		getOrCreateRestorer(player).deEnchant();
	}
    public static void restoreEffectsOnReenter(ArenaPlayer player, Collection<PotionEffect> c) {
        getOrCreateRestorer(player).enchant(c);
    }

//    public static void killAllOnReenter(Set<String> keys) {
//		if (keys==null)
//			return;
//		for (String name: keys){
//			BAPlayerListener.killOnReenter(name);}
//	}

//	public static void clearInventoryOnReenter(Set<String> keys) {
//		if (keys==null)
//			return;
//		for (String name: keys){
//			BAPlayerListener.clearInventoryOnReenter(name);}
//	}

	public static Map<UUID, PlayerRestoreController> getPlayerRestores() {
		return restore;
	}

	public static void setBackLocation(ArenaPlayer player, Location location) {
		getOrCreateRestorer(player).setBackLocation(location);
	}

	public static Location getBackLocation(Player player) {
        UUID id = PlayerUtil.getID(player);
        return restore.containsKey(id) ? restore.get(id).getBackLocation() : null;
    }


}
