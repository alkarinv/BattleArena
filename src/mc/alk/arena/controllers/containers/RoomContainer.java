package mc.alk.arena.controllers.containers;

import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;


public class RoomContainer extends AreaContainer{

	public RoomContainer(LocationType type){
		super(type);
	}

	public RoomContainer(MatchParams params, LocationType type){
		super(params,type);
	}

	@ArenaEventHandler(suppressCastWarnings=true,priority=EventPriority.LOW)
	public void onEntityDamageEvent(EntityDamageEvent event) {
//		if (event.getEntity() instanceof Player && players.contains(((Player)event.getEntity()).getName()))
			event.setCancelled(true);
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerBlockPlace(BlockPlaceEvent event){
//		if (this.players.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerBlockBreak(BlockBreakEvent event){
//		if (this.players.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}
}
