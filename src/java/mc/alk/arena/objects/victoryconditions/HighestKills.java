package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import org.bukkit.configuration.ConfigurationSection;

@Deprecated
public class HighestKills extends PlayerKills {
	public HighestKills(Match match, ConfigurationSection section) {
		super(match, section);
	}
}
