package mc.alk.arena.objects.victoryconditions;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.VictoryUtil;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/// TODO refactor with MobKills and PlayerKills
public class AllKills extends VictoryCondition implements ScoreTracker {
	final ArenaObjective kills;

	public AllKills(Match match) {
		super(match);
		kills = new ArenaObjective("allkills","All Kills");
		kills.setDisplayName(MessageUtil.colorChat("&4All Kills"));
	}

	@MatchEventHandler(suppressCastWarnings=true, priority=EventPriority.LOW)
	public void playerDeathEvent(PlayerDeathEvent event) {
		if (match.isWon()){
			return;}
		final ArenaPlayer p = BattleArena.toArenaPlayer(event.getEntity());
		if (p==null)
			return;
		final ArenaTeam team = match.getTeam(p);
		if (team == null)
			return;
		final ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event);
		handleDeath(p,team, killer);
	}

	protected void handleDeath(ArenaPlayer p,ArenaTeam team, ArenaPlayer killer) {
		/// Add a kill to the killing team, and a death to the other team
		if (killer != null && killer != p){
			ArenaTeam killerTeam = match.getTeam(killer);
			if (killerTeam != null){
				killerTeam.addKill(killer);
				kills.addPoints(killer, 1);
				kills.addPoints(killerTeam, 1);
			}
		}
		team.addDeath(p);
	}

	@MatchEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		Collection<ArenaTeam> leaders = VictoryUtil.getLeaderByHighestKills(match);
		if (leaders.size() > 1){
			event.setCurrentDrawers(leaders);
		} else {
			event.setCurrentLeaders(leaders);
		}
	}

	@MatchEventHandler(priority=EventPriority.LOW)
	public void mobDeathEvent(EntityDeathEvent event) {
		switch(event.getEntityType()){
		case BAT:
			break;
		case BLAZE:
			break;
		case CAVE_SPIDER:
			break;
		case CHICKEN:
			break;
		case COW:
			break;
		case CREEPER:
			break;
		case ENDERMAN:
			break;
		case ENDER_DRAGON:
			break;
		case GHAST:
			break;
		case GIANT:
			break;
		case IRON_GOLEM:
			break;
		case MAGMA_CUBE:
			break;
		case MUSHROOM_COW:
			break;
		case OCELOT:
			break;
		case PIG:
			break;
		case PIG_ZOMBIE:
			break;
		case SHEEP:
			break;
		case SILVERFISH:
			break;
		case SKELETON:
			break;
		case SLIME:
			break;
		case SNOWMAN:
			break;
		case SPIDER:
			break;
		case SQUID:
			break;
		case VILLAGER:
			break;
		case WITHER:
			break;
		case WOLF:
			break;
		case ZOMBIE:
			break;
		default:
			return;
		}
		ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event.getEntity().getLastDamageCause());
		if (killer == null){
			return;}
		ArenaTeam t = match.getTeam(killer);
		if (t == null)
			return;
		t.addKill(killer);
		kills.addPoints(t, 1);
		kills.addPoints(killer, 1);
	}

	@Override
	public List<ArenaTeam> getLeaders() {
		return kills.getTeamLeaders();
	}

	@Override
	public TreeMap<Integer,Collection<ArenaTeam>> getRanks() {
		return kills.getTeamRanks();
	}

	@Override
	public void setScoreBoard(ArenaScoreboard scoreboard) {
		this.kills.setScoreBoard(scoreboard);
		scoreboard.addObjective(kills);
	}
}
