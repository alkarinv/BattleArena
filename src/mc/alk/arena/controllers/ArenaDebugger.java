package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.ItemSpawn;
import net.minecraft.server.Entity;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.inventory.ItemStack;


public class ArenaDebugger {
	static HashMap<Arena,ArenaDebugger> arenas = null;
	

	public static ArenaDebugger getDebugger(Arena arena){
		if (arenas == null){
			arenas = new HashMap<Arena, ArenaDebugger>();
		}
		
		ArenaDebugger ad = arenas.get(arena);
		if (ad == null){
			ad = new ArenaDebugger(arena);
			arenas.put(arena, ad);
		}
		return ad;
	}
	

	public static void removeDebugger(ArenaDebugger ad) {
		arenas.remove(ad.arena);
		if (arenas.isEmpty()){
			arenas = null;
		}
	}
	
	Arena arena;
	HashMap<Location, ItemStack> oldBlocks = new HashMap<Location, ItemStack>(); /// Used for debugging with show/hide spawns
	HashMap<Integer, ItemSpawn> entityIds = new HashMap<Integer, ItemSpawn>(); /// Used for debugging with show/hide spawns
	
	public ArenaDebugger(Arena arena) {
		this.arena = arena;
	}

	static public Location getLocKey(Location l){
		Location rloc = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
		return rloc;
	}

	public void hideSpawns() {
		for (Location l : oldBlocks.keySet()){
			ItemStack is = oldBlocks.get(l);
			l.getBlock().setType(is.getType());
			l.getBlock().setData((byte)is.getDurability());
		}
		for (Integer i: entityIds.keySet()){
			ItemSpawn is = entityIds.get(i);
			WorldServer ws = getWorldServer(is.getWorld());
			Entity entity = ws.getEntity(i);
			if (entity != null){
				ws.removeEntity(entity);
			}
		}
	}
	
	public void showSpawns() {
		oldBlocks = new HashMap<Location,ItemStack>();
//		if (arena.spawnsGroups != null){
//			/// TODO fix showing spawns
////			for (ItemSpawn is: arena.spawnsGroups.values()){
////				Item item = is.loc.getWorld().dropItem(is.loc, is.is);
////				entityIds.put(item.getEntityId(), is);
////			}
//		}
		for (Location l: arena.getSpawnLocs().values()){
			Location key = getLocKey(l);
			if (!oldBlocks.containsKey(key)){
				Block b = l.getBlock();
				oldBlocks.put(key, new ItemStack(b.getType(), b.getData()));
				b.setTypeIdAndData(Material.WOOL.getId(), (byte)4, true);
			}
		}
	}

	public static WorldServer getWorldServer(org.bukkit.World world){
		return (world instanceof CraftWorld)?  ((CraftWorld)world).getHandle() : null;
	}


}
