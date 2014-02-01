package mc.alk.arena.util;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Stub class for future expansion
 *
 * @author alkarin
 */
@Deprecated
public class WorldGuardUtil {
    public static boolean hasWorldGuard = false;

    public static boolean hasWorldGuard() {
        return hasWorldGuard;
    }

    public static ProtectedRegion getRegion(String world, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.getRegion(world, id);
    }

    public static ProtectedRegion getRegion(World w, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.getRegion(w, id);
    }

    public static boolean hasRegion(WorldGuardRegion region) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.hasRegion(region);
    }

    public static boolean hasRegion(World world, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.hasRegion(world, id);
    }

    public static boolean hasRegion(String world, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.hasRegion(world, id);
    }


    public static ProtectedRegion updateProtectedRegion(Player p, String id) throws Exception {
        return mc.alk.arena.util.plugins.WorldGuardUtil.updateProtectedRegion(p, id);
    }

    public static ProtectedRegion createProtectedRegion(Player p, String id) throws Exception {
        return mc.alk.arena.util.plugins.WorldGuardUtil.createProtectedRegion(p, id);
    }

    public static void clearRegion(WorldGuardRegion region) {
        mc.alk.arena.util.plugins.WorldGuardUtil.clearRegion(region);
    }

    public static void clearRegion(String world, String id) {
        mc.alk.arena.util.plugins.WorldGuardUtil.clearRegion(world, id);
    }

    public static boolean isLeavingArea(final Location from, final Location to, final WorldGuardRegion region) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.isLeavingArea(from, to, region);
    }

    public static boolean isLeavingArea(final Location from, final Location to, final World w, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.isLeavingArea(from, to, w, id);
    }

    public static boolean setFlag(WorldGuardRegion region, String flag, boolean enable) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.setFlag(region, flag, enable);
    }

    public static Flag<?> getWGFlag(String flagString) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.getWGFlag(flagString);
    }

    public static StateFlag getStateFlag(String flagString) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.getStateFlag(flagString);
    }

    public static boolean setFlag(String worldName, String id, String flag, boolean enable) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.setFlag(worldName, id, flag, enable);
    }

    public static boolean setWorldGuard(Plugin plugin) {
        hasWorldGuard = true;
        return mc.alk.arena.util.plugins.WorldGuardUtil.setWorldGuard(plugin);
    }

    public static boolean allowEntry(Player player, String regionWorld, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.allowEntry(player, regionWorld, id);
    }

    public static boolean addMember(String playerName, WorldGuardRegion region) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.addMember(playerName, region);
    }

    public static boolean addMember(String playerName, String regionWorld, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.addMember(playerName, regionWorld, id);
    }

    public static boolean removeMember(String playerName, WorldGuardRegion region) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.removeMember(playerName, region);
    }

    public static boolean removeMember(String playerName, String regionWorld, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.removeMember(playerName, regionWorld, id);
    }

    public static void deleteRegion(String worldName, String id) {
        mc.alk.arena.util.plugins.WorldGuardUtil.deleteRegion(worldName, id);
    }


    public static boolean pasteSchematic(CommandSender consoleSender, String worldName, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.pasteSchematic(consoleSender, worldName, id);
    }

    public static boolean pasteSchematic(WorldGuardRegion region) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.pasteSchematic(region);
    }

    public static boolean pasteSchematic(String worldName, String id) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.pasteSchematic(worldName, id);
    }

    public static boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.pasteSchematic(sender, pr, schematic, world);
    }

    public static boolean saveSchematic(Player p, String schematicName) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.saveSchematic(p, schematicName);
    }

    public static boolean loadAndPaste(CommandContext args, WorldEdit we,
                                       LocalSession session, com.sk89q.worldedit.LocalPlayer player, EditSession editSession, Vector pos) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.loadAndPaste(args, we, session, player, editSession, pos);
    }

    public static boolean contains(Location location, WorldGuardRegion region) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.contains(location, region);
    }

    public static boolean hasPlayer(String playerName, WorldGuardRegion region) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.hasPlayer(playerName, region);
    }

    public static boolean trackRegion(WorldGuardRegion region) throws RegionNotFound {
        return mc.alk.arena.util.plugins.WorldGuardUtil.trackRegion(region);
    }

    public static boolean trackRegion(String world, String id) throws RegionNotFound {
        return mc.alk.arena.util.plugins.WorldGuardUtil.trackRegion(world, id);
    }

    public static int regionCount() {
        return mc.alk.arena.util.plugins.WorldGuardUtil.regionCount();
    }

    public static WorldGuardRegion getContainingRegion(Location location) {
        return mc.alk.arena.util.plugins.WorldGuardUtil.getContainingRegion(location);
    }

}
