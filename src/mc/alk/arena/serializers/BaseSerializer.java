package mc.alk.arena.serializers;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BaseSerializer {

	FileConfiguration config;
	File f = null;

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
		return f;
	}

	public void setConfig(String file){
		setConfig(new File(file));
	}
	public void setConfig(File file){
		this.f = file;
		if (!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config = new YamlConfiguration();
		try {
			config.load(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reloadFile(){
		try {
			config.load(f);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
