package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.pairs.QueueResult;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerEnterQueueEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	QueueResult result;

	public ArenaPlayerEnterQueueEvent(ArenaPlayer player, ArenaTeam team, QueueResult queueResult) {
		super(player);
		this.team = team;
		this.result = queueResult;
	}

	public ArenaTeam getTeam() {
		return team;
	}
	public QueueResult getQueueResult(){
		return result;
	}
}
