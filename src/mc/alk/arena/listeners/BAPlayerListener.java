package mc.alk.arena.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.PlayerStoreController.PInv;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.FileLogger;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.alk.virtualPlayer.VirtualPlayers;


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

	public static HashMap<String,Integer> expRestore = new HashMap<String,Integer>();
	public static HashMap<String,PInv> itemRestore = new HashMap<String,PInv>();
	public static HashMap<String,GameMode> gamemodeRestore = new HashMap<String,GameMode>();
	public static HashMap<String,String> messagesOnRespawn = new HashMap<String,String>();

	BattleArenaController bac;

	public BAPlayerListener(BattleArenaController bac){
		die.clear();
		clearInventory.clear();
		clearWool.clear();
		tp.clear();
		expRestore.clear();
		itemRestore.clear();
		gamemodeRestore.clear();
		messagesOnRespawn.clear();
		this.bac = bac;
	}

	/**
	 * 
	 * Why priority.HIGHEST: if an exception happens after we have already set their respawn location, 
	 * they relog in at a separate time and will not get teleported to the correct place.
	 * As a workaround, try to handle this event last.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
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
		final String msg = messagesOnRespawn.remove(p.getName());
		if (msg != null){
			MessageUtil.sendMessage(p, msg);
		}
		//		System.out.println(" playerReturned event player = " + p.getName() +"  " + event +"   " + itemRestore.containsKey(p.getName()));

		if (die.remove(name)){
			MessageUtil.sendMessage(p, "&eYou have been killed by the Arena for not being online");
			p.setHealth(0);
			return;
		}
		if (gamemodeRestore.containsKey(name)){
			PlayerStoreController.restoreGameMode(p, gamemodeRestore.remove(name));
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

		if (expRestore.containsKey(p.getName())){
			final int exp = expRestore.remove(p.getName());
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
		if (itemRestore.containsKey(p.getName())){
			Plugin plugin = BattleArena.getSelf();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					Player pl;
					if (Defaults.DEBUG_VIRTUAL){ pl = VirtualPlayers.getPlayer(name);} 
					else {pl = Bukkit.getPlayer(name);}
					//					System.out.println("restoring items to " + name +"   pl = " + pl);
					if (pl != null){

						PInv pinv = itemRestore.remove(pl.getName());
						ArenaPlayer ap = PlayerController.toArenaPlayer(pl);
						PlayerStoreController.setInventory(ap, pinv);
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

	public static void killOnReenter(String playerName) {
		die.add(playerName);
	}
	public static void teleportOnReenter(String playerName, Location loc) {
		tp.put(playerName,loc);
	}

	public static void addMessageOnReenter(String playerName, String string) {
		messagesOnRespawn.put(playerName, string);
	}
	public static void restoreExpOnReenter(String playerName, Integer f) {
		if (expRestore.containsKey(playerName)){
			f += expRestore.get(playerName);}
		expRestore.put(playerName, f);
	}

	public static void restoreItemsOnReenter(String playerName, PInv pinv) {
		itemRestore.put(playerName,pinv);
	}

	public static void clearWoolOnReenter(String playerName, int color) {
		if (playerName==null || color == -1)
			return;
		clearWool.put(playerName, color);
	}

	public static void restoreGameModeOnEnter(String playerName, GameMode gamemode) {
		gamemodeRestore.put(playerName, gamemode);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event){
		if (Defaults.DEBUG_TRACE) System.out.println("onSignChange Event");
		final Block block = event.getBlock();
		final Material type = block.getType();

		if (!(type.equals(Material.SIGN) || type.equals(Material.SIGN_POST) || type.equals(Material.WALL_SIGN))) {
			return;}

		Player p = event.getPlayer();

		/// Is the sign a trade sign?
		final boolean admin = p.isOp() || p.hasPermission(Defaults.ARENA_ADMIN);
		String lines[] = event.getLines();
		ArenaClass ac = ArenaClassController.getClass(MessageUtil.decolorChat(lines[0]).replaceAll("\\*", ""));
		if (ac == null)
			return;
		for (int i=1;i<lines.length;i++){
			if (!lines[i].isEmpty()) /// other text, not our sign
				return;
		}

		if (!admin){
			cancelSignPlace(event,block);
			return;
		}

		try{
			event.setLine(0, MessageUtil.colorChat(ChatColor.GOLD+"*"+ac.getPrettyName()));
		} catch (Exception e){
			MessageUtil.sendMessage(p, "&cError creating Arena Class Sign");
			e.printStackTrace();
			cancelSignPlace(event,block);
			return;
		}
	}

	public static void cancelSignPlace(SignChangeEvent event, Block block){
		event.setCancelled(true);
		block.setType(Material.AIR);
		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));   	
	}
}
