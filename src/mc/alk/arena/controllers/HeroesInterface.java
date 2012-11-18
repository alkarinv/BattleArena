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
		try{return HeroesUtil.hasHeroClass(className);}catch(Exception e){e.printStackTrace();}
		return false;
	}
	public static void setHeroClass(Player player, String className) {
		if (!hasHeroes) return;
		try{HeroesUtil.setHeroClass(player, className);}catch(Exception e){e.printStackTrace();}
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
		try{return HeroesUtil.getHeroClassName(player);}catch(Exception e){e.printStackTrace();}
		return null;
	}

	public static int getLevel(Player player) {
		if (!hasHeroes) return -1;
		try{return HeroesUtil.getLevel(player);}catch(Exception e){e.printStackTrace();}
		return -1;
	}
	public static void setMagic(Player p, Integer magic) {
		if (!hasHeroes) return;
		try{HeroesUtil.setMagic(p,magic);}catch(Exception e){e.printStackTrace();}
	}
	public static boolean isInCombat(Player player) {
		if (!hasHeroes) return false;
		try{return HeroesUtil.isInCombat(player);}catch(Exception e){e.printStackTrace();}
		return false;
	}
	public static void deEnchant(Player p) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.deEnchant(p);}catch(Exception e){e.printStackTrace();}
	}

	public static void createTeam(Team team) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.createTeam(team);}catch(Exception e){e.printStackTrace();}
	}

	public static void removeTeam(Team team) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.removeTeam(team);}catch(Exception e){e.printStackTrace();}
	}

	public static void addedToTeam(Team team, Player player) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.addedToTeam(team, player);}catch(Exception e){e.printStackTrace();}
	}

	public static void removedFromTeam(Team team, Player player) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.removedFromTeam(team, player);}catch(Exception e){e.printStackTrace();}
	}

	public static Team getTeam(Player player) {
		if (!hasHeroes)
			return null;
		try{return HeroesUtil.getTeam(player);}catch(Exception e){e.printStackTrace();}
		return null;
	}

	public static Integer getMagicLevel(Player player) {
		if (!hasHeroes)
			return null;
		try{return HeroesUtil.getMagicLevel(player);}catch(Exception e){e.printStackTrace();}
		return null;
	}
}
