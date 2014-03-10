package mc.alk.arena.controllers.joining.scoreboard;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.scoreboard.ScoreboardFactory;
import mc.alk.arena.objects.scoreboard.WaitingScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

import java.util.Collection;

public class AbridgedScoreboard implements WaitingScoreboard {
    final ArenaScoreboard scoreboard;
    final ArenaObjective ao;


    public AbridgedScoreboard(MatchParams params) {
        scoreboard = ScoreboardFactory.createScoreboard(String.valueOf(this.hashCode()), params);
        ao = scoreboard.createObjective("waiting",
                "Queue Players", "&6Waiting Players", SAPIDisplaySlot.SIDEBAR, 100);
        ao.setDisplayPlayers(false);
    }

    @Override
    public void addedToTeam(ArenaTeam team, ArenaPlayer player) {
        STeam t = scoreboard.addedToTeam(team, player);
        scoreboard.setScoreboard(player.getPlayer());
        setTeamSuffix(team,t);
        ao.setTeamPoints(t, team.size());
    }

    @Override
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> players) {
        STeam t = scoreboard.getTeam(team.getIDString());
        for (ArenaPlayer player : players) {
            scoreboard.addedToTeam(team, player);
            scoreboard.setScoreboard(player.getPlayer());
            setTeamSuffix(team, t);
        }
        ao.setTeamPoints(t, team.size());
    }

    @Override
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
        STeam t = scoreboard.getTeam(team.getIDString());
        scoreboard.removedFromTeam(team,player);
        setTeamSuffix(team, t);
        ao.setTeamPoints(t, team.size());
    }

    @Override
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> players) {
        STeam t = scoreboard.getTeam(team.getIDString());
        for (ArenaPlayer player : players) {
            scoreboard.removedFromTeam(team,player);
            setTeamSuffix(team, t);
        }
        ao.setTeamPoints(t, team.size());
    }

    private void setTeamSuffix(ArenaTeam team, STeam t) {
        String s;
        if (team.getMinPlayers() == team.getMaxPlayers()) {
            s = " " + team.size() + "/" + team.getMinPlayers();
        } else {
            s = " " + team.size() + "/" + team.getMinPlayers() + "/" + team.getMaxPlayers();
        }
        scoreboard.setEntryNameSuffix(t, s);
    }

    @Override
    public boolean addedTeam(ArenaTeam team) {
        STeam t = scoreboard.addTeam(team);
        setTeamSuffix(team, t);
        return true;
    }

    @Override
    public boolean removedTeam(ArenaTeam team) {
        STeam t = scoreboard.getTeam(team.getIDString());
        scoreboard.removeEntry(t);
        return false;
    }
}
