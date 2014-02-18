package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.joining.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerEnterQueueEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	final JoinResult result;
	final TeamJoinObject tqo;

	public ArenaPlayerEnterQueueEvent(ArenaPlayer player, ArenaTeam team, TeamJoinObject tqo, JoinResult queueResult) {
		super(player);
		this.team = team;
		this.result = queueResult;
		this.tqo = tqo;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public JoinResult getQueueResult(){
		return result;
	}

	public Arena getArena(){
		return tqo.getJoinOptions().getArena();
	}
}
