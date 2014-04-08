package mc.alk.arena.util.plugins;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.kitteh.vanish.VanishPlugin;

public class VanishNoPacketUtil {
	static VanishPlugin vanish;

	public static void setPlugin(Plugin plugin) {
		vanish = (VanishPlugin) plugin;
	}

	public static boolean isVanished(Player player) {
		return vanish.getManager().isVanished(player);
	}

	public static void toggleVanish(Player player) {
		vanish.getManager().toggleVanish(player);
	}
}
