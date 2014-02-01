package mc.alk.arena.controllers.plugins;

import mc.alk.arena.listeners.competition.plugins.TagAPIListener;
import mc.alk.arena.objects.arenas.ArenaListener;


public class TagAPIController {
	static boolean hasTagAPI = false;

	public static boolean enabled() {
		return hasTagAPI;
	}

	public static void setEnable(boolean enable) {
		hasTagAPI = enable;
		TagAPIListener.enable();
	}

	public static ArenaListener getNewListener() {
		return TagAPIListener.INSTANCE;
	}
}
