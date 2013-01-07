package mc.alk.arena.serializers;

import java.util.Map;
import java.util.Set;

import mc.alk.arena.controllers.SignController;
import mc.alk.arena.objects.signs.ArenaStatusSign;

import org.bukkit.configuration.ConfigurationSection;

public class SignSerializer extends BaseSerializer {
	public void loadAll(SignController sc){
		Set<String> arenaTypes = config.getKeys(false);
//		Log.debug("##############   " + sc +" @@@@@@@@@@@@@@@@@@");

		for (String arenaType: arenaTypes){
			ConfigurationSection cs = config.getConfigurationSection(arenaType);
//			Log.debug("##############   " + arenaType +"      cs=" + cs);
			if (cs == null)
				continue;
			Set<String> signLocations = cs.getKeys(false);
//			Log.debug("##############   " + arenaType +"      cs=" + cs  +"   signLocats = " + signLocations);
			if (signLocations == null || signLocations.isEmpty())
				continue;
			for (String strloc : signLocations){
				ArenaStatusSign ass = (ArenaStatusSign) cs.get(strloc);
//				Log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   " + ass);
				sc.addStatusSign(ass);
			}
		}
	}

	public void saveAll(SignController sc){
		Map<String, Map<String,ArenaStatusSign>> statusSigns = sc.getStatusSigns();
		for (String matches: statusSigns.keySet()){
			Map<String,ArenaStatusSign> map = statusSigns.get(matches);
//			Log.debug("##############   " + matches +" @@@@@@@@@@@@@@@@@@   " + map);
			if (map == null)
				continue;
			config.createSection(matches, map);
		}
//		Log.debug("file = " + file +"    " + config);
		save();

	}
}
