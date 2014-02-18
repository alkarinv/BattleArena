package mc.alk.arena.controllers.joining.scoreboard;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.scoreboard.ScoreboardFactory;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.TeamUtil;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class FullScoreboard implements WaitingScoreboard {
    Map<ArenaTeam, LinkedList<SEntry>> reqPlaceHolderPlayers = new HashMap<ArenaTeam, LinkedList<SEntry>>();

    Map<ArenaTeam, LinkedList<SEntry>> opPlaceHolderPlayers = new HashMap<ArenaTeam, LinkedList<SEntry>>();
    ArenaScoreboard scoreboard;
    ArenaObjective ao;

    public FullScoreboard(MatchParams params) {
        scoreboard = ScoreboardFactory.createScoreboard(String.valueOf(this.hashCode()), params);
        ao = scoreboard.createObjective("waiting",
                "Queue Players", "&6Waiting Players", SAPIDisplaySlot.SIDEBAR, 100);
        ao.setDisplayTeams(false);
    }


    private int getReqSize(ArenaTeam team) {
        return reqPlaceHolderPlayers.containsKey(team) ? reqPlaceHolderPlayers.get(team).size() : 0;
    }


    private void addPlaceholder(ArenaTeam team, STeam t) {
        String name;

        LinkedList<SEntry> r;
        int index;
        int points;
        if (getReqSize(team) < team.getMinPlayers()) {
            r = reqPlaceHolderPlayers.get(team);
            if (r == null) {
                r = new LinkedList<SEntry>();
                reqPlaceHolderPlayers.put(team, r);
            }
            name = "needed";
            points = 1;
            index = r.size();
        } else {
            r = opPlaceHolderPlayers.get(team);
            if (r == null) {
                r = new LinkedList<SEntry>();
                opPlaceHolderPlayers.put(team, r);
            }
            name = "open";
            points = 0;
            index = team.getMinPlayers() + r.size();
        }

        String dis = "- " + name + " -" + team.getTeamChatColor() + TeamUtil.getTeamChatColor(index);
        if (dis.length() > 16) {
            dis = dis.substring(0, 16);
        }

        String pname = "p_" + team.getIndex() + "_" + index;
        SEntry e = scoreboard.getEntry(pname);
        if (e == null) {
            e = scoreboard.createEntry(pname, dis);
        }

        r.addLast(e);
        t.addPlayer(e.getOfflinePlayer());
        ao.addEntry(e, points);

    }

    private void removePlaceHolder(ArenaTeam team, ArenaPlayer player){
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

    @Override
    public boolean addedToTeam(ArenaTeam team, ArenaPlayer player) {
        removePlaceHolder(team,player);
        return true;
    }

    @Override
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> player) {

    }


    @Override
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {

    }

    @Override
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> player) {

    }

    @Override
    public boolean addTeam(ArenaTeam team) {
        STeam t = scoreboard.getTeam(team.getIDString());
        for (int i = 0; i < team.getMaxPlayers() - team.size(); i++) {
            addPlaceholder(team, t);
        }
        return true;
    }

    @Override
    public boolean removeTeam(ArenaTeam team) {
        STeam t = scoreboard.getTeam(team.getIDString());
        scoreboard.removeEntry(t);
        return false;
    }
}
