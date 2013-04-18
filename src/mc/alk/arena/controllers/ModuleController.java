package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.util.CaseInsensitiveMap;

import org.bukkit.Bukkit;

public enum ModuleController {
	INSTANCE;

	CaseInsensitiveMap<ArenaModule> modules = new CaseInsensitiveMap<ArenaModule>();

	public static void addModule(ArenaModule module){
		INSTANCE.modules.put(module.getName(), module);
		Bukkit.getPluginManager().registerEvents(module, BattleArena.getSelf());
	}

	public static ArenaModule getModule(String moduleName){
		return INSTANCE.modules.get(moduleName);
	}
}
