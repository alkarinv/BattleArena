package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerListener implements ArenaListener{
	MatchTransitions transitionOptions;
	PlayerHolder match;

	public HungerListener(PlayerHolder match){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.match = match;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){
		if (transitionOptions.hasInArenaOrOptionAt(match.getMatchState(), TransitionOption.HUNGEROFF)){
			event.setCancelled(true);
        }
	}
}
