package mc.alk.arena.events.players;

import java.util.List;

import mc.alk.arena.events.CompetitionEvent;
import mc.alk.arena.objects.ArenaClass;

import org.bukkit.inventory.ItemStack;

public class ArenaPlayerClassSelectedEvent extends CompetitionEvent{
	ArenaClass arenaClass;
	List<ItemStack> items = null;
	public ArenaPlayerClassSelectedEvent(ArenaClass arenaClass) {
		this.arenaClass = arenaClass;
	}
	public ArenaClass getArenaClass() {
		return arenaClass;
	}
	public void setArenaClass(ArenaClass arenaClass) {
		this.arenaClass = arenaClass;
	}
	public List<ItemStack> getItems() {
		return items;
	}
	public void setItems(List<ItemStack> items) {
		this.items = items;
	}
}
