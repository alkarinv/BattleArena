package mc.alk.arena.controllers;

import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.HeroesUtil;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HeroesInterface {
	static boolean hasHeroes = false;
	HeroesUtil heroes = null;

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
	public static void setMagic(Player p, Integer magic) {
		if (!hasHeroes) return;
		HeroesUtil.setMagic(p,magic);
	}
	public static boolean isInCombat(Player player) {
		if (!hasHeroes) return false;
		return HeroesUtil.isInCombat(player);
	}
	public static void deEnchant(Player p) {
		if (!hasHeroes)
			return;
		HeroesUtil.deEnchant(p);
	}

	public static void createTeam(Team team) {
		if (!hasHeroes)
			return;
		HeroesUtil.createTeam(team);
	}

	public static void removeTeam(Team team) {
		if (!hasHeroes)
			return;
		HeroesUtil.removeTeam(team);
	}

	public static void addedToTeam(Team team, Player player) {
		if (!hasHeroes)
			return;
		HeroesUtil.addedToTeam(team, player);
	}

	public static void removedFromTeam(Team team, Player player) {
		if (!hasHeroes)
			return;
		HeroesUtil.removedFromTeam(team, player);
	}
	public static Team getTeam(Player player) {
		if (!hasHeroes)
			return null;
		return HeroesUtil.getTeam(player);
	}

}
