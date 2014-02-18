package mc.alk.arena.controllers.plugins;

import mc.alk.arena.util.DisguiseUtil;
import mc.alk.arena.util.plugins.DisguiseCraftUtil;
import mc.alk.arena.util.plugins.LibsDisguiseUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DisguiseInterface {
	public static final int DEFAULT = Integer.MAX_VALUE;

    private static DisguiseUtil handler;
    static boolean hasDC = false;
    static boolean hasLD = false;

    public static void setDisguiseCraft(Plugin plugin){
		handler = DisguiseCraftUtil.setPlugin(plugin);
        if (handler != null)
            hasDC = true;
    }

    public static void setLibsDisguise(Plugin plugin){
        handler = LibsDisguiseUtil.setPlugin(plugin);
        if (handler != null)
            hasLD = true;
    }

    public static boolean enabled(){
		return handler != null;
	}

	public static void undisguise(Player player) {
		if (!enabled()) return;
		handler.undisguise(player);
	}

	public static void disguisePlayer(Player player, String disguise) {
		if (!enabled()) return;
        handler.disguisePlayer(player, disguise);
	}

    public static boolean hasLibs() {
        return hasLD;
    }

    public static boolean hasDC() {
        return hasDC;
    }
}
