package mc.alk.arena.objects.teams;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.util.TeamUtil;

import java.util.Set;

public class TeamFactory {

	public static ArenaTeam createCompositeTeam(int index, MatchParams params, ArenaPlayer p) {
        CompositeTeam ct = (CompositeTeam) createCompositeTeam(index, params);
        ct.addPlayer(p);
        return ct;
    }

    public static CompositeTeam createCompositeTeam(int index, MatchParams params, Set<ArenaPlayer> players) {
        CompositeTeam ct = (CompositeTeam) createCompositeTeam(index, params);
        ct.addPlayers(players);
        return ct;
    }

    public static CompositeTeam createCompositeTeam(MatchParams params, Set<ArenaPlayer> players) {
        CompositeTeam ct = (CompositeTeam) createCompositeTeam(-1, params);
        ct.addPlayers(players);
		return ct;
	}

	public static CompositeTeam createCompositeTeam() {
		return new CompositeTeam();
	}

    public static ArenaTeam createCompositeTeam(Integer index, MatchParams params) {
        ArenaTeam at = new CompositeTeam();
        if (index != null && index != -1) {
            at.setIndex(index);
        }
        TeamUtil.initTeam(at, params);
        return at;
    }

}
