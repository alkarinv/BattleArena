package mc.alk.arena.controllers;

import java.awt.Color;
import java.util.HashMap;

import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.util.DisguiseInterface;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.entity.Player;

public class ArenaClassController {
	static HashMap<String,ArenaClass> classes = new HashMap<String,ArenaClass>();
	static {
		classes.put(ArenaClass.CHOSEN_CLASS.getName().toUpperCase(), ArenaClass.CHOSEN_CLASS);
	}

	public static void addClass(ArenaClass ac){
		classes.put(ac.getName().toUpperCase(), ac);
		classes.put(MessageUtil.decolorChat(ac.getDisplayName()).toUpperCase(),ac);
	}

	public static ArenaClass getClass(String name){
		return classes.get(name.toUpperCase());
	}

	public static void giveClass(Player player, ArenaClass ac) {
		giveClass(player,ac,null);
	}
	public static void giveClass(Player player, ArenaClass ac, Color color) {
		if (HeroesController.enabled())
			ac = giveHeroClass(player,ac);
		try{if (ac.getItems() != null) InventoryUtil.addItemsToInventory(player, ac.getItems(),true, color);} catch (Exception e){}
		giveClassEnchants(player,ac);
		if (ac.getDisguiseName()!=null && DisguiseInterface.enabled())
			DisguiseInterface.disguisePlayer(player, ac.getDisguiseName());
	}
	private static ArenaClass giveHeroClass(Player player, ArenaClass ac){
		if (ac == ArenaClass.CHOSEN_CLASS){
			String className = HeroesController.getHeroClassName(player);
			if (className != null){
				ArenaClass ac2 = ArenaClassController.getClass(className);
				if (ac2 != null)
					return ac2;
			}
		}
		/// Set them to the appropriate heros class if one exists with this name
		if (HeroesController.hasHeroClass(ac.getName())){
			HeroesController.setHeroClass(player, ac.getName());
		}
		return ac;
	}

	public static void giveClassEnchants(Player player, ArenaClass ac) {
		try{if (ac.getEffects() != null) EffectUtil.enchantPlayer(player, ac.getEffects());} catch (Exception e){}
	}
}
