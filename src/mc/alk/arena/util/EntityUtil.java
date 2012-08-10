package mc.alk.arena.util;

import org.bukkit.entity.EntityType;

public class EntityUtil {

	static final String TAMED = "tamed_";
	public static EntityType parseEntity(String str) {
		boolean tamed = str.startsWith(TAMED);
		if (tamed){
			str = str.substring(TAMED.length(), str.length());
		}
//		System.out.println("str = " + str);
		EntityType et = EntityType.fromName(str);
//		System.out.println("et = " + et);
		if (tamed && et == EntityType.WOLF){
//			Wolf w = EntityType.
		}
			
		return et;
	}

}
