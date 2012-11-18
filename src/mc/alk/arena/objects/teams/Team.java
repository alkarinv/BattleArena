package mc.alk.arena.objects.teams;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.options.JoinOptions;

import org.bukkit.entity.Player;

public interface Team {

	public void init();

	public Set<ArenaPlayer> getPlayers();

	public Set<Player> getBukkitPlayers();

	public Set<ArenaPlayer> getDeadPlayers();

	public Set<ArenaPlayer> getLivingPlayers();

	public boolean wouldBeDeadWithout(ArenaPlayer player);

	public boolean hasMember(ArenaPlayer player);

	public boolean hasAliveMember(ArenaPlayer player);

	public boolean hasLeft(ArenaPlayer player);

	public boolean isPickupTeam();

	public void setPickupTeam(boolean isPickupTeam);

	public int getId();

	public String getName();

	public void setName(String name);

	public boolean hasSetName();

	public void setAlive();

	public void setAlive(ArenaPlayer player);

	public boolean isDead();

	public int size();

	public void resetScores() ;

	public void addDeath(ArenaPlayer teamMemberWhoDied);

	public void addKill(ArenaPlayer teamMemberWhoKilled);

	public int getNKills();

	public int getNDeaths();

	public Integer getNDeaths(ArenaPlayer player);

	public Integer getNKills(ArenaPlayer player);

	/**
	 *
	 * @param p
	 * @return whether all players are dead
	 */
	public boolean killMember(ArenaPlayer player);

	/**
	 *
	 * @param p
	 * @return whether all players are dead
	 */
	public void playerLeft(ArenaPlayer player);

	public boolean allPlayersOffline();

	public void sendMessage(String message);

	public void sendToOtherMembers(ArenaPlayer player, String message);

	public String getDisplayName();

	public void setDisplayName(String displayName);

	public boolean hasTeam(Team team);

	public String getTeamInfo(Set<String> insideMatch);

	public String getTeamSummary();

	public String getOtherNames(ArenaPlayer player);

	public JoinOptions getJoinPreferences();

	public void setJoinPreferences(JoinOptions jp);

	public int getPriority();

	public void addPlayer(ArenaPlayer player);

	public void removePlayer(ArenaPlayer player);

	public void addPlayers(Collection<ArenaPlayer> players);

	public void removePlayers(Collection<ArenaPlayer> players);

}

