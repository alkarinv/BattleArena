package mc.alk.arena.listeners.competition;

import mc.alk.arena.Permissions;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements ArenaListener{
	MatchTransitions transitionOptions;
	Match match;

	public PlayerTeleportListener(Match match){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.match = match;
	}

	@MatchEventHandler(priority=EventPriority.HIGH)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if (event.isCancelled() || event.getPlayer().hasPermission(Permissions.TELEPORT_BYPASS_PERM))
			return;

		if (transitionOptions.hasOptionAt(match.getState(),TransitionOption.NOTELEPORT)){
			MessageUtil.sendMessage(event.getPlayer(), "&cTeleports are disabled in this arena");
			event.setCancelled(true);
			return;
		}
		if (transitionOptions.hasOptionAt(match.getState(),TransitionOption.NOWORLDCHANGE)){
			if (event.getFrom().getWorld().getUID() != event.getTo().getWorld().getUID()){
				MessageUtil.sendMessage(event.getPlayer(), "&cWorldChanges are disabled in this arena");
				event.setCancelled(true);
			}
		}
	}
}
