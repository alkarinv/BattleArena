package mc.alk.arena.listeners;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.EssentialsController;
import mc.alk.arena.controllers.FactionsController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.controllers.MobArenaInterface;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.PylamoController;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.controllers.TagAPIController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.util.DisguiseInterface;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PermissionsUtil;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.dthielke.herochat.Herochat;


/**
 *
 * @author alkarin
 *
 */
public class BAPluginListener implements Listener {

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getName().equalsIgnoreCase("BattleTracker"))
			loadBattleTracker();
		else if (event.getPlugin().getName().equalsIgnoreCase("Essentials"))
			loadEssentials();
		else if (event.getPlugin().getName().equalsIgnoreCase("Factions"))
			loadFactions();
		else if (event.getPlugin().getName().equalsIgnoreCase("Herochat"))
			loadHeroChat();
		else if (event.getPlugin().getName().equalsIgnoreCase("Heroes"))
			loadHeroes();
		else if (event.getPlugin().getName().equalsIgnoreCase("DisguiseCraft"))
			loadDisguiseCraft();
		else if (event.getPlugin().getName().equalsIgnoreCase("MobArena"))
			loadMobArena();
		else if (event.getPlugin().getName().equalsIgnoreCase("MultiInv"))
			loadMultiInv();
		else if (event.getPlugin().getName().equalsIgnoreCase("Multiverse-Core"))
			loadMultiverseCore();
		else if (event.getPlugin().getName().equalsIgnoreCase("Multiverse-Inventories"))
			loadMultiverseInventory();
		else if (event.getPlugin().getName().equalsIgnoreCase("PylamoRestorationSystem"))
			loadPylamoRestoration();
		else if (event.getPlugin().getName().equalsIgnoreCase("TagAPI"))
			loadTagAPI();
		else if (event.getPlugin().getName().equalsIgnoreCase("WorldEdit"))
			loadWorldEdit();
		else if (event.getPlugin().getName().equalsIgnoreCase("WorldGuard"))
			loadWorldGuard();
		else if (event.getPlugin().getName().equalsIgnoreCase("Vault"))
			loadVault();
	}

	public void loadAll(){
		loadBattleTracker();
		loadDisguiseCraft();
		loadEssentials();
		loadFactions();
		loadHeroChat();
		loadHeroes();
		loadMobArena();
		loadMultiInv();
		loadMultiverseCore();
		loadMultiverseInventory();
		loadPylamoRestoration();
		loadTagAPI();
		loadWorldEdit();
		loadWorldGuard();
		loadVault();
	}


	public void loadBattleTracker(){
		if (!StatController.enabled()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("BattleTracker");
			if (plugin != null) {
				StatController.setPlugin(plugin);
			} else {
				Log.info("[BattleArena] BattleTracker not detected, not tracking wins");
			}
		}
	}

	public void loadDisguiseCraft(){
		if (!DisguiseInterface.enabled()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("DisguiseCraft");
			if (plugin != null) {
				DisguiseInterface.setDisguiseCraft(plugin);
				Log.info("[BattleArena] DisguiseCraft detected, enabling disguises");
			}
		}
	}

	public void loadEssentials(){
		if (!EssentialsController.enabled()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
			if (plugin != null) {
				if (EssentialsController.enableEssentials(plugin)){
					Log.info("[BattleArena] Essentials detected. God mode handling activated");
				} else {
					Log.info("[BattleArena] Essentials detected but could not hook properly");
				}
			}
		}
	}

	public void loadFactions(){
		if (!FactionsController.enabled()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
			if (plugin != null) {
				if (FactionsController.enableFactions(true)){
					Log.info("[BattleArena] Factions detected. Configurable power loss enabled (default no powerloss)");
				} else {
					Log.info("[BattleArena] Old Factions detected that does not have a PowerLossEvent");
				}
			}
		}
	}

	public void loadHeroChat(){
		if (AnnouncementOptions.hc == null){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("Herochat");
			if (plugin != null) {
				AnnouncementOptions.setHerochat((Herochat) plugin);
				Log.info("[BattleArena] Herochat detected, adding channel options");
			}
		}
	}

	public void loadHeroes(){
		if (!HeroesController.enabled()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("Heroes");
			if (plugin != null) {
				HeroesController.setHeroes(plugin);
				Log.info("[BattleArena] Heroes detected. Implementing heroes class options");
			}
		}
	}

	public void loadMobArena(){
		if (!MobArenaInterface.hasMobArena()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("MobArena");
			if (plugin != null) {
				MobArenaInterface.init(plugin);
				Log.info("[BattleArena] MobArena detected.  Implementing no join when in MobArena");
			}
		}
	}

	public void loadMultiInv(){
		if (!Defaults.PLUGIN_MULTI_INV){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("MultiInv");
			if (plugin != null) {
				Defaults.PLUGIN_MULTI_INV=true;
				Log.info("[BattleArena] MultiInv detected.  Implementing teleport/gamemode workarounds");
			}
		}
	}

	public void loadMultiverseCore(){
		if (!Defaults.PLUGIN_MULITVERSE_CORE){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
			if (plugin != null) {
				Defaults.PLUGIN_MULITVERSE_CORE=true;
				Log.info("[BattleArena] Multiverse-Core detected. Implementing teleport/gamemode workarounds");
			}
		}
	}

	public void loadMultiverseInventory(){
		if (!Defaults.PLUGIN_MULITVERSE_INV){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
			if (plugin != null) {
				Defaults.PLUGIN_MULITVERSE_INV=true;
				Log.info("[BattleArena] Multiverse-Inventories detected. Implementing teleport/gamemode workarounds");
			}
		}
	}

	public void loadPylamoRestoration(){
		if (!PylamoController.enabled()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("PylamoRestorationSystem");
			if (plugin != null){
				PylamoController.setPylamo(plugin);
				Log.info(BattleArena.getPluginName() +" found PylamoRestorationSystem");
			}
		}
	}

	public void loadWorldEdit(){
		if (!WorldGuardController.hasWorldEdit()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
			if (plugin != null) {
				if (WorldGuardController.setWorldEdit(plugin)){
					Log.info("[BattleArena] WorldEdit detected.");
				}
			}
		}
	}

	public void loadWorldGuard(){
		if (!WorldGuardController.hasWorldGuard()){
			Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
			if (plugin != null) {
				if (WorldGuardController.setWorldGuard(plugin)){
					Log.info("[BattleArena] WorldGuard detected. WorldGuard regions can now be used");
				}
			}
		}
	}

	public void loadTagAPI(){
		if (!TagAPIController.enabled()){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("TagAPI");
			if (plugin != null) {
				TagAPIController.setEnable(true);
				Log.info("[BattleArena] TagAPI detected. Implementing Team colored player names");
			}
		}
	}

	public void loadVault(){
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
		if (plugin != null ){
			/// Load vault economy
			if (!MoneyController.hasEconomy()){
				try{
					RegisteredServiceProvider<Economy> provider = Bukkit.getServer().
							getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
					if (provider==null || provider.getProvider() == null){
						Log.warn(BattleArena.getPluginName() +" found no economy plugin. Attempts to use money in arenas might result in errors.");
						return;
					} else {
						MoneyController.setEconomy(provider.getProvider());
						Log.info(BattleArena.getPluginName() +" found economy plugin Vault. [Default]");
					}
				} catch (Error e){
					Log.err(BattleArena.getPluginName() +" exception loading economy through Vault");
					e.printStackTrace();
				}
			}
			/// Load Vault chat
			if (AnnouncementOptions.chat == null){
				try{
					RegisteredServiceProvider<Chat> provider = Bukkit.getServer().
							getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
					if (provider != null && provider.getProvider() != null) {
						AnnouncementOptions.setVaultChat(provider.getProvider());
					} else if (AnnouncementOptions.hc == null){
						Log.info("[BattleArena] Vault chat not detected, ignoring channel options");
					}
				} catch (Error e){
					Log.err(BattleArena.getPluginName() +" exception loading chat through Vault");
					e.printStackTrace();
				}
			}
			/// Load Vault Permissions
			PermissionsUtil.setPermission(plugin);
		}
	}

}
