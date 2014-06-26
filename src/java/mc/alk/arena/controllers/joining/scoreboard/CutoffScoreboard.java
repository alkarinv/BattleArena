package mc.alk.arena.controllers.joining.scoreboard;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.scoreboard.ScoreboardFactory;
import mc.alk.arena.objects.scoreboard.WaitingScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamFactory;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.TeamUtil;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CutoffScoreboard implements WaitingScoreboard {
    Map<Integer, LinkedList<SEntry>> reqPlaceHolderPlayers = new HashMap<Integer, LinkedList<SEntry>>();

    Map<Integer, LinkedList<SEntry>> opPlaceHolderPlayers = new HashMap<Integer, LinkedList<SEntry>>();
    ArenaScoreboard scoreboard;
    ArenaObjective ao;
    final int minTeams;
    Countdown countdown;
    public CutoffScoreboard(MatchParams params, List<ArenaTeam> teams) {
        scoreboard = ScoreboardFactory.createScoreboard(String.valueOf(this.hashCode()), params);
        ao = scoreboard.createObjective("waiting",
                "Queue Players", "&6Waiting Players", SAPIDisplaySlot.SIDEBAR, 100);
        ao.setDisplayTeams(false);
        minTeams = params.getMinTeams();
        int maxTeams = params.getMaxTeams();
        int count = 0;
        int ppteam = 15;
        if (maxTeams < 16) {
            ppteam = 15 / maxTeams;
        }
        for (int i = 0; i <maxTeams && count < 15; i++) {
//            Log.debug("&4i = " + i);
            ArenaTeam team = i < teams.size() ? teams.get(i) : TeamFactory.createCompositeTeam(i, params);
            team.setIDString(String.valueOf(team.getIndex()));
            STeam t = scoreboard.addTeam(team);
            for (int j = 0; j < team.getMaxPlayers() && count < 15 && j < ppteam; j++) {
                count++;
                addPlaceholder(team, t, i >= minTeams);
            }
        }
        if (params.getForceStartTime() >0 &&
                params.getForceStartTime() != ArenaSize.MAX
                && !params.getMaxPlayers().equals(params.getMinPlayers())
                ){
            countdown = new Countdown(BattleArena.getSelf(), params.getForceStartTime(),1,new DisplayCountdown());
        }
    }

    class DisplayCountdown implements CountdownCallback {
        @Override
        public boolean intervalTick(int secondsRemaining) {
            if (secondsRemaining == 0){
                ao.setDisplayNameSuffix("");
            } else {
                ao.setDisplayNameSuffix(" &e("+secondsRemaining+")");
            }
            return true;
        }
    }

    @Override
    public void setRemainingSeconds(int seconds) {
        if (countdown !=null){
            countdown.stop();
        }
        countdown = new Countdown(BattleArena.getSelf(), seconds,1,new DisplayCountdown());
    }

    private int getReqSize(int teamIndex) {
        return reqPlaceHolderPlayers.containsKey(teamIndex) ?
                reqPlaceHolderPlayers.get(teamIndex).size() : 0;
    }


    private void addPlaceholder(ArenaTeam team, STeam t, boolean optionalTeam) {
        String name;

        LinkedList<SEntry> r;
        int index;
        int points;
        if (!optionalTeam && getReqSize(team.getIndex()) < team.getMinPlayers()) {
            r = reqPlaceHolderPlayers.get(team.getIndex());
            if (r == null) {
                r = new LinkedList<SEntry>();
                reqPlaceHolderPlayers.put(team.getIndex(), r);
            }
            name = "needed";
            points = 1;
            index = r.size();
        } else {
            r = opPlaceHolderPlayers.get(team.getIndex());
            if (r == null) {
                r = new LinkedList<SEntry>();
                opPlaceHolderPlayers.put(team.getIndex(), r);
            }
            name = "open";
            points = 0;
            index = optionalTeam ? r.size() : team.getMinPlayers() + r.size();
        }

        String dis = "- " + name + " -" + team.getTeamChatColor() + TeamUtil.getTeamChatColor(index);
        SEntry e = scoreboard.getEntry(dis);
        if (e == null) {
            e = scoreboard.createEntry(OfflinePlayerTeams.getOfflinePlayer(dis), dis);
            ao.addEntry(e, points);
        } else {
            ao.setPoints(e, points);
        }

        r.addLast(e);
        t.addPlayer(e.getOfflinePlayer());
    }

    private void removePlaceHolder(int teamIndex){
        LinkedList<SEntry> list = reqPlaceHolderPlayers.get(teamIndex);
        if (list == null || list.isEmpty()) {
            list = opPlaceHolderPlayers.get(teamIndex);
        }
        if (list == null || list.isEmpty()) {
            return;
        }
        SEntry e = list.removeLast();
        scoreboard.removeEntry(e);
    }

    @Override
    public void addedToTeam(ArenaTeam team, ArenaPlayer player) {
        STeam t = scoreboard.getTeam(String.valueOf(team.getIndex()));
        scoreboard.addedToTeam(t, player);
        ao.setPoints(player, 10);
        removePlaceHolder(team.getIndex());
    }

    @Override
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> players) {
        for (ArenaPlayer player : players) {
            addedToTeam(team,player);
        }
    }

    @Override
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
        STeam t = scoreboard.getTeam(String.valueOf(team.getIndex()));
        scoreboard.removedFromTeam(t,player);
        addPlaceholder(team, t,team.getIndex()>= minTeams);
    }

    @Override
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> players) {
        STeam t = scoreboard.getTeam(String.valueOf(team.getIndex()));
        for (ArenaPlayer player : players) {
            scoreboard.removedFromTeam(team,player);
            addPlaceholder(team, t, team.getIndex()>= minTeams);
        }
    }

    @Override
    public boolean addedTeam(ArenaTeam team) {
        scoreboard.createTeamEntry(String.valueOf(team.getIndex()), "");
        for (ArenaPlayer ap : team.getPlayers()) {
            addedToTeam(team, ap);
        }
        return true;
    }

    @Override
    public boolean removedTeam(ArenaTeam team) {
        STeam t = scoreboard.getTeam(String.valueOf(team.getIndex()));
        scoreboard.removeEntry(t);
        return false;
    }

    @Override
    public ArenaScoreboard getScoreboard() {
        return scoreboard;
    }


}
