package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.entity.Player;

public class ArenaClassController {
	static HashMap<String,ArenaClass> classes = new HashMap<String,ArenaClass>();
	public static void addClass(ArenaClass ac){
		classes.put(ac.getName().toUpperCase(), ac);
	}
	
	public static ArenaClass getClass(String name){
		return classes.get(name.toUpperCase());
	}

	public static void giveItems(Player player, ArenaClass ac) {
		try{if (ac.getEffects() != null) EffectUtil.enchantPlayer(player, ac.getEffects());} catch (Exception e){}
		try{if (ac.getItems() != null) InventoryUtil.addItemsToInventory(player, ac.getItems(),true);} catch (Exception e){}		
	}
}
