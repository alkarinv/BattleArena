package mc.alk.arena.controllers;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.MobArenaUtil;

import org.bukkit.plugin.Plugin;

public class MobArenaInterface {
	static MobArenaInterface mai = null;
	MobArenaUtil ma = null;

	public static void init(Plugin plugin){
		try {
			mai = new MobArenaInterface();
			mai.ma = new MobArenaUtil(plugin);
		} catch (Error e){
			mai = null;
			e.printStackTrace();
		}
	}
	public static boolean hasMobArena() {
		return mai != null;
	}

	public static boolean insideMobArena(ArenaPlayer p) {
		return mai.getMobArena().insideMobArena(p.getPlayer());
	}

	public MobArenaUtil getMobArena(){
		return ma;
	}
}
