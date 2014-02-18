package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.joining.ArenaMatchQueue;

public class ArenaPlayerLeaveQueueEvent extends ArenaPlayerEvent {
//    final ArenaTeam team;
    final MatchParams params;
    final Arena arena;


    public ArenaPlayerLeaveQueueEvent(ArenaPlayer arenaPlayer, MatchParams params, Arena arena) {
        super(arenaPlayer);
        this.params = params;
        this.arena = arena;
    }

//    public ArenaTeam getTeam() {
//        return team;
//    }

    public MatchParams getParams() {
        return params;
    }

	public Arena getArena() {
        return arena;
    }

    public int getPlayersInArenaQueue(Arena arena) {
        return ArenaMatchQueue.getPlayersInArenaQueue(arena);
    }
//
//	public int getNPlayers(){
//		return ptp.getNPlayersInQueue();
//	}
}
