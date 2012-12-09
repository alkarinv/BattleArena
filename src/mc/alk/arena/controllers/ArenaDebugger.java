package mc.alk.arena.controllers;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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

	public ArenaDebugger(Arena arena) {
		this.arena = arena;
	}

	static public Location getLocKey(Location l){
		Location rloc = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
		return rloc;
	}

	public void hideSpawns(Player player) {
		for (Location l : oldBlocks.keySet()){
			ItemStack is = oldBlocks.get(l);
			player.sendBlockChange(l, is.getType(), (byte) is.getDurability());
		}
		oldBlocks.clear();
		SpawnController sc = arena.getSpawnController();
		if (sc != null){
			sc.stop();
		}
	}

	public void showSpawns(Player player) {
		oldBlocks = new HashMap<Location,ItemStack>();
		SpawnController sc = arena.getSpawnController();
		if (sc != null){
			sc.start();
		}
		Map<Integer,Location> locs = arena.getSpawnLocs();
		if (locs != null){
			for (Integer i: locs.keySet()){
				changeBlocks(player, locs.get(i), TeamUtil.getTeamHead(i));
			}
		}
		locs = arena.getWaitRoomSpawnLocs();
		if (locs != null){
			for (Integer i: locs.keySet()){
				changeBlocks(player, locs.get(i), TeamUtil.getTeamHead(i));
			}
		}
	}

	private void changeBlocks(Player player, Location l, ItemStack is) {
		Location key = getLocKey(l);
		if (!oldBlocks.containsKey(key)){
			Block b = l.getBlock();
			player.sendBlockChange(l, is.getTypeId(), (byte)is.getDurability());
			oldBlocks.put(key, new ItemStack(b.getType(), b.getData()));
		}
	}

}
