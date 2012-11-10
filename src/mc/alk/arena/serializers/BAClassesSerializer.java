package mc.alk.arena.serializers;

import java.util.List;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.util.Log;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class BAClassesSerializer extends BaseSerializer{

	public void loadAll(){
		try {config.load(file);} catch (Exception e){e.printStackTrace();}
		loadClasses(config.getConfigurationSection("classes"));
	}

	public static void loadClasses(ConfigurationSection cs) {
		if (cs == null){
			Log.info(BattleArena.getPName() +" has no classes");
			return;}
		StringBuilder sb = new StringBuilder();
		Set<String> keys = cs.getKeys(false);
		boolean first = true;
		for (String className : keys){
			ArenaClass ac = parseArenaClass(cs.getConfigurationSection(className));
			if (ac == null)
				continue;
			if (first) first = false;
			else sb.append(", ");
			sb.append(ac.getName());
			ArenaClassController.addClass(ac);
		}
		if (first){
			Log.info(BattleArena.getPName() +" no predefined classes found. inside of " + cs.getCurrentPath());
		} else {
			Log.info(BattleArena.getPName()+" registering classes: " +sb.toString());
		}
	}


	private static ArenaClass parseArenaClass(ConfigurationSection cs) {
		List<ItemStack> items = null;
		List<PotionEffect> effects = null;
		if (cs.contains("items")){ items = BAConfigSerializer.getItemList(cs,"items");}
		if (cs.contains("enchants")){ effects = BAConfigSerializer.getEffectList(cs,"enchants");}
		String prettyName = cs.getString("displayName", null);
		if (prettyName==null) cs.getString("prettyName", null);
		return new ArenaClass(cs.getName(),prettyName, items,effects);
	}

}
