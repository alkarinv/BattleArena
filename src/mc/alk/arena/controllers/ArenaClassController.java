package mc.alk.arena.controllers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.util.DisguiseInterface;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PlayerUtil;

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

	public static void giveClass(ArenaPlayer player, ArenaClass ac) {
		giveClass(player,ac,null);
	}

	public static void giveClass(ArenaPlayer player, ArenaClass ac, Color color) {
		if (HeroesController.enabled())
			ac = giveHeroClass(player,ac);
		try{if (ac.getItems() != null) InventoryUtil.addItemsToInventory(player.getPlayer(), ac.getItems(),true, color);} catch (Exception e){}
		giveClassEnchants(player.getPlayer(),ac);
		if (ac.getDisguiseName()!=null && DisguiseInterface.enabled())
			DisguiseInterface.disguisePlayer(player.getPlayer(), ac.getDisguiseName());
		if (ac.getMobs() != null){
			try{
				List<SpawnInstance> mobs = new ArrayList<SpawnInstance>(ac.getMobs());
				player.setMobs(mobs);
				player.spawnMobs();
			} catch (Exception e){
				Log.printStackTrace(e);
			}
		}
		if (ac.getDoCommands() != null){
			PlayerUtil.doCommands(player.getPlayer(),ac.getDoCommands());
		}
		player.setCurrentClass(ac);
	}
	private static ArenaClass giveHeroClass(ArenaPlayer player, ArenaClass ac){
		if (ac == ArenaClass.CHOSEN_CLASS){
			String className = HeroesController.getHeroClassName(player.getPlayer());
			if (className != null){
				ArenaClass ac2 = ArenaClassController.getClass(className);
				if (ac2 != null)
					return ac2;
			}
		}
		/// Set them to the appropriate heros class if one exists with this name
		if (HeroesController.hasHeroClass(ac.getName())){
			HeroesController.setHeroClass(player.getPlayer(), ac.getName());
		}
		return ac;
	}

	public static void giveClassEnchants(Player player, ArenaClass ac) {
		try{if (ac.getEffects() != null) EffectUtil.enchantPlayer(player, ac.getEffects());} catch (Exception e){}
	}
}
