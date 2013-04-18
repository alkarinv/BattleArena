package mc.alk.arena.controllers;

import java.util.List;

import mc.alk.arena.listeners.competition.HeroesListener;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.HeroesUtil;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HeroesController {
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
		HeroesListener.enable();
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

	public static void createTeam(ArenaTeam team) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.createTeam(team);}catch(Exception e){e.printStackTrace();}
	}

	public static void removeTeam(ArenaTeam team) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.removeTeam(team);}catch(Exception e){e.printStackTrace();}
	}

	public static void removedFromTeam(ArenaTeam team, Player player) {
		if (!hasHeroes)
			return;
		try{HeroesUtil.removedFromTeam(team, player);}catch(Exception e){e.printStackTrace();}
	}

	public static ArenaTeam getTeam(Player player) {
		if (!hasHeroes)
			return null;
		try{return HeroesUtil.getTeam(player);}catch(Exception e){e.printStackTrace();}
		return null;
	}

	public static void setMagicLevel(Player p, Integer magic) {
		if (!hasHeroes) return;
		try{HeroesUtil.setMagicLevel(p,magic);}catch(Exception e){e.printStackTrace();}
	}

	public static void setMagicLevelP(Player p, Integer magic) {
		if (!hasHeroes) return;
		try{HeroesUtil.setMagicLevelP(p,magic);}catch(Exception e){e.printStackTrace();}
	}

	public static Integer getMagicLevel(Player player) {
		if (!hasHeroes)
			return null;
		try{return HeroesUtil.getMagicLevel(player);}catch(Exception e){e.printStackTrace();}
		return null;
	}

	public static int getHealth(Player player) {
		return hasHeroes ? HeroesUtil.getHealth(player) : player.getHealth();
	}

	public static void setHealth(Player player, int health) {
		if (hasHeroes)
			try{HeroesUtil.setHealth(player,health);}catch(Exception e){e.printStackTrace();}
		else
			player.setHealth(health);
	}

	public static void setHealthP(Player player, int health) {
		if (hasHeroes)
			try{HeroesUtil.setHealthP(player,health);}catch(Exception e){e.printStackTrace();}
		else
			player.setHealth(health);
	}

	public static void cancelExpLoss(Player player, boolean cancel) {
		if (!hasHeroes)
			return;
		if (cancel)
			HeroesListener.setCancelExpLoss(player);
		else
			HeroesListener.removeCancelExpLoss(player);
	}

	public static void addDisabledCommands(List<String> disabled) {
		try{HeroesListener.addDisabledCommands(disabled);}catch(Exception e){e.printStackTrace();}
	}
}
