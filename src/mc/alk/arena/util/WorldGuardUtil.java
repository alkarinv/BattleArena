package mc.alk.arena.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import mc.alk.arena.controllers.WorldGuardController.WorldGuardFlag;
import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.regions.WorldGuardRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.commands.SchematicCommands;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
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
	static Map<String,Set<String>> trackedRegions = new ConcurrentHashMap<String,Set<String>>();

	public static boolean hasWorldGuard() {
		return WorldEditUtil.hasWorldEdit() && hasWorldGuard;
	}

	/**
	 * Hold over from spleef, use createProtectedRegion instead
	 * @param sender
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@Deprecated
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

	public static ProtectedRegion getRegion(String world, String id) {
		World w = Bukkit.getWorld(world);
		return getRegion(w,id);
	}

	public static ProtectedRegion getRegion(World w, String id) {
		if (w == null)
			return null;
		return wgp.getRegionManager(w).getRegion(id);
	}

	public static boolean hasRegion(WorldGuardRegion region){
		return hasRegion(region.getWorld(),region.getRegionID());
	}

	public static boolean hasRegion(World world, String id){
		RegionManager mgr = wgp.getGlobalRegionManager().get(world);
		return mgr.hasRegion(id);
	}

	public static boolean hasRegion(String world, String id){
		World w = Bukkit.getWorld(world);
		if (w == null)
			return false;
		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
		return mgr.hasRegion(id);
	}


	public static ProtectedRegion updateProtectedRegion(Player p, String id) throws Exception {
		return createRegion(p, id);
	}

	public static ProtectedRegion createProtectedRegion(Player p, String id) throws Exception {
		return createRegion(p,id);
	}

	private static ProtectedRegion createRegion(Player p, String id)
			throws ProtectionDatabaseException {
		Selection sel = WorldEditUtil.getSelection(p);
		World w = sel.getWorld();
		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
		mgr.removeRegion(id);

		ProtectedRegion region = new ProtectedCuboidRegion(id,
				sel.getNativeMinimumPoint().toBlockVector(), sel.getNativeMaximumPoint().toBlockVector());
		region.setPriority(11); /// some relatively high priority
		region.setFlag(DefaultFlag.PVP,State.ALLOW);
		wgp.getRegionManager(w).addRegion(region);
		mgr.save();
		return region;
	}


	public static void clearRegion(WorldGuardRegion region) {
		clearRegion(region.getRegionWorld(),region.getRegionID());
	}

	public static void clearRegion(String world, String id) {
		World w = Bukkit.getWorld(world);
		if (w==null)
			return;
		ProtectedRegion region = getRegion(w,id);
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
	public static boolean isLeavingArea(final Location from, final Location to, final WorldGuardRegion region) {
		return isLeavingArea(from,to,region.getWorld(),region.getRegionID());
	}
	public static boolean isLeavingArea(final Location from, final Location to, final World w,  String id) {
		ProtectedRegion pr = getRegion(w,id);
		if (pr == null)
			return false;
		return (!pr.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ()) &&
				pr.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ()));
	}

	public static boolean setFlag(WorldGuardRegion region, WorldGuardFlag flag, boolean enable) {
		return setFlag(region.getRegionWorld(), region.getRegionID(), flag,enable);
	}

	public static boolean setFlag(String worldName, String id, WorldGuardFlag flag, boolean enable) {
		World w = Bukkit.getWorld(worldName);
		if (w == null)
			return false;
		ProtectedRegion pr = getRegion(w, id);
		if (pr == null)
			return false;
		StateFlag f = null;
		switch (flag){
		case ENTRY: f = DefaultFlag.ENTRY; break;
		case EXIT: f = DefaultFlag.EXIT; break;
		default: return false;
		}
		State newState = enable ? State.ALLOW : State.DENY;
		State state = pr.getFlag(f);

		if (state == null || state != newState){
			pr.setFlag(f, newState);}
		return true;
	}

	public static boolean setWorldGuard(Plugin plugin) {
		wgp = (WorldGuardPlugin) plugin;
		hasWorldGuard = true;
		return hasWorldGuard();
	}

	public static boolean allowEntry(Player player, String regionWorld, String id) {
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

	public static boolean addMember(String playerName, WorldGuardRegion region) {
		return addMember(playerName, region.getRegionWorld(),region.getRegionID());
	}
	public static boolean addMember(String playerName, String regionWorld, String id) {
		return changeMember(playerName,regionWorld,id,true);
	}

	public static boolean removeMember(String playerName, WorldGuardRegion region) {
		return removeMember(playerName, region.getRegionWorld(),region.getRegionID());
	}
	public static boolean removeMember(String playerName, String regionWorld, String id) {
		return changeMember(playerName,regionWorld,id,false);
	}

	private static boolean changeMember(String name, String regionWorld, String id, boolean add){
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

	public static void deleteRegion(String worldName, String id) {
		World w = Bukkit.getWorld(worldName);
		if (w == null)
			return;
		RegionManager mgr = wgp.getRegionManager(w);
		if (mgr == null)
			return;
		mgr.removeRegion(id);
	}


	public static boolean pasteSchematic(CommandSender consoleSender, String worldName, String id) {
		World w = Bukkit.getWorld(worldName);
		if (w ==null)
			return false;
		ProtectedRegion pr = getRegion(w,id);
		if (pr == null)
			return false;
		return pasteSchematic(consoleSender,pr,id,w);
	}

	public static boolean pasteSchematic(WorldGuardRegion region) {
		return pasteSchematic(region.getRegionWorld(),region.getRegionID());
	}

	public static boolean pasteSchematic(String worldName, String id) {
		return pasteSchematic(Bukkit.getConsoleSender(),worldName,id);
	}

	public static boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world) {
		CommandContext cc = null;
		String args[] = {"load", schematic};
		final WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
		final WorldEdit we = wep.getWorldEdit();
		LocalPlayer bcs = new ConsolePlayer(wep,wep.getServerInterface(),sender, world);

		final LocalSession session = wep.getWorldEdit().getSession(bcs);
		session.setUseInventory(false);
		EditSession editSession = session.createEditSession(bcs);
		Vector pos = new Vector(pr.getMinimumPoint());
		try {
			cc = new CommandContext(args);
			return loadAndPaste(cc, we, session, bcs,editSession,pos);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static class ConsolePlayer extends BukkitCommandSender {
		LocalWorld world;
		public ConsolePlayer(WorldEditPlugin plugin, ServerInterface server,CommandSender sender, World w) {
			super(plugin, server, sender);
			world = BukkitUtil.getLocalWorld(w);
		}

		@Override
		public boolean isPlayer() {
			return true;
		}
		@Override
		public LocalWorld getWorld() {
			return world;
		}
//	    @Override
//	    public BlockBag getInventoryBlockBag() {
//	        return new BukkitPlayerBlockBag(player);
//	    }


	}
	public static boolean saveSchematic(Player p, String schematicName){
		CommandContext cc = null;
		WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
		final LocalSession session = wep.getSession(p);
		final BukkitPlayer lPlayer = wep.wrapPlayer(p);
		EditSession editSession = session.createEditSession(lPlayer);

		try {
			Region region = session.getSelection(lPlayer.getWorld());
			Vector min = region.getMinimumPoint();
			Vector max = region.getMaximumPoint();
			CuboidClipboard clipboard = new CuboidClipboard(
					max.subtract(min).add(new Vector(1, 1, 1)),
					min, new Vector(0,0,0));
			clipboard.copy(editSession);
			session.setClipboard(clipboard);

			SchematicCommands sc = new SchematicCommands(wep.getWorldEdit());
			String args2[] = {"save", "mcedit", schematicName};
			cc = new CommandContext(args2);
			sc.save(cc, session, lPlayer, editSession);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This is just copied and pasted from world edit source, with small changes to also paste
	 * @param args
	 * @param we
	 * @param session
	 * @param player
	 * @param editSession
	 */
	public static boolean loadAndPaste(CommandContext args, WorldEdit we,
			LocalSession session, com.sk89q.worldedit.LocalPlayer player, EditSession editSession, Vector pos) {

		LocalConfiguration config = we.getConfiguration();

		String filename = args.getString(0);
		File dir = we.getWorkingDirectoryFile(config.saveDir);
		File f = null;
		try {
			f = we.getSafeOpenFile(player, dir, filename, "schematic",new String[] {"schematic"});
			String filePath = f.getCanonicalPath();
			String dirPath = dir.getCanonicalPath();

			if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
				printError(player,"Schematic could not read or it does not exist.");
				return false;
			}
			SchematicFormat format = SchematicFormat.getFormat(f);
			if (format == null) {
				printError(player,"Unknown schematic format for file" + f);
				return false;
			}

			if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
				printError(player,"Schematic could not read or it does not exist.");
			} else {
				session.setClipboard(format.load(f));
				//				WorldEdit.logger.info(player.getName() + " loaded " + filePath);
				//				print(player,filePath + " loaded");
			}
			session.getClipboard().paste(editSession, pos, false, true);
			//			WorldEdit.logger.info(player.getName() + " pasted schematic" + filePath +"  at " + pos);
		} catch (DataException e) {
			printError(player,"Load error: " + e.getMessage());
		} catch (IOException e) {
			printError(player,"Schematic could not read or it does not exist: " + e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			printError(player,"Error : " + e.getMessage());
		}
		return true;
	}

	private static void printError(LocalPlayer player, String msg){
		if (player == null){
			Log.err(msg);
		} else {
			player.printError(msg);
		}
	}

	public static boolean contains(Location location, WorldGuardRegion region) {
		ProtectedRegion pr = getRegion(region.getWorld(),region.getRegionID());
		if (pr == null)
			return false;
		return pr.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public static boolean hasPlayer(String playerName, WorldGuardRegion region) {
		ProtectedRegion pr = getRegion(region.getWorld(),region.getRegionID());
		if (pr == null)
			return true;
		DefaultDomain dd = pr.getMembers();
		if (dd.contains(playerName))
			return true;
		dd = pr.getOwners();
		return dd.contains(playerName);
	}

	public static boolean trackRegion(WorldGuardRegion region) throws RegionNotFound {
		return trackRegion(region.getRegionWorld(), region.getRegionID());
	}

	public static boolean trackRegion(String world, String id) throws RegionNotFound{
		ProtectedRegion pr = WorldGuardUtil.getRegion(world, id);
		if (pr == null){
			throw new RegionNotFound("The region " + id +" not found in world " + world);}
		Set<String> regions = trackedRegions.get(world);
		if (regions == null){
			regions = new CopyOnWriteArraySet<String>();
			trackedRegions.put(world, regions);
		}
		return regions.add(id);
	}

	public static int regionCount() {
		int count = 0;
		for (String world : trackedRegions.keySet()){
			Set<String> sets = trackedRegions.get(world);
			if (sets != null)
				count += sets.size();
		}
		return count;
	}

	public static WorldGuardRegion getContainingRegion(Location location) {
		for (String world : trackedRegions.keySet()){
			World w = Bukkit.getWorld(world);
			if (w == null || location.getWorld().getUID() != w.getUID())
				continue;
			for (String id: trackedRegions.get(world)){
				ProtectedRegion pr = getRegion(w,id);
				if (pr == null)
					continue;
				if (pr.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
					return new WorldGuardRegion(world, id);
			}
		}
		return null;
	}

}
