package mc.alk.arena.events.entity;

import mc.alk.arena.events.ExtendedBukkitEvent;

import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaPlayerKilledEvent extends ExtendedBukkitEvent{

	public ArenaPlayerKilledEvent(PlayerDeathEvent event){
		this.event = event;
	}

	@Override
	public PlayerDeathEvent getBukkitEvent() {
		return (PlayerDeathEvent) event;
	}

}
