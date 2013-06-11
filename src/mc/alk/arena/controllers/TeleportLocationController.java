package mc.alk.arena.controllers;

import java.util.Random;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.containers.LobbyWRContainer;
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

import org.bukkit.Location;

public class TeleportLocationController {
	static Random rand = new Random();

	public static void teleport(PlayerHolder am, ArenaTeam team,
			ArenaPlayer player, TransitionOptions mo, int teamIndex) {
		MatchParams mp = am.getParams();
		final boolean randomRespawn = mo.hasOption(TransitionOption.RANDOMRESPAWN);
		final boolean teleportIn = mo.shouldTeleportIn();
		final boolean teleportWaitRoom = mo.shouldTeleportWaitRoom();
		final boolean teleportLobby = mo.shouldTeleportLobby();

		/// EnterWaitRoom is supposed to happen before the teleport in event, but it depends on the result of a teleport
		/// Since we cant really tell the eventual result.. do our best guess
		Location l;
		final LocationType type;
		if (teleportWaitRoom){
			final LobbyWRContainer wr ;
			if (am instanceof Match){
				Match m = (Match) am;
				wr = m.getArena().getWaitroom();
			} else {
				wr = (LobbyWRContainer) am;
			}
			Log.debug("wr === " + wr +"      am============" + am);
//			JoinOptions jo = player.getJoinOptions();
//			Arena arena = jo.getArena();
			type = LocationType.WAITROOM;
			l = jitter(wr.getSpawn(teamIndex, randomRespawn),teamIndex);
		} else if (teleportLobby){
			type = LocationType.LOBBY;
			l = jitter(LobbyController.getLobbySpawn(teamIndex,mp.getType(),randomRespawn),0);
		} else {
			Arena arena = null;
			if (am instanceof Match){
				Match m = (Match) am;
				arena = m.getArena();
			}

//			JoinOptions jo = player.getJoinOptions();
//			Arena arena = jo.getArena();
//			Arena arena = player.getArena();
			type = LocationType.ARENA;
			l = arena.getSpawnLoc(teamIndex);
//			PlayerUtil.setGod(p,false);
//			PlayerStoreController.setGameMode(p, GameMode.SURVIVAL);
		}
		ArenaLocation src = new ArenaLocation(player.getLocation(),player.getCurLocation());
		ArenaLocation dest = new ArenaLocation(l,type);
		TeleportDirection td = TeleportDirection.IN;

		player.markOldLocation();
		ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(mp.getType(),
				player,team,src,dest,td);
		am.callEvent(apte);
		TeleportController.teleportPlayer(player.getPlayer(), l, false, true);
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
