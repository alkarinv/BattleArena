package mc.alk.arena.serializers;

import mc.alk.arena.listeners.SignUpdateListener;
import mc.alk.arena.objects.signs.ArenaCommandSign;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MapOfTreeSet;
import mc.alk.arena.util.SerializerUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SignSerializer extends BaseConfig {
	public void loadAll(SignUpdateListener sc){
		Set<String> arenas = config.getKeys(false);

		for (String arenastr: arenas){
			ConfigurationSection maincs = config.getConfigurationSection(arenastr);
			if (maincs == null)
				continue;
			Set<String> signLocations = maincs.getKeys(false);
			if (signLocations == null || signLocations.isEmpty())
				continue;
			for (String strloc : signLocations){
				ConfigurationSection cs = maincs.getConfigurationSection(strloc);
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
		MapOfTreeSet<String, ArenaCommandSign> statusSigns = sc.getStatusSigns();
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
