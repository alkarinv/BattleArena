package mc.alk.arena.listeners.competition;

import mc.alk.arena.Permissions;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.util.MessageUtil;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements ArenaListener{
    final PlayerHolder holder;

	public PlayerTeleportListener(PlayerHolder holder){
		this.holder = holder;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if (event.isCancelled() || event.getPlayer().hasPermission(Permissions.TELEPORT_BYPASS_PERM))
			return;
		if (holder.hasOption(TransitionOption.NOTELEPORT)){
			MessageUtil.sendMessage(event.getPlayer(), "&cTeleports are disabled in this arena");
			event.setCancelled(true);
			return;
		}
		if (event.getFrom().getWorld().getUID() != event.getTo().getWorld().getUID() &&
				holder.hasOption(TransitionOption.NOWORLDCHANGE)){
			MessageUtil.sendMessage(event.getPlayer(), "&cWorldChanges are disabled in this arena");
			event.setCancelled(true);
		}
	}
}
