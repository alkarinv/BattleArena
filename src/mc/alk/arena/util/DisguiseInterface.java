package mc.alk.arena.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DisguiseInterface {
	public static final int DEFAULT = Integer.MAX_VALUE;

	private static boolean enabled = false;

	public static void setDisguiseCraft(Plugin plugin){
		DisguiseUtil.setDisguiseCraft(plugin);
		enabled = true;
	}

	public static boolean enabled(){
		return enabled;
	}

	public static void undisguise(Player player) {
		if (!enabled) return;
		DisguiseUtil.undisguise(player);
	}

	public static void disguisePlayer(Player player, String disguise) {
		if (!enabled) return;
		DisguiseUtil.disguisePlayer(player, disguise);
	}

}
