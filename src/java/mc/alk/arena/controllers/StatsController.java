package mc.alk.arena.controllers;

import mc.alk.arena.objects.ArenaParams;

import java.util.HashMap;
import java.util.Map;

/**
 * @author alkarin
 */
public enum StatsController {
    INSTANCE;

    Map<ArenaParams, CompetitionStat> stats = new HashMap<ArenaParams, CompetitionStat>();

    class CompetitionStat{
        int nComps;
        int totalPlayers;
    }

    public static void addCompetition(ArenaParams params, int nPlayers){
        CompetitionStat stat = INSTANCE.getOrCreateStat(params);
        stat.nComps++;
        stat.totalPlayers += nPlayers;
    }

    private CompetitionStat getOrCreateStat(ArenaParams params) {
        CompetitionStat stat = stats.get(params);
        if (stat ==null) {
            stat = new CompetitionStat();
            stats.put(params, stat);
        }
        return stat;
    }


}
