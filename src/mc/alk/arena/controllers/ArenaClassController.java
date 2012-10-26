package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.entity.Player;

public class ArenaClassController {
	static HashMap<String,ArenaClass> classes = new HashMap<String,ArenaClass>();
	static {
		classes.put(ArenaClass.CHOSEN_CLASS.getName().toUpperCase(), ArenaClass.CHOSEN_CLASS);
	}
	public static void addClass(ArenaClass ac){
		classes.put(ac.getName().toUpperCase(), ac);
	}

	public static ArenaClass getClass(String name){
		return classes.get(name.toUpperCase());
	}

	public static void giveClass(Player player, ArenaClass ac) {
		giveHeroClass(player,ac);
		try{if (ac.getItems() != null) InventoryUtil.addItemsToInventory(player, ac.getItems(),true);} catch (Exception e){}
		try{if (ac.getEffects() != null) EffectUtil.enchantPlayer(player, ac.getEffects());} catch (Exception e){}
	}

	public static void giveHeroClass(Player player, ArenaClass ac){
		if (!HeroesInterface.enabled() || ac == ArenaClass.CHOSEN_CLASS){
			return;} /// Either heroes is not enabled or they already have their own hero class

		/// Set them to the appropriate heros class if one exists with this name
		if (HeroesInterface.hasHeroClass(ac.getName())){
			HeroesInterface.setHeroClass(player, ac.getName());
		}
	}
}
