package mc.alk.arena.serializers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;


public class ArenaControllerSerializer {
	public static YamlConfiguration config = new YamlConfiguration();
	static File f;

	public ArenaControllerSerializer(){
		try {
			f = new File(BattleArena.getSelf().getDataFolder()+"/saves/arenaplayers.yml");
			if (!f.exists())
				f.createNewFile();
		} catch (Exception e){e.printStackTrace();}
	}

	public void load(){
		try {
			config.load(f);
		} catch (Exception e){e.printStackTrace();}

		ConfigurationSection cs = config.getConfigurationSection("tpOnReenter");
		if (cs != null){
			for (String name : cs.getKeys(false)){
				ConfigurationSection loccs = cs.getConfigurationSection(name);
				Location loc = null;
				try {
					loc = SerializerUtil.getLocation(loccs.getString("loc"));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
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

		cs = config.getConfigurationSection("restoreGameModeOnReenter");
		if (cs != null){
			for (String name : cs.getKeys(false)){
				ConfigurationSection cs2 = cs.getConfigurationSection(name);
				String strgm = cs2.getString("gamemode");
				if (strgm == null){
					System.err.println("Couldnt load the player " + name +" when reading restoreGameModeOnReenter inside arenaplayers.yml");
					continue;
				}
				GameMode gm = GameMode.valueOf(strgm);
				if (gm == null){
					System.err.println("Couldnt load the player " + name +" when reading restoreGameModeOnReenter inside arenaplayers.yml");
					continue;
				}
				BAPlayerListener.restoreGameModeOnEnter(name, gm);
			}
		}
		cs = config.getConfigurationSection("restoreInv");
		if (cs != null){
			for (String name: cs.getKeys(false)){
				ConfigurationSection pcs = cs.getConfigurationSection(name);
				PInv pinv = getInventory(pcs);
				BAPlayerListener.restoreItemsOnReenter(name, pinv);
			}
		}
	}

	public static PInv getInventory(ConfigurationSection cs){
			PInv pinv = new PInv();
			List<ItemStack> items = new ArrayList<ItemStack>();
			List<String> stritems = cs.getStringList("armor");
			for (String stritem : stritems){
				ItemStack is;
				try {
					is = InventoryUtil.parseItem(stritem);
				} catch (Exception e) {
					System.err.println("Couldnt reparse "+stritem +" for player " + cs.getName());
					e.printStackTrace();
					continue;
				}
				items.add(is);
			}
			pinv.armor = items.toArray(new ItemStack[items.size()]);

			items = new ArrayList<ItemStack>();
			stritems = cs.getStringList("contents");
			for (String stritem : stritems){
				ItemStack is;
				try {
					is = InventoryUtil.parseItem(stritem);
				} catch (Exception e) {
					System.err.println("Couldnt reparse "+stritem +" for player " + cs.getName());
					e.printStackTrace();
					continue;
				}
				items.add(is);
			}
			pinv.contents = items.toArray(new ItemStack[items.size()]);
			return pinv;
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

		cs = config.createSection("restoreGameModeOnReenter");
		for (String player : BAPlayerListener.gamemodeRestore.keySet()){
			ConfigurationSection pc = cs.createSection(player);
			pc.set("gamemode", BAPlayerListener.gamemodeRestore.get(player).toString());
		}

		cs = config.createSection("restoreInv");
		Map<String, PInv> items = BAPlayerListener.itemRestore;
		for (String name: items.keySet()){
			ConfigurationSection pcs = cs.createSection(name);
			PInv inv = items.get(name);
			List<String> stritems = new ArrayList<String>();
			for (ItemStack is : inv.armor){
				if (is == null || is.getType() == Material.AIR)
					continue;
				stritems.add(InventoryUtil.getItemString(is));}
			pcs.set("armor", stritems);

			stritems = new ArrayList<String>();
			for (ItemStack is : inv.contents){
				if (is == null || is.getType() == Material.AIR)
					continue;
				stritems.add(InventoryUtil.getItemString(is));}
			pcs.set("contents", stritems);
		}

		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
