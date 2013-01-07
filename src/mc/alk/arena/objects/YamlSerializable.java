package mc.alk.arena.objects;

import org.bukkit.configuration.ConfigurationSection;

public interface YamlSerializable {

	Object yamlToObject(ConfigurationSection cs, String value);
	Object objectToYaml();
}
