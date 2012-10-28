package mc.alk.arena.util;

import mc.alk.arena.controllers.WorldGuardInterface.WorldGuardFlag;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
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
	public static WorldGuardPlugin wgp;
	public static boolean hasWorldGuard = false;

	public static boolean hasWorldGuard() {
		return WorldEditUtil.hasWorldEdit && hasWorldGuard;
	}

	public static boolean addRegion(Player sender, String id) throws Exception {
		Selection sel = WorldEditUtil.getSelection(sender);
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
		if (w == null)
			return null;
		return wgp.getRegionManager(w).getRegion(id);
	}

	public static boolean hasRegion(World world, String id){
		RegionManager mgr = wgp.getGlobalRegionManager().get(world);
		return mgr.hasRegion(id);
	}


	public static void updateProtectedRegion(Player p, String id) throws Exception {
		Selection sel = WorldEditUtil.getSelection(p);
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
		Selection sel = WorldEditUtil.getSelection(p);
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
	public static boolean setFlag(String id, String worldName, WorldGuardFlag flag, boolean enable) {
		World w = Bukkit.getWorld(worldName);
		if (w == null)
			return false;
		ProtectedRegion pr = getRegion(w, id);
		if (pr == null)
			return false;
		StateFlag f = null;
		switch (flag){
		case ENTRY:
			f = DefaultFlag.ENTRY;
			break;
		case EXIT:
			f = DefaultFlag.EXIT;
			break;

		default:
			return false;
		}
		State newState = enable ? State.ALLOW : State.DENY;
		State state = pr.getFlag(f);
		if (state == null || state != newState){
			pr.setFlag(f, newState);
		}
		return true;
	}

	public static boolean setWorldGuard(Plugin plugin) {
		wgp = (WorldGuardPlugin) plugin;
		hasWorldGuard = true;
		return hasWorldGuard();
	}

	public static boolean allowEntry(Player player, String id, String regionWorld) {
		World w = Bukkit.getWorld(regionWorld);
		if (w == null)
			return false;
		ProtectedRegion pr = getRegion(w, id);
		if (pr == null)
			return false;
		DefaultDomain dd = pr.getMembers();
		dd.addPlayer(player.getName());
		pr.setMembers(dd);
		return true;
	}

	public static boolean addMember(String name, String id, String regionWorld) {
		return changeMember(name,id,regionWorld,true);
	}

	public static boolean removeMember(String name, String id, String regionWorld) {
		return changeMember(name,id,regionWorld,false);
	}

	private static boolean changeMember(String name, String id, String regionWorld, boolean add){
		World w = Bukkit.getWorld(regionWorld);
		if (w == null)
			return false;
		ProtectedRegion pr = getRegion(w, id);
		if (pr == null)
			return false;

		DefaultDomain dd = pr.getMembers();
		if (add){
		dd.addPlayer(name);
		} else {
			dd.removePlayer(name);
		}
		pr.setMembers(dd);
		return true;

	}

	public static void deleteRegion(String id, String worldName) {
		World w = Bukkit.getWorld(worldName);
		if (w == null)
			return;
		RegionManager mgr = wgp.getRegionManager(w);
		if (mgr == null)
			return;
		mgr.removeRegion(id);
	}

}
