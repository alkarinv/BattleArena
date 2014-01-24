package mc.alk.arena.events.entity;

import mc.alk.arena.events.ExtendedBukkitEvent;
import mc.alk.arena.objects.ArenaPlayer;

import org.bukkit.event.entity.EntityDeathEvent;

public class MobDeathEvent extends ExtendedBukkitEvent {
	final ArenaPlayer killer;

	public MobDeathEvent(EntityDeathEvent event, ArenaPlayer killer){
		this.event = event;
		this.killer = killer;
	}

	@Override
	public EntityDeathEvent getBukkitEvent() {
		return (EntityDeathEvent) event;
	}

	public ArenaPlayer getPlayer(){
		return killer;
	}
}
