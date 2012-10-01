package mc.alk.arena.serializers;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.PlayerStoreController.PInv;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class InventorySerializer {

	public static void saveInventory(String name, PInv pinv) {
		BaseSerializer serializer = getSerializer(name);
		Date now = new Date();
		String date = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG).format(now);
		System.out.println("10. " + date);
		int curSection = serializer.config.getInt("curSection", 0);
		serializer.config.set("curSection", (curSection +1) % 3);
		ConfigurationSection pcs = serializer.config.createSection(curSection+"");
		pcs.set("storedDate", date);
		List<String> stritems = new ArrayList<String>();
		for (ItemStack is : pinv.armor){
			if (is == null || is.getType() == Material.AIR)
				continue;
			stritems.add(InventoryUtil.getItemString(is));}
		pcs.set("armor", stritems);

		stritems = new ArrayList<String>();
		for (ItemStack is : pinv.contents){
			if (is == null || is.getType() == Material.AIR)
				continue;
			stritems.add(InventoryUtil.getItemString(is));}
		pcs.set("contents", stritems);
		
		serializer.save();
	}

	private static BaseSerializer getSerializer(String name) {
		BaseSerializer bs = new BaseSerializer();
		File dir = new File(BattleArena.getSelf().getDataFolder()+"/inventories/");
		if (!dir.exists()){
			dir.mkdirs();}
		bs.setConfig(dir.getPath()+"/"+name+".yml");
		return bs;
	}

}
