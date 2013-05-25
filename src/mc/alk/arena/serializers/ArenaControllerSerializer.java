package mc.alk.arena.serializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.PlayerRestoreController;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;


public class ArenaControllerSerializer extends BaseConfig{

	public ArenaControllerSerializer(){
		this.setConfig(BattleArena.getSelf().getDataFolder()+"/saves/arenaplayers.yml");
	}

	public void load(){
		try {
			config.load(file);
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
				BAPlayerListener.teleportOnReenter(name, loc,null);
			}
		}

		cs = config.getConfigurationSection("dieOnReenter");
		if (cs != null){
			BAPlayerListener.killAllOnReenter(cs.getKeys(false));
		}
		cs = config.getConfigurationSection("clearInventoryOnReenter");
		if (cs != null){
			//			BAPlayerListener.clearInventory.addAll(cs.getKeys(false));
			BAPlayerListener.clearInventoryOnReenter(cs.getKeys(false));
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
				GameMode gm = null;
				try{
					gm = GameMode.valueOf(strgm);
					if (gm == null){
						System.err.println("Couldnt load the player " + name +" when reading restoreGameModeOnReenter inside arenaplayers.yml");
						continue;
					}
				} catch (Exception e){
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




	@Override
	public void save(){
		Map<String, PlayerRestoreController> prcs =
				new HashMap<String,PlayerRestoreController>(BAPlayerListener.getPlayerRestores());

		Map<String,Location> playerLocs = new HashMap<String,Location>();
		List<String> dieOnReenter = new ArrayList<String>();
		List<String> clearInventoryReenter = new ArrayList<String>();
		Map<String,GameMode> gameModes = new HashMap<String,GameMode>();
		Map<String, PInv> items = new HashMap<String,PInv>();

		for (PlayerRestoreController prc: prcs.values()){
			final String name = prc.getName();
			if (prc.getTeleportLocation() != null)
				playerLocs.put(name, prc.getTeleportLocation());
			if (prc.getKill())
				dieOnReenter.add(name);
			if (prc.getClearInventory())
				clearInventoryReenter.add(name);
			if (prc.getGamemode()!=null)
				gameModes.put(name, prc.getGamemode());
			if (prc.getItem() != null){
				items.put(name, prc.getItem());}
		}

		ConfigurationSection cs = config.createSection("tpOnReenter");
		for (String player : playerLocs.keySet()){
			ConfigurationSection pc = cs.createSection(player);
			Location loc = playerLocs.get(player);
			pc.set("loc", SerializerUtil.getLocString(loc));
		}

		cs = config.createSection("dieOnReenter");
		for (String player : dieOnReenter){
			cs.createSection(player);}

		cs = config.createSection("clearInventoryOnReenter");
		for (String player : clearInventoryReenter){
			cs.createSection(player);}

		cs = config.createSection("restoreGameModeOnReenter");
		for (Entry<String,GameMode> entry: gameModes.entrySet()){
			ConfigurationSection pc = cs.createSection(entry.getKey());
			pc.set("gamemode", entry.getValue().toString());
		}

		cs = config.createSection("restoreInv");
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
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
