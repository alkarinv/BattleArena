package mc.alk.arena.controllers.containers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.players.ArenaPlayerEnterLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.TeleportDirection;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class LobbyContainer extends PlayerContainer{
	Set<ArenaPlayer> inLobby = Collections.synchronizedSet(new HashSet<ArenaPlayer>());

	public LobbyContainer(MatchParams params){
		this.params = params;
	}

	@Override
	public boolean teamJoining(ArenaTeam team) {
		super.teamJoining(team);
		for (ArenaPlayer ap: team.getPlayers()){
			playerJoining(ap,team);}
//		Log.debug("########### 3333 Lobby :teamJoining" + team);
		return true;
	}

	protected boolean playerJoining(ArenaPlayer player, ArenaTeam team){
		oldLocs.put(player, player.getLocation());
//		Log.debug("###########  Lobby :playerJoining = " + player +"    team = " + team.getId() +"   teamname = " + team.getName());
		doTransition(this, MatchState.ONJOIN, player,team, true);
		return true;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event){
		playerLeaving(BattleArena.toArenaPlayer(event.getPlayer()));
	}

	@Override
	public void playerLeaving(ArenaPlayer ap){
//		Log.debug("       ----###########  Lobby playerLeaving  player = " +ap.getName() +" inlobby has = " + inLobby.contains(ap));
		if (inLobby.contains(ap)){
			/// remove from lobby
//			Log.debug("###########  Lobby playerLeaving  player = " +ap.getName());
			doTransition(this, MatchState.ONCANCEL, ap, ap.getTeam(), true);
			callEvent(new ArenaPlayerLeaveLobbyEvent(ap,null));
		}
		super.playerLeaving(ap);
	}

	@EventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
//		Log.debug("#### leaving  " + event.getPlayer().getName() +"   continas=" + players.contains(event.getPlayer()));
		if (players.remove(event.getPlayer())){
			doTransition(this, MatchState.ONCANCEL, event.getPlayer(),event.getTeam(), false);
			callEvent(new ArenaPlayerLeaveLobbyEvent(event.getPlayer(),event.getTeam()));
		}
	}

	@ArenaEventHandler(suppressCastWarnings=true,priority=EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event, final ArenaPlayer target){
//		Log.debug("###########  Lobby PlayerDeathEvent  player = " + event.getEntity().getName());
	}


	@EventHandler
	public void onArenaPlayerTeleportEvent(ArenaPlayerTeleportEvent event){
//		Log.debug("############ ArenaPlayerTeleportEvent " + event.getDirection() +"   " + event.getArenaType());
		if (event.getArenaType() != this.getParams().getType()){
			return;}
		ArenaPlayer ap = event.getPlayer();
		boolean inside = inLobby.contains(ap);
		if (event.getDirection() == TeleportDirection.IN){
			if (!inside && event.getDestType() == LocationType.LOBBY){
//				Log.debug("############ entering lobby   " + ap.getName() +"   " );
				doTransition(this, MatchState.ONENTER, ap,event.getTeam(), false);
				callEvent(new ArenaPlayerEnterLobbyEvent(ap, event.getTeam()));
//				ap.addLocationType(LocationType.LOBBY);
				ap.setCurLocation(LocationType.LOBBY);
				inLobby.add(ap);
			} else if (inside){
//				Log.debug("-------------- leaving lobby but   " + ap.getName() +"   " );
				/// they are going somewhere else, maybe into a game.
//				ap.removeLocationType(LocationType.LOBBY);
				players.remove(ap);
				inLobby.remove(ap);
			}
		} else if (inside && event.getDirection() == TeleportDirection.OUT){
//			Log.debug("#888888## leaving lobby   " + ap.getName() +"   " );
			callEvent(new ArenaPlayerLeaveLobbyEvent(ap, event.getTeam()));
			if (event.getDirection() == TeleportDirection.OUT){
				doTransition(this, MatchState.ONLEAVE, ap,event.getTeam(), false);
				oldLocs.remove(ap);
			}
			players.remove(ap);
			inLobby.remove(ap);
			ap.setCurLocation(LocationType.HOME);
//			ap.removeLocationType(LocationType.LOBBY);
		}

	}

	public void addSpawn(int index, Location loc) {
		spawns.add(loc);
	}

	@Override
	public LocationType getLocationType() {
		return LocationType.LOBBY;
	}

	public void cancel() {
		synchronized(inLobby){
			for (ArenaPlayer ap: inLobby){
				doTransition(this, MatchState.ONCANCEL, ap, null, false);
			}
		}
		players.clear();
		inLobby.clear();
	}

	public Collection<String> getInsidePlayers() {
		HashSet<String> in = new HashSet<String>();
		synchronized(inLobby){
			for (ArenaPlayer ap: inLobby){
				in.add(ap.getName());}
		}
		return in;
	}

}
