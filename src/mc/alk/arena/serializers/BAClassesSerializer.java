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

public class BAClassesSerializer extends BaseConfig{

	public void loadAll(){
		try {config.load(file);} catch (Exception e){e.printStackTrace();}
		loadClasses(config.getConfigurationSection("classes"));
	}

	public void loadClasses(ConfigurationSection cs) {
		if (cs == null){
			Log.info(BattleArena.getPluginName() +" has no classes");
			return;}
		StringBuilder sb = new StringBuilder();
		Set<String> keys = cs.getKeys(false);
		boolean first = true;
		for (String className : keys){
			try{
				ArenaClass ac = parseArenaClass(cs.getConfigurationSection(className));
				if (ac == null)
					continue;
				if (first) first = false;
				else sb.append(", ");
				sb.append(ac.getName());
				ArenaClassController.addClass(ac);
			} catch (Exception e){
				Log.err("There was an error loading the class " + className +". It will be disabled");
				e.printStackTrace();
			}
		}
		if (first){
			Log.info(BattleArena.getPluginName() +" no predefined classes found. inside of " + cs.getCurrentPath());
		} else {
			Log.info(BattleArena.getPluginName()+" registering classes: " +sb.toString());
		}
	}

	public ArenaClass parseArenaClass(ConfigurationSection cs) {
		List<ItemStack> items = null;
		List<PotionEffect> effects = null;
		if (cs.contains("items")){ items = ConfigSerializer.getItemList(cs,"items");}
		if (cs.contains("enchants")){ effects = ConfigSerializer.getEffectList(cs,"enchants");}
		String displayName = cs.getString("displayName", null);
		if (displayName==null) cs.getString("prettyName", null);
		ArenaClass ac = new ArenaClass(cs.getName(),displayName, items,effects);
		if (cs.contains("disguise")){ ac.setDisguiseName(cs.getString("disguise"));}
		return ac;
	}

}
