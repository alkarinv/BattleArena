package mc.alk.arena.controllers.joining.scoreboard;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.Collection;

public class AbridgedScoreboard implements WaitingScoreboard{

    public AbridgedScoreboard(MatchParams matchParams) {

    }

    @Override
    public boolean addTeam(ArenaTeam team) {
        return false;
    }

    @Override
    public boolean removeTeam(ArenaTeam team) {
        return false;
    }

    @Override
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> players) {

    }

    @Override
    public boolean addedToTeam(ArenaTeam team, ArenaPlayer player) {
        return false;
    }

    @Override
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> players) {

    }

    @Override
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {

    }
}
