package mc.alk.arena.listeners;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.Location;
import org.bukkit.event.Listener;


public interface PlayerHolder extends Listener, ArenaListener{
	/**
	 * Add an arena listener for this competition
	 * @param arenaListener
	 */
	public void addArenaListener(ArenaListener arenaListener);

	public MatchParams getParams();

	public CompetitionState getState();

	public MatchState getMatchState();

	public boolean isHandled(ArenaPlayer player);

	public int indexOf(ArenaTeam team);

	public boolean checkReady(ArenaPlayer player, ArenaTeam team, TransitionOptions mo, boolean b);

	public void callEvent(BAEvent event);

	public Location getSpawn(int index, LocationType type, boolean random);

	public Location getSpawn(ArenaPlayer player, LocationType type, boolean random);

	public LocationType getLocationType();

	public ArenaTeam getTeam(ArenaPlayer player);
}
