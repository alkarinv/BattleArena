package mc.alk.arena.controllers;

import mc.alk.arena.listeners.TagAPIListener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TagAPIController {
	static boolean hasTagAPI = false;

	public static boolean enabled() {
		return hasTagAPI;
	}

	public static void enableTagAPI(boolean enable) {
		hasTagAPI = enable;
	}

	public static void setNameColor(Player player, ChatColor teamColor) {
		try{TagAPIListener.setNameColor(player,teamColor);}catch(Exception e){e.printStackTrace();}
	}

	public static void removeNameColor(Player player) {
		try{TagAPIListener.removeNameColor(player);}catch(Exception e){e.printStackTrace();}
	}

}
