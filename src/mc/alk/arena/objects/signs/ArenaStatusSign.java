package mc.alk.arena.objects.signs;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class ArenaStatusSign implements ConfigurationSerializable{
	public static enum StatusUpdate{
		STATUS
	}

	String arenaType;

	StatusUpdate update;
	Location location;
	MatchParams mp;

	public ArenaStatusSign(MatchParams mp) {
		this.arenaType = mp.getType().getName();
		this.mp = mp;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getType() {
		return arenaType;
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("arenaType", arenaType);
		map.put("updateType", update);
		map.put("location", SerializerUtil.getBlockLocString(location));
		return map;
	}

	public MatchParams getMatchParams() {
		return mp;
	}
}
