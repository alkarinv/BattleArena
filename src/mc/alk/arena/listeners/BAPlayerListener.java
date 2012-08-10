package mc.alk.arena.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.PlayerStoreController.PInv;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.util.FileLogger;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.alk.virtualPlayer.VirtualPlayers;
import com.alkmoeba.havockits.GiveKitEvent;


/**
 * 
 * @author alkarin
 *
 */
public class BAPlayerListener implements Listener  {
	public static final HashSet<String> disabled = 
			new HashSet<String>(Arrays.asList( "/home", "/spawn", "/trade", "/paytrade", "/payhome", 
					"/warp","/watch", "/sethome","/inf", "/va","/survival","/ma","/mob","/ctp","/chome","/csethome"));
	public static HashSet<String> die = new HashSet<String>();
	public static HashSet<String> clearInventory= new HashSet<String>();
	public static HashMap<String,Integer> clearWool= new HashMap<String,Integer>();
	public static HashMap<String,Location> tp = new HashMap<String,Location>();

	public static HashMap<Player,Integer> expRestore = new HashMap<Player,Integer>();
	public static HashMap<Player,PInv> itemRestore = new HashMap<Player,PInv>();
	public static HashMap<Player,String> messagesOnRespawn = new HashMap<Player,String>();

	BattleArenaController bac;

	public BAPlayerListener(BattleArenaController bac){
		this.bac = bac;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		final String name = p.getName();
		if (clearInventory.remove(name)){
			Log.warn("[BattleArena] clearing inventory for quitting during a match " + p.getName());
			for (ItemStack is: p.getInventory().getContents()){
				if (is == null || is.getType()==Material.AIR)
					continue;
				FileLogger.log("d  itemstack="+ InventoryUtil.getItemString(is));
			}
			for (ItemStack is: p.getInventory().getArmorContents()){
				if (is == null || is.getType()==Material.AIR)
					continue;
				FileLogger.log("d aitemstack="+ InventoryUtil.getItemString(is));
			}
			InventoryUtil.clearInventory(p);
		}
		playerReturned(p, null);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player p = event.getPlayer();
		playerReturned(p, event);
	}

	private void playerReturned(Player p, PlayerRespawnEvent event) {
		final String name = p.getName();
		final String msg = messagesOnRespawn.remove(p);
		if (msg != null){
			MessageController.sendMessage(p, msg);
		}
//		System.out.println(" playerReturned bukkitEvent player = " + p.getName() +"  " + bukkitEvent);

		if (die.remove(name)){
			MessageController.sendMessage(p, "&eYou have been killed by the Arena for not being online");
			p.setHealth(0);
			return;
		}
		if (tp.containsKey(name)){
			Location loc = tp.get(name);
			if (loc != null){
//								Log.err(name + " respawn loc ="+ SerializerUtil.getLocString(loc));
				if (event == null){
					TeleportController.teleport(p, tp.get(name));
				} else {
					if (Defaults.PLUGIN_MULTI_INV){ /// teleportController does this as well, so we only need to do it for respawn
						/// TeamJoinResult the multi-inv ignore this player permission node, do it for 3 ticks
						p.addAttachment(BattleArena.getSelf(), Defaults.MULTI_INV_IGNORE_NODE, true, 3);
					}
					event.setRespawnLocation(tp.get(name));
				}
			} else { /// this is bad, how did they get a null tp loc
				Log.err(name + " respawn loc =null");
			}
			tp.remove(name);
		}

		if (expRestore.containsKey(p)){
			final int exp = expRestore.remove(p);
			//			System.out.println("restoring exp to " +p.getName()+"   exp="+ exp);
			Plugin plugin = BattleArena.getSelf();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					Player pl = Bukkit.getPlayerExact(name);
					if (pl != null){
						//						pl.setTotalExperience(exp);	
						pl.giveExp(exp);
					}

				}
			});
		}
		if (itemRestore.containsKey(p)){
			Plugin plugin = BattleArena.getSelf();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
//					System.out.println("restoring items to " + name);
					Player pl;
					if (Defaults.DEBUG_VIRTUAL){ pl = VirtualPlayers.getPlayer(name);} 
					else {pl = Bukkit.getPlayer(name);}
					if (pl != null){
						PInv pinv = itemRestore.remove(pl);
						PlayerStoreController.setInventory(pl, pinv);
					}
				}
			});
		}
		/// TODO fix the teamHeads removal
//		if (clearWool.containsKey(name)){
//			int s = clearWool.remove(name);
//			p.getInventory().remove(new ItemStack(Material.WOOL,0,(short) s));
//		}
	}

	public static void killOnReenter(Player p, boolean wipeInventory) {
		if (wipeInventory) clearInventory.add(p.getName());
		die.add(p.getName());
	}
	public static void teleportOnReenter(Player p, Location loc, boolean wipeInventory) {
		if (wipeInventory) clearInventory.add(p.getName());
		tp.put(p.getName(),loc);
	}

	public static void addMessageOnReenter(Player p, String string) {
		messagesOnRespawn.put(p, string);
	}
	public static void restoreExpOnReenter(Player p, Integer f) {
		if (expRestore.containsKey(p)){
			f += expRestore.get(p);}
		expRestore.put(p, f);
	}

	public static void restoreItemsOnReenter(Player p, PInv pinv) {
		itemRestore.put(p,pinv);
	}

	public static void clearWoolOnReenter(Player p, int color) {
		if (p==null || color == -1)
			return;
		clearWool.put(p.getName(), color);
	}
}
