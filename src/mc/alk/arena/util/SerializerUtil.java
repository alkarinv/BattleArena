package mc.alk.arena.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mc.alk.arena.controllers.BukkitInterface;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class SerializerUtil {

	public static HashMap<String, String> createSaveableLocations(Map<Integer, Location> mlocs) {
		HashMap<String,String> locations = new HashMap<String,String>();
		for (Integer key: mlocs.keySet()){
			String s = SerializerUtil.getLocString(mlocs.get(key));
			locations.put(key+"",s);
		}
		return locations;
	}


	public static void expandMapIntoConfig(ConfigurationSection conf, Map<String, Object> map) {
		for (Entry<String, Object> e : map.entrySet()) {
			if (e.getValue() instanceof Map<?,?>) {
				ConfigurationSection section = conf.createSection(e.getKey());
				@SuppressWarnings("unchecked")
				Map<String,Object> subMap = (Map<String, Object>) e.getValue();
				expandMapIntoConfig(section, subMap);
			} else {
				conf.set(e.getKey(), e.getValue());
			}
		}
	}

	public static Location getLocation(String locstr) throws IllegalArgumentException {
		//		String loc = node.getString(nodestr,null);
		if (locstr == null)
			throw new IllegalArgumentException("Error parsing location. Location string was null");
		String split[] = locstr.split(",");
		String w = split[0];
		float x = Float.valueOf(split[1]);
		float y = Float.valueOf(split[2]);
		float z = Float.valueOf(split[3]);
		float yaw = 0, pitch = 0;
		if (split.length > 4){yaw = Float.valueOf(split[4]);}
		if (split.length > 5){pitch = Float.valueOf(split[5]);}
		World world = null;
		if (w != null){
			world = BukkitInterface.getWorld(w);}
		if (world ==null){
			throw new IllegalArgumentException("Error parsing location, World '"+locstr+"' does not exist");}
		return new Location(world,x,y,z,yaw,pitch);
	}

	public static String getLocString(Location l){
		if (l == null) return null;
		return l.getWorld().getName() +"," + l.getX() + "," + l.getY() + "," + l.getZ()+","+l.getYaw()+","+l.getPitch();
	}

	public static String getBlockLocString(Location l){
		if (l == null) return null;
		return l.getWorld().getName() +"," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
	}

	public static Map<Integer, Location> parseLocations(ConfigurationSection cs) throws IllegalArgumentException {
		if (cs == null)
			return null;
		HashMap<Integer,Location> locs = new HashMap<Integer,Location>();
		Set<String> indices = cs.getKeys(false);
		for (String locIndexStr : indices){
			Location loc = null;
			loc = SerializerUtil.getLocation(cs.getString(locIndexStr));
			Integer i = Integer.valueOf(locIndexStr);
			locs.put(i, loc);
		}
		return locs;
	}

}
