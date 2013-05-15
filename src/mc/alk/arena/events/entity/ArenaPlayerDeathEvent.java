package mc.alk.arena.events.entity;

import mc.alk.arena.events.ExtendedBukkitEvent;

import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaPlayerDeathEvent extends ExtendedBukkitEvent{

	public ArenaPlayerDeathEvent(PlayerDeathEvent event){
		this.event = event;
	}

	@Override
	public PlayerDeathEvent getBukkitEvent() {
		return (PlayerDeathEvent) event;
	}

}
