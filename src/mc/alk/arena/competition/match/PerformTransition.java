package mc.alk.arena.competition.match;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.PylamoController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.DisguiseInterface;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;


public class PerformTransition {

	static Random rand = new Random();
	//	public static boolean debug = false;

	/**
	 * Perform a transition
	 * @param Match, which match to perform the transition on
	 * @param transition: which transition are we doing
	 * @param teams: which teams to affect
	 * @param onlyInMatch: only perform the actions on people still in the arena match
	 */
	public static void transition(Match am, MatchState transition, Collection<Team> teams, boolean onlyInMatch){
		if (teams == null)
			return;
		boolean first = true;
		for (Team team: teams){
			transition(am,transition,team,onlyInMatch,first);
			first = false;
		}
	}

	public static boolean transition(Match am, final MatchState transition, Team team, boolean onlyInMatch) {
		return transition(am,transition,team,onlyInMatch,true);
	}

	static boolean transition(Match am, final MatchState transition, Team team, boolean onlyInMatch,
			boolean performOncePerTransitionOptions) {
		final TransitionOptions mo = am.tops.getOptions(transition);
		//		System.out.println("doing effects for " + transition + "  " + team.getName() + "  " + mo );
		if (mo == null)
			return true;
		if (performOncePerTransitionOptions){
			/// Options that don't affect players first
			if (WorldGuardController.hasWorldGuard() && am.getArena().hasRegion()){
				WorldGuardRegion region = am.getArena().getWorldGuardRegion();
				/// Clear the area
				if (mo.shouldClearRegion()){
					WorldGuardController.clearRegion(region);}

				if (mo.hasOption(TransitionOption.WGRESETREGION)){
					if (PylamoController.enabled() && am.getArena().getPylamoRegion() != null){
						PylamoController.resetRegion(am.getArena().getPylamoRegion());
					} else {
						WorldGuardController.pasteSchematic(region);
					}
				}
			}
		}
		for (ArenaPlayer p : team.getPlayers()){
			transition(am, transition,p,team, onlyInMatch);
		}
		return true;
	}

