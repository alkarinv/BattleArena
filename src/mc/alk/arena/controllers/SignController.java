package mc.alk.arena.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.objects.signs.ArenaStatusSign;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;

public class SignController {
	Map<String, Map<String,ArenaStatusSign>> statusSigns = new HashMap<String, Map<String,ArenaStatusSign>>();

	public void addStatusSign(ArenaStatusSign acs) {
		Map<String,ArenaStatusSign> map = getMatchSigns(acs.getType());
		map.put(toKey(acs.getLocation()), acs);
	}

	private Map<String, ArenaStatusSign> getMatchSigns(String arenaType) {
		Map<String,ArenaStatusSign> map = statusSigns.get(arenaType);
		if (map == null){
			map = Collections.synchronizedMap(new HashMap<String,ArenaStatusSign>());
			statusSigns.put(arenaType, map);
		}
		return map;
	}

	public String toKey(Location loc){
		return SerializerUtil.getBlockLocString(loc);
	}

	public Map<String, Map<String, ArenaStatusSign>> getStatusSigns() {
		return statusSigns;
	}
}
