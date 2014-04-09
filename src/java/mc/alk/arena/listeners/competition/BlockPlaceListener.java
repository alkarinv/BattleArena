package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements ArenaListener{
	PlayerHolder holder;

	public BlockPlaceListener(PlayerHolder holder){
		this.holder = holder;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerBlockPlace(BlockPlaceEvent event){
		if (holder.hasOption(TransitionOption.BLOCKPLACEOFF)){
			event.setCancelled(true);
		} else if (holder.hasOption(TransitionOption.BLOCKPLACEON)){
			event.setCancelled(false);
		}
	}
}
