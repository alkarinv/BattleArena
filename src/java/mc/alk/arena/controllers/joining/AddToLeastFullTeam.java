package mc.alk.arena.controllers.joining;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.events.Event.TeamSizeComparator;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.joining.TeamJoinObject;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.JoinOptions.JoinOption;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddToLeastFullTeam extends AbstractJoinHandler {

    public AddToLeastFullTeam(MatchParams params, Competition competition, Class<? extends ArenaTeam> clazz) throws NeverWouldJoinException{
        super(params,competition,clazz);
        if (minTeams == ArenaSize.MAX || maxTeams == ArenaSize.MAX)
            throw new NeverWouldJoinException("If you add players by adding them to the next team in the list, there must be a finite number of players");
        /// Lets add in all our teams first
        if (minTeams > Defaults.MAX_TEAMS)
            throw new NeverWouldJoinException("You can't make more than "+Defaults.MAX_TEAMS +" teams");
        for (int i=0;i<minTeams;i++){
            ArenaTeam team = TeamFactory.createTeam(i, params,clazz);
            addTeam(team);
        }
    }

    @Override
    public boolean switchTeams(ArenaPlayer player, Integer toTeamIndex) {
        ArenaTeam oldTeam = player.getTeam();
        if (oldTeam == null || oldTeam.size()-1 < oldTeam.getMinPlayers())
            return false;
        ArenaTeam team = addToPreviouslyLeftTeam(player);
        if (team != null)
            return true;

        /// Try to let them join their specified team if possible
        if (toTeamIndex < maxTeams) { /// they specified a team index within range
            team = teams.get(toTeamIndex);
            if (team.size()+1 <= team.getMaxPlayers()){
                removeFromTeam(oldTeam, player);
                addToTeam(team, player);
                return true;
            }
        }
        return false;
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
        /// Try to let them join their specified team if possible
        JoinOptions jo = tqo.getJoinOptions();
        if (jo != null && jo.hasOption(JoinOption.TEAM)){
            Integer index = (Integer) jo.getOption(JoinOption.TEAM);
            if (index < maxTeams){ /// they specified a team index within range
                ArenaTeam baseTeam= teams.get(index);
                TeamJoinResult tjr = teamFits(baseTeam, team);
                if (tjr != CANTFIT)
                    return tjr;
            }
        }
        boolean hasZero = false;
        for (ArenaTeam t : teams){
            if (t.size() == 0){
                hasZero = true;
                break;
            }
        }
        /// Since this is nearly the same as BinPack add... can we merge somehow easily?
        if (!hasZero && teams.size() < maxTeams){
            ArenaTeam ct = TeamFactory.createTeam(teams.size(), matchParams, clazz);
            ct.setCurrentParams(tqo.getMatchParams());
            ct.addPlayers(team.getPlayers());
            team.setIndex(ct.getIndex());
            if (ct.size() >= ct.getMinPlayers()){
                addTeam(ct);
                return new TeamJoinResult(TeamJoinStatus.ADDED, ct.getMinPlayers() - ct.size(),ct);
            } else {
                addTeam(ct);
                return new TeamJoinResult(TeamJoinStatus.ADDED_STILL_NEEDS_PLAYERS, ct.getMinPlayers() - ct.size(),ct);
            }
        } else {
            /// Try to fit them with an existing team
            List<ArenaTeam> sortedBySize = new ArrayList<ArenaTeam>(teams);
            Collections.sort(sortedBySize, new TeamSizeComparator());
            for (ArenaTeam baseTeam : sortedBySize){
                TeamJoinResult tjr = teamFits(baseTeam, team);
                if (tjr != CANTFIT)
                    return tjr;
            }
            /// sorry peeps.. full up
            return CANTFIT;
        }
    }

    private TeamJoinResult teamFits(ArenaTeam baseTeam, ArenaTeam team) {
        if ( baseTeam.size() + team.size() <= baseTeam.getMaxPlayers()){
            team.setIndex(baseTeam.getIndex());
            addToTeam(baseTeam, team.getPlayers());
            if (baseTeam.size() == 0){
                return new TeamJoinResult(TeamJoinStatus.ADDED, baseTeam.getMinPlayers() - baseTeam.size(),baseTeam);
            } else {
                return new TeamJoinResult(TeamJoinStatus.ADDED_TO_EXISTING,
                        baseTeam.getMinPlayers() - baseTeam.size(),baseTeam);
            }
        }
        return CANTFIT;
    }

    @Override
    public String toString(){
        return "["+competition.getParams().getName() +":JH:AddToLeast]";
    }


}
