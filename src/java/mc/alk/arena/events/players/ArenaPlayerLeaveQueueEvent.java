package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerLeaveQueueEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	final MatchParams params;
	final ParamTeamPair ptp;

	public ArenaPlayerLeaveQueueEvent(ArenaPlayer arenaPlayer, ArenaTeam team,
			MatchParams params, ParamTeamPair ptp) {
		super(arenaPlayer);
		this.team = team;
		this.params = params;
		this.ptp = ptp;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public MatchParams getParams(){
		return params;
	}

	public Arena getArena(){
		return ptp.getArena();
	}

	public int getNPlayers(){
		return ptp.getNPlayersInQueue();
	}
}
