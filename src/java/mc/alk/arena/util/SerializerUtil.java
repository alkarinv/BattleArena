package mc.alk.arena.util;

import mc.alk.arena.controllers.BukkitInterface;
import mc.alk.arena.objects.spawns.FixedLocation;
import mc.alk.arena.objects.spawns.SpawnLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class SerializerUtil {

    public static Map<String, List<String>> createSaveableLocations(Map<Integer, List<SpawnLocation>> mlocs) {
        Map<String,List<String>> locations = new TreeMap<String,List<String>>();
        if (mlocs != null){
            for (Integer key: mlocs.keySet()) {
                ArrayList<String> list = new ArrayList<String>();
                for (SpawnLocation l : mlocs.get(key)) {
                    String s = Util.getLocString(l);
                    list.add(s);
                }
                locations.put(key + "", list);
            }
        }
        return locations;
    }

    public static Map<Integer, List<SpawnLocation>> toMap(List<List<SpawnLocation>> spawns) {
        if (spawns == null)
            return null;
        HashMap<Integer,List<SpawnLocation>> map = new HashMap<Integer,List<SpawnLocation>>();
        for (int i=0;i<spawns.size();i++) {
            List<SpawnLocation> list = new ArrayList<SpawnLocation>(spawns.get(i));
            map.put(i, list);
        }
        return map;
    }

    static public String getLocString(SpawnLocation l){
        return l.getLocation().getWorld().getName() +"," + l.getLocation().getX() +
                "," + l.getLocation().getY() + "," + l.getLocation().getZ() + ","+
                l.getLocation().getYaw() +","+l.getLocation().getPitch();
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

    public static Map<Integer, List<SpawnLocation>> parseLocations(ConfigurationSection cs) throws IllegalArgumentException {
        if (cs == null)
            return null;
        Map<Integer,List<SpawnLocation>> locs = new TreeMap<Integer,List<SpawnLocation>>();
        Set<String> indices = cs.getKeys(false);
        for (String locIndexStr : indices) {
            Integer i = Integer.valueOf(locIndexStr);
            ArrayList<SpawnLocation> list = new ArrayList<SpawnLocation>();
            if (cs.getList(locIndexStr, null) != null) {
                for (String strloc : cs.getStringList(locIndexStr)) {
                    Location loc = SerializerUtil.getLocation(strloc);
                    list.add(new FixedLocation(loc));
                }
            } else {
                Location loc = SerializerUtil.getLocation(cs.getString(locIndexStr));
                list.add(new FixedLocation(loc));
            }
            locs.put(i, list);
        }
        return locs;
    }


    public static String getBlockString(Block b) {
        return b.getTypeId() +";" +b.getData() + ";"+getBlockLocString(b.getLocation());
    }


    public static Block parseBlock(String string) {
        String[] split = string.split(";");
        Location l = getLocation(split[2]);
        return l.getWorld().getBlockAt(l);
    }

}
