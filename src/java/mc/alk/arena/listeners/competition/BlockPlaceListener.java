package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.StateGraph;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;

import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements ArenaListener{
	StateGraph transitionOptions;
	PlayerHolder holder;

	public BlockPlaceListener(PlayerHolder holder){
		this.transitionOptions = holder.getParams().getStateGraph();
		this.holder = holder;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerBlockPlace(BlockPlaceEvent event){
		if (transitionOptions.hasInArenaOrOptionAt(holder.getState(), TransitionOption.BLOCKPLACEOFF)){
			event.setCancelled(true);
		} else if (transitionOptions.hasInArenaOrOptionAt(holder.getState(), TransitionOption.BLOCKPLACEON)){
			event.setCancelled(false);
		}
	}
}
