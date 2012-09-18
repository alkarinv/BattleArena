package mc.alk.arena.controllers;

import mc.alk.arena.util.WorldGuardUtil;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author alkarin
 * 
 * The key to these optional dependencies(OD) seem to be there can be no direct
 * function call to a method that USES any of the OD classes.
 * So this entire class is just a wrapper for functions.
 * Also other classes should not declare variables of the OD as a class variable
 *
 */
public class WorldGuardInterface {
	static boolean hasWorldGuardInterface = false;
	
	public static void init(){
		hasWorldGuardInterface = true;
	}
	public static class WorldGuardException extends Exception{
		private static final long serialVersionUID = 1L;
		public WorldGuardException(String msg) {
			super(msg);
		}
	}
	public static boolean hasWorldGuard() {
		return hasWorldGuardInterface;
	}

	public boolean addRegion(Player sender, String id) throws Exception {
		return WorldGuardUtil.addRegion(sender, id);
	}
	

	public static boolean hasRegion(World world, String id){
		return WorldGuardUtil.hasRegion(world, id);
	}

	public static void updateProtectedRegion(Player p, String id) throws Exception {
		WorldGuardUtil.updateProtectedRegion(p, id);
	}

	public static void createProtectedRegion(Player p, String id) throws Exception {
		WorldGuardUtil.createProtectedRegion(p, id);
	}
	
	public static void clearRegion(String world, String id) {
		WorldGuardUtil.clearRegion(world, id);
	}

	public static void isLeavingArea(final Location from, final Location to, World w, String id) {
		WorldGuardUtil.isLeavingArea(from , to , w, id);
	}

}
