package mc.alk.arena.controllers;

import java.util.HashMap;
import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.serializers.InventorySerializer;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PermissionsUtil;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerStoreController {

	/**
	 * Thinking aloud.
	 * Store the experience, no problem
	 * Restoring
	 * case1: player in match, dies : should restore his exp
	 * case2: player in match, quits,
	 * 		Will that person come back into match if they come back?
	 * 			a) yes: dont do anything
	 * 			b) no: set a flag to restore when they join mc again
	 * case3: match ends
	 * 		are the players still in match, gone, or offline
	 * 			a) inmatch: restore
	 * 			b) outofmatch: do nothing
	 * 			c) offline &&
	 */
	final HashMap <String, Integer> expmap = new HashMap<String,Integer>();
	final HashMap <String, PInv> itemmap = new HashMap<String,PInv>();
	final HashMap <String, GameMode> gamemode = new HashMap<String,GameMode>();
	final HashMap <String, String> arenaclass = new HashMap<String,String>();


	@SuppressWarnings("deprecation")
	public void storeExperience(ArenaPlayer player) {
		Player p = player.getPlayer();
		int exp = ExpUtil.getTotalExperience(p);
		final String name = p.getName();
//		FileLogger.log("storing exp for = " + p.getName() +" exp="+ exp +"   online=" + p.isOnline() +"   isdead=" +p.isDead());
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
//		FileLogger.log("restoring exp for = "+p.getName()+" exp="+exp+",curexp="+p.getPlayer().getTotalExperience()+",online=" + p.isOnline() +"   isdead=" +p.isDead());
		if (p.isOnline() && !p.isDead()){
			ExpUtil.giveExperience(p.getPlayer(), exp);
		} else {
			BAPlayerListener.restoreExpOnReenter(p.getName(), exp);
		}
	}

	public void storeItems(ArenaPlayer player) {
		final Player p = player.getPlayer();
		final String name = p.getName();
		if (Defaults.DEBUG_STORAGE)  System.out.println("storing items for = " + p.getName() +" contains=" + itemmap.containsKey(name));

		if (itemmap.containsKey(name))
			return;
		InventoryUtil.closeInventory(p);
		final PInv pinv = new PInv(player.getInventory());
		itemmap.put(name, pinv);
		InventorySerializer.saveInventory(name,pinv);
	}

	public void restoreItems(ArenaPlayer p) {
		if (Defaults.DEBUG_STORAGE)  System.out.println("   "+p.getName()+" psc contains=" + itemmap.containsKey(p.getName()) +"  dead=" + p.isDead()+" online=" + p.isOnline());
		final PInv pinv = itemmap.remove(p.getName());
		if (pinv == null)
			return;
		setInventory(p, pinv);
	}

	public static void setInventory(ArenaPlayer p, PInv pinv) {
		if (Defaults.DEBUG_STORAGE) Log.info("restoring items for " + p.getName() +" = "+" o="+p.isOnline() +"  dead="+p.isDead());
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

	public void allowEntry(ArenaPlayer p, String regionWorld, String region) {
		if (!WorldGuardInterface.hasWorldGuard()){
			return;}
		WorldGuardInterface.allowEntry(p.getPlayer(), regionWorld, region);
	}

	public void addMember(ArenaPlayer p, String regionWorld, String region) {
		WorldGuardInterface.addMember(p.getName(), regionWorld, region);
	}
	public void removeMember(ArenaPlayer p, String regionWorld, String region) {
		WorldGuardInterface.removeMember(p.getName(), regionWorld, region);
	}

	public void storeArenaClass(ArenaPlayer p) {
		if (!HeroesInterface.enabled())
			return;
		String heroClass = HeroesInterface.getHeroClassName(p.getPlayer());
		if (heroClass == null)
			return;
		arenaclass.put(p.getName(), heroClass);
	}

	public void restoreHeroClass(ArenaPlayer p) {
		if (!HeroesInterface.enabled())
			return;
		String heroClass = arenaclass.get(p.getName());
		if (heroClass == null)
			return;
		HeroesInterface.setHeroClass(p.getPlayer(), heroClass);
	}

	public void setNameColor(ArenaPlayer p, ChatColor teamColor) {
		if (!TagAPIInterface.enabled())
			return;
		TagAPIInterface.setNameColor(p.getPlayer(), teamColor);
	}

	public void removeNameColor(ArenaPlayer p) {
		if (!TagAPIInterface.enabled())
			return;
		TagAPIInterface.removeNameColor(p.getPlayer());
	}
}
