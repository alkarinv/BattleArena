package mc.alk.arena.listeners;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.WorldGuardInterface;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.WorldGuardUtil;
import mc.alk.tracker.Tracker;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import com.alk.massDisguise.MassDisguise;
import com.dthielke.herochat.Herochat;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


/**
 * 
 * @author alkarin
 *
 */
public class BAPluginListener implements Listener {

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getName() == "BattleTracker")
			loadBT();
		if (event.getPlugin().getName() == "MassDisguise")
			loadMD();
		if (event.getPlugin().getName() == "MultiInv")
			loadMultiInv();
		if (event.getPlugin().getName() == "Herochat")
			loadHeroChat();
		if (event.getPlugin().getName() == "WorldGuard")
			loadWorldGuard();
		if (event.getPlugin().getName() == "WorldEdit")
			loadWorldEdit();

	}

	public void loadAll(){
		loadMD();
		loadBT();
		loadMultiInv();
		loadHeroChat();
		loadWorldEdit();
		loadWorldGuard();
	}

	public void loadHeroChat(){
		if (AnnouncementOptions.hc == null){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Herochat");
			if (plugin != null) {
				AnnouncementOptions.setHerochat((Herochat) plugin);
			} else {
				Log.info("[BattleArena] Herochat not detected, ignoring Herochat channel options");
			}
		}

	}

	public void loadMD(){
		if (BattleArena.md == null){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MassDisguise");
			if (plugin != null) {
				BattleArena.md = (MassDisguise) plugin;
			} else {
				Log.info("[BattleArena] MassDisguise not detected, ignoring disguises");
			}
		}
	}

	public void loadBT(){
		if (BTInterface.battleTracker == null){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("BattleTracker");
			if (plugin != null) {
				BTInterface.battleTracker = (Tracker) plugin;
			} else {
				Log.info("[BattleArena] BattleTracker not detected, not tracking wins");
			}
		}
	}

	public void loadMultiInv(){
		if (Defaults.PLUGIN_MULTI_INV == false){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MultiInv");
			if (plugin != null) {
				Defaults.PLUGIN_MULTI_INV=true;
				Log.info("[BattleArena] MultiInv detected.  Implementing MultiInv teleport workarounds");
			} 
		}
	}

	public void loadWorldEdit(){
		if (Defaults.PLUGIN_MULTI_INV == false){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
			if (plugin != null) {
				WorldGuardUtil.wep = (WorldEditPlugin) plugin;
				if (WorldGuardUtil.hasWorldGuard()){					
					Log.info("[BattleArena] WorldGuard detected. WorldGuard regions now be used");
				}
			} 
		}
	}
	
	public void loadWorldGuard(){
		if (Defaults.PLUGIN_MULTI_INV == false){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
			if (plugin != null) {
				WorldGuardUtil.wgp = (WorldGuardPlugin) plugin;
				if (WorldGuardUtil.hasWorldGuard()){			
					WorldGuardInterface.init();
					Log.info("[BattleArena] WorldGuard detected. WorldGuard regions now be used");
				}
			} 
		}
	}

}
