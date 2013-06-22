package mc.alk.arena.controllers.containers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.competition.BlockBreakListener;
import mc.alk.arena.listeners.competition.BlockPlaceListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.v1r6.util.Log;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;


public class LobbyWRContainer extends PlayerContainer{
	Map<String, Long> userTime = new ConcurrentHashMap<String, Long>();
	Map<String, Integer> deathTimer = new ConcurrentHashMap<String, Integer>();
	Map<String, Integer> respawnTimer = new ConcurrentHashMap<String, Integer>();
	final LocationType type;

	public LobbyWRContainer(MatchParams params, LocationType type){
		super();
		this.params = params;
		methodController.addListener(new BlockPlaceListener(this));
		methodController.addListener(new BlockBreakListener(this));
		this.type = type;
	}

	@Override
	public boolean teamJoining(ArenaTeam team) {
		super.teamJoining(team);
		for (ArenaPlayer ap: team.getPlayers()){
			playerJoining(ap,team);}
		return true;
	}

	protected boolean playerJoining(ArenaPlayer player, ArenaTeam team){
		doTransition(this, MatchState.ONJOIN, player,team, true);
		return true;
	}

	@Override
	public void playerLeaving(ArenaPlayer ap){
//		if (inTheWRL.contains(ap)){
//			/// remove from lobby
//			doTransition(this, MatchState.ONCANCEL, ap, ap.getTeam(), true);
//			callEvent(new ArenaPlayerLeaveLobbyEvent(ap,null));
////			updateBukkitEvents(MatchState.ONLEAVE,ap);
//		}
//		super.playerLeaving(ap);
	}

	@ArenaEventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		Log.debug(this + " 11 ##^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ " + event.getPlayer().getName() +"  ^^^^^  " + event);
		if (players.remove(event.getPlayer())){
			Log.debug(this + " 22 ##^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ " + event.getPlayer().getName() +"  ^^^^^  " + event);
			updateBukkitEvents(MatchState.ONLEAVE, event.getPlayer());
			callEvent(new ArenaPlayerLeaveLobbyEvent(event.getPlayer(),event.getTeam()));
			event.addMessage(MessageHandler.getSystemMessage("you_left_competition",this.params.getName()));
			event.getPlayer().reset();
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
//		synchronized(inTheWRL){
//			for (ArenaPlayer ap: inTheWRL){
//				doTransition(this, MatchState.ONCANCEL, ap, null, false);
//			}
//		}
		players.clear();
//		inTheWRL.clear();
	}

	public Collection<String> getInsidePlayers() {
		HashSet<String> in = new HashSet<String>();
		synchronized(players){
			for (ArenaPlayer ap: players){
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

	public boolean hasSpawns() {
		return !spawns.isEmpty();
	}

	@Override
	public void onPreJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(this + " %%%%%%%%%%  onPostJoin   " + player.getName() +"    " + players.contains(player));
	}

	@Override
	public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(this + " %%%%%%%%%%  onPreQuit   " + player.getName() +"    " + players.contains(player));

	}

	@Override
	public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(this + " %%%%%%%%%%  onPostQuit   " + player.getName() +"    " + players.contains(player));
	}

	@Override
	public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(this + " %%%%%%%%%%  onPreEnter   " + player.getName() +"    " + players.contains(player));
	}

	@Override
	public void onPostEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(this + " %%%%%%%%%%  onPostEnter   " + player.getName() +"    " + players.contains(player));
		updateBukkitEvents(MatchState.ONENTER,player);
		players.add(player);
	}

	@Override
	public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(this + " %%%%%%%%%%  onPreLeave   " + player.getName() +"    " + players.contains(player));
		updateBukkitEvents(MatchState.ONLEAVE,player);
		players.remove(player);
	}

	@Override
	public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(this + " %%%%%%%%%%  onPostLeave   " + player.getName() +"    " + players.contains(player));
	}

}
