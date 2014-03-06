package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.containers.AbstractAreaContainer;
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

import java.util.Random;

public class TeleportLocationController {
	static Random rand = new Random();

    public static ArenaLocation createCurrentArenaLocation(ArenaPlayer ap){
        return new ArenaLocation(AbstractAreaContainer.HOMECONTAINER, ap.getLocation(),LocationType.HOME);
    }

	public static void teleport(PlayerHolder am, ArenaTeam team,
			ArenaPlayer player, TransitionOptions mo, int teamIndex) {
		player.markOldLocation();
		MatchParams mp = am.getParams();

		/// EnterWaitRoom is supposed to happen before the teleport in event, but it depends on the result of a teleport
		/// Since we cant really tell the eventual result.. do our best guess
		ArenaLocation dest = getArenaLocation(am,team,player,mo,teamIndex);
		ArenaLocation src = player.getCurLocation();
		src.setLocation(player.getLocation());
		if (Defaults.DEBUG_TRACE)Log.info(" ########### @@ " + player.getCurLocation()  +"  -->  " + am.getTeam(player) );

		TeleportDirection td = calcTeleportDirection(player, src,dest);
		ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(mp.getType(),player,team,src,dest,td);

		movePlayer(player, apte, mp);
	}

	public static void teleportOut(PlayerHolder am, ArenaTeam team,
			ArenaPlayer player, TransitionOptions mo, int teamIndex,
			boolean insideArena, boolean onlyInMatch, boolean wipeInventory) {
        MatchParams mp = am.getParams();
		Location loc;
		ArenaLocation src = player.getCurLocation();
		final LocationType type;
		if (mo.hasOption(TransitionOption.TELEPORTTO)){
			loc = mo.getTeleportToLoc();
			type = LocationType.CUSTOM;
		} else {
			type = LocationType.HOME;
			loc = player.getOldLocation();
			/// TODO
			/// This is a bit of a kludge, sometimes we are "teleporting them out"
			/// when they are already out... so need to rethink how this can happen and should it
			if (loc == null && src.getType()==LocationType.HOME){
				loc = src.getLocation();
			}
		}
		player.clearOldLocation();
		if (loc == null){
			Log.err(BattleArena.getNameAndVersion()+" Teleporting to a null location!  teleportTo=" + mo.hasOption(TransitionOption.TELEPORTTO));
		}

		ArenaLocation dest = new ArenaLocation(AbstractAreaContainer.HOMECONTAINER, loc,type);
		ArenaPlayerTeleportEvent apte = new ArenaPlayerTeleportEvent(am.getParams().getType(),
				player,team,src,dest,TeleportDirection.OUT);
		movePlayer(player, apte,mp);
	}

	private static void movePlayer(ArenaPlayer player, ArenaPlayerTeleportEvent apte, MatchParams mp) {
		PlayerHolder src = apte.getSrcLocation().getPlayerHolder();
		PlayerHolder dest = apte.getDestLocation().getPlayerHolder();
		TeleportDirection td = apte.getDirection();
		if (Defaults.DEBUG_TRACE)Log.info(" ###########  " + player.getCurLocation()  +"  -->  " + dest.getLocationType() );
		if (Defaults.DEBUG_TRACE)Log.info(" ---- << -- " + player.getName() +"   src=" + src +"   dest="+dest +"    td=" + td);

		switch (td){
		case RESPAWN:
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
		if (!TeleportController.teleport(player, apte.getDestLocation().getLocation(), true) &&
				player.isOnline() && !player.isDead() && !Defaults.DEBUG_VIRTUAL){
			Log.err("[BA Warning] couldn't teleport "+player.getName()+" srcLoc="+apte.getSrcLocation() +" destLoc=" + apte.getDestLocation());
		}
		player.setCurLocation(apte.getDestLocation());
		switch (td){
		case RESPAWN:
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
		if (player.getCurLocation().getType() == LocationType.HOME){
			return TeleportDirection.FIRSTIN;
		} else if (player.getCurLocation().getType() == dest.getType()){
			return TeleportDirection.RESPAWN;
		}
		return TeleportDirection.IN;
	}

	private static ArenaLocation getArenaLocation(PlayerHolder am, ArenaTeam team,
			ArenaPlayer player, TransitionOptions mo, int teamIndex){
		final MatchParams mp = am.getParams();
		final boolean randomRespawn = mo.hasOption(TransitionOption.RANDOMRESPAWN);
		Location l;
		final boolean teleportWaitRoom = mo.shouldTeleportWaitRoom();
		final boolean teleportLobby = mo.shouldTeleportLobby();
		final boolean teleportSpectate = mo.shouldTeleportSpectate();
		final LocationType type;
		final PlayerHolder ph;
		if (Defaults.DEBUG_TRACE)Log.info(" teamindex = " + teamIndex +"   " + am.getClass().getSimpleName()  +"  " +am);

		if (teleportWaitRoom){
			if (mo.hasOption(TransitionOption.TELEPORTMAINWAITROOM)){
				teamIndex = Defaults.MAIN_SPAWN;}
			ph = (am instanceof Match) ? ((Match)am).getArena().getWaitroom() : am;
			type = LocationType.WAITROOM;
			l = jitter(ph.getSpawn(teamIndex, randomRespawn),teamIndex);
		} else if (teleportLobby){
			if (mo.hasOption(TransitionOption.TELEPORTMAINLOBBY)){
				teamIndex = Defaults.MAIN_SPAWN;}
			ph = RoomController.getLobby(mp.getType());
			type = LocationType.LOBBY;
			l = jitter(RoomController.getLobbySpawn(teamIndex,mp.getType(),randomRespawn),0);
		} else if (teleportSpectate){
			ph = (am instanceof Match) ? ((Match)am).getArena().getSpectatorRoom() : am;
			type = LocationType.SPECTATE;
			l = jitter(ph.getSpawn(teamIndex, randomRespawn),teamIndex);
		} else { // They should teleportIn, aka to the Arena
			final Arena arena;
			if (am instanceof Arena){
				arena = (Arena) am;
			} else if (am instanceof Match){
				Match m = (Match) am;
				arena = m.getArena();
			} else {
				throw new IllegalStateException("[BA Error] Instance is " + am.getClass().getSimpleName());
			}
			ph = am;
			type = LocationType.ARENA;
			l = arena.getSpawn(teamIndex,false);
		}
		return new ArenaLocation(ph, l,type);
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
