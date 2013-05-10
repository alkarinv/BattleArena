package mc.alk.arena.controllers;

import java.util.HashMap;
import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.serializers.InventorySerializer;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PermissionsUtil;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerStoreController {
	static final PlayerStoreController INSTANCE = new PlayerStoreController();
	/// TODO since the number of things I am storing is so large now, this might be a good time to refactor these
	/// into something a bit cleaner
	final HashMap <String, Integer> expmap = new HashMap<String,Integer>();
	final HashMap <String, Integer> healthmap = new HashMap<String,Integer>();
	final HashMap <String, Integer> healthpmap = new HashMap<String,Integer>();
	final HashMap <String, Integer> hungermap = new HashMap<String,Integer>();
	final HashMap <String, Integer> magicmap = new HashMap<String,Integer>();
	final HashMap <String, Integer> magicpmap = new HashMap<String,Integer>();
	final HashMap <String, PInv> itemmap = new HashMap<String,PInv>();
	final HashMap <String, PInv> matchitemmap = new HashMap<String,PInv>();
	final HashMap <String, GameMode> gamemode = new HashMap<String,GameMode>();
	final HashMap <String, String> arenaclass = new HashMap<String,String>();

	@SuppressWarnings("deprecation")
	public void storeExperience(ArenaPlayer player) {
		Player p = player.getPlayer();
		int exp = ExpUtil.getTotalExperience(p);
		final String name = p.getName();
		if (exp == 0)
			return;
		if (expmap.containsKey(name)){
			exp += expmap.get(name);}
		expmap.put(name, exp);
		ExpUtil.setTotalExperience(p, 0);
		try{p.updateInventory();} catch(Exception e){}
	}

	public void restoreExperience(ArenaPlayer p) {
		if (!expmap.containsKey(p.getName()))
			return;
		Integer exp = expmap.remove(p.getName());
		if (p.isOnline() && !p.isDead()){
			ExpUtil.giveExperience(p.getPlayer(), exp);
		} else {
			BAPlayerListener.restoreExpOnReenter(p.getName(), exp);
		}
	}

	public void storeHealth(ArenaPlayer player) {
		final String name = player.getName();
		if (healthmap.containsKey(name))
			return;
		int health = player.getHealth();
		if (Defaults.DEBUG_STORAGE) Log.info("storing health=" + health+" for player=" + player.getName());
		healthmap.put(name, health);
	}

	public void restoreHealth(ArenaPlayer p) {
		if (!healthmap.containsKey(p.getName()))
			return;
		Integer val = healthmap.remove(p.getName());
		if (val == null || val <= 0)
			return;
		if (Defaults.DEBUG_STORAGE) Log.info("restoring health=" + val+" for player=" + p.getName());
		if (p.isOnline() && !p.isDead()){
			p.setHealth(val);
		} else {
			BAPlayerListener.restoreHealthOnReenter(p.getName(), val);
		}
	}

	public void storeHunger(ArenaPlayer player) {
		final String name = player.getName();
		if (hungermap.containsKey(name))
			return;

		hungermap.put(name, player.getFoodLevel());
	}

	public void restoreHunger(ArenaPlayer p) {
		if (!hungermap.containsKey(p.getName()))
			return;
		Integer val = hungermap.remove(p.getName());
		if (val == null || val <= 0)
			return;
		if (p.isOnline() && !p.isDead()){
			p.getPlayer().setFoodLevel(val);
		} else {
			BAPlayerListener.restoreHungerOnReenter(p.getName(), val);
		}
	}

	public void storeMagic(ArenaPlayer player) {
		if (!HeroesController.enabled())
			return;
		final String name = player.getName();
		if (magicmap.containsKey(name))
			return;

		Integer val = HeroesController.getMagicLevel(player.getPlayer());
		if (val == null)
			return;
		magicmap.put(name, val);
	}

	public void restoreMagic(ArenaPlayer p) {
		if (!magicmap.containsKey(p.getName()))
			return;
		Integer val = magicmap.remove(p.getName());
		if (val == null)
			return;
		if (p.isOnline() && !p.isDead()){
			HeroesController.setMagicLevel(p.getPlayer(), val);
		} else {
			BAPlayerListener.restoreMagicOnReenter(p.getName(), val);
		}
	}

	public void storeItems(ArenaPlayer player) {
		final String name= player.getName();
		if (Defaults.DEBUG_STORAGE) Log.info("storing items for = " + name +" contains=" + itemmap.containsKey(name));
		if (itemmap.containsKey(name))
			return;
		InventoryUtil.closeInventory(player.getPlayer());
		final PInv pinv = new PInv(player.getInventory());
		itemmap.put(name, pinv);
		InventorySerializer.saveInventory(name,pinv);
	}

	/**
	 * Warning!!! Unlike most other methods in the StoreController, this one
	 * overwrites previous values
	 * @param player
	 */
	public void storeMatchItems(ArenaPlayer player) {
		final String name= player.getName();
		if (Defaults.DEBUG_STORAGE) Log.info("storing in match items for = " + name +" contains=" + matchitemmap.containsKey(name));
		InventoryUtil.closeInventory(player.getPlayer());
		final PInv pinv = new PInv(player.getInventory());
		if (matchitemmap.put(name, pinv) != null){
			/// on the first entry, lets log that to disk
			InventorySerializer.saveInventory(name,pinv);
		}
		BAPlayerListener.restoreMatchItemsOnReenter(name, pinv);
	}

	public void clearMatchItems(ArenaPlayer player) {
		final String name= player.getName();
		matchitemmap.remove(name);
		BAPlayerListener.removeMatchItems(name);
	}

	public void restoreItems(ArenaPlayer p) {
		if (Defaults.DEBUG_STORAGE)  Log.info("   "+p.getName()+" psc contains=" + itemmap.containsKey(p.getName()) +"  dead=" + p.isDead()+" online=" + p.isOnline());
		final PInv pinv = itemmap.remove(p.getName());
		if (pinv == null)
			return;
		setInventory(p, pinv);
	}

	public void restoreMatchItems(ArenaPlayer p){
		if (Defaults.DEBUG_STORAGE)  Log.info("   "+p.getName()+" psc matchitemmap=" + matchitemmap.containsKey(p.getName()) +"  dead=" + p.isDead()+" online=" + p.isOnline());
		final PInv pinv = matchitemmap.remove(p.getName());
		if (pinv == null)
			return;
		setMatchInventory(p, pinv);
	}


	public static void setMatchInventory(ArenaPlayer p, PInv pinv) {
		if (Defaults.DEBUG_STORAGE) Log.info("restoring match items for " + p.getName() +"= "+" o="+p.isOnline() +"  dead="+p.isDead() +" h=" + p.getHealth()+"");
		if (p.isOnline() && !p.isDead()){
			InventoryUtil.addToInventory(p.getPlayer(), pinv);
		} else {
			BAPlayerListener.restoreItemsOnReenter(p.getName(), pinv);
		}
	}

	public static void setInventory(ArenaPlayer p, PInv pinv) {
		if (Defaults.DEBUG_STORAGE) Log.info("restoring items for " + p.getName() +"= "+" o="+p.isOnline() +"  dead="+p.isDead() +" h=" + p.getHealth()+"");
		if (p.isOnline() && !p.isDead()){
			InventoryUtil.addToInventory(p.getPlayer(), pinv);
		} else {
			BAPlayerListener.restoreItemsOnReenter(p.getName(), pinv);
		}
	}

	public void storeGamemode(ArenaPlayer p) {
		if (gamemode.containsKey(p.getName()))
			return;
		if (Defaults.DEBUG_STORAGE)  Log.info("storing gamemode " + p.getName() +" " + p.getPlayer().getGameMode());

		PermissionsUtil.givePlayerInventoryPerms(p);
		gamemode.put(p.getName(), p.getPlayer().getGameMode());
	}

	public void restoreGamemode(ArenaPlayer p) {
		GameMode gm = gamemode.remove(p.getName());
		if (gm == null)
			return;
		if (!p.isOnline() || p.isDead()){
			BAPlayerListener.restoreGameModeOnEnter(p.getName(), gm);
		} else {
			setGameMode(p.getPlayer(), gm);
		}
	}

	public static void setGameMode(Player p, GameMode gm){
		if (Defaults.DEBUG_STORAGE)  Log.info("set gamemode " + p.getName() +" " + p.isOnline()+":"+p.isDead() +" gm=" +gm +"  " + p.getGameMode());
		if (gm != null && gm != p.getGameMode()){
			PermissionsUtil.givePlayerInventoryPerms(p);
			p.getPlayer().setGameMode(gm);
		}
	}

	public static void removeItem(ArenaPlayer p, ItemStack is) {
		if (p.isOnline()){
			InventoryUtil.removeItems(p.getInventory(),is);
		} else {
			BAPlayerListener.removeItemOnEnter(p,is);
		}
	}

	public static void removeItems(ArenaPlayer p, List<ItemStack> items) {
		if (p.isOnline()){
			InventoryUtil.removeItems(p.getInventory(),items);
		} else {
			BAPlayerListener.removeItemsOnEnter(p,items);
		}
	}

	public void addMember(ArenaPlayer p, WorldGuardRegion region) {
		WorldGuardController.addMember(p.getName(), region);
	}
	public void removeMember(ArenaPlayer p, WorldGuardRegion region) {
		WorldGuardController.removeMember(p.getName(), region);
	}

	public void storeArenaClass(ArenaPlayer player) {
		if (!HeroesController.enabled())
			return;
		final String name = player.getName();
		if (arenaclass.containsKey(name))
			return;

		String heroClass = HeroesController.getHeroClassName(player.getPlayer());
		if (heroClass == null)
			return;
		arenaclass.put(name, heroClass);
	}

	public void restoreHeroClass(ArenaPlayer p) {
		if (!HeroesController.enabled())
			return;
		String heroClass = arenaclass.get(p.getName());
		if (heroClass == null)
			return;
		HeroesController.setHeroClass(p.getPlayer(), heroClass);
	}

//	public void setNameColor(ArenaPlayer p, ChatColor teamColor) {
//		if (!TagAPIController.enabled())
//			return;
//		TagAPIController.setNameColor(p.getPlayer(), teamColor);
//	}
//
//	public void removeNameColor(ArenaPlayer p) {
//		if (!TagAPIController.enabled() || !p.isOnline())
//			return;
//		TagAPIController.removeNameColor(p.getPlayer());
//	}

	public void cancelExpLoss(ArenaPlayer p, boolean cancel) {
		if (!HeroesController.enabled())
			return;
		HeroesController.cancelExpLoss(p.getPlayer(),cancel);
	}

	public static PlayerStoreController getPlayerStoreController() {
		return INSTANCE;
	}

}
