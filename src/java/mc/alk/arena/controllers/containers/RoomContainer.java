package mc.alk.arena.controllers.containers;

import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.util.InventoryUtil;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;


public class RoomContainer extends AreaContainer{

    public RoomContainer(String name, LocationType type){
        super(name, type);
    }

    public RoomContainer(String name, MatchParams params, LocationType type){
        super(name,params,type);
    }

    @ArenaEventHandler(suppressCastWarnings=true,priority=EventPriority.LOW)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @ArenaEventHandler(priority=EventPriority.HIGH)
    public void onPlayerBlockPlace(BlockPlaceEvent event){
        event.setCancelled(true);
    }

    @ArenaEventHandler(priority=EventPriority.HIGH)
    public void onInventoryOpenEvent(InventoryOpenEvent event){
        if (InventoryUtil.isEnderChest(event.getInventory().getType())){
            event.setCancelled(true);}
    }

    @ArenaEventHandler(priority=EventPriority.HIGH)
    public void onPlayerBlockBreak(BlockBreakEvent event){
        event.setCancelled(true);
    }

    @ArenaEventHandler(priority=EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event){
        event.setCancelled(true);
    }

    @ArenaEventHandler(priority=EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        event.setCancelled(true);
    }

    @ArenaEventHandler(priority=EventPriority.HIGH)
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){
        event.setCancelled(true);
    }

}
