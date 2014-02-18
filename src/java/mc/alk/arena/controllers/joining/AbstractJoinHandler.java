package mc.alk.arena.controllers.joining;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.joining.scoreboard.AbridgedScoreboard;
import mc.alk.arena.controllers.joining.scoreboard.FullScoreboard;
import mc.alk.arena.controllers.joining.scoreboard.WaitingScoreboard;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.joining.JoinHandler;
import mc.alk.arena.objects.joining.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.TeamFactory;
import mc.alk.arena.objects.teams.TeamHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractJoinHandler implements JoinHandler, TeamHandler {
    public static final TeamJoinResult CANTFIT = new TeamJoinResult(TeamJoinStatus.CANT_FIT,-1,null);

    MatchParams matchParams;

    List<ArenaTeam> teams = new ArrayList<ArenaTeam>();

    Competition competition;
    int minTeams,maxTeams;
    Class<? extends ArenaTeam> clazz;
    int nPlayers;
    WaitingScoreboard scoreboard;

    public Collection<ArenaPlayer> getPlayers() {
        List<ArenaPlayer> players = new ArrayList<ArenaPlayer>();
        for (ArenaTeam at: teams) {
            players.addAll(at.getPlayers());
        }
        return players;
    }


    public static enum TeamJoinStatus{
        ADDED, CANT_FIT, ADDED_TO_EXISTING, ADDED_STILL_NEEDS_PLAYERS
    }

    public static class TeamJoinResult{
        final public TeamJoinStatus status;
        final public int remaining;
        final public ArenaTeam team;

        public TeamJoinResult(TeamJoinStatus status, int remaining, ArenaTeam team){
            this.status = status; this.remaining = remaining; this.team = team;}
        public TeamJoinStatus getEventType(){ return status;}
        public int getRemaining(){return remaining;}
    }



    public AbstractJoinHandler(MatchParams params, Competition competition){
        this(params,competition,CompositeTeam.class);
    }

    public AbstractJoinHandler(MatchParams params, Competition competition, Class<? extends ArenaTeam> clazz) {
        setParams(params);
        this.clazz = clazz;
        setCompetition(competition);
        initWaitingScoreboard();
    }

    private void initWaitingScoreboard() {
        if (maxTeams <= -1) {
            int needed = 0;
            int optional = 0;
            for (int i = 0; i < maxTeams; i++) {
                ArenaTeam team = TeamFactory.createTeam(i, matchParams, clazz);
                if (team.getMinPlayers() < 16) {
                    needed += team.getMinPlayers();
                    if (team.getMaxPlayers() < 100) {
                        optional += team.getMaxPlayers();
                    } else {
                        optional += 1000;
                    }
                }
            }
            if (needed + optional <= 16) {
                scoreboard = new FullScoreboard(matchParams);
            } else {
                scoreboard = new AbridgedScoreboard(matchParams);
            }
        }
    }

    public abstract boolean switchTeams(ArenaPlayer player, Integer toTeamIndex);

    public void setCompetition(Competition comp) {
        this.competition = comp;
    }

    public void setParams(MatchParams mp) {
        this.matchParams = mp;
        this.minTeams = mp.getMinTeams();
        this.maxTeams = mp.getMaxTeams();
    }

    protected ArenaTeam addToPreviouslyLeftTeam(ArenaPlayer player) {
        for (ArenaTeam t: teams){
            if (t.hasLeft(player)){
                t.addPlayer(player);
                nPlayers++;
                JoinHandler jh = competition != null ? competition : scoreboard;
                if (jh != null) jh.addedToTeam(t,player);
                return t;
            }
        }
        return null;
    }


    @Override
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> players) {
        team.addPlayers(players);
        nPlayers+=players.size();
        JoinHandler jh = competition != null ? competition : scoreboard;
        if (jh != null) jh.addedToTeam(team,players);
    }

    @Override
    public boolean addedToTeam(ArenaTeam team, ArenaPlayer player) {
        team.addPlayer(player);
        nPlayers++;
        JoinHandler jh = competition != null ? competition : scoreboard;
        return jh != null && jh.addedToTeam(team,player);
    }

    @Override
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
        team.removePlayer(player);
        nPlayers--;
        JoinHandler jh = competition != null ? competition : scoreboard;
        if (jh != null) jh.removedFromTeam(team,player);

