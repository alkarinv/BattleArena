package mc.alk.arena.serializers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.Scheduler;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.KeyValue;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;

public class InventorySerializer {

	public static List<String> getDates(final OfflinePlayer player) {
        UUID id = PlayerUtil.getID(player);
        BaseConfig serializer = getSerializer(id);
        if (serializer == null)
            return null;
        PriorityQueue<Long> dates = new PriorityQueue<Long>(Defaults.NUM_INV_SAVES, Collections.reverseOrder());
        Set<String> keys = serializer.config.getKeys(false);

        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);

        for (String key : keys) {
            ConfigurationSection cs = serializer.config.getConfigurationSection(key);
            if (cs == null)
                continue;
            String strdate = cs.getString("storedDate");
            Date date;
            try {
                date = format.parse(strdate);
            } catch (ParseException e) {
                Log.printStackTrace(e);
                continue;
            }
            dates.add(date.getTime());
        }
        List<String> strdates = new ArrayList<String>();
        for (Long l : dates) {
            strdates.add(format.format(l));
        }
        return strdates;
    }

	public static PInv getInventory(final OfflinePlayer player, int index){
		if (index < 0 || index >= Defaults.NUM_INV_SAVES){
			return null;}
        UUID id = PlayerUtil.getID(player);
		BaseConfig serializer = getSerializer(id);
		if (serializer == null)
			return null;
		PriorityQueue<KeyValue<Long,PInv>> dates =
				new PriorityQueue<KeyValue<Long,PInv>>(Defaults.NUM_INV_SAVES, new Comparator<KeyValue<Long,PInv>>(){
					@Override
					public int compare(KeyValue<Long, PInv> arg0, KeyValue<Long, PInv> arg1) {
						return arg1.key.compareTo(arg0.key);
					}
				});
		Set<String> keys = serializer.config.getKeys(false);

		DateFormat format = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG);

		for (String key: keys){
			ConfigurationSection cs = serializer.config.getConfigurationSection(key);
			if (cs == null)
				continue;
			String strdate = cs.getString("storedDate");
			Date date;
			try {
				date = format.parse(strdate);
			} catch (ParseException e) {
				Log.printStackTrace(e);
				continue;
			}
			PInv pinv = ArenaControllerSerializer.getInventory(cs);
			dates.add(new KeyValue<Long,PInv>(date.getTime(),pinv));
		}
		int i=0;
		for (KeyValue<Long,PInv> l: dates){
			if (i++==index)
				return l.value;
		}
		return null;
	}

	public static void saveInventory(final UUID id, final PInv pinv) {
		if (Defaults.NUM_INV_SAVES <= 0){
			return;}
        Scheduler.scheduleAsynchronousTask(new TimerTask(){
			@Override
			public void run() {
				BaseConfig serializer = getSerializer(id);
				if (serializer == null)
					return;
				Date now = new Date();
				String date = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG).format(now);
				int curSection = serializer.config.getInt("curSection", 0);
				serializer.config.set("curSection", (curSection +1) % Defaults.NUM_INV_SAVES);
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

		}, 0);
	}

	private static BaseConfig getSerializer(UUID id) {
		BaseConfig bs = new BaseConfig();
		File dir = new File(BattleArena.getSelf().getDataFolder()+"/saves/inventories/");
		if (!dir.exists()){
			try {dir.mkdirs();}catch (Exception e){/* do nothing */}
			}
		return bs.setConfig(dir.getPath()+"/"+id+".yml") ? bs : null;
	}

	@SuppressWarnings("deprecation")
	public static boolean giveInventory(ArenaPlayer player, Integer index, Player other) {
		PInv pinv = getInventory(player.getPlayer(), index);
		if (pinv == null)
			return false;

		for (ItemStack is: pinv.armor){
			InventoryUtil.addItemToInventory(other, is);
		}
		for (ItemStack is: pinv.contents){
			InventoryUtil.addItemToInventory(other, is);
		}
		try{other.updateInventory();} catch(Exception e){ /// yes this has thrown errors on me before
			return false; /// do I really want to return false? do I care if this doesnt go through?
		}
		return true;
	}

}
