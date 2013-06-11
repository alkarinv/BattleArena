package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.NotifierUtil;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.ServerUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;


public class PlayerRestoreController {
	final String name;
	boolean kill = false;
	boolean clearInventory = false;
	int clearWool = -1; /// -1, or positive with wool color
	Location teleportLocation = null; /// Location to teleport to
	Location tp2 = null;
	Location lastLoc = null;

	Integer exp = null;
	Integer health = null;
	Integer hunger = null;
	Integer magic = null;
	GameMode gamemode = null;

	PInv item = null;
	PInv matchItems = null;
	List<ItemStack> removeItems = null;
	String message = null;

	public PlayerRestoreController(String name) {
		this.name = name;
	}

	public synchronized boolean handle(final Player p, PlayerRespawnEvent event) {
		if (message != null){
			handleMessage(p);}

		if (clearInventory){
			handleClearInventory(p);}

		if (kill){
			handleKill(p);
			return stillHandling();
		}

		/// Teleport players, or set respawn point
		if (tp2 != null && lastLoc != null){
			handleEnsureTeleport(p, event);}

		/// Teleport players, or set respawn point
		if (teleportLocation != null){
			handleTeleport(p, event);}

		/// Do these after teleports
		/// Restore game mode
		if (gamemode != null){
			handleGameMode();}

		/// Exp restore
		if (exp != null){
			handleExp();}

		/// Health restore
		if (health != null){
			handleHealth();}

		/// Hunger restore
		if (hunger != null){
			handleHunger();}

		/// Magic restore
		if (magic != null){
			handleMagic();}

		/// Restore Items
		if (item != null){
			handleItems();}

		/// Restore Match Items
		if (matchItems !=null){
			handleMatchItems();}

		/// Remove Items
		if (removeItems != null){
			handleRemoveItems();}
		return stillHandling();
	}

