package mc.alk.arena.controllers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
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
public class WorldGuardInterface {

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
		} catch (ProtectionDatabaseException e) {
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


	public static void loadWorldGuardPlugin() {
		Plugin plugin= Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

		if (plugin == null) {
			System.out.println("[ArenaSpleef] WorldEdit not detected!");
			return;
		}        
		wep = ((WorldEditPlugin) plugin);


		plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

		if (plugin == null) {
			System.out.println("[ArenaSpleef] WorldGuard not detected!");
			return;
		}        
		wgp = ((WorldGuardPlugin) plugin);
	}

	public static WorldEditPlugin getWorldEditPlugin() {
		return wep;
	}

	public static void updateProtectedRegion(Selection sel, ProtectedRegion pr) {
//		World w = sel.getWorld();
//		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
//		ProtectedRegion region = mgr.getRegion(id);
	}

	public static void createProtectedRegion(Selection sel, String makeRegionName) {
		// TODO Auto-generated method stub
		
	}

	public static void clearRegion(String region) {
		// TODO Auto-generated method stub
		
	}

}
