package mc.alk.arena.controllers;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.RegisteredCompetition;

import org.bukkit.plugin.Plugin;

public class CompetitionController {
	static HashMap<String, Map<String, RegisteredCompetition>> registeredCompetitions =
			new HashMap<String, Map<String, RegisteredCompetition>>();

	public static Map<String,RegisteredCompetition> getOrCreate(String pluginName){
		Map<String, RegisteredCompetition> comps = registeredCompetitions.get(pluginName);
		if (comps == null){
			comps = new HashMap<String, RegisteredCompetition>();
			registeredCompetitions.put(pluginName, comps);
		}
		return comps;
	}

	public static void addRegisteredCompetition(RegisteredCompetition rc) {
		String pluginName = rc.getPlugin().getName();
		Map<String, RegisteredCompetition> comps = getOrCreate(pluginName);
		comps.put(rc.getCompetitionName().toUpperCase(), rc);
	}

	public static RegisteredCompetition getCompetition(Plugin plugin, String name) {
		String pluginName = plugin.getName();
		Map<String, RegisteredCompetition> comps = registeredCompetitions.get(pluginName);
		if (comps == null || comps.isEmpty())
			return null;
		return comps.get(name.toUpperCase());
	}

	public static RegisteredCompetition getCompetition(String name) {
		name = name.toUpperCase();
		for (String plugin : registeredCompetitions.keySet()){
			Map<String, RegisteredCompetition> comps = registeredCompetitions.get(plugin);
			if (comps == null || comps.isEmpty())
				continue;
			if (comps.containsKey(name))
				return comps.get(name);
		}
		return null;
	}

	public static boolean reloadCompetition(Plugin plugin, MatchParams mp) {
		RegisteredCompetition rc = getCompetition(plugin,mp.getName());
		if (rc == null)
			return false;

		rc.reload();
		return true;
	}

}
