package mc.alk.arena.util;

import mc.alk.arena.util.compat.IEntityHelper;
import mc.alk.plugin.updater.Version;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;

public class EntityUtil {

	static final String TAMED = "tamed_";
    static IEntityHelper handler;

    /**
     * 1_4_5 was the version where colors came in
     */
    static {
        Class<?>[] args = {};
        try {
            Version version = Util.getCraftBukkitVersion();
            if (version.compareTo("v1_4_5") >= 0){
                final Class<?> clazz = Class.forName("mc.alk.arena.util.compat.v1_4_5.EntityHelper");
                handler = (IEntityHelper) clazz.getConstructor(args).newInstance((Object[])args);
            } else {
                final Class<?> clazz = Class.forName("mc.alk.arena.util.compat.pre.EntityHelper");
                handler = (IEntityHelper) clazz.getConstructor(args).newInstance((Object[])args);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

	public static EntityType parseEntityType(String str) {
		boolean tamed = str.startsWith(TAMED);
		if (tamed){
			str = str.substring(TAMED.length(), str.length());}
        return EntityType.fromName(str);
	}

    public static void setCollarColor(Wolf wolf, DyeColor color) {
        handler.setCollarColor(wolf, color);
    }
}
