package mc.alk.arena.serializers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;

import org.bukkit.configuration.ConfigurationSection;

public class MatchParamsSerializer extends BaseConfig{
	final MatchParams params;

	public MatchParamsSerializer(MatchParams mp){
		this.params = mp;
	}

	@Override
	public void save(){
		ConfigurationSection main = config.createSection(params.getName());
		main.set("name", params.getName());
		main.set("command", params.getCommand());
		main.set("db", params.getDBName());
		main.set("prefix", params.getPrefix());
		main.set("arenaType", params.getType().getName());
		main.set("arenaClass", ArenaType.getArenaClass(params.getType()).getSimpleName());
		main.set("victoryCondition", params.getVictoryType().getName());
		main.set("teamSize", params.getTeamSizeRange());
		main.set("nTeams", params.getNTeamRange());
		main.set("nLives", params.getNLives());

		main.set("matchTime", params.getMatchTime());
		main.set("timeBetweenRounds", params.getTimeBetweenRounds());
		main.set("secondsToLoot", params.getSecondsToLoot());
		main.set("secondsTillMatch", params.getSecondsTillMatch());

		main.set("matchUpdateInterval", params.getIntervalTime());

		main.set("overrideBattleTracker", params.getOverrideBattleTracker());

		main.set("nConcurrentCompetitions", params.getNConcurrentCompetitions());

		Map<String,Object> map = new LinkedHashMap<String,Object>();
		MatchTransitions alltops = params.getTransitionOptions();
		Map<MatchState,TransitionOptions> transitions =
				new TreeMap<MatchState,TransitionOptions>(alltops.getAllOptions());
		for (MatchState ms: transitions.keySet()){
			TransitionOptions tops = transitions.get(ms);
			if (tops == null)
				continue;
			Map<TransitionOption,Object> ops = tops.getOptions();
			if (ops == null || ops.isEmpty())
				continue;
			List<String> list = new ArrayList<String>();
			ops = new TreeMap<TransitionOption,Object>(ops); /// try to maintain some ordering
			for (TransitionOption to: ops.keySet()){
				String s;
				Object value = ops.get(to);
				if (value == null){
					s = to.toString();
				} else {
					s = to.toString() + "="+value.toString();
				}
				list.add(s);
			}
			map.put(ms.toString(), list);
		}
		main.set("options", map);
		super.save();
	}
}
