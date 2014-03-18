package mc.alk.arena.objects.teams;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.stats.ArenaStat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;

public interface ArenaTeam {

	public void init();

	public Set<ArenaPlayer> getPlayers();

	public Set<Player> getBukkitPlayers();

	public Set<ArenaPlayer> getDeadPlayers();

    public Set<ArenaPlayer> getLivingPlayers();

    public Set<ArenaPlayer> getLeftPlayers();

	public boolean wouldBeDeadWithout(ArenaPlayer player);

	public boolean hasMember(ArenaPlayer player);

	public boolean hasAliveMember(ArenaPlayer player);

	public boolean hasLeft(ArenaPlayer player);

	public boolean isPickupTeam();

	/**
	 * Is this team ready to play
	 * @return true if all players are "ready" to play
	 */
	public boolean isReady();

	public void setPickupTeam(boolean isPickupTeam);

	public int getId();

	public String getName();

	public void setName(String name);

	public boolean hasSetName();

	public void setAlive();

	public void setAlive(ArenaPlayer player);

	public boolean isDead();

	/**
	 * The number of players in this team
	 * @return the number of players in the team
	 */
	public int size();

	public void reset() ;

	public int addDeath(ArenaPlayer teamMemberWhoDied);

	/**
	 *
	 * @param teamMemberWhoKilled player
	 * @return the number of kills they have
	 */
	public int addKill(ArenaPlayer teamMemberWhoKilled);

	public int getNKills();

	public int getNDeaths();

	/**
	 * Get the number of deaths of the specified player
	 * @param player player
	 * @return number of deaths, null if player doesn't exist or has no deaths
	 */
	public Integer getNDeaths(ArenaPlayer player);

	/**
	 * Get the number of kills of the specified player
	 * @param player player
	 * @return number of kills, null if player doesn't exist or has no kills
	 */
	public Integer getNKills(ArenaPlayer player);

	/**
	 * Kill off a team member
	 * @param player that died
	 * @return whether all players are dead
	 */
	public boolean killMember(ArenaPlayer player);

	/**
	 * Call when a player has left the team
	 * @param player player
	 */
	public void playerLeft(ArenaPlayer player);

	public boolean allPlayersOffline();

	public void sendMessage(String message);

	public void sendToOtherMembers(ArenaPlayer player, String message);

	public String getDisplayName();

	public void setDisplayName(String displayName);

	public boolean hasTeam(ArenaTeam team);

	public String getTeamInfo(Set<String> insideMatch);

	public String getTeamSummary();

	public String getOtherNames(ArenaPlayer player);

	public int getPriority();

	public void addPlayer(ArenaPlayer player);

	public boolean removePlayer(ArenaPlayer player);

	public void addPlayers(Collection<ArenaPlayer> players);

	public void removePlayers(Collection<ArenaPlayer> players);

	/**
	 * Reset/clear all variables of this team
	 */
	public void clear();

	public void setArenaObjective(ArenaObjective objective);

	public void setTeamChatColor(ChatColor color);

	public ChatColor getTeamChatColor();

	public String getIDString();

	public void setScoreboardDisplayName(String name);

	String getScoreboardDisplayName();

	public ItemStack getHeadItem();

	public void setHeadItem(ItemStack item);

	public void setArenaStat(ArenaStat stat);

	public ArenaStat getStat();

	public ArenaStat getStat(MatchParams mp);

	public MatchParams getCurrentParams();

	public void setCurrentParams(MatchParams params);

    public int getMinPlayers();

    public int getMaxPlayers();

    public void setMinPlayers(int num);

    public void setMaxPlayers(int num);

    public void setIndex(int index);

    public int getIndex();

    public void setIDString(String id);

}

