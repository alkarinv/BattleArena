package mc.alk.arena.controllers;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

public class ProtectionController {
	public WorldGuardPlugin wgp;

	public WorldEditPlugin wep;
	public enum Owner {OWNER,MEMBER}
	HashMap<World, ProtectedRegion> defaultRegions = new HashMap<World, ProtectedRegion>();

	public boolean addDefaultRegion(World w, String id) {
		RegionManager mgr = wgp.getGlobalRegionManager().get(w);
		ProtectedRegion region = mgr.getRegion(id);
		if (region != null) {
			defaultRegions.put(w, region);
			return false;
		}
		
		region = new ProtectedCuboidRegion(id, new BlockVector(0,0,0),new BlockVector(0,0,0));
		try {
			wgp.getRegionManager(w).addRegion(region);
			mgr.save();
			region.setFlag(DefaultFlag.CHEST_ACCESS,State.ALLOW);
			region.setFlag(DefaultFlag.PVP,State.DENY);
			defaultRegions.put(w, region);
		} catch (ProtectionDatabaseException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ProtectedRegion addRegion(Player p, Selection sel, String id) {
		RegionManager mgr = wgp.getGlobalRegionManager().get(sel.getWorld());

		if (mgr.hasRegion(id)) {
			return mgr.getRegion(id);
		}

		ProtectedRegion region;

		// Detect the type of region from WorldEdit
		if (sel instanceof Polygonal2DSelection) {
			Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
			int minY = polySel.getNativeMinimumPoint().getBlockY();
			int maxY = polySel.getNativeMaximumPoint().getBlockY();
			region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
		} else if (sel instanceof CuboidSelection) {
			BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
			BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
			region = new ProtectedCuboidRegion(id, min, max);
		} else {
			return null;
		}
		/// TeamJoinResult our owner
		try {
			DefaultDomain owners = new DefaultDomain();
			final LocalPlayer wgplayer = wgp.wrapPlayer(p);
			owners.addPlayer(wgplayer);
			region.setOwners(owners);
			wgp.getRegionManager(p.getWorld()).addRegion(region);
			region.setParent(defaultRegions.get(p.getWorld()));
			mgr.save();
		} catch (ProtectionDatabaseException e) {
			e.printStackTrace();
			return null;
		} catch (CircularInheritanceException e) {
			e.printStackTrace();
			return null;
		}
		return region;
	}

	public boolean delete(World w, ProtectedRegion region) {
		RegionManager mgr = wgp.getRegionManager(w);
		try {
			mgr.removeRegion(region.getId());
			mgr.save();
		} catch (ProtectionDatabaseException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ProtectedRegion getRegion(World w, String id) {
		return wgp.getRegionManager(w).getRegion(id);
	}

	public boolean ownsRegion(Player p, ProtectedRegion region) {
		final LocalPlayer wgplayer = wgp.wrapPlayer(p);
		return region.isOwner(wgplayer);
	}

	public WorldEditPlugin getWorldEdit() {
		return wep;
	}


}
