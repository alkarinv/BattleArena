package mc.alk.arena.serializers;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.alk.util.Log;

public class BaseSerializer {

	FileConfiguration config;
	File file = null;

	public boolean getBoolean(String node) {return config.getBoolean(node, false);}
	public String getString(String node) {return config.getString(node,null);}
	public String getString(String node,String def) {return config.getString(node,def);}
	public int getInt(String node,int i) {return config.getInt(node, i);}
	public double getDouble(String node, double d) {return config.getDouble(node, d);}
	public ConfigurationSection getConfigurationSection(String path) {return config.getConfigurationSection(path);}

	public FileConfiguration getConfig() {
		return config;
	}

	public File getFile() {
		return file;
	}

	public boolean setConfig(String file){
		return setConfig(new File(file));
	}
	public boolean setConfig(File file){
		this.file = file;
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				Log.err("Couldn't create the config file=" + file);
				e.printStackTrace();
				return false;
			}
		}

		config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void reloadFile(){
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void save() {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
