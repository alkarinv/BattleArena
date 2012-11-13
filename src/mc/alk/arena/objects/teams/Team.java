package mc.alk.arena.objects.teams;

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

	public boolean wouldBeDeadWithout(ArenaPlayer p);

	public boolean hasMember(ArenaPlayer p);
	public boolean hasAliveMember(ArenaPlayer p);
	public boolean isPickupTeam();
	public void setPickupTeam(boolean isPickupTeam);
	public String getName();
	public int getId();
	public void setName(String name);
	public void setAlive();
	public boolean isDead();
	public boolean hasSetName();
	public int size();
	public void resetScores() ;

	public void addDeath(ArenaPlayer teamMemberWhoDied);

	public void addKill(ArenaPlayer teamMemberWhoKilled);

	public int getNKills();

	public int getNDeaths();

	public Integer getNDeaths(ArenaPlayer p);
	public Integer getNKills(ArenaPlayer p);

	/**
	 *
	 * @param p
	 * @return whether all players are dead
	 */
	public boolean killMember(ArenaPlayer p);

	/**
	 *
	 * @param p
	 * @return whether all players are dead
	 */
	public void playerLeft(ArenaPlayer p);

	public boolean allPlayersOffline();

	public void sendMessage(String message);

	public void sendToOtherMembers(ArenaPlayer player, String message);

	public String getDisplayName();
	public void setDisplayName(String n);

	public boolean hasTeam(Team team);

	public String getTeamInfo(Set<String> insideMatch);

	public int getPlayerIndex(ArenaPlayer p);

	public String getTeamSummary();

	public String getOtherNames(ArenaPlayer player);

	public JoinOptions getJoinPreferences();

	public void setJoinPreferences(JoinOptions jp);

	public int getPriority();

}

