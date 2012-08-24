package mc.alk.arena.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.controllers.WorldGuardInterface;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.TransitionOptions;
import mc.alk.arena.objects.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.EffectUtil.EffectWithArgs;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.alk.massDisguise.MassDisguise;

public class PerformTransition {

	static Random rand = new Random();
	public static boolean debug = false;

	/**
	 * Perform a transition 
	 * @param Match, which match to perform the transition on
	 * @param transition: which transition are we doing
	 * @param inEvent: which inEvent to affect
	 * @param onlyInArena: only perform the actions on people still in the arena
	 */
	public static void transition(Match am, MatchState transition, Collection<Team> teams, boolean onlyInArena){
		if (teams == null)
			return;
		for (Team t: teams)
			transition(am, transition,t, onlyInArena);
	}

	public static boolean transition(Match am, final MatchState transition, Team team, boolean onlyInMatch) {
		final Set<ArenaPlayer> validPlayers = team.getPlayers();
		//		System.out.println(transition +"  " + team +"      arena=" + arena);
		final TransitionOptions mo = am.tops.getOptions(transition);
		//		System.out.println("doing effects for " + transition + "  " + team.getName() + "  " + mo);
		if (mo == null)
			return true;

		/// Check for requirements if we are going to teleport in, or they are joining
		final boolean teleportIn = mo.shouldTeleportIn();
		if (transition == MatchState.ONJOIN || teleportIn){
			Set<ArenaPlayer> stillAlive = am.checkReady(team,mo);
			validPlayers.retainAll(stillAlive);
		}

		/// Alas no players
		if (validPlayers.isEmpty())
			return false;

		for (ArenaPlayer p : validPlayers){
			transition(am, transition,p,team, onlyInMatch);
		}
		return true;
	}

