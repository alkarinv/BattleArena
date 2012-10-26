package mc.alk.arena.events.matches;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaClass;

import org.bukkit.inventory.ItemStack;

public class MatchClassSelectedEvent extends MatchEvent {
	ArenaClass arenaClass;
	List<ItemStack> items = null;
	public MatchClassSelectedEvent(Match match, ArenaClass arenaClass) {
		super(match);
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
