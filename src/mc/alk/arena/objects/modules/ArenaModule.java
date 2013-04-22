package mc.alk.arena.objects.modules;

import java.io.File;
import java.io.IOException;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.util.FileUtil;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class ArenaModule implements Listener, ArenaListener{
	private boolean enabled;
	protected FileConfiguration config;

	public ArenaModule(){
//		config = new BaseConfig(BattleArena.getSelf().getModuleDirectory()+"/"+this.getName());
	}

	/**
	 * Called when the module is first created
	 */
	public void onEnable(){}

	/**
	 * Called when the module is being disabled
	 */
	public void onDisable(){}

	/**
	 * Return the Name of this module
	 * @return module name
	 */
	public abstract String getName();

	/**
	 * Return the version of this Module
	 * @return version
	 */
	public abstract String getVersion();

	/**
	 * Is this module currently enabled
	 * @return enabled
	 */
	public boolean isEnabled(){
		return enabled;
	}

	/**
	 * Set the module to be enabled or not
	 * @param enable
	 */
	public void setEnabled(boolean enable){
		if (this.enabled != enable){
			if (enable){
				this.onEnable();
				Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
			} else {
				this.onDisable();
				HandlerList.unregisterAll(this);
			}
		}
		this.enabled = enable;
	}

	public String getDescription(){
		return getName();
	}

	public void reloadConfig(){
		try {
			config.load(getConfigFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected File getConfigFile(){
		return new File(BattleArena.getSelf().getModuleDirectory()+"/"+this.getName());
	}

	/**
	 * create or save the default config.yml
	 */
	protected void saveDefaultConfig(){
		File f = getConfigFile();
		if (config == null || !f.exists()){
			if (FileUtil.hasResource(this.getClass(), "/config.yml")){
				f = FileUtil.load(this.getClass(), f.getPath(), "/config.yml");
			} else {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return;
		}
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig(){
		if (config == null){
			saveDefaultConfig();
		}
		return config;
	}
}
