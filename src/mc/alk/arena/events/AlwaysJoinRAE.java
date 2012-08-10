package mc.alk.arena.events;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.entity.Player;


public class AlwaysJoinRAE extends ReservedArenaEvent {
	public AlwaysJoinRAE(MatchParams params) {
		super(params);
	}

	@Override
	public boolean canLeave(Player p) {
		/// you can enter and leave at any time
		return true;
	}

	@Override
	public boolean canJoin() {
		/// can join at any time, even if its been running as long as super allows
		return isOpen() || isRunning();
	}

	@Override
	public boolean canJoin(Team t) {
		return canJoin();
	}
}
