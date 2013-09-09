package mc.alk.arena.serializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.listeners.SignUpdateListener;
import mc.alk.arena.objects.signs.ArenaCommandSign;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.configuration.ConfigurationSection;

public class SignSerializer extends BaseConfig {
	public void loadAll(SignUpdateListener sc){
		Set<String> arenas = config.getKeys(false);

		for (String arenastr: arenas){
			ConfigurationSection cs = config.getConfigurationSection(arenastr);
			if (cs == null)
				continue;
			Set<String> signLocations = cs.getKeys(false);
			if (signLocations == null || signLocations.isEmpty())
				continue;
			for (String strloc : signLocations){
				cs = cs.getConfigurationSection(strloc);
				if (cs == null)
					continue;
				ArenaCommandSign acs = null;
				try {
					acs = ArenaCommandSign.deserialize(cs.getValues(true));
				} catch (IllegalArgumentException e){
					Log.err("[BattleArena] Sign not loaded: " + e.getMessage());
				}
				if (acs == null)
					continue;
				sc.addSign(acs);
			}
		}
	}

	public void saveAll(SignUpdateListener sc){
		Map<String, Set<ArenaCommandSign>> statusSigns = sc.getStatusSigns();
		for (String matches: statusSigns.keySet()){
			Set<ArenaCommandSign> set = statusSigns.get(matches);
			if (set == null)
				continue;
			Map<String, Map<String,Object>> map =new HashMap<String,Map<String,Object>>();
			for (ArenaCommandSign acs: set){
				map.put(SerializerUtil.getBlockLocString(acs.getLocation()), acs.serialize());
			}
			config.createSection(matches, map);
		}
		save();

	}
}
