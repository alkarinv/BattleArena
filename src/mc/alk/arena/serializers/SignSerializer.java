package mc.alk.arena.serializers;

import java.util.Map;
import java.util.Set;

import mc.alk.arena.controllers.SignController;
import mc.alk.arena.objects.signs.ArenaStatusSign;
import mc.alk.arena.util.Log;

public class SignSerializer extends BaseSerializer {
	public void loadAll(SignController sc){
		Set<String> keys = config.getKeys(false);

		for (String key: keys){
			Log.debug("##############   " + key);
		}
	}

	public void saveAll(SignController sc){
		Map<String, Map<String,ArenaStatusSign>> statusSigns = sc.getStatusSigns();
		for (String matches: statusSigns.keySet()){
			Map<String,ArenaStatusSign> map = statusSigns.get(matches);
			config.createSection(matches, map);
		}

	}
}
