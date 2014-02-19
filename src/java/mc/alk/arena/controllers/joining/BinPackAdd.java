package mc.alk.arena.controllers.joining;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.joining.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamFactory;

/**
 * When there is an infinite number of teams
 * @author alkarin
 *
 */
public class BinPackAdd extends AbstractJoinHandler {
    boolean full = false;

    public BinPackAdd(MatchParams params, Competition competition, Class<? extends ArenaTeam> clazz) throws NeverWouldJoinException {
        super(params, competition,clazz);
    }

    @Override
    public boolean switchTeams(ArenaPlayer player, Integer toTeamIndex) {
        if (toTeamIndex>= maxTeams)
            return false;
        ArenaTeam oldTeam = player.getTeam();
        if (oldTeam == null || oldTeam.size()-1 < oldTeam.getMinPlayers())
            return false;

        ArenaTeam team = addToPreviouslyLeftTeam(player);
        if (team != null)
            return true;
        team = teams.get(toTeamIndex);

        final int size = team.size()+1;
        if (size <= team.getMaxPlayers() && size <= team.getMinPlayers()){
            removeFromTeam(oldTeam,player);
            addToTeam(team, player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public TeamJoinResult joiningTeam(TeamJoinObject tqo) {
        ArenaTeam team = tqo.getTeam();
        if (team.size()==1){

            ArenaTeam oldTeam = addToPreviouslyLeftTeam(team.getPlayers().iterator().next());
            if (oldTeam != null){
                team.setIndex(oldTeam.getIndex());
                return new TeamJoinResult(TeamJoinStatus.ADDED_TO_EXISTING,oldTeam.getMinPlayers() - oldTeam.size(), oldTeam);
            }
        }
        /// So we couldnt add them to an existing team
        /// Can we add them to a new team
        if (teams.size() < maxTeams){
            ArenaTeam ct = TeamFactory.createTeam(teams.size(), matchParams, clazz);
            ct.addPlayers(team.getPlayers());
            team.setIndex(ct.getIndex());
            if (ct.size() == ct.getMaxPlayers()){
                addTeam(ct);
                return new TeamJoinResult(TeamJoinStatus.ADDED, ct.getMinPlayers() - ct.size(),ct);
            } else {
                addTeam(ct);
                return new TeamJoinResult(TeamJoinStatus.ADDED_STILL_NEEDS_PLAYERS,
                        ct.getMinPlayers() - ct.size(),ct);
            }
        }

        for (ArenaTeam t: teams){
            final int size = t.size()+team.size();
            if (size <= t.getMaxPlayers()){
                t.addPlayers(team.getPlayers());
                if ( size >= t.getMinPlayers()){ /// the new team would be a valid range, add them
                    team.setIndex(t.getIndex());
                    addToTeam(t, team.getPlayers());
                    return new TeamJoinResult(TeamJoinStatus.ADDED, 0,t);
                } else {
                    return new TeamJoinResult(TeamJoinStatus.ADDED_STILL_NEEDS_PLAYERS, t.getMinPlayers() - t.size(),t);
                }
            }
        }
        /// sorry peeps.. full up
        return CANTFIT;
    }

    @Override
    public String toString(){
        return (competition == null ? " null" : "["+competition.getParams().getName()) +":JH:BinPackAdd]";
    }
}