	public static boolean transition(final Match am, final MatchState transition, final ArenaPlayer p, 
			final Team team, final boolean onlyInMatch) {
		if (debug) System.out.println("transition "+am.arena.getName()+"  " + transition + " p= " +p.getName() +
				" ops="+am.tops.getOptions(transition));
		//		FileLogger.log("doMatchTransitionEffects " + transition + " Player= " +p.getName() +"  am="+am +"   arena=" + am.arena +" pvp="+am.moc.getOptions(transition));
		final TransitionOptions mo = am.tops.getOptions(transition);
		if (mo == null){
			return true;}
		final boolean teleportIn = mo.shouldTeleportIn();
		final boolean teleportWaitRoom = mo.shouldTeleportWaitRoom();

		final boolean insideArena = am.insideArena(p);
		/// If the flag onlyInMatch is set, we should leave if the player isnt inside.  disregard if we are teleporting people in
		if (!(teleportIn || teleportWaitRoom) && onlyInMatch && !insideArena){
			return true;}
		final boolean teleportOut = mo.shouldTeleportOut();

		final boolean wipeOnFirstEnter = !insideArena && mo.hasOption(TransitionOption.CLEARINVENTORYONFIRSTENTER);
		final boolean wipeInventory = mo.clearInventory() || wipeOnFirstEnter;
		List<EffectWithArgs> effects = mo.getEffects();
		final Integer health = mo.getHealth();
		final Integer hunger = mo.getHunger();
		final String disguiseAllAs = mo.getDisguiseAllAs();
		final Boolean undisguise = mo.undisguise();
		final int teamIndex = am.indexOf(team);
		/// Clear the arena region
		if (mo.shouldClearRegion() && am.getArena().hasRegion()) WorldGuardInterface.clearRegion(am.getArena().getRegion());
		final Set<ArenaPlayer> players = am.getAlivePlayers();
		final boolean dead = !p.isOnline() || p.isDead();
		if (teleportWaitRoom){ /// Teleport waiting room
			/// EnterWaitRoom is supposed to happen before the teleport in bukkitEvent, but it depends on the result of a teleport
			/// Since we cant really tell the eventual result.. do our best guess
			if (!dead) am.enterWaitRoom(p); 
			final Location l = jitter(am.getWaitRoomSpawn(teamIndex,false),team.getPlayerIndex(p));
			//			final Location l = am.getWaitRoomSpawn(teamIndex,false);
			TeleportController.teleportPlayer(p.getPlayer(), l, true, true, false,players);
		}

		/// Teleport In
		if (teleportIn && transition != MatchState.ONSPAWN){ /// only tpin, respawn tps happen elsewhere
			/// enterArena is supposed to happen before the teleport in bukkitEvent, but it depends on the result of a teleport
			/// Since we cant really tell the eventual result.. do our best guess
			if (!dead) am.enterArena(p);
			final Location l = jitter(am.getTeamSpawn(teamIndex,false),team.getPlayerIndex(p));
			//			final Location l = am.getTeamSpawn(teamIndex,false);
			TeleportController.teleportPlayer(p.getPlayer(), l, true, true, false,players);
		}
		/// Teleport out
		else if (teleportOut && insideArena){ /// Lets not teleport people out who are already out(like dead ppl)
			TeleportController.teleportPlayer(p.getPlayer(), am.oldlocs.get(p.getName()), false, false, wipeInventory,players);
			/// supposed to happen after the teleport out bukkitEvent.. so delay till next tick so we can complete this transition
			/// before starting the onLeave transition
			Plugin plugin = BattleArena.getSelf();
			if (plugin.isEnabled()){
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						am.leaveArena(p); 
					}
				});				
			} else { /// We are shutting down.. complete immediately
				am.leaveArena(p); 				
			}
			/// If players are outside of the match, but need requirements, warn them 
		} else if (transition == MatchState.ONPRESTART && !insideArena){
			/// Warn players about requirements
			if (!am.tops.playerReady(p)){
				MessageController.sendMessage(p, am.tops.getRequiredString(p,"&eRemember you still need the following"));}
		}

		/// If the player is offline, still restore their exp and items
		if (mo.restoreExperience()) { am.psc.restoreExperience(p);}
		if (mo.restoreItems()){
			if (am.woolTeams && insideArena){
				TeamUtil.removeTeamHead(teamIndex, p.getPlayer());}

			if (debug)  System.out.println("   "+transition+" transition restoring items");
			am.psc.restoreItems(p);
		}

		if (!p.isOnline()){ /// only check after the teleports, just dont give items etc
			return true;}
		if (wipeInventory){ InventoryUtil.clearInventory(p.getPlayer());}
		if (mo.storeExperience()){ am.psc.storeExperience(p);}
		if (mo.storeItems()) { am.psc.storeItems(p);}
		if (health != null) p.setHealth(health);
		if (hunger != null) p.setFoodLevel(hunger);
		try{if (mo.deEnchant() != null && mo.deEnchant()) EffectUtil.unenchantAll(p.getPlayer());} catch (Exception e){}
		if (BattleArena.md != null && undisguise != null && undisguise) {MassDisguise.undisguise(p.getPlayer());}
		if (BattleArena.md != null && disguiseAllAs != null) {MassDisguise.disguisePlayer(p.getPlayer(), disguiseAllAs);}
		if (mo.getMoney() != null) {MoneyController.add(p.getName(), mo.getMoney());}
		if (mo.getExperience() != null) {p.getPlayer().giveExp(mo.getExperience());}
		if (mo.woolTeams() && am.getParams().getMinTeamSize() >1){
			if (insideArena){
				TeamUtil.setTeamHead(teamIndex, p);}
			am.woolTeams= true;
		}
		List<ItemStack> items = null;
		if (mo.hasClasses()){
			final ArenaClass ac = getArenaClass(mo,teamIndex);
			/// Give class items and effects
			if (ac != null){
				items = new ArrayList<ItemStack>(ac.getItems());
				if (ac.getEffects() != null){
					if (effects == null) effects = ac.getEffects();
					else effects.addAll(ac.getEffects());
				}
			}
		}
		if (mo.hasItems()){
			if (items == null){
				items = new ArrayList<ItemStack>(mo.getItems());
			} else {
				items.addAll(mo.getItems());				
			}
		}

		try{if (effects != null) EffectUtil.enchantPlayer(p.getPlayer(), effects);} catch (Exception e){}
		if (items != null) {
			giveSyncedItems(transition, p, items,teamIndex, am.woolTeams, insideArena);
		}

		String prizeMsg = mo.getPrizeMsg(null);
		if (prizeMsg != null)
			MessageController.sendMessage(p,"&eYou have been given \n"+prizeMsg);
		if (teleportIn){
			transition(am, MatchState.ONSPAWN, p, team, false);
		}
		//		FileLogger.log(transition+ " --- doMatchTransitionEffects Player= " +p.getName() + "  am="+am +"   arena=" + am.arena);
		return true;
	}

	private static void giveSyncedItems(final MatchState ms, final ArenaPlayer p, final List<ItemStack> items,
			final int teamIndex,final boolean woolTeams, final boolean insideArena) {
		if (woolTeams && insideArena){
			TeamUtil.setTeamHead(teamIndex, p);}
		if (debug)  System.out.println("   "+ms+" transition giving items to " + p.getName());
		InventoryUtil.addItemsToInventory(p.getPlayer(),items,woolTeams);
	}
	private static ArenaClass getArenaClass(TransitionOptions mo, final int teamIndex) {
		Map<Integer,ArenaClass> classes = mo.getClasses();
		if (classes.containsKey(teamIndex)){
			return classes.get(teamIndex);
		} else if (classes.containsKey(ArenaClass.DEFAULT)){
			return classes.get(ArenaClass.DEFAULT);
		}
		return null;
	}
	private static Location jitter(final Location teamSpawn, int index) {
		index = index % 6;
		Location loc = teamSpawn.clone();
		loc.setY(loc.getY()+1.0);/// Try to offset for people that somehow are getting put into the floor

		switch(index){
		case 0: break;
		case 1: loc.setX(loc.getX()-1); break;
		case 2:	loc.setX(loc.getX()+1); break;
		case 3:	loc.setZ(loc.getZ()-1); break;
		case 4:	loc.setZ(loc.getZ()+1); break;
		case 5:
			loc.setX(loc.getX() + rand.nextDouble()-0.5);
			loc.setZ(loc.getZ() + rand.nextDouble()-0.5);
		}				
		return loc;
	}

}
