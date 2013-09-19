package mc.alk.arena.controllers.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveLobbyEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;


public class AreaContainer extends AbstractAreaContainer{
//	Map<String, Long> userTime = new ConcurrentHashMap<String, Long>();
//	Map<String, Integer> deathTimer = new ConcurrentHashMap<String, Integer>();
	Map<String, Integer> respawnTimer = null;
	final LocationType type;

	public AreaContainer(String name, LocationType type){
		super(name);
		this.type = type;
	}

	public AreaContainer(String name, MatchParams params, LocationType type){
		super(name);
		this.params = params;
		this.params.flatten();
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

	}

	@EventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		if (players.remove(event.getPlayer().getName())){
			updateBukkitEvents(MatchState.ONLEAVE, event.getPlayer());
			callEvent(new ArenaPlayerLeaveLobbyEvent(event.getPlayer(),event.getTeam()));
			event.addMessage(MessageHandler.getSystemMessage("you_left_competition",this.params.getName()));
			event.getPlayer().reset();
		}
	}

	@Override
	public LocationType getLocationType() {
		return LocationType.LOBBY;
	}

	public void cancel() {
		players.clear();
	}

	public Collection<String> getInsidePlayers() {
		return new HashSet<String>(players);
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
			ArenaMatch.signClick(event,this);
		} else if (event.getClickedBlock().getType().equals(Defaults.READY_BLOCK)) {
			if (respawnTimer == null)
				new HashMap<String, Integer>();
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
	}

	@Override
	public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPostEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		players.add(player.getName());
		updateBukkitEvents(MatchState.ONENTER,player);
	}

	@Override
	public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		updateBukkitEvents(MatchState.ONLEAVE,player);
		players.remove(player.getName());
	}

	@Override
	public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	/**
	 * Return a string of appended spawn locations
	 * @return
	 */
	public String getSpawnLocationString(){
		StringBuilder sb = new StringBuilder();
		List<Location> locs = getSpawns();
		for (int i=0;i<locs.size(); i++ ){
			if (locs.get(i) != null) sb.append("["+(i+1)+":"+Util.getLocString(locs.get(i))+"] ");
		}
		return sb.toString();
	}

}
