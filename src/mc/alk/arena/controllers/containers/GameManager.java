package mc.alk.arena.controllers.containers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.PerformTransition;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.v1r6.util.Log;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GameManager implements PlayerHolder{
	static HashMap<ArenaType, GameManager> map = new HashMap<ArenaType, GameManager>();

	final MatchParams params;
	final Set<ArenaPlayer> handled = new HashSet<ArenaPlayer>(); /// which players are now being handled
	final MethodController methodController = new MethodController();

	public static GameManager getGameManager(MatchParams mp) {
		if (map.containsKey(mp.getType()))
			return map.get(mp.getType());
		GameManager gm = new GameManager(mp);
		map.put(mp.getType(), gm);
		return gm;
	}

	protected void updateBukkitEvents(MatchState matchState,ArenaPlayer player){
		methodController.updateEvents(matchState, player);
	}

	private GameManager(MatchParams params){
		this.params = params;
		methodController.addAllEvents(this);
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
	}

	private GameManager(MatchParams mp, GameManager gameManager) {
		this.params = mp;
//		Set<ArenaPlayer> alreadyJoined = new HashSet<ArenaPlayer>(players);
//		alreadyJoined.retainAll(players);
//		gameManager.handled.removeAll(players);
//		this.handled.addAll(alreadyJoined);
	}

	@Override
	public void addArenaListener(ArenaListener arenaListener) {

	}

//	@EventHandler(priority=org.bukkit.event.EventPriority.HIGHEST)
	@ArenaEventHandler(priority=EventPriority.HIGHEST)
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		Log.debug(this+"   222  onPlayerQuit   -- " + event.getPlayer().getName() +"  handled="+handled.contains(event.getPlayer()));
		if (handled.contains(event.getPlayer()) && !event.isHandledQuit()){
			ArenaPlayer player = event.getPlayer();
			Log.debug("onPlayerQuit   -- " + player.getName() +"   --- ");
			ArenaTeam t = getTeam(player);
			PerformTransition.transition(this, MatchState.ONCANCEL, player, t, false);
		}
	}

	private void quitting(ArenaPlayer player){
		Log.debug("~~~~~~~~~~~~~~~~~~ quitting   -- " + player.getName() +" ~~~~~~~~~~~~~~~~~~ ");
		if (handled.remove(player)){
			PerformTransition.transition(this, MatchState.ONLEAVE, player, null, false);
			updateBukkitEvents(MatchState.ONLEAVE, player);
			handled.remove(player);
		}
	}

	@Override
	public MatchParams getParams() {
		return params;
	}

	@Override
	public CompetitionState getState() {
		return null;
	}

	@Override
	public MatchState getMatchState() {
		return null;
	}

	@Override
	public boolean isHandled(ArenaPlayer player) {
		return false;
	}

	@Override
	public int indexOf(ArenaTeam team) {
		return 0;
	}

	@Override
	public boolean checkReady(ArenaPlayer player, ArenaTeam team, TransitionOptions mo, boolean b) {
		return false;
	}

	@Override
	public void callEvent(BAEvent event) {

	}

	@Override
	public Location getSpawn(int index, boolean random) {
		return null;
	}

	@Override
	public Location getSpawn(ArenaPlayer player, boolean random) {
		return null;
	}

	@Override
	public LocationType getLocationType() {
		return null;
	}

	@Override
	public ArenaTeam getTeam(ArenaPlayer player) {
		return null;
	}

	@Override
	public void onPreJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPreJoin -------------- " + player.getName() +"    " + handled.contains(player));
		if (handled.add(player)){
			Log.debug(" 222 !!!!!! onPreJoin -------------- " + player.getName() +"    " + handled.contains(player));
			PerformTransition.transition(this, MatchState.ONENTER, player, null, false);
			updateBukkitEvents(MatchState.ONENTER, player);

			player.getMetaData().setJoining(true);
			handled.add(player);
		}
	}

	@Override
	public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPostJoin -------------- " + player.getName() +"    " );
		player.getMetaData().setJoining(false);
	}

	@Override
	public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPreQuit -------------- " + player.getName() +"    " );
	}

	@Override
	public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPostQuit -------------- " + player.getName() +"    " );
//		PerformTransition.transition(this, MatchState.ONLEAVE, player, null, false);
//		handled.remove(player);
		this.quitting(player);
	}

	@Override
	public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPreEnter -------------- " + player.getName() +"    " );
	}

	@Override
	public void onPostEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPostEnter -------------- " + player.getName() +"    " );
	}

	@Override
	public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPreLeave -------------- " + player.getName() +"    " );
	}

	@Override
	public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		Log.debug(" !!!!!! onPostLeave -------------- " + player.getName() +"    " );

	}

	public boolean hasPlayer(ArenaPlayer player) {
		return handled.contains(player);
	}

}
