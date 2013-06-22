package mc.alk.arena.controllers;

import java.util.Random;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.containers.LobbyWRContainer;
import mc.alk.arena.controllers.containers.PlayerContainer;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaLocation;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.TeleportDirection;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.Util;

import org.bukkit.Location;

public class TeleportLocationController {
	static Random rand = new Random();

	public static void teleport(PlayerHolder am, ArenaTeam team,
			ArenaPlayer player, TransitionOptions mo, int teamIndex) {
		MatchParams mp = am.getParams();

		/// EnterWaitRoom is supposed to happen before the teleport in event, but it depends on the result of a teleport
		/// Since we cant really tell the eventual result.. do our best guess
		ArenaLocation dest = getArenaLocation(am,team,player,mo,teamIndex);
		ArenaLocation src;
		if (player.getCurLocation() == LocationType.HOME){
			src = new ArenaLocation(PlayerContainer.HOMECONTAINER, player.getLocation(),player.getCurLocation());
		} else {
			src = new ArenaLocation(am, player.getLocation(),player.getCurLocation());
		}
		TeleportDirection td = calcTeleportDirection(player, src,dest);
		player.markOldLocation();
		ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(mp.getType(),player,team,src,dest,td);

		movePlayer(player, apte, mp);
	}

	public static void teleportOut(PlayerHolder am, ArenaTeam team,
			ArenaPlayer player, TransitionOptions mo, int teamIndex,
			boolean insideArena, boolean onlyInMatch, boolean wipeInventory) {
		MatchParams mp = am.getParams();
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
			TeleportController.teleportPlayer(player.getPlayer(), loc, wipeInventory, true);
		}
		ArenaLocation src = new ArenaLocation(am, player.getLocation(),player.getCurLocation());
		ArenaLocation dest = new ArenaLocation(PlayerContainer.HOMECONTAINER, loc,type);
		ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(am.getParams().getType(),
				player,team,src,dest,TeleportDirection.OUT);
		movePlayer(player, apte,mp);
	}

	private static void movePlayer(ArenaPlayer player, ArenaPlayerTeleportEvent apte, MatchParams mp) {
		PlayerHolder src = apte.getSrcLocation().getPlayerHolder();
		PlayerHolder dest = apte.getDestLocation().getPlayerHolder();
		TeleportDirection td = apte.getDirection();
		Log.debug( player.getName() +"   " + player.getCurLocation()
				 + "  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@   "   + td);
		Log.debug("" + src.getLocationType() +"  --- " + dest.getLocationType());

		switch (td){
		case RESPAWN:
			Util.printStackTrace();
			Log.debug("################################# what to do here???? ########");
			break;
		case FIRSTIN:
			mp.getGameManager().onPreJoin(player,apte);
			dest.onPreJoin(player, apte);
		case IN:
			src.onPreLeave(player, apte);
			dest.onPreEnter(player, apte);
			break;
		case OUT:
			mp.getGameManager().onPreQuit(player,apte);
			src.onPreQuit(player, apte);
			dest.onPreJoin(player, apte);
			break;
		default:
			break;
		}
		dest.callEvent(apte);
		TeleportController.teleportPlayer(player.getPlayer(), apte.getDestLocation().getLocation(), false, true);
		player.setCurLocation(dest.getLocationType());
		switch (td){
		case RESPAWN:
			Log.debug("################################# what to do here???? ########");
			break;
		case FIRSTIN:
			mp.getGameManager().onPostJoin(player,apte);
			dest.onPostJoin(player, apte);
		case IN:
			src.onPostLeave(player, apte);
			dest.onPostEnter(player, apte);
			break;
		case OUT:
			mp.getGameManager().onPostQuit(player,apte);
			src.onPostQuit(player, apte);
			dest.onPostJoin(player, apte);
			break;
		default:
			break;
		}

	}

	private static TeleportDirection calcTeleportDirection(ArenaPlayer player, ArenaLocation src, ArenaLocation dest) {
		if (player.getCurLocation() == LocationType.HOME){
			return TeleportDirection.FIRSTIN;
		} else if (player.getCurLocation() == dest.getType()){
			return TeleportDirection.RESPAWN;
		}
		return TeleportDirection.IN;
	}

	private static ArenaLocation getArenaLocation(PlayerHolder am, ArenaTeam team,
			ArenaPlayer player, TransitionOptions mo, int teamIndex){
		final MatchParams mp = am.getParams();
		final boolean randomRespawn = mo.hasOption(TransitionOption.RANDOMRESPAWN);
		Location l;
		final boolean teleportIn = mo.shouldTeleportIn();
		final boolean teleportWaitRoom = mo.shouldTeleportWaitRoom();
		final boolean teleportLobby = mo.shouldTeleportLobby();
		final LocationType type;
		final PlayerHolder ph;
		Log.debug(player.getName() +" ^^^^^^^^^^^^^^^  " + teleportIn +"  ^^^^^^^^^^ " + teleportWaitRoom +"   lobby="+ teleportLobby +"   " + mp.getName());
		if (teleportWaitRoom){
			final LobbyWRContainer wr ;
			if (am instanceof Match){
				Match m = (Match) am;
				wr = m.getArena().getWaitroom();
			} else {
				wr = (LobbyWRContainer) am;
			}
			ph = wr;
			Log.debug("wr === " + wr +"      am============" + am);
			type = LocationType.WAITROOM;
			l = jitter(wr.getSpawn(teamIndex, randomRespawn),teamIndex);
		} else if (teleportLobby){
			ph = LobbyController.getLobby(mp.getType());
			Log.debug(" ######### ph ==== " + ph +"   " );
			type = LocationType.LOBBY;
			l = jitter(LobbyController.getLobbySpawn(teamIndex,mp.getType(),randomRespawn),0);
		} else {
			Arena arena = null;
			if (am instanceof Match){
				Match m = (Match) am;
				arena = m.getArena();
			}
			ph = am;
			type = LocationType.ARENA;
			l = arena.getSpawnLoc(teamIndex);
		}
		return new ArenaLocation(ph, l,type);
	}
	/// enterArena is supposed to happen before the teleport in Event, but it depends on the result of a teleport
	/// Since we cant really tell the eventual result.. do our best guess
//	final LocationType type = LocationType.ARENA;
//	player.markOldLocation();
//	final Location l = am.getSpawn(teamIndex, type, randomRespawn);
//	ArenaLocation src = new ArenaLocation(p.getLocation(),player.getCurLocation());
//	ArenaLocation dest = new ArenaLocation(l,type);
//	ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(am.getParams().getType(),
//			player,team,src,dest,TeleportDirection.IN);
//	am.callEvent(apte);
//	TeleportController.teleportPlayer(p, l, false, true);
//	PlayerUtil.setGod(p,false);
//	PlayerStoreController.setGameMode(p, GameMode.SURVIVAL);

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
