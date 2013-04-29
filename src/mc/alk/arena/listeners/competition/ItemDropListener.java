package mc.alk.arena.listeners.competition;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.options.TransitionOption;

import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements ArenaListener{
	MatchTransitions transitionOptions;
	Match match;

	public ItemDropListener(Match match){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.match = match;
	}

	@MatchEventHandler(priority=EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if (transitionOptions.hasOptionAt(match.getState(), TransitionOption.ITEMDROPOFF)){
			event.setCancelled(true);}
	}
}
