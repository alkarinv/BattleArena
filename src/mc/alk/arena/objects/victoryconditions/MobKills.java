package mc.alk.arena.objects.victoryconditions;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesLeaderRanking;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.Log;

import org.bukkit.event.entity.EntityDeathEvent;

public class MobKills extends VictoryCondition implements DefinesLeaderRanking{
	final PointTracker mkills;

	public MobKills(Match match) {
		super(match);
		this.mkills = new PointTracker(match);
	}

	@Override
	public List<Team> getLeaders() {
		return mkills.getLeaders();
	}

	@Override
	public TreeMap<Integer,Collection<Team>> getRanks() {
		return mkills.getRanks();
	}

	@Override
	@Deprecated
	public List<Team> getRankings() {
		return mkills.getRankings();
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
		Team t = match.getTeam(killer);
		if (t == null)
			return;
		t.addKill(killer);
		mkills.addPoints(t, 1);
	}

	@MatchEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		Collection<Team> leaders = mkills.getLeaders();
		if (leaders.size() > 1){
			event.setCurrentDrawers(leaders);
		} else {
			event.setCurrentLeaders(leaders);
		}
	}
}
