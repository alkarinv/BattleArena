package mc.alk.arena.objects.joining;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.Collection;

public interface JoinHandler {

    /**
     * Add a team to this competition
     * @param team ArenaTeam
     * @return true if the team was added, false if not
     */
    public boolean addTeam(ArenaTeam team);

    /**
     * Remove the team from the competition
     * @param team ArenaTeam
     * @return whether or not the team was removed
     */
    public boolean removeTeam(ArenaTeam team);

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
     * @return true if the player could be added to the team, false otherwise
     */
    public boolean addedToTeam(ArenaTeam team, ArenaPlayer player);

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
