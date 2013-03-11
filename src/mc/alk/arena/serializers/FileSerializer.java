package mc.alk.arena.serializers;

import org.bukkit.configuration.ConfigurationSection;

public interface FileSerializer {
	public boolean getBoolean(String node, boolean defaultValue);
	public String getString(String node,String defaultValue);
	public int getInt(String node,int defaultValue);
	public double getDouble(String node, double defaultValue);
	public ConfigurationSection getConfigurationSection(String node);
}
