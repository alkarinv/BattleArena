package mc.alk.arena.controllers;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.EssentialsUtil;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsController {
	static boolean hasEssentials = false;

	public static boolean enabled() {
		return hasEssentials;
	}

	public static boolean enableEssentials(Plugin plugin) {
		hasEssentials = EssentialsUtil.enableEssentials(plugin);
		return hasEssentials;
	}

	public static void setGod(Player player, boolean enable) {
		if (!hasEssentials) return;
		EssentialsUtil.setGod(player.getName(),enable);
	}

	public static void setFlight(Player player, boolean enable) {
		if (!hasEssentials) return;
		EssentialsUtil.setFlight(player.getName(),enable);
	}

	public static void setFlightSpeed(Player player, Float flightSpeed) {
		if (!hasEssentials) return;
		EssentialsUtil.setFlightSpeed(player.getName(),flightSpeed);
	}

	public static boolean inJail(ArenaPlayer player) {
		if (!hasEssentials) return false;
		return EssentialsUtil.inJail(player.getName());
	}

}
