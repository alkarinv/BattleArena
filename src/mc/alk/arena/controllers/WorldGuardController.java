package mc.alk.arena.controllers;

import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.util.WorldEditUtil;
import mc.alk.arena.util.WorldGuardUtil;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author alkarin
 *
 * The key to these optional dependencies(OD) seem to be there can be no direct
 * function call to a method that USES any of the OD classes.
 * So this entire class is just a wrapper for functions.
 * Also other classes should not declare variables of the OD as a class variable
 *
 */
public class WorldGuardController {
	static boolean hasWorldGuard = false;
	static boolean hasWorldEdit = false;

	public static enum WorldGuardFlag{
		ENTRY, EXIT
	}

	public static class WorldGuardException extends Exception{
		private static final long serialVersionUID = 1L;
		public WorldGuardException(String msg) {
			super(msg);
		}
	}

	public static boolean hasWorldGuard() {
		return hasWorldGuard;
	}

	public static boolean hasWorldEdit() {
		return hasWorldEdit;
	}

	public boolean addRegion(Player sender, String id) throws Exception {
		return WorldGuardUtil.createProtectedRegion(sender, id)!=null;
	}

	public static boolean hasRegion(WorldGuardRegion region){
		return WorldGuardUtil.hasRegion(region);
	}
	public static boolean hasRegion(World world, String id){
		return WorldGuardUtil.hasRegion(world, id);
	}

	public static boolean hasRegion(String world, String id){
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

	public static void clearRegion(WorldGuardRegion region) {
		WorldGuardUtil.clearRegion(region);
	}

	public static boolean isLeavingArea(final Location from, final Location to, WorldGuardRegion region) {
		return WorldGuardUtil.isLeavingArea(from , to , region);
	}
	public static boolean isLeavingArea(final Location from, final Location to, World w, String id) {
		return WorldGuardUtil.isLeavingArea(from , to , w, id);
	}

	public static boolean setWorldGuard(Plugin plugin) {
		hasWorldGuard = WorldGuardUtil.setWorldGuard(plugin);
		return hasWorldGuard;
	}

	public static boolean setWorldEdit(Plugin plugin) {
		hasWorldEdit = WorldEditUtil.setWorldEdit(plugin);
		return hasWorldEdit;
	}

	public static boolean setFlag(WorldGuardRegion region, WorldGuardFlag flag, boolean enable) {
		return WorldGuardUtil.setFlag(region, flag, enable);
	}

	public static boolean setFlag(String worldName, String id, WorldGuardFlag flag, boolean enable) {
		return WorldGuardUtil.setFlag(worldName, id, flag, enable);
	}

	public static void allowEntry(Player player, String regionWorld, String id) {
		WorldGuardUtil.allowEntry(player,regionWorld, id);
	}

	public static void addMember(String playerName, WorldGuardRegion region) {
		WorldGuardUtil.addMember(playerName, region);
	}

	public static void addMember(String playerName, String regionWorld, String id) {
		WorldGuardUtil.addMember(playerName,regionWorld, id);
	}

	public static void removeMember(String playerName, WorldGuardRegion region) {
		WorldGuardUtil.removeMember(playerName,region);
	}

	public static void removeMember(String playerName, String regionWorld, String id) {
		WorldGuardUtil.removeMember(playerName,regionWorld, id);
	}

	public static void deleteRegion(String worldName, String id) {
		WorldGuardUtil.deleteRegion(worldName, id);
	}

	public static void saveSchematic(Player p, String id) {
		WorldGuardUtil.saveSchematic(p,id);
	}

	public static void pasteSchematic(CommandSender sender, String regionWorld, String id) {
		WorldGuardUtil.pasteSchematic(sender,regionWorld,id);
	}

	public static void pasteSchematic(String regionWorld, String id) {
		WorldGuardUtil.pasteSchematic(regionWorld,id);
	}

	public static void pasteSchematic(WorldGuardRegion region) {
		WorldGuardUtil.pasteSchematic(region);
	}

	public static boolean regionContains(Location location, WorldGuardRegion region) {
		return WorldGuardUtil.contains(location, region);
	}

	public static boolean hasPlayer(String playerName, WorldGuardRegion region) {
		return WorldGuardUtil.hasPlayer(playerName,region);
	}

	public static int regionCount() {
		return WorldGuardUtil.regionCount();
	}

	public static WorldGuardRegion getContainingRegion(Location location) {
		return WorldGuardUtil.getContainingRegion(location);
	}

	public static boolean trackRegion(WorldGuardRegion region) throws RegionNotFound{
		return WorldGuardUtil.trackRegion(region);
	}

	public static boolean trackRegion(String world, String id) throws RegionNotFound{
		return WorldGuardUtil.trackRegion(world, id);
	}
}
