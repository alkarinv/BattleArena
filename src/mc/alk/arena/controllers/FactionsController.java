package mc.alk.arena.controllers;

import mc.alk.arena.listeners.competition.FactionsListener;

public class FactionsController {
	static boolean hasFactions = false;

	public static boolean enabled() {
		return hasFactions;
	}

	public static boolean enableFactions(boolean enable) {
		hasFactions = FactionsListener.enable();
		return hasFactions;
	}
}
