package mc.alk.arena.controllers;

import mc.alk.arena.util.HeroesUtil;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HeroesInterface {
	static boolean hasHeroes = false;

	public static boolean hasHeroClass(String className) {
		if (!hasHeroes) return false;
		return HeroesUtil.hasHeroClass(className);
	}
	public static void setHeroClass(Player player, String className) {
		if (!hasHeroes) return;
		HeroesUtil.setHeroClass(player, className);
	}

	public static void setHeroes(Plugin plugin){
		HeroesUtil.setHeroes(plugin);
		hasHeroes = true;
	}

	public static boolean enabled() {
		return hasHeroes;
	}
	public static String getHeroClassName(Player player) {
		if (!hasHeroes) return null;
		return HeroesUtil.getHeroClassName(player);
	}

	public static int getLevel(Player player) {
		if (!hasHeroes) return -1;
		return HeroesUtil.getLevel(player);
	}
}
