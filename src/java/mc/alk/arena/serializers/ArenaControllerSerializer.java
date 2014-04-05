package mc.alk.arena.serializers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.PlayerRestoreController;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.Util;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;


public class ArenaControllerSerializer extends BaseConfig{

	public ArenaControllerSerializer(){
		this.setConfig(BattleArena.getSelf().getDataFolder()+"/saves/arenaplayers.yml");
	}

	public void load(){
		try {
			config.load(file);
		} catch (Exception e){Log.printStackTrace(e);}

		ConfigurationSection cs = config.getConfigurationSection("tpOnReenter");
		if (cs != null){
			for (String name : cs.getKeys(false)){
				ConfigurationSection loccs = cs.getConfigurationSection(name);
				Location loc = null;
				try {
					loc = SerializerUtil.getLocation(loccs.getString("loc"));
				} catch (IllegalArgumentException e) {
					Log.printStackTrace(e);
				}
				if (loc == null){
					System.err.println("Couldnt load the player " + name +" when reading tpOnReenter inside arenaplayers.yml");
					continue;
				}
                UUID id = Util.fromString(name);
                ArenaPlayer ap = PlayerController.toArenaPlayer(id);
                BAPlayerListener.teleportOnReenter(ap, loc,null);
			}
		}

		cs = config.getConfigurationSection("clearInventoryOnReenter");
		if (cs != null) {
            //			BAPlayerListener.clearInventory.addAll(cs.getKeys(false));
            Collection<String> strs = cs.getKeys(false);

            for (String s : strs){
                UUID id = Util.fromString(s);
                BAPlayerListener.clearInventoryOnReenter(PlayerController.toArenaPlayer(id));
            }

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
				GameMode gm;
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

                UUID id = Util.fromString(name);
				BAPlayerListener.restoreGameModeOnEnter(PlayerController.toArenaPlayer(id), gm);
			}
		}
		cs = config.getConfigurationSection("restoreInv");
		if (cs != null){
			for (String name: cs.getKeys(false)){
				ConfigurationSection pcs = cs.getConfigurationSection(name);
				PInv pinv = getInventory(pcs);
                UUID id = Util.fromString(name);
                BAPlayerListener.restoreItemsOnReenter(PlayerController.toArenaPlayer(id), pinv);
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
				Log.printStackTrace(e);
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
				Log.printStackTrace(e);
				continue;
			}
			items.add(is);
		}
		pinv.contents = items.toArray(new ItemStack[items.size()]);
		return pinv;
	}




	@Override
	public void save(){
		Map<UUID, PlayerRestoreController> prcs =
				new HashMap<UUID,PlayerRestoreController>(BAPlayerListener.getPlayerRestores());

		Map<UUID,Location> playerLocs = new HashMap<UUID,Location>();
		List<UUID> dieOnReenter = new ArrayList<UUID>();
		List<UUID> clearInventoryReenter = new ArrayList<UUID>();
		Map<UUID,GameMode> gameModes = new HashMap<UUID,GameMode>();
		Map<UUID, PInv> items = new HashMap<UUID,PInv>();

		for (PlayerRestoreController prc: prcs.values()){
			final UUID id = prc.getUUID();
			if (prc.getTeleportLocation() != null)
				playerLocs.put(id, prc.getTeleportLocation());
			if (prc.getKill())
				dieOnReenter.add(id);
			if (prc.getClearInventory())
				clearInventoryReenter.add(id);
			if (prc.getGamemode()!=null)
				gameModes.put(id, prc.getGamemode());
			if (prc.getItem() != null){
				items.put(id, prc.getItem());}
		}

		ConfigurationSection cs = config.createSection("tpOnReenter");
		for (UUID player : playerLocs.keySet()){
			ConfigurationSection pc = cs.createSection(player.toString());
			Location loc = playerLocs.get(player);
			pc.set("loc", SerializerUtil.getLocString(loc));
		}

		cs = config.createSection("dieOnReenter");
		for (UUID player : dieOnReenter){
			cs.createSection(player.toString());}

		cs = config.createSection("clearInventoryOnReenter");
		for (UUID player : clearInventoryReenter){
			cs.createSection(player.toString());}

		cs = config.createSection("restoreGameModeOnReenter");
		for (Entry<UUID,GameMode> entry: gameModes.entrySet()){
			ConfigurationSection pc = cs.createSection(entry.getKey().toString());
			pc.set("gamemode", entry.getValue().toString());
		}

		cs = config.createSection("restoreInv");
		for (UUID name: items.keySet()){
			ConfigurationSection pcs = cs.createSection(name.toString());
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
			Log.printStackTrace(e);
		}
	}

}
