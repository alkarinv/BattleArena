package mc.alk.arena.competition.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.WorldGuardInterface;
import mc.alk.arena.events.PlayerLeftEvent;
import mc.alk.arena.events.matches.MatchClassSelectedEvent;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.PVPState;
import mc.alk.arena.objects.TransitionOptions;
import mc.alk.arena.objects.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.DisabledCommandsUtil;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import com.alk.util.Log;


public class ArenaMatch extends Match {
	private HashMap<String, Long> userTime = new HashMap<String, Long>();

	public ArenaMatch(Arena arena, MatchParams mp) {
		super(arena, mp);
	}

	@MatchEventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		ArenaPlayer player = BattleArena.toArenaPlayer(event.getPlayer());
		if (woolTeams)
			BAPlayerListener.clearWoolOnReenter(player.getName(), teams.indexOf(getTeam(player)));
		/// If they are just in the arena waiting for match to start, or they havent joined yet
		if (state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL ||
				state == MatchState.ONOPEN || !insideArena.contains(player.getName())){
			return;}
		///TODO Should they be killed when they come back for this trangression?
		Team t = getTeam(player);
		t.killMember(player);
		PerformTransition.transition(this, MatchState.ONCOMPLETE, player, t, true);
		notifyListeners(new PlayerLeftEvent(player));
	}

	@MatchEventHandler(suppressCastWarnings=true)
	public void onPlayerDeath(PlayerDeathEvent event, ArenaPlayer target){
		//		System.out.println(this+"!!!!! onPlayerDeath = " + target.getName() + "  complete=" +matchResult.matchComplete()  +
		//				": inside=" + insideArena.contains(target.getName()) +"   clearInv?=" + clearsInventoryOnDeath +"   " + isWon());
		if (state == MatchState.ONCANCEL || state == MatchState.ONCOMPLETE || !insideArena.contains(target.getName())){
			return;}

		Team t = getTeam(target);
		/// Handle Drops from bukkitEvent
		if (clearsInventoryOnDeath){ /// Very important for deathmatches.. otherwise tons of items on floor
			try {event.getDrops().clear();} catch (Exception e){}
		} else if (woolTeams){  /// Get rid of the wool from teams so it doesnt drop
			int color = teams.indexOf(t);
			//				System.out.println("resetting wool team " + target.getName() +" color  ");
			List<ItemStack> items = event.getDrops();
			for (ItemStack is : items){
				if (is.getType() == Material.WOOL && color == is.getData().getData()){
					final int amt = is.getAmount();
					if (amt > 1)
						is.setAmount(amt-1);
					else
						is.setType(Material.AIR);
					break;
				}
			}
		}
		if (!respawns){
			PerformTransition.transition(this, MatchState.ONCOMPLETE, target, t, true);
		}
	}

	@MatchEventHandler(suppressCastWarnings=true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (Defaults.DEBUG_DAMAGE) {Log.info("BA onEntityDamage : ");}
		if (!(event.getEntity() instanceof Player))
			return;
		ArenaPlayer target = BattleArena.toArenaPlayer((Player) event.getEntity());
		if (Defaults.DEBUG_DAMAGE) {Log.info("BA onEntityDamage: "+target);}
		TransitionOptions to = tops.getOptions(state);
		if (to == null)
			return;
		final PVPState pvp = to.getPVP();
		if (pvp == null)
			return;
		if (pvp == PVPState.INVINCIBLE){
			if (Defaults.DEBUG_DAMAGE) {Log.info("BA onEntityDamage: "+target.getName()+ "  " + pvp + " ");}
			/// all damage is cancelled
			target.setFireTicks(0);
			event.setDamage(0);
			event.setCancelled(true);
			return;
		}
		if (!(event instanceof EntityDamageByEntityEvent)){
			return;}

		Entity damagerEntity = ((EntityDamageByEntityEvent)event).getDamager();

		ArenaPlayer damager=null;
		switch(pvp){
		case ON:
			Team targetTeam = getTeam(target);
			if (targetTeam == null || !targetTeam.hasAliveMember(target)) /// We dont care about dead players
				return;
			damager = DmgDeathUtil.getPlayerCause(damagerEntity);
			if (damager == null){ /// damage from some source, its not pvp though. so we dont care
				return;}
			Team t = getTeam(damager);
			if (Defaults.DEBUG_DAMAGE) {Log.info("BA onEntityDamage: "+target.getName()+ " pvp=" + pvp + " t=" +
					t +" " +(t!=null ? (t.getDisplayName()+":-:"+t.hasMember(target))  : ""));}

			if (t != null && t.hasMember(target)){ /// attacker is on the same team
				event.setCancelled(true);
			} else {/// different teams... lets make sure they can actually hit
				event.setCancelled(false);
			}
			break;
		case OFF:
			damager = DmgDeathUtil.getPlayerCause(damagerEntity);
			if (Defaults.DEBUG_DAMAGE) {Log.info("BA onEntityDamage: "+target.getName()+ " pvp=" + pvp +"  dmger="+damager);}
			if (damager != null){ /// damage done from a player
				event.setDamage(0);
				event.setCancelled(true);
			}
			break;
		default:
			break;
		}
	}

	@MatchEventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event, final ArenaPlayer p){
		if (isWon()){
			return;}
		final TransitionOptions mo = tops.getOptions(MatchState.ONDEATH);
		if (mo == null)
			return;
		if (respawns){
			Location loc = getTeamSpawn(getTeam(p), mo.randomRespawn());
			event.setRespawnLocation(loc);
			/// For some reason, the player from onPlayerRespawn Event isnt the one in the main thread, so we need to
			/// resync before doing any effects
			final Match am = this;
			Plugin plugin = BattleArena.getSelf();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					try{
						PerformTransition.transition(am, MatchState.ONDEATH, p, getTeam(p), false);
						PerformTransition.transition(am, MatchState.ONSPAWN, p, getTeam(p), false);
						if (woolTeams){
							Team t= getTeam(p);
							TeamUtil.setTeamHead(teams.indexOf(t), t);
						}
					} catch(Exception e){}
					if (respawnsWithClass){
						try{
							if (p.getChosenClass() != null){
								ArenaClass ac = p.getChosenClass();
								ArenaClassController.giveClass(p.getPlayer(), ac);
							}
						} catch(Exception e){}
					} else {
						p.setChosenClass(null);
					}
				}
			});
		} else { /// This player is now out of the system now that we have given the ondeath effects
			Location l = oldlocs.get(p.getName());
			if (l != null)
				event.setRespawnLocation(l);
			stopTracking(p);
		}
	}

	@MatchEventHandler
	public void onPlayerBlockBreak(BlockBreakEvent event){
		TransitionOptions to = tops.getOptions(state);
		if (to==null)
			return;
		if (to.blockBreakOff() == true){
			event.setCancelled(true);
		}
	}

	@MatchEventHandler
	public void onPlayerBlockPlace(BlockPlaceEvent event){
		TransitionOptions to = tops.getOptions(state);
		if (to==null)
			return;
		if (to.blockPlaceOff() == true){
			event.setCancelled(true);
		}
	}

	@MatchEventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		TransitionOptions to = tops.getOptions(state);
		if (to==null)
			return;
		if (to.hasOption(TransitionOption.DROPITEMOFF)){
			event.setCancelled(true);
		}
	}

	@MatchEventHandler
	public void onPlayerInventoryClick(InventoryClickEvent event, ArenaPlayer p) {
		if (woolTeams && event.getSlot() == 39/*Helm Slot*/)
			event.setCancelled(true);
	}

	@MatchEventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		TransitionOptions to = tops.getOptions(state);
		if (to==null)
			return;
		if (to.hasOption(TransitionOption.WGNOLEAVE) && WorldGuardInterface.hasWorldGuard()){
			/// Did we actually even move
			if (event.getFrom().getBlockX() != event.getTo().getBlockX()
					|| event.getFrom().getBlockY() != event.getTo().getBlockY()
					|| event.getFrom().getBlockZ() != event.getTo().getBlockZ() &&
					Bukkit.getWorld(arena.getRegionWorld()).getUID() == event.getTo().getWorld().getUID()) {
				if (WorldGuardInterface.isLeavingArea(event.getFrom(), event.getTo(), Bukkit.getWorld(arena.getRegionWorld()), arena.getRegion())){
					event.setCancelled(true);}
			}
		}
	}


	@MatchEventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (event.isCancelled() || state == MatchState.ONCOMPLETE || state == MatchState.ONCANCEL){
			return;}
		final Player p = event.getPlayer();
		if (event.getPlayer().isOp())
			return;

		String msg = event.getMessage();
		final int index = msg.indexOf(' ');
		if (index != -1){
			msg = msg.substring(0, index);
		}
		if(DisabledCommandsUtil.contains(msg)){
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED+"You cannot use that command when you are in a match");
		}
	}

	@MatchEventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.isCancelled())
			return;
		/// Check to see if it's a sign
		final Material m = event.getClickedBlock().getType();
		if (!(m.equals(Material.SIGN) || m.equals(Material.SIGN_POST)||m.equals(Material.WALL_SIGN))){ /// Only checking for signs
			return;}
		/// Get our sign
		final Sign sign = (Sign) event.getClickedBlock().getState();
		/// Check to see if sign has correct format (is more efficient than doing string manipulation for getting the )
		if (!sign.getLine(0).matches("^.[0-9a-fA-F]\\*.*$")){
			return;}

		Action action = event.getAction();
		if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK){
			return;}
		if (action == Action.LEFT_CLICK_BLOCK){ /// Dont let them break the sign
			event.setCancelled(true);}

		ArenaClass ac = ArenaClassController.getClass(MessageUtil.decolorChat(sign.getLine(0)).replace('*',' ').trim());
		if (ac == null) /// Not a valid class sign
			return;

		final Player p = event.getPlayer();
		if (!p.hasPermission("arena.class.use."+ac.getName().toLowerCase())){
			MessageUtil.sendMessage(p, "&cYou don't have permissions to use the &6 "+ac.getName()+"&c class!");
			return;
		}

		final ArenaPlayer ap = BattleArena.toArenaPlayer(p);
		ArenaClass chosen = ap.getChosenClass();
		if (chosen != null && chosen.getName().equals(ac.getName())){
			MessageUtil.sendMessage(p, "&cYou already are a &6" + ac.getName());
			return;
		}
		String playerName = p.getName();
		if(userTime.containsKey(playerName)){
			if((System.currentTimeMillis() - userTime.get(playerName)) < 3000){
				MessageUtil.sendMessage(p, "&cYou must wait &63&c seconds between class selects");
				return;
			}
		}

		userTime.put(playerName, System.currentTimeMillis());

		final TransitionOptions mo = tops.getOptions(state);
		if (mo == null)
			return;
		/// Have They have already selected a class this match, have they changed their inventory since then?
		/// If so, make sure they can't just select a class, drop the items, then choose another
		if (chosen != null){
			List<ItemStack> items = new ArrayList<ItemStack>();
			if (chosen.getItems()!=null)
				items.addAll(chosen.getItems());
			if (mo.hasItems()){
				items.addAll(mo.getGiveItems());}
			if (!InventoryUtil.sameItems(items, p.getInventory(), woolTeams)){
				MessageUtil.sendMessage(p,"&cYou can't swich classes after changing items!");
				return;
			}
		}
		notifyListeners(new MatchClassSelectedEvent(this,ac));

		/// Clear their inventory first, then give them the class and whatever items were due to them from the config
		InventoryUtil.clearInventory(p, woolTeams);
		/// Also debuff them
		EffectUtil.deEnchantAll(p);

		/// Regive class/items
		ArenaClassController.giveClass(p, ac);
		if (mo.hasItems()){
			try{ InventoryUtil.addItemsToInventory(p, mo.getGiveItems(), true);} catch(Exception e){e.printStackTrace();}}

		/// Deal with effects/buffs
		List<PotionEffect> effects = mo.getEffects();
		if (effects != null){
			EffectUtil.enchantPlayer(p, effects);}

		ap.setChosenClass(ac);
		MessageUtil.sendMessage(p, "&2You have chosen the &6"+ac.getName());
	}
}
