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
import mc.alk.arena.controllers.ArenaController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.controllers.LobbyController;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.PylamoController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaLocation;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.TeleportDirection;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.objects.teams.ArenaTeam;
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
	public static void transition(Match am, MatchState transition, Collection<ArenaTeam> teams, boolean onlyInMatch){
		if (teams == null)
			return;
		boolean first = true;
		for (ArenaTeam team: teams){
			transition(am,transition,team,onlyInMatch,first);
			first = false;
		}
	}

	public static boolean transition(PlayerHolder am, final MatchState transition, ArenaTeam team, boolean onlyInMatch) {
		return transition(am,transition,team,onlyInMatch,true);
	}

	static boolean transition(PlayerHolder am, final MatchState transition, ArenaTeam team, boolean onlyInMatch,
			boolean performOncePerTransitionOptions) {
//		final TransitionOptions mo = am.tops.getOptions(transition);
		final TransitionOptions mo = am.getParams().getTransitionOptions().getOptions(transition);
		//		System.out.println("doing effects for " + transition + "  " + team.getName() + "  " + mo );
		if (mo == null)
			return true;
		if (performOncePerTransitionOptions && (am instanceof ArenaController)){
			ArenaController ac = (ArenaController) am;
			/// Options that don't affect players first
			if (WorldGuardController.hasWorldGuard() && ac.getArena() != null && ac.getArena().hasRegion()){
				WorldGuardRegion region = ac.getArena().getWorldGuardRegion();
				/// Clear the area
				if (mo.shouldClearRegion()){
					WorldGuardController.clearRegion(region);}

				if (mo.hasOption(TransitionOption.WGRESETREGION)){
					if (PylamoController.enabled() && ac.getArena().getPylamoRegion() != null){
						PylamoController.resetRegion(ac.getArena().getPylamoRegion());
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

//	static boolean transition(final Match am, final MatchState transition, final ArenaPlayer player,
//			final ArenaTeam team, final boolean onlyInMatch) {
//		if (am.playerLeft(player)) /// The player has purposefully left the match, we have nothing to do with them anymore
//			return true;
//		return transition(am,transition,player,team, onlyInMatch);
//	}

	public static boolean transition(final PlayerHolder am, final MatchState transition,
			final ArenaPlayer player, final ArenaTeam team, final boolean onlyInMatch) {
		if (Defaults.DEBUG_TRANSITIONS) Log.debug("-- transition "+am.getClass().getSimpleName()+"  " + transition + " p= " +player.getName() +
				" ops="+am.getParams().getTransitionOptions().getOptions(transition)
				+"  inArena="+am.isHandled(player) + "   clearInv=" +
				am.getParams().getTransitionOptions().hasOptionAt(transition, TransitionOption.CLEARINVENTORY));

		final boolean insideArena = am.isHandled(player);
		final TransitionOptions mo = am.getParams().getTransitionOptions().getOptions(transition);
		if (mo == null){ /// no options
			return true;}
		final boolean teleportIn = mo.shouldTeleportIn();
		final boolean teleportWaitRoom = mo.shouldTeleportWaitRoom();
		final boolean teleportLobby = mo.shouldTeleportLobby();
		/// If the flag onlyInMatch is set, we should leave if the player isnt inside.  disregard if we are teleporting people in
		if (onlyInMatch && !insideArena && !(teleportIn || teleportWaitRoom || teleportLobby)){
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

		final int teamIndex = team == null ? -1 : am.indexOf(team);
		boolean playerReady = player.isOnline();
		final boolean dead = !player.isOnline() || player.isDead();
		final Player p = player.getPlayer();
		final boolean randomRespawn = mo.hasOption(TransitionOption.RANDOMRESPAWN);
		final MatchTransitions tops = am.getParams().getTransitionOptions();

		if (teleportWaitRoom || teleportLobby){ /// Teleport waiting room
			if ( (insideArena || am.checkReady(player, team, mo, true)) && !dead){
				/// EnterWaitRoom is supposed to happen before the teleport in event, but it depends on the result of a teleport
				/// Since we cant really tell the eventual result.. do our best guess
				Location l;
				final LocationType type;
				if (teleportWaitRoom){
					type = LocationType.WAITROOM;
					l = jitter(
							am.getSpawn(teamIndex, type, randomRespawn),
							rand.nextInt(team.size()));
				} else {
					type = LocationType.LOBBY;
					l = jitter(
							LobbyController.getLobbySpawn(am.indexOf(team), am.getParams().getType(),randomRespawn),
							rand.nextInt(team.size()));
				}
				/// Feels like a kludge, but I don't want to reteleport them in onJoin
				if (!(type == LocationType.LOBBY && player.getCurLocation() == LocationType.LOBBY)){
					ArenaLocation src = new ArenaLocation(p.getLocation(),player.getCurLocation());
					ArenaLocation dest = new ArenaLocation(l,type);
					ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(am.getParams().getType(),
							player,team,src,dest,TeleportDirection.IN);

					am.callEvent(apte);
					player.markOldLocation();
//					am.entering(player);
					TeleportController.teleportPlayer(p, l, false, true);
					PlayerStoreController.setGameMode(p, GameMode.SURVIVAL);
				}
			} else {
				playerReady = false;
			}
		}

		/// Teleport In
		if (teleportIn && transition != MatchState.ONSPAWN){ /// only tpin, respawn tps happen elsewhere
			if ((insideArena || am.checkReady(player, team, mo, true)) && !dead){
				/// enterArena is supposed to happen before the teleport in Event, but it depends on the result of a teleport
				/// Since we cant really tell the eventual result.. do our best guess
				final LocationType type = LocationType.ARENA;
				player.markOldLocation();
				final Location l = am.getSpawn(teamIndex, type, randomRespawn);
				ArenaLocation src = new ArenaLocation(p.getLocation(),player.getCurLocation());
				ArenaLocation dest = new ArenaLocation(l,type);
				ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(am.getParams().getType(),
						player,team,src,dest,TeleportDirection.IN);
				am.callEvent(apte);
				TeleportController.teleportPlayer(p, l, false, true);
				PlayerUtil.setGod(p,false);
				PlayerStoreController.setGameMode(p, GameMode.SURVIVAL);
			} else {
				playerReady = false;
			}
		}

		final boolean storeAll = mo.hasOption(TransitionOption.STOREALL);
		PlayerStoreController psc = PlayerStoreController.getPlayerStoreController();
		final boolean armorTeams = tops.hasAnyOption(TransitionOption.ARMORTEAMS);
		final boolean woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS);

		/// Only do if player is online options
		if (playerReady && !dead){
			if (storeAll || mo.hasOption(TransitionOption.STOREGAMEMODE)){ psc.storeGamemode(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREEXPERIENCE)){ psc.storeExperience(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREITEMS)) { psc.storeItems(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREHEALTH)){ psc.storeHealth(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREHUNGER)){ psc.storeHunger(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREMAGIC)){ psc.storeMagic(player);}
			if (storeAll || mo.hasOption(TransitionOption.STOREHEROCLASS)){psc.storeArenaClass(player);}
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
			if (mo.hasOption(TransitionOption.GIVECLASS) && player.getCurrentClass() == null){
				final ArenaClass ac = getArenaClass(mo,teamIndex);
				if (ac != null && ac.valid()){ /// Give class items and effects
					if (mo.woolTeams()) TeamUtil.setTeamHead(teamIndex, player); // give wool heads first
					if (armorTeams){
						ArenaClassController.giveClass(player, ac, TeamUtil.getTeamColor(teamIndex));
					} else{
						ArenaClassController.giveClass(player, ac);
					}
				}
			}
			if (mo.hasOption(TransitionOption.CLASSENCHANTS)){
				ArenaClass ac = player.getCurrentClass();
				if (ac != null){
					ArenaClassController.giveClassEnchants(p, ac);}
			}
			if (mo.hasOption(TransitionOption.GIVEDISGUISE) && DisguiseInterface.enabled()){
				final String disguise = getDisguise(mo,teamIndex);
				if (disguise != null){ /// Give class items and effects
					DisguiseInterface.disguisePlayer(p, disguise);}
			}
			if (mo.hasOption(TransitionOption.GIVEITEMS)){
				Color color = armorTeams ? TeamUtil.getTeamColor(teamIndex) : null;
				giveItems(transition, player, mo.getGiveItems(),teamIndex, woolTeams, insideArena,color);
			}

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
			final LocationType type;
			if (mo.hasOption(TransitionOption.TELEPORTTO)){
				loc = mo.getTeleportToLoc();
				type = LocationType.CUSTOM;
			} else {
				type = LocationType.HOME;
				loc = player.getOldLocation();
			}
			player.clearOldLocation();
			if (loc == null){
				Log.err("[BA Error] Teleporting to a null location!  teleportTo=" + mo.hasOption(TransitionOption.TELEPORTTO));
			} else if (insideArena || !onlyInMatch){
				TeleportController.teleportPlayer(p, loc, wipeInventory, true);
			}
//			LocationType type = LocationType.ARENA;
			ArenaLocation src = new ArenaLocation(p.getLocation(),player.getCurLocation());
			ArenaLocation dest = new ArenaLocation(loc,type);
			ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(am.getParams().getType(),
					player,team,src,dest,TeleportDirection.OUT);
//			ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(player,team,null,type,false);
			am.callEvent(apte);
			player.setReady(false);
			/// If players are outside of the match, but need requirements, warn them
		}
//		else if (transition == MatchState.ONPRESTART && !insideArena){
//			World w = am.getArena().getSpawnLoc(0).getWorld();
//			/// Warn players about requirements
//			if (!am.tops.playerReady(player, w)){
//				MessageUtil.sendMessage(player, am.tops.getRequiredString(player,w,"&eRemember you still need the following"));}
//		}
		/// Restore their exp and items.. Has to happen AFTER teleport
		boolean restoreAll = mo.hasOption(TransitionOption.RESTOREALL);
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREGAMEMODE)){ psc.restoreGamemode(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREEXPERIENCE)) { psc.restoreExperience(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREITEMS)){
			if (woolTeams && teamIndex != -1){
				/// Teams that have left can have a -1 teamIndex
				TeamUtil.removeTeamHead(teamIndex, p);
			}
			if (Defaults.DEBUG_TRANSITIONS)System.out.println("   "+transition+" transition restoring items "+insideArena);
			psc.restoreItems(player);
		}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREHEALTH)){ psc.restoreHealth(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREHUNGER)){ psc.restoreHunger(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREMAGIC)) { psc.restoreMagic(player);}
		if (restoreAll || mo.hasOption(TransitionOption.RESTOREHEROCLASS)){psc.restoreHeroClass(player);}
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


	static Location jitter(final Location teamSpawn, int index) {
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
