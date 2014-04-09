package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements ArenaListener{
	PlayerHolder holder;

	public ItemDropListener(PlayerHolder holder){
		this.holder = holder;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if (holder.hasOption(TransitionOption.ITEMDROPOFF)){
			event.setCancelled(true);}
	}
}
