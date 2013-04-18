package mc.alk.arena.objects.teams;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.scoreboard.ArenaObjective;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A lightweight team that is only used for finding correct matches
 * @author alkarin
 *
 */
public class MatchingTeam implements ArenaTeam{
	static int count = 0;
	static final int id = count++;
	Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();

	public MatchingTeam(){}

	@Override
	public void init() {}

	@Override
	public Set<ArenaPlayer> getPlayers() {
		return players;
	}

	@Override
	public Set<Player> getBukkitPlayers() {
		return null;
	}

	@Override
	public Set<ArenaPlayer> getDeadPlayers() {
		return null;
	}

	@Override
	public Set<ArenaPlayer> getLivingPlayers() {
		return null;
	}

	@Override
	public boolean wouldBeDeadWithout(ArenaPlayer p) {
		return false;
	}

	@Override
	public boolean hasMember(ArenaPlayer p) {
		return players.contains(p);
	}

	@Override
	public boolean hasAliveMember(ArenaPlayer p) {
		return false;
	}

	@Override
	public boolean hasLeft(ArenaPlayer p) {
		return false;
	}

	@Override
	public boolean isPickupTeam() {
		return false;
	}

	@Override
	public void setPickupTeam(boolean isPickupTeam) {}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setName(String name) {}

	@Override
	public void setAlive() {}

	@Override
	public void setAlive(ArenaPlayer player){}

	@Override
	public boolean isDead() {
		return false;
	}

	@Override
	public boolean hasSetName() {
		return false;
	}

	@Override
	public int size() {
		return players.size();
	}

	@Override
	public void reset() {}

	@Override
	public int addDeath(ArenaPlayer teamMemberWhoDied) {return 0;}

	@Override
	public int addKill(ArenaPlayer teamMemberWhoKilled) {return 0;}

	@Override
	public int getNKills() {
		return 0;
	}

	@Override
	public int getNDeaths() {
		return 0;
	}

	@Override
	public Integer getNDeaths(ArenaPlayer p) {
		return null;
	}

	@Override
	public Integer getNKills(ArenaPlayer p) {
		return null;
	}

	@Override
	public boolean killMember(ArenaPlayer p) {
		return false;
	}

	@Override
	public void playerLeft(ArenaPlayer p) {
		players.remove(p);
	}

	@Override
	public boolean allPlayersOffline() {
		return false;
	}

	@Override
	public void sendMessage(String message) {}

	@Override
	public void sendToOtherMembers(ArenaPlayer player, String message) {}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public void setDisplayName(String n) {}

	@Override
	public boolean hasTeam(ArenaTeam team) {
		return false;
	}

	@Override
	public String getTeamInfo(Set<String> insideMatch) {
		return null;
	}

	@Override
	public String getTeamSummary() {
		return null;
	}

	@Override
	public String getOtherNames(ArenaPlayer player) {
		return null;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void addPlayer(ArenaPlayer player) {
		this.players.add(player);
	}

	@Override
	public void removePlayer(ArenaPlayer player) {
		this.players.remove(player);
	}

	@Override
	public void addPlayers(Collection<ArenaPlayer> players) {
		this.players.addAll(players);
	}

	@Override
	public void removePlayers(Collection<ArenaPlayer> players) {
		this.players.removeAll(players);
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void clear() {
		this.players.clear();
	}

	@Override
	public String geIDString(){ return String.valueOf(id);}

	@Override
	public void setArenaObjective(ArenaObjective objective) {/* do nothing */}

	@Override
	public void setTeamChatColor(ChatColor color) { /* do nothing */}

	@Override
	public ChatColor getTeamChatColor() { return null;}

	@Override
	public void setScoreboardDisplayName(String name) {/* do nothing */}

	@Override
	public String getScoreboardDisplayName() { return null;}

	@Override
	public ItemStack getHeadItem() {return null;}

	@Override
	public void setHeadItem(ItemStack item) { /* do nothing */}
}
