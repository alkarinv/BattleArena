package mc.alk.arena.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;


public class StateFlagSerializer extends BaseConfig{	
	public List<String> load(){
		ConfigurationSection cs = config.getConfigurationSection("enabled");
		List<String> disabled = new ArrayList<String>();
		if (cs != null){
			for (String name : cs.getKeys(false)){
				if (!cs.getBoolean(name)){
					disabled.add(name);
				}
			}
		}
		return disabled;
	}

	public void save(Collection<String> disabled){
		ConfigurationSection cs = config.createSection("enabled");
		if (disabled != null){
			for (String s: disabled){
				cs.set(s, false);}			
		}
		save();
	}

}
