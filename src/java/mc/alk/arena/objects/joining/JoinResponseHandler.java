package mc.alk.arena.objects.joining;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.Collection;

public interface JoinResponseHandler {

    /**
     * Added a team to this object
     * @param team ArenaTeam
     * @return true if the team was added, false if not
     */
    public boolean addedTeam(ArenaTeam team);

    /**
     * Removed a team from the object
     * @param team ArenaTeam
     * @return whether or not the team was removed
     */
    public boolean removedTeam(ArenaTeam team);

    /**
     * Signify that the set of players were added to the team
     * @param team ArenaTeam
     * @param players ArenaPlayers
     */
    public void addedToTeam(ArenaTeam team, Collection<ArenaPlayer> players);

    /**
     * Signify that the set of players were added to the team
     * @param team ArenaTeam
     * @param player ArenaPlayer
     */
    public void addedToTeam(ArenaTeam team, ArenaPlayer player);

    /**
     * Signify that the set of players were removed from the team
     * @param team ArenaTeam
     * @param players ArenaPlayers
     */
    public void removedFromTeam(ArenaTeam team, Collection<ArenaPlayer> players);

    /**
     * Signify that the set of players were added to the team
     * @param team ArenaTeam
     * @param player ArenaPlayer
     */
    public void removedFromTeam(ArenaTeam team, ArenaPlayer player);

}
