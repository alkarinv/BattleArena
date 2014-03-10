package mc.alk.arena.controllers.plugins;

import mc.alk.arena.util.plugins.CombatTagUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CombatTagInterface {
    private static boolean enabled = false;

    public static void setPlugin(Plugin plugin) {
        CombatTagUtil.setPlugin(plugin);
        enabled = true;
    }

    public static boolean enabled(){
        return enabled;
    }

    public static void untag(Player player) {
        if (!enabled) return;
        CombatTagUtil.untag(player);
    }

    public static boolean isTagged(Player player) {
        return enabled && CombatTagUtil.isTagged(player);
    }

}
