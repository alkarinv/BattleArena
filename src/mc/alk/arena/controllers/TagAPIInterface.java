package mc.alk.arena.controllers;

import mc.alk.arena.listeners.TagAPIListener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TagAPIInterface {
	static boolean hasTagAPI = false;

	public static boolean enabled() {
		return hasTagAPI;
	}

	public static void enableTagAPI(boolean enable) {
		hasTagAPI = enable;
	}

	public static void setNameColor(Player player, ChatColor teamColor) {
		TagAPIListener.setNameColor(player,teamColor);
	}

	public static void removeNameColor(Player player) {
		TagAPIListener.removeNameColor(player);
	}

}
