package mc.alk.arena.listeners;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.HeroesInterface;
import mc.alk.arena.controllers.MobArenaInterface;
import mc.alk.arena.controllers.WorldGuardInterface;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.DisguiseInterface;
import mc.alk.arena.util.Log;
import mc.alk.tracker.Tracker;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

import com.dthielke.herochat.Herochat;


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
		else if (event.getPlugin().getName() == "MassDisguise")
			loadMD();
		else if (event.getPlugin().getName() == "MultiInv")
			loadMultiInv();
		else if (event.getPlugin().getName() == "Multiverse-Inventories")
			loadMultiverseInventory();
		else if (event.getPlugin().getName() == "Multiverse-Core")
			loadMultiverseCore();
		else if (event.getPlugin().getName() == "Herochat")
			loadHeroChat();
		else if (event.getPlugin().getName() == "WorldGuard")
			loadWorldGuard();
		else if (event.getPlugin().getName() == "WorldEdit")
			loadWorldEdit();
		else if (event.getPlugin().getName() == "MobArena")
			loadMobArena();
		else if (event.getPlugin().getName() == "Heroes")
			loadHeroes();
	}

	public void loadAll(){
		loadMD();
		loadBT();
		loadHeroChat();
		loadWorldEdit();
		loadWorldGuard();
		loadMultiInv();
		loadMultiverseInventory();
		loadMultiverseCore();
		loadMobArena();
		loadHeroes();
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
		if (DisguiseInterface.disguiseInterface == null){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MassDisguise");
			if (plugin != null) {
				DisguiseInterface.disguiseInterface = DisguiseCraft.getAPI();
			} else {
				Log.info("[BattleArena] DisguiseCraft not detected, ignoring disguises");
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
		if (!Defaults.PLUGIN_MULTI_INV){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MultiInv");
			if (plugin != null) {
				Defaults.PLUGIN_MULTI_INV=true;
				Log.info("[BattleArena] MultiInv detected.  Implementing teleport/gamemode workarounds");
			}
		}
	}

	public void loadMultiverseCore(){
		if (!Defaults.PLUGIN_MULITVERSE_CORE){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
			if (plugin != null) {
				Defaults.PLUGIN_MULITVERSE_CORE=true;
				Log.info("[BattleArena] Multiverse-Core detected. Implementing teleport/gamemode workarounds");
			}
		}
	}

	public void loadMultiverseInventory(){
		if (!Defaults.PLUGIN_MULITVERSE_INV){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Inventories");
			if (plugin != null) {
				Defaults.PLUGIN_MULITVERSE_INV=true;
				Log.info("[BattleArena] Multiverse-Inventories detected. Implementing teleport/gamemode workarounds");
			}
		}
	}

	public void loadWorldEdit(){
		if (!Defaults.PLUGIN_MULTI_INV){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
			if (plugin != null) {
				if (WorldGuardInterface.setWorldEdit(plugin)){
					WorldGuardInterface.init();
					Log.info("[BattleArena] WorldGuard detected. WorldGuard regions now enabled");
				}
			}
		}
	}

	public void loadWorldGuard(){
		if (Defaults.PLUGIN_MULTI_INV == false){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
			if (plugin != null) {
				if (WorldGuardInterface.setWorldGuard(plugin)){
					WorldGuardInterface.init();
					Log.info("[BattleArena] WorldGuard detected. WorldGuard regions now be used");
				}
			}
		}
	}
	public void loadMobArena(){
		if (!MobArenaInterface.hasMobArena()){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MobArena");
			if (plugin != null) {
				MobArenaInterface.init(plugin);
				Log.info("[BattleArena] MobArena detected.  Implementing no join when in MobArena");
			}
		}
	}

	public void loadHeroes(){
		if (!HeroesInterface.enabled()){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Heroes");
			if (plugin != null) {
				HeroesInterface.setHeroes(plugin);
				Log.info("[BattleArena] Heroes detected. Implementing heroes class options");
			}
		}
	}

}
