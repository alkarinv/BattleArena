package mc.alk.arena.listeners.competition.plugins;


import mc.alk.arena.listeners.competition.InArenaListener;
import mc.alk.arena.util.PlayerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.massivecraft.factions.event.FactionsEventPowerChange;

public enum FactionsListener implements Listener{
	INSTANCE;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFactionLoss(FactionsEventPowerChange event){
		if (InArenaListener.inArena(PlayerUtil.getID(event.getUPlayer().getPlayer()))){
			event.setCancelled(true);}
	}

	public static boolean enable() {
		try {
			Class.forName("com.massivecraft.factions.event.FactionsEventPowerChange");
			InArenaListener.addListener(INSTANCE);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
