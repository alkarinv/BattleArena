package mc.alk.arena.controllers;

import mc.alk.arena.objects.arenas.Arena;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class ArenaEditor {
	public class CurrentSelection{
		public long lastUsed;
		public Arena arena;
        public Location lastClick;

        CurrentSelection(long used, Arena arena){
			this.lastUsed = used; this.arena = arena;
		}
        public void updateCurrentSelection(){
            lastUsed = System.currentTimeMillis();
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


	public CurrentSelection getCurrentSelection(CommandSender sender) {
		return selections.get(sender.getName());
	}
}
