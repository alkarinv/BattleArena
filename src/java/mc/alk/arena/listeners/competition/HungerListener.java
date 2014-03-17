package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.options.TransitionOption;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerListener implements ArenaListener{
	MatchTransitions transitionOptions;
	PlayerHolder holder;

	public HungerListener(PlayerHolder holder){
		this.transitionOptions = holder.getParams().getTransitionOptions();
		this.holder = holder;
	}

	@ArenaEventHandler
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){
		if (transitionOptions.hasInArenaOrOptionAt(holder.getState(), TransitionOption.HUNGEROFF)){
			event.setCancelled(true);
        }
	}
}