	private void handleRemoveItems() {
		final List<ItemStack> items = removeItems;
		removeItems=null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (pl != null){
					PlayerStoreController.removeItems(BattleArena.toArenaPlayer(pl), items);
				}
			}
		});
	}

	private void handleMatchItems() {
		final PInv items = matchItems;
		matchItems=null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (pl != null){
					ArenaPlayer ap = PlayerController.toArenaPlayer(pl);
					PlayerStoreController.setInventory(ap, items);
				}
			}
		});
	}

	private void handleItems() {
		final PInv items = item;
		item=null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (Defaults.DEBUG_STORAGE) System.out.println("### restoring items to " + name +"   pl = " + pl);
				if (pl != null){
					ArenaPlayer ap = PlayerController.toArenaPlayer(pl);
					PlayerStoreController.setInventory(ap, items);
				}
			}
		});
	}

	private void handleMagic() {
		final int val = magic;
		magic = null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (pl != null){
					HeroesController.setMagicLevel(pl, val);
				}
			}
		});
	}

	private void handleHunger() {
		final int val = hunger;
		hunger = null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (pl != null){
					BattleArena.toArenaPlayer(pl).setFoodLevel(val);}
			}
		});
	}

	private void handleHealth() {
		final int val = health;
		health = null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (pl != null){
					BattleArena.toArenaPlayer(pl).setHealth(val);}
			}
		});
	}

	private void handleExp() {
		final int val = exp;
		exp = null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable() {
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (pl != null){
					pl.giveExp(val);}
			}
		});
	}

	private void handleGameMode() {
		final GameMode gm = gamemode;
		gamemode = null;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
			@Override
			public void run() {
				Player pl = ServerUtil.findPlayerExact(name);
				if (pl != null){
					PlayerStoreController.setGameMode(pl, gm);}
			}
		});
	}

	private void handleTeleport(final Player p, PlayerRespawnEvent event) {
		final Location loc = teleportLocation;
		tp2 = loc;
		teleportLocation = null;
		NotifierUtil.notify("tp", name+"  respawning to loc=" + loc +"   curloc="+p.getLocation());
		if (loc != null){
			if (event == null){
				Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
					@Override
					public void run() {
						Player pl = ServerUtil.findPlayerExact(name);
						if (pl != null){
							TeleportController.teleport(pl, loc);
						} else {
							Util.printStackTrace();
						}
					}
				});
			} else {
				PermissionsUtil.givePlayerInventoryPerms(p);
				event.setRespawnLocation(loc);
				/// Set a timed event to check to make sure the player actually arrived
				/// Then do a teleport if needed
				/// This can happen on servers where plugin conflicts prevent the respawn (somehow!!!)
				if (HeroesController.enabled()){
					Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
						@Override
						public void run() {
							Player pl = ServerUtil.findPlayerExact(name);
							if (pl != null){
								if (pl.getLocation().getWorld().getUID()!=loc.getWorld().getUID() ||
										pl.getLocation().distanceSquared(loc) > 100){
									TeleportController.teleport(p, loc);
								}
							} else {
								Util.printStackTrace();
							}
						}
					},2L);
				}
			}
		} else { /// this is bad, how did they get a null tp loc
			Log.err(name + " respawn loc =null");
		}
	}

	private void handleEnsureTeleport(final Player p, PlayerRespawnEvent event) {
		final Location loc = tp2;
		final Location lastL = lastLoc;
		tp2= null;
		lastLoc = null;
		NotifierUtil.notify("tp", name+"  respawning2 to loc=" + loc +"   curloc="+p.getLocation());
		if (p.getLocation().getWorld().getUID() == lastL.getWorld().getUID() &&
				p.getLocation().distanceSquared(lastL) <=1){
			if (event == null){
				Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
					@Override
					public void run() {
						Player pl = ServerUtil.findPlayerExact(name);
						if (pl != null){
							TeleportController.teleport(pl, loc);
						} else {
							Util.printStackTrace();
						}
					}
				});
			} else {
				PermissionsUtil.givePlayerInventoryPerms(p);
				event.setRespawnLocation(loc);
				/// Set a timed event to check to make sure the player actually arrived
				/// Then do a teleport if needed
				/// This can happen on servers where plugin conflicts prevent the respawn (somehow!!!)
				if (HeroesController.enabled()){
					Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
						@Override
						public void run() {
							Player pl = ServerUtil.findPlayerExact(name);
							if (pl != null){
								if (pl.getLocation().getWorld().getUID()!=loc.getWorld().getUID() ||
										pl.getLocation().distanceSquared(loc) > 100){
									TeleportController.teleport(p, loc);
								}
							} else {
								Util.printStackTrace();
							}
						}
					},2L);
				}
			}
		} else { /// this is bad, how did they get a null tp loc
			Log.err(name + " respawn loc =null");
		}
	}

	private boolean stillHandling() {
		return clearInventory || kill ||clearWool!=-1||teleportLocation!=null || tp2 != null || lastLoc!=null||
				exp != null || health!=null || hunger!=null || magic !=null || gamemode!=null || item!=null ||
				matchItems!=null||removeItems!=null||message!=null;
	}

	private void handleKill(Player p) {
		MessageUtil.sendMessage(p, "&eYou have been killed by the Arena for not being online");
		p.setHealth(0);
	}

	private void handleMessage(Player p) {
		MessageUtil.sendMessage(p, message);
	}

	private void handleClearInventory(Player p) {
		Log.warn("[BattleArena] clearing inventory for quitting during a match " + p.getName());
		for (ItemStack is: p.getInventory().getContents()){
			if (is == null || is.getType()==Material.AIR)
				continue;
		}
		for (ItemStack is: p.getInventory().getArmorContents()){
			if (is == null || is.getType()==Material.AIR)
				continue;
		}
		InventoryUtil.clearInventory(p);
	}

	public void setKill(boolean kill) {
		this.kill=kill;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setClearInventory(boolean clearInventory) {
		this.clearInventory = clearInventory;
	}

	public void setClearWool(int clearWool) {
		this.clearWool = clearWool;
	}

	public void setTp(Location tp) {
		this.teleportLocation = tp;
	}

	public void setTp2(Location tp2) {
		this.tp2 = tp2;
	}

	public void setLastLocs(Location lastLocs) {
		this.lastLoc = lastLocs;
	}

	public void addExp(Integer exp) {
		if (this.exp == null)
			this.exp = exp;
		else
			this.exp += exp;
	}

	public void setHealth(Integer health) {
		this.health = health;
	}

	public void setHunger(Integer hunger) {
		this.hunger = hunger;
	}

	public void setMagic(Integer magic) {
		this.magic = magic;
	}

	public void setGamemode(GameMode gamemode) {
		this.gamemode = gamemode;
	}

	public void setItem(PInv item) {
		this.item = item;
	}

	public void setMatchItems(PInv matchItems) {
		this.matchItems = matchItems;
	}

	public void setRemoveItems(List<ItemStack> removeItems) {
		this.removeItems = removeItems;
	}

	public void removeMatchItems() {
		this.matchItems = null;
	}

	public void addRemoveItem(ItemStack is) {
		if (removeItems == null){
			removeItems = new ArrayList<ItemStack>();}
		removeItems.add(is);
	}

	public void addRemoveItem(List<ItemStack> itemsToRemove) {
		if (removeItems == null){
			removeItems = new ArrayList<ItemStack>();}
		removeItems.addAll(itemsToRemove);
	}

	public Location getTeleportLocation() {
		return teleportLocation;
	}
	public String getName(){
		return name;
	}

	public boolean getKill() {
		return kill;
	}

	public boolean getClearInventory() {
		return clearInventory;
	}

	public int getClearWool() {
		return clearWool;
	}

	public Location getTp2() {
		return tp2;
	}

	public Location getLastLoc() {
		return lastLoc;
	}

	public Integer getExp() {
		return exp;
	}

	public Integer getHealth() {
		return health;
	}

	public Integer getHunger() {
		return hunger;
	}

	public Integer getMagic() {
		return magic;
	}

	public GameMode getGamemode() {
		return gamemode;
	}

	public PInv getItem() {
		return item;
	}

	public PInv getMatchItems() {
		return matchItems;
	}

	public List<ItemStack> getRemoveItems() {
		return removeItems;
	}

	public String getMessage() {
		return message;
	}
}
