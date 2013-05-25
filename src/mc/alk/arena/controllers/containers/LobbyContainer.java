package mc.alk.arena.controllers.containers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.events.players.ArenaPlayerEnterLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.competition.BlockBreakListener;
import mc.alk.arena.listeners.competition.BlockPlaceListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.TeleportDirection;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class LobbyContainer extends PlayerContainer{
	Set<ArenaPlayer> inLobby = Collections.synchronizedSet(new HashSet<ArenaPlayer>());
	Map<String, Long> userTime = new ConcurrentHashMap<String, Long>();
	Map<String, Integer> deathTimer = new ConcurrentHashMap<String, Integer>();
	Map<String, Integer> respawnTimer = new ConcurrentHashMap<String, Integer>();

	public LobbyContainer(MatchParams params){
		super();
		this.params = params;
		methodController.addListener(new BlockPlaceListener(this));
		methodController.addListener(new BlockBreakListener(this));
	}

	@Override
	public boolean teamJoining(ArenaTeam team) {
		super.teamJoining(team);
		for (ArenaPlayer ap: team.getPlayers()){
			playerJoining(ap,team);}
		return true;
	}

	protected boolean playerJoining(ArenaPlayer player, ArenaTeam team){
		oldLocs.put(player, player.getLocation());
		doTransition(this, MatchState.ONJOIN, player,team, true);
		return true;
	}

	@ArenaEventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
		playerLeaving(ap);
	}

	@Override
	public void playerLeaving(ArenaPlayer ap){
		if (inLobby.contains(ap)){
			/// remove from lobby
			doTransition(this, MatchState.ONCANCEL, ap, ap.getTeam(), true);
			callEvent(new ArenaPlayerLeaveLobbyEvent(ap,null));
			updateBukkitEvents(MatchState.ONLEAVE,ap);
		}
		super.playerLeaving(ap);
	}

	@EventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		if (players.remove(event.getPlayer())){
			doTransition(this, MatchState.ONCANCEL, event.getPlayer(),event.getTeam(), false);
			callEvent(new ArenaPlayerLeaveLobbyEvent(event.getPlayer(),event.getTeam()));
			event.getPlayer().reset();
			updateBukkitEvents(MatchState.ONLEAVE,event.getPlayer());
		}
	}


	@EventHandler
	public void onArenaPlayerTeleportEvent(ArenaPlayerTeleportEvent event){
		if (event.getArenaType() != this.getParams().getType()){
			return;}
		ArenaPlayer ap = event.getPlayer();
		boolean inside = inLobby.contains(ap);
		if (Defaults.DEBUG) Log.info(event.getPlayer().getName() + " onArenaPlayerTeleportEvent --- " + event.getArenaType() +"   " + event.getSrcLocation() +"  -- " +
				event.getDestLocation() +"    inside="+inside);
		if (event.getDirection() == TeleportDirection.IN){
			if (!inside && event.getDestType() == LocationType.LOBBY){
				updateBukkitEvents(MatchState.ONENTER,event.getPlayer());
				doTransition(this, MatchState.ONENTER, ap,event.getTeam(), false);
				callEvent(new ArenaPlayerEnterLobbyEvent(ap, event.getTeam()));
				ap.setCurLocation(LocationType.LOBBY);
				inLobby.add(ap);
//				methodController.updateEvents(MatchState.ON, player)
			} else if (inside){
				/// they are going somewhere else, maybe into a game.
				players.remove(ap);
				inLobby.remove(ap);
				updateBukkitEvents(MatchState.ONLEAVE,event.getPlayer());
			}
		} else if (inside && event.getDirection() == TeleportDirection.OUT){
			callEvent(new ArenaPlayerLeaveLobbyEvent(ap, event.getTeam()));
			updateBukkitEvents(MatchState.ONENTER,event.getPlayer());
			if (event.getDirection() == TeleportDirection.OUT && ap.getCompetition()==null){
				doTransition(this, MatchState.ONLEAVE, ap,event.getTeam(), false);
				oldLocs.remove(ap);
			}
			players.remove(ap);
			inLobby.remove(ap);
			ap.setCurLocation(LocationType.HOME);
		}

	}

	public void addSpawn(int index, Location loc) {
		if (spawns.size() <= index)
			spawns.add(loc);
		else
			spawns.set(index, loc);
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

	@Override
	public ArenaTeam getTeam(ArenaPlayer player) {
		return player.getTeam();
	}

	@ArenaEventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.isCancelled() || event.getClickedBlock() == null)
			return;
		/// Check to see if it's a sign
		if (event.getClickedBlock().getType().equals(Material.SIGN_POST)||
				event.getClickedBlock().getType().equals(Material.WALL_SIGN)){ /// Only checking for signs
			ArenaMatch.signClick(event,this,userTime);
		} else if (event.getClickedBlock().getType().equals(Defaults.READY_BLOCK)) {
			if (respawnTimer.containsKey(event.getPlayer().getName())){
				ArenaMatch.respawnClick(event,this, respawnTimer);
			} else {
//				readyClick(event);
			}
		}
	}

}
