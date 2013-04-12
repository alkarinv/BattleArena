package mc.alk.arena.objects.scoreboard;

import org.bukkit.scoreboard.Objective;

public class BukkitObjective extends ArenaObjective{
	Objective o;

	public BukkitObjective(String name, String criteria) {
		super(name, criteria);
	}

}
