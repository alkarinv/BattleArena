package mc.alk.arena.controllers.plugins;

import mc.alk.arena.listeners.competition.plugins.FactionsListener;

public class FactionsController {
	static boolean hasFactions = false;

	public static boolean enabled() {
		return hasFactions;
	}

	public static boolean setPlugin(boolean enable) {
		hasFactions = FactionsListener.enable();
		return hasFactions;
	}
}
