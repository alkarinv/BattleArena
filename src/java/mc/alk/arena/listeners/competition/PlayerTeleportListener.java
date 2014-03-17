package mc.alk.arena.listeners.competition;

import mc.alk.arena.Permissions;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements ArenaListener{
	final MatchTransitions transitionOptions;
    final PlayerHolder holder;

	public PlayerTeleportListener(PlayerHolder holder){
		this.transitionOptions = holder.getParams().getTransitionOptions();
		this.holder = holder;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if (event.isCancelled() || event.getPlayer().hasPermission(Permissions.TELEPORT_BYPASS_PERM))
			return;
		if (transitionOptions.hasInArenaOrOptionAt(holder.getState(), TransitionOption.NOTELEPORT)){
			MessageUtil.sendMessage(event.getPlayer(), "&cTeleports are disabled in this arena");
			event.setCancelled(true);
			return;
		}
		if (event.getFrom().getWorld().getUID() != event.getTo().getWorld().getUID() &&
				transitionOptions.hasInArenaOrOptionAt(holder.getState(),TransitionOption.NOWORLDCHANGE)){
			MessageUtil.sendMessage(event.getPlayer(), "&cWorldChanges are disabled in this arena");
			event.setCancelled(true);
		}
	}
}
