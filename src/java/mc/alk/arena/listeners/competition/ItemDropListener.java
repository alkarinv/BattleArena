package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;

import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements ArenaListener{
	MatchTransitions transitionOptions;
	PlayerHolder match;

	public ItemDropListener(PlayerHolder match){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.match = match;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if (transitionOptions.hasInArenaOrOptionAt(match.getMatchState(), TransitionOption.ITEMDROPOFF)){
			event.setCancelled(true);}
	}
}
