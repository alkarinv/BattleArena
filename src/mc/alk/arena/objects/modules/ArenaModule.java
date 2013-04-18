package mc.alk.arena.objects.modules;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.arenas.ArenaListener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class ArenaModule implements Listener, ArenaListener{
	private boolean enabled;

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
}
