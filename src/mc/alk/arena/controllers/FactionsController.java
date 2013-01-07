package mc.alk.arena.controllers;

import mc.alk.arena.listeners.FactionsListener;

import org.bukkit.entity.Player;

public class FactionsController {
	static boolean hasFactions = false;

	public static boolean enabled() {
		return hasFactions;
	}

	public static boolean enableFactions(boolean enable) {
		hasFactions = FactionsListener.hasPowerLoss();
		return hasFactions;
	}

	public static void addPlayer(Player player) {
		try{FactionsListener.addPlayer(player);}catch(Exception e){e.printStackTrace();}
	}

	public static void removePlayer(Player player) {
		try{FactionsListener.removePlayer(player);}catch(Exception e){e.printStackTrace();}
	}

}
