package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;

import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements ArenaListener{
	MatchTransitions transitionOptions;
	PlayerHolder match;

	public BlockPlaceListener(PlayerHolder match){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.match = match;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerBlockPlace(BlockPlaceEvent event){
		if (transitionOptions.hasInArenaOrOptionAt(match.getMatchState(), TransitionOption.BLOCKPLACEOFF)){
			event.setCancelled(true);
		} else if (transitionOptions.hasInArenaOrOptionAt(match.getMatchState(), TransitionOption.BLOCKPLACEON)){
			event.setCancelled(false);
		}
	}
}
