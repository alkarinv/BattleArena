package mc.alk.arena.listeners.competition;

import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PreClearInventoryListener implements ArenaListener{

	@ArenaEventHandler(bukkitPriority = org.bukkit.event.EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
    }
}
