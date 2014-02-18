package mc.alk.arena.competition.util;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.queues.TeamJoinObject;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.scoreboard.ScoreboardFactory;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.TeamFactory;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.util.TeamUtil;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TeamJoinHandler implements TeamHandler {
    MatchParams matchParams;

    public static final TeamJoinResult CANTFIT = new TeamJoinResult(TeamJoinStatus.CANT_FIT,-1,null);
    ArenaScoreboard scoreboard;
    Map<ArenaTeam, LinkedList<SEntry>> reqPlaceHolderPlayers;
    Map<ArenaTeam, LinkedList<SEntry>> opPlaceHolderPlayers;
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
    ArenaObjective ao;
    boolean usePlaceHolder = false;
    boolean useCondensedPlaceHolder = false;

    public TeamJoinHandler(MatchParams params, Competition competition){
        this(params,competition,CompositeTeam.class);
    }

    public TeamJoinHandler(MatchParams params, Competition competition, Class<? extends ArenaTeam> clazz) {
        setParams(params);
        this.clazz = clazz;
        setCompetition(competition);
        if (competition == null){
            scoreboard = ScoreboardFactory.createScoreboard(String.valueOf(hashCode()), params);
            ao = scoreboard.createObjective("waiting",
                    "Queue Players", "&6Waiting Players", SAPIDisplaySlot.SIDEBAR, 100);
            ao.setDisplayTeams(false);
        }
        if (maxTeams <= -1) { /// TODO reenable
            int needed = 0;
            int optional = 0;
            for (int i = 0; i < maxTeams; i++) {
                ArenaTeam team = TeamFactory.createTeam(i, params, clazz);
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
                usePlaceHolder = true;
                reqPlaceHolderPlayers = new HashMap<ArenaTeam, LinkedList<SEntry>>();
                opPlaceHolderPlayers = new HashMap<ArenaTeam, LinkedList<SEntry>>();
            } else {
                useCondensedPlaceHolder = true;
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
                if (competition != null)
                    competition.addedToTeam(t, player);
                _addedToTeam(t,player);
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
        } else if (scoreboard != null ) {
            for (ArenaPlayer ap : players) {
                _addedToTeam(team,ap);
            }
        }
    }

    protected void addToTeam(ArenaTeam team, ArenaPlayer player) {
        team.addPlayer(player);
        nPlayers++;
        if (competition != null)
            competition.addedToTeam(team,player);
        _addedToTeam(team,player);
    }

    void _addedToTeam(ArenaTeam team, ArenaPlayer player) {
        if (competition == null && scoreboard != null) {
            scoreboard.addedToTeam(team, player);
            ao.setPoints(player, 10);
            removePlaceHolder(team, player);
        }
    }

    protected void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
        team.removePlayer(player);
        nPlayers--;
        if (competition != null){
            competition.removedFromTeam(team, player);
        } else if (scoreboard != null ){
            scoreboard.removedFromTeam(team, player);
            addPlaceholders(team, scoreboard.getTeam(team.getIDString()), team.size() < team.getMinPlayers());
        }
    }

    private LinkedList<SEntry> getTeamMap(ArenaTeam team) {
        LinkedList<SEntry> es;
        if (team.size() < team.getMinPlayers()) {
            es = reqPlaceHolderPlayers.get(team);
            if (es == null) {
                es = new LinkedList<SEntry>();
                reqPlaceHolderPlayers.put(team, es);
            }
        } else {
            es = opPlaceHolderPlayers.get(team);
            if (es == null) {
                es = new LinkedList<SEntry>();
                opPlaceHolderPlayers.put(team, es);
            }
        }
        return es;
    }

    protected void removePlaceHolder(ArenaTeam team, ArenaPlayer player){
        if (usePlaceHolder) {
            LinkedList<SEntry> list = reqPlaceHolderPlayers.get(team);
            if (list == null || list.isEmpty()) {
                list = opPlaceHolderPlayers.get(team);
            }
            if (list == null || list.isEmpty()) {
                return;
            }
            SEntry e = list.removeLast();
            scoreboard.removeEntry(e);
        }
    }

    private int getPlayerHolderSize(ArenaTeam team) {
        int size = 0;
        LinkedList<SEntry> es = reqPlaceHolderPlayers.get(team);
        if (es != null) {
            size += es.size();
        }

        es = opPlaceHolderPlayers.get(team);
        if (es != null) {
            size += es.size();
        }
        return size;
    }

    private void addPlaceholders(ArenaTeam team, STeam t, boolean needed) {
        if (usePlaceHolder) {
            String name;

            LinkedList<SEntry> r;
            int size = getPlayerHolderSize(team);
            int points;
            if (needed) {
                r = reqPlaceHolderPlayers.get(team);
                if (r == null) {
                    r = new LinkedList<SEntry>();
                    reqPlaceHolderPlayers.put(team, r);
                }
                name = "needed";
                points = 1;
            } else {
                r = opPlaceHolderPlayers.get(team);
                if (r == null) {
                    r = new LinkedList<SEntry>();
                    opPlaceHolderPlayers.put(team, r);
                }
                name = "open";
                points = 0;
            }

            String dis ="- "+name+" -" + team.getTeamChatColor() +TeamUtil.getTeamChatColor(size);
            if (dis.length() > 16) {
                dis = dis.substring(0, 16);
            }
            String pname = "p_" + team.getIndex() + "_" + size;
            SEntry e = scoreboard.getEntry(pname);
            if (e == null){
                e = scoreboard.createEntry(pname, dis);
            }

            r.addLast(e);
            t.addPlayer(e.getOfflinePlayer());
            ao.addEntry(e, points);
        }
    }

    private void addTeamPlaceholders(ArenaTeam team) {
        STeam t = scoreboard.getTeam(team.getIDString());
        if (usePlaceHolder){
            for (int i = 0; i < team.getMinPlayers() - team.size(); i++) {
                addPlaceholders(team, t, true);
            }
            for (int i = 0; i < team.getMaxPlayers() - team.getMinPlayers() - team.size(); i++) {
                addPlaceholders(team, t, false);
            }
        }

    }

    protected void addTeam(ArenaTeam team) {
        nPlayers+=team.size();
        team.setIndex(teams.size());
        teams.add(team);
        if (competition != null){
            competition.addTeam(team);
        } else if (scoreboard != null ) {
            TeamUtil.initTeam(team, matchParams);
            STeam t = scoreboard.addTeam(team);
            String teamSuffix = "("+(team.getMinPlayers() - team.size())+")";
            scoreboard.setEntryNameSuffix(t,  teamSuffix);
            addTeamPlaceholders(team);
        }
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
                } else if (scoreboard != null ){
                    scoreboard.leaving(t,p);
                    addPlaceholders(t, scoreboard.getTeam(t.getIDString()), t.size() < t.getMinPlayers());
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
