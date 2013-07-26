package mc.alk.arena.executors;

import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.containers.LobbyContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;

public class VoteExecutor extends CustomCommandExecutor{
	BattleArenaController bac;
	public VoteExecutor(BattleArenaController bac){
		this.bac = bac;
	}

	@MCCommand
	public boolean voteForArena(ArenaPlayer ap, Arena arena){
		LobbyContainer pc = RoomController.getLobby(arena.getArenaType());
		if (pc == null){
			return sendMessage(ap, "&cThere is no lobby for "+arena.getArenaType());}
		if (!pc.isHandled(ap)){
			return sendMessage(ap, "&cYou aren't inside the lobby for "+arena.getArenaType());}
		MatchParams mp = ParamController.getMatchParamCopy(arena.getArenaType());
		if (!hasMPPerm(ap, mp,"join")){
			return sendMessage(ap, "&cYou don't have permission to vote in a &6" + mp.getCommand());}
		pc.castVote(ap,mp,arena);
		return true;
	}
}
