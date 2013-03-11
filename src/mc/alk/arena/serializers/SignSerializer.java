package mc.alk.arena.serializers;

import java.util.Map;
import java.util.Set;

import mc.alk.arena.controllers.SignController;
import mc.alk.arena.objects.signs.ArenaStatusSign;

import org.bukkit.configuration.ConfigurationSection;

public class SignSerializer extends BaseConfig {
	public void loadAll(SignController sc){
		Set<String> arenaTypes = config.getKeys(false);

		for (String arenaType: arenaTypes){
			ConfigurationSection cs = config.getConfigurationSection(arenaType);
			if (cs == null)
				continue;
			Set<String> signLocations = cs.getKeys(false);
			if (signLocations == null || signLocations.isEmpty())
				continue;
			for (String strloc : signLocations){
				ArenaStatusSign ass = (ArenaStatusSign) cs.get(strloc);
				sc.addStatusSign(ass);
			}
		}
	}

	public void saveAll(SignController sc){
		Map<String, Map<String,ArenaStatusSign>> statusSigns = sc.getStatusSigns();
		for (String matches: statusSigns.keySet()){
			Map<String,ArenaStatusSign> map = statusSigns.get(matches);
			if (map == null)
				continue;
			config.createSection(matches, map);
		}
		save();

	}
}
