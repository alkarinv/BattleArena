package mc.alk.arena.controllers;

import mc.alk.arena.listeners.competition.TagAPIListener;
import mc.alk.arena.objects.arenas.ArenaListener;


public class TagAPIController {
	static boolean hasTagAPI = false;

	public static boolean enabled() {
		return hasTagAPI;
	}

	public static void setEnable(boolean enable) {
		hasTagAPI = enable;
		TagAPIListener.enable(enable);
	}

	public static ArenaListener getNewListener() {
		return TagAPIListener.INSTANCE;
	}
}
