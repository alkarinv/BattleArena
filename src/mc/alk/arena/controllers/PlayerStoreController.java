package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.match.PerformTransition;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.FileLogger;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


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

	public static class PInv {
		ItemStack[] contents;
		ItemStack[] armor;
	}

	@SuppressWarnings("deprecation")
	public void storeExperience(ArenaPlayer player) {
		Player p = player.getPlayer();
		int exp = p.getTotalExperience();
		final String name = p.getName();
		FileLogger.log("storing exp for = " + p.getName() +" exp="+ exp +"   online=" + p.isOnline() +"   isdead=" +p.isDead());
		if (exp == 0)
			return;
		if (expmap.containsKey(name)){
			exp += expmap.get(name);}
		expmap.put(name, exp);
		p.setExp(0);
		p.setLevel(0);
		p.setTotalExperience(0);
		try{
			p.updateInventory();
		} catch(Exception e){

		}
	}

	public void restoreExperience(ArenaPlayer p) {
		if (!expmap.containsKey(p.getName()))
			return;
		Integer exp = expmap.remove(p.getName());
		FileLogger.log("restoring exp for = " + p.getName() +" exp="+ exp +"   online=" + p.isOnline() +"   isdead=" +p.isDead());
		if (p.isOnline() && !p.isDead()){
			p.getPlayer().giveExp(exp);
		} else {
			BAPlayerListener.restoreExpOnReenter(p.getName(), exp);
		}
	}

	public void storeItems(ArenaPlayer player) {
		Player p = player.getPlayer();
		final String name = p.getName();

		FileLogger.log("storing items for = " + p.getName() +" contains=" + itemmap.containsKey(name));
		if (PerformTransition.debug)  System.out.println("storing items for = " + p.getName() +" contains=" + itemmap.containsKey(name));

		if (itemmap.containsKey(name))
			return;
		PInv pinv = new PInv();
		try{
			pinv.contents = p.getInventory().getContents();
			pinv.armor = p.getInventory().getArmorContents();
			for (ItemStack is: pinv.contents){
				if (is == null || is.getType()==Material.AIR)
					continue;
				FileLogger.log("s itemstack="+ InventoryUtil.getItemString(is));
			}
			for (ItemStack is: pinv.armor){
				if (is == null || is.getType()==Material.AIR)
					continue;
				FileLogger.log("s armor itemstack="+ InventoryUtil.getItemString(is));
			}

			if (itemmap.containsKey(name)){
				PInv oldItems = itemmap.get(name);
				for (ItemStack is: oldItems.contents){
					if (is == null || is.getType()==Material.AIR)
						continue;
					FileLogger.log("olditems itemstack="+ InventoryUtil.getItemString(is));
				}
				for (ItemStack is: oldItems.armor){
					if (is == null || is.getType()==Material.AIR)
						continue;
					FileLogger.log("olditems armor itemstack="+ InventoryUtil.getItemString(is));
				}
			}
			InventoryUtil.closeInventory(p);
			p.setGameMode(GameMode.SURVIVAL);
			InventoryUtil.clearInventory(p);
		}catch (Exception e){
			e.printStackTrace();
		}
		itemmap.put(name, pinv);
	}

	public void restoreItems(ArenaPlayer p) {
		if (PerformTransition.debug)  System.out.println("   "+p.getName()+" psc conatins=" + itemmap.containsKey(p.getName()) +"  dead=" + p.isDead()+" online=" + p.isOnline());
		PInv pinv = itemmap.remove(p.getName());
		if (pinv == null)
			return;
		if (p.isOnline() && !p.isDead()){
			setInventory(p, pinv);
		} else {
			BAPlayerListener.restoreItemsOnReenter(p.getName(), pinv);
		}
	}

	@SuppressWarnings("deprecation")
	public static void setInventory(ArenaPlayer p, PInv pinv) {
		FileLogger.log("restoring items for = " + p.getName() +" = "+" o="+p.isOnline() +"  dead="+p.isDead());
		if (PerformTransition.debug)
			System.out.println("restoring items for = " + p.getName() +" = "+" o="+p.isOnline() +"  dead="+p.isDead());
		PlayerInventory inv = p.getPlayer().getInventory();
		inv.setArmorContents(pinv.armor);
		inv.setContents(pinv.contents);
		try {p.getPlayer().updateInventory(); } catch (Exception e){} /// Yes this can throw errors	
		for (ItemStack is: pinv.contents){
			if (is == null || is.getType()==Material.AIR)
				continue;
			FileLogger.log("r  itemstack="+ InventoryUtil.getItemString(is));
		}
		for (ItemStack is: pinv.armor){
			if (is == null || is.getType()==Material.AIR)
				continue;
			FileLogger.log("r aitemstack="+ InventoryUtil.getItemString(is));
		}
	}

}
