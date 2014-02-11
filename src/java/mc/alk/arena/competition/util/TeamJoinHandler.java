package mc.alk.arena.competition.util;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.queues.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.TeamHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TeamJoinHandler implements TeamHandler {
    MatchParams matchParams;

    public static final TeamJoinResult CANTFIT = new TeamJoinResult(TeamJoinStatus.CANT_FIT,-1,null);

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

    List<ArenaTeam> teams = new ArrayList<ArenaTeam>();

    Competition competition;
    int minTeams,maxTeams;
    Class<? extends ArenaTeam> clazz;
    int nPlayers;

    public TeamJoinHandler(MatchParams params, Competition competition){
        this(params,competition,CompositeTeam.class);
    }

    public TeamJoinHandler(MatchParams params, Competition competition, Class<? extends ArenaTeam> clazz) {
        setParams(params);
        this.clazz = clazz;
        setCompetition(competition);
    }

    public abstract boolean switchTeams(ArenaPlayer player, Integer toTeamIndex);

    public void setCompetition(Competition comp) {
        this.competition = comp;
    }

    public void setParams(MatchParams mp) {
        this.matchParams = mp;
//        this.minTeamSize = mp.getMinTeamSize(); this.maxTeamSize = mp.getMaxTeamSize();
        this.minTeams = mp.getMinTeams();
        this.maxTeams = mp.getMaxTeams();
    }

    protected ArenaTeam addToPreviouslyLeftTeam(ArenaPlayer player) {
        for (ArenaTeam t: teams){
            if (t.hasLeft(player)){
                t.addPlayer(player);
                nPlayers++;
                if (competition != null){
                    competition.addedToTeam(t,player);}
                return t;
            }
        }
        return null;
    }

    protected void addToTeam(ArenaTeam team, Set<ArenaPlayer> players) {
        team.addPlayers(players);
        nPlayers+=players.size();

        if (competition != null){
            competition.addedToTeam(team,players);
        }
    }

    protected void addToTeam(ArenaTeam team, ArenaPlayer player) {
        team.addPlayer(player);
        nPlayers++;
        if (competition != null){
            competition.addedToTeam(team,player);
        }
    }

    protected void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
        team.removePlayer(player);
        nPlayers--;
        if (competition != null){
            competition.removedFromTeam(team,player);
        }
    }

    protected void addTeam(ArenaTeam team) {
        nPlayers+=team.size();
        team.setIndex(teams.size());
        teams.add(team);
        if (competition != null){
            competition.addTeam(team);
        }
    }

    public void deconstruct() {
//        for (ArenaTeam t: pickupTeams){
//            TeamController.removeTeamHandler(t, this);
//        }
//        pickupTeams.clear();
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
                }
//                pickupTeams.remove(t);
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
                TeamController.removeTeamHandler(t, this);
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
