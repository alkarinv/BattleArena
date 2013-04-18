package mc.alk.arena.listeners.competition;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.massivecraft.factions.event.PowerLossEvent;

public enum FactionsListener implements Listener{
	INSTANCE;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFactionLoss(PowerLossEvent event){
		Player p = event.getPlayer(); /// Factions doesnt play pretty with virtualplayers, this can be null
		if (p != null && InArenaListener.inArena(p.getName())){
			event.setMessage("&2You lost no power &d%d / %d");
			event.setCancelled(true);
		}
	}

	public static boolean enable() {
		try {
			Class.forName("com.massivecraft.factions.event.PowerLossEvent");
			InArenaListener.addListener(INSTANCE);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
