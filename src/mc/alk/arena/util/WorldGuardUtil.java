package mc.alk.arena.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Stub class for future expansion
 * @author alkarin
 *
 */
public class WorldGuardUtil {	
	public static WorldEditPlugin wep;
	public static WorldGuardPlugin wgp;

	public static class WorldGuardException extends Exception{
		private static final long serialVersionUID = 1L;
		public WorldGuardException(String msg) {
			super(msg);
		}
	}
	
	public static boolean hasWorldGuard() {
		return wgp != null && wep != null;
	}

	public static boolean addRegion(Player sender, String id) throws Exception {
		Selection sel = getSelection(sender);
		World w = sel.getWorld();
		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
		ProtectedRegion region = mgr.getRegion(id);

		region = new ProtectedCuboidRegion(id, 
				sel.getNativeMinimumPoint().toBlockVector(), sel.getNativeMaximumPoint().toBlockVector());
		try {
			wgp.getRegionManager(w).addRegion(region);
			mgr.save();
			region.setFlag(DefaultFlag.PVP,State.DENY);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static ProtectedRegion getRegion(World w, String id) {
		return wgp.getRegionManager(w).getRegion(id);
	}

	public static boolean hasRegion(World world, String id){
		RegionManager mgr = wgp.getGlobalRegionManager().get(world);
		return mgr.hasRegion(id);
	}


	public static Selection getSelection(Player player) {
		return wep.getSelection(player);
	}

	public static WorldEditPlugin getWorldEditPlugin() {
		return wep;
	}

	public static void updateProtectedRegion(Player p, String id) throws Exception {
		Selection sel = wep.getSelection(p);
		World w = sel.getWorld();
		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
		mgr.removeRegion(id);

		ProtectedRegion region = mgr.getRegion(id);

		region = new ProtectedCuboidRegion(id, 
				sel.getNativeMinimumPoint().toBlockVector(), sel.getNativeMaximumPoint().toBlockVector());
		region.setPriority(11); /// some relatively high priority
		region.setFlag(DefaultFlag.PVP,State.ALLOW);
		wgp.getRegionManager(w).addRegion(region);
		mgr.save();
	}

	public static ProtectedRegion createProtectedRegion(Player p, String id) throws Exception {
		Selection sel = wep.getSelection(p);
		World w = sel.getWorld();
		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
		ProtectedRegion region = mgr.getRegion(id);

		region = new ProtectedCuboidRegion(id, 
				sel.getNativeMinimumPoint().toBlockVector(), sel.getNativeMaximumPoint().toBlockVector());
		region.setPriority(11); /// some relatively high priority
		region.setFlag(DefaultFlag.PVP,State.ALLOW);
		wgp.getRegionManager(w).addRegion(region);
		mgr.save();
		return region;
	}
	
//	public static void updateProtectedRegion(Selection sel, ProtectedRegion pr) throws Exception {
//		wgi._updateProtectedRegion(sel, pr);
//	}

	public static void clearRegion(String world, String id) {
		World w = Bukkit.getWorld(world);
		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
		if (mgr == null)
			return;
		ProtectedRegion region = mgr.getRegion(id);
		if (region == null)
			return;
		Location l;
		for (Item entity: w.getEntitiesByClass(Item.class)) {
			l = entity.getLocation();
			if (region.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ())){
				entity.remove();
			}
		}
	}

	public static boolean isLeavingArea(final Location from, final Location to, final World w, final String id) {
		ProtectedRegion pr = getRegion(w, id);
    	return (!pr.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ()) &&
    			pr.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ()));
	}

	public static boolean setWorldGuard(Plugin plugin) {
		wgp = (WorldGuardPlugin) plugin;
		return hasWorldGuard();
	}

	public static boolean setWorldEdit(Plugin plugin) {
		wep = (WorldEditPlugin) plugin;
		return hasWorldGuard();
	}

}