	static boolean transition(final Match am, final MatchState transition, final ArenaPlayer player,
			final Team team, final boolean onlyInMatch) {
		if (Defaults.DEBUG_TRANSITIONS) System.out.println("transition "+am.arena.getName()+"  " + transition + " p= " +player.getName() +
				" ops="+am.tops.getOptions(transition) +"  inArena="+am.insideArena(player) +"  left="+am.playerLeft(player));
		if (am.playerLeft(player)) /// The player has purposefully left the match, we have nothing to do with them anymore
			return true;

		final TransitionOptions mo = am.tops.getOptions(transition);
		if (mo == null){ /// no options
			return true;}
		final boolean teleportIn = mo.shouldTeleportIn();
		final boolean teleportWaitRoom = mo.shouldTeleportWaitRoom();
		final boolean insideArena = am.insideArena(player);
		/// If the flag onlyInMatch is set, we should leave if the player isnt inside.  disregard if we are teleporting people in
		if (onlyInMatch && !insideArena && !(teleportIn || teleportWaitRoom)){
			return true;}
		final boolean teleportOut = mo.shouldTeleportOut();
		final boolean wipeInventory = mo.clearInventory();
		/// People that are quiting/leaving with wipeInventory should lose their inventory
		/// even if they are "dead" or "offline"
		final boolean forceClearInventory = wipeInventory && mo.shouldTeleportOut();

		List<PotionEffect> effects = mo.getEffects()!=null ? new ArrayList<PotionEffect>(mo.getEffects()) : null;
		final Integer health = mo.getHealth();
		final Integer hunger = mo.getHunger();
		final String disguiseAllAs = mo.getDisguiseAllAs();
		final Boolean undisguise = mo.undisguise();
		final int teamIndex = am.indexOf(team);
		boolean playerReady = player.isOnline();
		final boolean dead = !player.isOnline() || player.isDead();
		final Player p = player.getPlayer();
		if (teleportWaitRoom){ /// Teleport waiting room
			if ( (insideArena || am.checkReady(player, team, mo, true)) && !dead){
				/// EnterWaitRoom is supposed to happen before the teleport in event, but it depends on the result of a teleport
				/// Since we cant really tell the eventual result.. do our best guess
				am.enterWaitRoom(player);
				final Location l = jitter(
						am.getWaitRoomSpawn(teamIndex,am.spawnsRandom),
						rand.nextInt(team.size()));
				TeleportController.teleportPlayer(p, l, false, true);
				PlayerStoreController.setGameMode(p, GameMode.SURVIVAL);
			} else {
				playerReady = false;
			}
		}

		/// Teleport In
		if (teleportIn && transition != MatchState.ONSPAWN){ /// only tpin, respawn tps happen elsewhere
			if ((insideArena || am.checkReady(player, team, mo, true)) && !dead){
				/// enterArena is supposed to happen before the teleport in Event, but it depends on the result of a teleport
				/// Since we cant really tell the eventual result.. do our best guess
				am.enterArena(player,team);
				final Location l = jitter(am.getTeamSpawn(teamIndex,false),rand.nextInt(team.size()));
				TeleportController.teleportPlayer(p, l, false, true);
				PlayerUtil.setGod(p,false);
				PlayerStoreController.setGameMode(p, GameMode.SURVIVAL);
			} else {
				playerReady = false;
			}
		}

		final boolean storeAll = mo.hasOption(TransitionOption.STOREALL);
		/// Only do if player is online options
		if (playerReady && !dead){
			if (storeAll || mo.hasOption(TransitionOption.STOREGAMEMODE)){ am.psc.storeGamemode(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREEXPERIENCE)){ am.psc.storeExperience(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREITEMS)) { am.psc.storeItems(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREHEALTH)){ am.psc.storeHealth(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREHUNGER)){ am.psc.storeHunger(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREMAGIC)){ am.psc.storeMagic(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREHEROCLASS)){am.psc.storeArenaClass(player);}
			if (wipeInventory){ InventoryUtil.clearInventory(p); }
			if (mo.hasOption(TransitionOption.CLEAREXPERIENCE)){ ExpUtil.clearExperience(p);}
			if (mo.hasOption(TransitionOption.HEALTH)) { PlayerUtil.setHealth(p, health);}
			if (mo.hasOption(TransitionOption.HEALTHP)) { PlayerUtil.setHealthP(p, mo.getHealthP());}
			if (mo.hasOption(TransitionOption.MAGIC)) { setMagicLevel(p, mo.getMagic()); }
			if (mo.hasOption(TransitionOption.MAGICP)) { setMagicLevelP(p, mo.getMagicP()); }
			if (hunger != null) { PlayerUtil.setHunger(p, hunger); }
			if (mo.hasOption(TransitionOption.INVULNERABLE)) { PlayerUtil.setInvulnerable(p,mo.getInvulnerable()); }
			if (mo.hasOption(TransitionOption.GAMEMODE)) { PlayerUtil.setGameMode(p,mo.getGameMode()); }
			if (mo.hasOption(TransitionOption.FLIGHTOFF)) { PlayerUtil.setFlight(p,false); }
			if (mo.hasOption(TransitionOption.FLIGHTON)) { PlayerUtil.setFlight(p,true); }
			if (mo.hasOption(TransitionOption.FLIGHTSPEED)) { PlayerUtil.setFlightSpeed(p,mo.getFlightSpeed()); }
			if (mo.hasOption(TransitionOption.DOCOMMANDS)) { PlayerUtil.doCommands(p,mo.getDoCommands()); }
			if (mo.deEnchant() != null && mo.deEnchant()) { deEnchant(p);}
			if (DisguiseInterface.enabled() && undisguise != null && undisguise) {DisguiseInterface.undisguise(p);}
			if (DisguiseInterface.enabled() && disguiseAllAs != null) {DisguiseInterface.disguisePlayer(p, disguiseAllAs);}
			if (mo.getMoney() != null) {MoneyController.add(player.getName(), mo.getMoney());}
			if (mo.getExperience() != null) {ExpUtil.giveExperience(p, mo.getExperience());}
			if (mo.hasOption(TransitionOption.REMOVEPERMS)){ removePerms(player, mo.getRemovePerms());}
			if (mo.hasOption(TransitionOption.ADDPERMS)){ addPerms(player, mo.getAddPerms(), 0);}
			if (mo.hasOption(TransitionOption.GIVECLASS)){
				final ArenaClass ac = getArenaClass(mo,teamIndex);
				if (ac != null){ /// Give class items and effects
					if (am.woolTeams) TeamUtil.setTeamHead(teamIndex, player); // give wool heads first
					if (am.armorTeams){
						ArenaClassController.giveClass(p, ac, TeamUtil.getTeamColor(teamIndex));
					} else{
						ArenaClassController.giveClass(p, ac);
					}
					player.setChosenClass(ac);
				}
			}
			if (mo.hasOption(TransitionOption.GIVEDISGUISE) && DisguiseInterface.enabled()){
				final String disguise = getDisguise(mo,teamIndex);
				if (disguise != null){ /// Give class items and effects
					DisguiseInterface.disguisePlayer(p, disguise);}
			}
			if (mo.hasOption(TransitionOption.GIVEITEMS)){
				Color color = am.armorTeams ? TeamUtil.getTeamColor(teamIndex) : null;
				giveItems(transition, player, mo.getGiveItems(),teamIndex, am.woolTeams, insideArena,color);}

			try{if (effects != null)
				EffectUtil.enchantPlayer(p, effects);} catch (Exception e){}

			String prizeMsg = mo.getPrizeMsg(null);
			if (prizeMsg != null)
				MessageUtil.sendMessage(player,"&eYou have been given \n"+prizeMsg);
			if (teleportIn){
				transition(am, MatchState.ONSPAWN, player, team, false);
			}
		} else if (forceClearInventory){
			InventoryUtil.clearInventory(p);
		}

		/// Teleport out, need to do this at the end so that all the onCancel/onComplete options are completed first
		if (teleportOut ){ /// Lets not teleport people out who are already out(like dead ppl)
			Location loc = null;
			if (mo.hasOption(TransitionOption.TELEPORTTO))
				loc = mo.getTeleportToLoc();
			else if (mo.hasOption(TransitionOption.TELEPORTONARENAEXIT))
				loc = mo.getTeleportToLoc();
			else
				loc = am.oldlocs.get(player.getName());
			if (loc == null){
				Log.err("[BA Error] Teleporting to a null location!  teleportTo=" + mo.hasOption(TransitionOption.TELEPORTTO)+
						", teleportOnArenaExit="+mo.hasOption(TransitionOption.TELEPORTONARENAEXIT));
			} else if (insideArena || !onlyInMatch){
				TeleportController.teleportPlayer(p, loc, wipeInventory, true);
				am.leaveArena(player);
			}
			/// If players are outside of the match, but need requirements, warn them
		} else if (transition == MatchState.ONPRESTART && !insideArena){
			World w = am.getArena().getSpawnLoc(0).getWorld();
			/// Warn players about requirements
			if (!am.tops.playerReady(player, w)){
				MessageUtil.sendMessage(player, am.tops.getRequiredString(player,w,"&eRemember you still need the following"));}
		}
		/// Restore their exp and items.. Has to happen AFTER teleport
		boolean restoreAll = mo.hasOption(TransitionOption.RESTOREALL);
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREGAMEMODE)){ am.psc.restoreGamemode(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREEXPERIENCE)) { am.psc.restoreExperience(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREITEMS)){
			if (am.woolTeams && teamIndex != -1){
				/// Teams that have left can have a -1 teamIndex
				TeamUtil.removeTeamHead(teamIndex, p);
			}
			if (Defaults.DEBUG_TRANSITIONS)System.out.println("   "+transition+" transition restoring items "+insideArena);
			am.psc.restoreItems(player);
		}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREHEALTH)){ am.psc.restoreHealth(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREHUNGER)){ am.psc.restoreHunger(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREMAGIC)) { am.psc.restoreMagic(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREHEROCLASS)){am.psc.restoreHeroClass(player);}
		return true;
	}

	private static void deEnchant(Player p) {
		try{ EffectUtil.deEnchantAll(p);} catch (Exception e){}
		HeroesController.deEnchant(p);
	}

	private static void setMagicLevel(Player p, Integer magic) {
		HeroesController.setMagicLevel(p, magic);
	}

	private static void setMagicLevelP(Player p, Integer magic) {
		HeroesController.setMagicLevelP(p, magic);
	}
	private static void removePerms(ArenaPlayer p, List<String> perms) {
		if (perms == null || perms.isEmpty())
			return;
		/// TODO complete
	}

	private static void addPerms(ArenaPlayer p, List<String> perms, int ticks) {
		if (perms == null || perms.isEmpty())
			return;
		PermissionAttachment attachment = p.getPlayer().addAttachment(BattleArena.getSelf(),ticks);
		for (String perm: perms){
			attachment.setPermission(perm, true);}
	}

	private static void giveItems(final MatchState ms, final ArenaPlayer p, final List<ItemStack> items,
			final int teamIndex,final boolean woolTeams, final boolean insideArena, Color color) {
		if (woolTeams && insideArena){
			TeamUtil.setTeamHead(teamIndex, p);}
		if (Defaults.DEBUG_TRANSITIONS)System.out.println("   "+ms+" transition giving items to " + p.getName());
		if (items == null || items.isEmpty())
			return;
		InventoryUtil.addItemsToInventory(p.getPlayer(),items,woolTeams,color);
	}

	private static ArenaClass getArenaClass(TransitionOptions mo, final int teamIndex) {
		Map<Integer,ArenaClass> classes = mo.getClasses();
		if (classes == null)
			return null;
		if (classes.containsKey(teamIndex)){
			return classes.get(teamIndex);
		} else if (classes.containsKey(ArenaClass.DEFAULT)){
			return classes.get(ArenaClass.DEFAULT);
		}
		return null;
	}

	private static String getDisguise(TransitionOptions mo, final int teamIndex) {
		Map<Integer,String> disguises = mo.getDisguises();
		if (disguises.containsKey(teamIndex)){
			return disguises.get(teamIndex);
		} else if (disguises.containsKey(DisguiseInterface.DEFAULT)){
			return disguises.get(DisguiseInterface.DEFAULT);
		}
		return null;
	}


	private static Location jitter(final Location teamSpawn, int index) {
		if (index == 0)
			return teamSpawn;
		index = index % 6;
		Location loc = teamSpawn.clone();

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
