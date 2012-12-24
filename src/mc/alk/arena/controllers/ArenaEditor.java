package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.objects.arenas.Arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaEditor {
	public class CurrentSelection{
		public long lastUsed;
		public Arena arena;
		CurrentSelection(long used, Arena arena){
			this.lastUsed = used; this.arena = arena;
		}
	}

	HashMap<String, CurrentSelection> selections = new HashMap<String,CurrentSelection>();

	public void setCurrentArena(CommandSender p, Arena arena) {
		CurrentSelection cs = new CurrentSelection(System.currentTimeMillis(), arena);
		selections.put(p.getName(), cs);
	}

	public Arena getArena(CommandSender p) {
		CurrentSelection cs = selections.get(p.getName());
		if (cs == null)
			return null;
		return cs.arena;
	}

	public CurrentSelection getCurrentSelection(Player p) {
		return selections.get(p.getName());
	}
}
