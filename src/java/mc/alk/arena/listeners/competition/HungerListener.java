package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.options.TransitionOption;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerListener implements ArenaListener{
	PlayerHolder holder;

	public HungerListener(PlayerHolder holder){
		this.holder = holder;
	}

	@ArenaEventHandler
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){
		if (holder.hasOption(TransitionOption.HUNGEROFF)){
			event.setCancelled(true);
        }
	}
}
