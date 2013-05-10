package mc.alk.arena.listeners.competition;

import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.events.players.ArenaPlayerEnterMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveMatchEvent;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.event.inventory.InventoryClickEvent;

public class TeamHeadListener implements ArenaListener{


	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		if (event.getSlot() == 39/*Helm Slot*/)
			event.setCancelled(true);
	}

	@ArenaEventHandler
	public void onArenaPlayerEnterEvent(ArenaPlayerEnterMatchEvent event){
		ArenaTeam t = event.getTeam();
		if (t.getHeadItem() != null)
			TeamUtil.setTeamHead(t.getHeadItem(), event.getPlayer());
	}

	@ArenaEventHandler
	public void onArenaPlayerLeaveMatchEvent(ArenaPlayerLeaveMatchEvent event){
		ArenaTeam t = event.getTeam();
		if (t.getHeadItem() != null)
			PlayerStoreController.removeItem(event.getPlayer(), t.getHeadItem());
	}
}
