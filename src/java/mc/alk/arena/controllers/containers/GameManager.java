package mc.alk.arena.controllers.containers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.TransitionController;
import mc.alk.arena.controllers.plugins.EssentialsController;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.players.ArenaPlayerEnterMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.listeners.custom.MethodController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameManager implements PlayerHolder{
	static final HashMap<ArenaType, GameManager> map = new HashMap<ArenaType, GameManager>();

	final MatchParams params;
	final Set<ArenaPlayer> handled = new HashSet<ArenaPlayer>(); /// which players are now being handled
	MethodController methodController;

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
		methodController = new MethodController("GM "+params.getName());
		methodController.addAllEvents(this);
		if (Defaults.TESTSERVER) {Log.info("GameManager Testing"); return;}
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
	}

	@Override
	public void addArenaListener(ArenaListener arenaListener) {
        methodController.addListener(arenaListener);
    }

    @Override
    public boolean removeArenaListener(ArenaListener arenaListener) {
        return methodController.removeListener(arenaListener);
    }


    @ArenaEventHandler(priority=EventPriority.HIGHEST)
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		if (handled.contains(event.getPlayer()) && !event.isHandledQuit()){
			ArenaPlayer player = event.getPlayer();
			ArenaTeam t = getTeam(player);
			TransitionController.transition(this, MatchState.ONCANCEL, player, t, false);
		}
	}

	private void quitting(ArenaPlayer player){
		if (handled.remove(player)){
			TransitionController.transition(this, MatchState.ONLEAVE, player, null, false);
			updateBukkitEvents(MatchState.ONLEAVE, player);
			player.reset(); /// reset their isReady status, chosen class, etc.
		}
	}

	private void cancel() {
		List<ArenaPlayer> col = new ArrayList<ArenaPlayer>(handled);
		for (ArenaPlayer player: col){
			ArenaTeam t = getTeam(player);
			TransitionController.transition(this, MatchState.ONCANCEL, player, t, false);
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
		return handled.contains(player);
	}

	@Override
	public boolean checkReady(ArenaPlayer player, ArenaTeam team, StateOptions mo, boolean b) {
		return false;
	}

	@Override
	public void callEvent(BAEvent event) {
		methodController.callEvent(event);
	}

	@Override
	public SpawnLocation getSpawn(int index, boolean random) {
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
		if (handled.add(player)){
			TransitionController.transition(this, MatchState.ONENTER, player, null, false);
			updateBukkitEvents(MatchState.ONENTER, player);
			if (EssentialsController.enabled())
				BAPlayerListener.setBackLocation(player.getName(),
						EssentialsController.getBackLocation(player.getName()));
			// When teleporting in for the first time defaults
			PlayerUtil.setGameMode(player.getPlayer(), GameMode.SURVIVAL);
			EssentialsController.setGod(player.getPlayer(), false);
            callEvent(new ArenaPlayerEnterMatchEvent(player, player.getTeam()));

			player.getMetaData().setJoining(true);
		}
	}

	@Override
	public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		player.getMetaData().setJoining(false);

    }

	@Override
	public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		this.quitting(player);
        callEvent(new ArenaPlayerLeaveMatchEvent(player,player.getTeam()));
		if (EssentialsController.enabled())
			BAPlayerListener.setBackLocation(player.getName(), null);

	}

	@Override
	public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPostEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	@Override
	public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
	}

	public boolean hasPlayer(ArenaPlayer player) {
		return handled.contains(player);
	}

	public static void cancelAll() {
		synchronized(map){
			for (GameManager gm: map.values()){
				gm.cancel();
			}
		}
	}

}
