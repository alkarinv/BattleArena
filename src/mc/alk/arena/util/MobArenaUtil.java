package mc.alk.arena.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.garbagemule.MobArena.MobArena;

public class MobArenaUtil {
	MobArena ma = null;
	public MobArenaUtil(Plugin plugin){
		ma = (MobArena) plugin;
	}
	public boolean insideMobArena(Player player) {
		if (ma == null)
			return false;
		boolean has = ma.getArenaMaster().getArenaWithPlayer(player) != null;
		if (!has){
			has = ma.getArenaMaster().getArenaWithSpectator(player) != null;
		}
		return has;
	}
}
