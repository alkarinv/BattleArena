package mc.alk.arena.util;

import org.bukkit.entity.EntityType;

public class EntityUtil {

	static final String TAMED = "tamed_";
	public static EntityType parseEntityType(String str) {
		boolean tamed = str.startsWith(TAMED);
		if (tamed){
			str = str.substring(TAMED.length(), str.length());}
		EntityType et = EntityType.fromName(str);
		return et;
	}

}
