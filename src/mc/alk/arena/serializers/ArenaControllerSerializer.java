package mc.alk.arena.serializers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;


public class ArenaControllerSerializer {
	public static YamlConfiguration config = new YamlConfiguration();
	static File f;

	public void load(){
		try {
			f = new File(BattleArena.getSelf().getDataFolder()+"/arenaplayers.yml");
			config.load(f);
		} catch (Exception e){e.printStackTrace();}

		ConfigurationSection cs = config.getConfigurationSection("tpOnReenter");
		if (cs != null){
			for (String name : cs.getKeys(false)){
				ConfigurationSection loccs = cs.getConfigurationSection(name);
				Location loc = SerializerUtil.getLocation(loccs.getString("loc"));
				if (loc == null){
					System.err.println("Couldnt load the player " + name +" when reading tpOnReenter inside arenaplayers.yml");
					continue;
				}
				BAPlayerListener.tp.put(name, loc);
			}
		}

		cs = config.getConfigurationSection("dieOnReenter");
		if (cs != null){
			BAPlayerListener.die.addAll(cs.getKeys(false));
		}
		cs = config.getConfigurationSection("clearInventoryOnReenter");
		if (cs != null){
			BAPlayerListener.clearInventory.addAll(cs.getKeys(false));
		}
	}

	public void save(){

		Map<String,Location> playerLocs = BAPlayerListener.tp;

		ConfigurationSection cs = config.createSection("tpOnReenter");
		for (String player : playerLocs.keySet()){
			ConfigurationSection pc = cs.createSection(player);
			Location loc = playerLocs.get(player);
			pc.set("loc", SerializerUtil.getLocString(loc));
		}

		cs = config.createSection("dieOnReenter");
		List<String> dieOnReenter = new ArrayList<String>(BAPlayerListener.die);
		for (String player : dieOnReenter){
			cs.createSection(player);}

		cs = config.createSection("dieOnReenter");
		List<String> clearInventoryReenter = new ArrayList<String>(BAPlayerListener.clearInventory);
		for (String player : clearInventoryReenter){
			cs.createSection(player);}

		// TODO allow people to leave the game before getting their items back 
//		cs = config.createSection("restoreInv");
//		Map<Player,PInv> items = BAPlayerListener.itemRestore;
//		for (Player p: items.keySet()){
//			ConfigurationSection pcs = cs.createSection(p.getName());
//			
//		}
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
