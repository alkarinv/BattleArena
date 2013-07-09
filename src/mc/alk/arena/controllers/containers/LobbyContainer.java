package mc.alk.arena.controllers.containers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;

public class LobbyContainer extends RoomContainer{
	Map<ArenaPlayer,Arena> votedFor = new HashMap<ArenaPlayer,Arena>();
	Map<Arena, Integer> arenaVotes = new ConcurrentHashMap<Arena, Integer>();
	Set<ArenaPlayer> waitingForMatch = new HashSet<ArenaPlayer>();

	public LobbyContainer(LocationType type) {
		super(type);
	}

	public LobbyContainer(MatchParams params, LocationType type){
		super(params,type);
	}

	public void castVote(ArenaPlayer ap, MatchParams mp, Arena arena) {
		if (!waitingForMatch.contains(ap)){
			return;
		}

		Arena a = votedFor.remove(ap);
		if (a != null){
			decrementVote(a);}
		Integer nVotes = incrementVote(arena);
	}

	private Integer incrementVote(Arena arena) {
		Integer count = arenaVotes.get(arena);
		if (count == null){
			count = 1;
			arenaVotes.put(arena, count);
		} else {
			arenaVotes.put(arena, ++count);
		}
		return count;
	}

	private void decrementVote(Arena arena) {
		Integer count = arenaVotes.get(arena);
		if (count != null){
			count--;
			arenaVotes.put(arena, count);
		}
	}

	@Override
	public void onPostEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		super.onPostEnter(player, apte);
		if (apte.getSrcType() == LocationType.HOME){
			waitingForMatch.add(player);}
	}

	@Override
	public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
		super.onPostLeave(player, apte);
		waitingForMatch.remove(player);
	}

}
