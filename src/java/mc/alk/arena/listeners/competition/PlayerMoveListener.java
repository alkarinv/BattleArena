package mc.alk.arena.listeners.competition;

import mc.alk.arena.controllers.plugins.WorldGuardController;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.regions.ArenaRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements ArenaListener{
	MatchTransitions transitionOptions;
	PlayerHolder match;
    ArenaRegion region;
    final World w;
	public PlayerMoveListener(PlayerHolder match, ArenaRegion region){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.match = match;
        this.region = region;
        this.w = Bukkit.getWorld(region.getWorld());
    }

    @ArenaEventHandler(priority=EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event){
        if (!event.isCancelled() && w.getUID() == event.getTo().getWorld().getUID() &&
                transitionOptions.hasInArenaOrOptionAt(match.getState(),TransitionOption.WGNOLEAVE) &&
                WorldGuardController.hasWorldGuard()){
            /// Did we actually even move
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()){
                if (WorldGuardController.isLeavingArea(event.getFrom(), event.getTo(),w,region.getID())){
                    event.setCancelled(true);}
            }
        }
    }
}
