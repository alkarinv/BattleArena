package mc.alk.arena.objects.signs;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class ArenaStatusSign implements ConfigurationSerializable{
	public static enum StatusUpdate{
		STATUS
	}

	String arenaType;

	StatusUpdate update = StatusUpdate.STATUS;
	Location location;
	MatchParams params;

	public ArenaStatusSign(MatchParams mp) {
		this.arenaType = mp.getType().getName();
		this.params = mp;
	}

	public ArenaStatusSign(String arenaType, StatusUpdate update, Location location, MatchParams params) {
		this.arenaType = arenaType;
		this.update = update;
		this.location = location;
		this.params = params;
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
		map.put("updateType", update.toString());
		map.put("location", SerializerUtil.getBlockLocString(location));
		return map;
	}

	public static ArenaStatusSign deserialize(Map<String, Object> map) {
		String arenaType = (String) map.get("arenaType");
		StatusUpdate update = StatusUpdate.valueOf((String) map.get("updateType"));
		Location location = SerializerUtil.getLocation((String) map.get("location"));
		if (arenaType == null || update == null || location == null)
			return null;
		MatchParams mp = ParamController.getMatchParamCopy(arenaType);

		return new ArenaStatusSign(arenaType, update, location,mp);
	}

	public MatchParams getMatchParams() {
		return params;
	}

	public void setQ(int i, int j) {
		Sign s = getSign();
		if (s == null)
			return;
		s.setLine(3, i +"/" +j);
	}

	private Sign getSign() {
		Block b = location.getBlock();
		if (b == null)
			return null;
		Material m = b.getType();
		return  m == Material.SIGN || m==Material.WALL_SIGN ? (Sign)b : null;
	}
}
