package mc.alk.arena.util.plugins;

import com.trc202.CombatTag.CombatTag;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CombatTagUtil {
    static CombatTag tag;

    public static void setPlugin(Plugin plugin) {
        tag = (CombatTag) plugin;
    }

    public static void untag(Player player) {
        tag.removeTagged(player.getName());
    }

    public static boolean isTagged(Player player) {
        return tag.isInCombat(player.getName());
    }
}