//        if (competition != null){
//            competition.removedFromTeam(team, player);
//        } else if (scoreboard != null ){
//            scoreboard.removedFromTeam(team, player);
////            addPlaceholders(team, scoreboard.getTeam(team.getIDString()));
//        }
    }

    @Override
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> player) {

    }

    @Override
    public boolean removeTeam(ArenaTeam team) {
        return true;
    }

    @Override
    public boolean addTeam(ArenaTeam team) {
        nPlayers+=team.size();
        team.setIndex(teams.size());
        teams.add(team);
        JoinHandler jh = competition != null ? competition : scoreboard;
        return jh != null && jh.addTeam(team);
//        if (competition != null){
//            competition.addTeam(team);
//        } else if (scoreboard != null ) {
//            scoreboard.addTeam(team);
////            TeamUtil.initTeam(team, matchParams);
////            STeam t = scoreboard.addTeam(team);
////            String teamSuffix = "(" + (team.getMinPlayers() - team.size()) + ")";
////            scoreboard.setEntryNameSuffix(t, teamSuffix);
////            addTeamPlaceholders(team);
//        }
//        return true;
    }

    public void deconstruct() {
        teams.clear();
        matchParams = null;
    }

    public abstract TeamJoinResult joiningTeam(TeamJoinObject tqo);

    public boolean canLeave(ArenaPlayer p) {
        return true;
    }

    public boolean leave(ArenaPlayer p) {
        for (ArenaTeam t: teams){
            if (t.hasMember(p)) {
                nPlayers--;
                t.removePlayer(p);
                if (competition!=null) {
                    competition.leave(p);
                } else if (scoreboard != null ) {
                    scoreboard.removedFromTeam(t,p);
//                    scoreboard.leave(p);
//                    scoreboard.leaving(t, p);
//                    STeam st = scoreboard.getTeam(t.getIDString());
//                    addPlaceholders(t, st);
                }
                return true;
            }
        }
        return false;
    }

    public Set<ArenaPlayer> getExcludedPlayers() {
        Set<ArenaPlayer> tplayers = new HashSet<ArenaPlayer>();
        for (ArenaTeam t: teams){
            tplayers.addAll(t.getPlayers());
        }
        return tplayers;
    }

    public List<ArenaTeam> removeImproperTeams(){
        List<ArenaTeam> improper = new ArrayList<ArenaTeam>();
        for (ArenaTeam t : teams) {
            if (t.size() < t.getMinPlayers()|| t.size() > t.getMaxPlayers()) {
                improper.add(t);
                nPlayers -= t.size();
            }
        }
        teams.removeAll(improper);
        return improper;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean hasEnough(int allowedTeamSizeDifference){
        if (teams ==null)
            return false;
        final int teamssize = teams.size();
        if (teamssize < minTeams)
            return false;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int valid = 0;
        for (ArenaTeam t: teams){
            final int tsize = t.size();
            if (tsize ==0)
                continue;
            if (tsize < min) min = tsize;
            if (tsize > max) max = tsize;

            if (max - min > allowedTeamSizeDifference)
                return false;

            if (tsize < t.getMinPlayers() || tsize > t.getMaxPlayers())
                continue;
            valid++;
        }
        return valid >= minTeams && valid <= maxTeams;
    }

    public boolean isFull() {
        if (maxTeams == CompetitionSize.MAX )
            return false;
        /// Check to see if we have filled up our number of teams
        if ( maxTeams > teams.size()){
            return false;}
        /// Check to see if there is any space left on the team
        for (ArenaTeam t: teams){
            if (t.size() < t.getMaxPlayers()){
                return false;}
        }
        /// we can't add a team.. and all teams are full
        return true;
    }
    public List<ArenaTeam> getTeams() {
        return teams;
    }

    public int getnPlayers() {
        return nPlayers;
    }
//    public abstract void switchTeams(ArenaPlayer p, Integer index);

    public String toString() {
        return "[TJH " + this.hashCode() + "]";
    }
}
