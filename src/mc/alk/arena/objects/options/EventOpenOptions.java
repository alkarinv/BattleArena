package mc.alk.arena.objects.options;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.Rating;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.util.MinMax;

import org.apache.commons.lang.StringUtils;

public class EventOpenOptions {
	public static enum EventOpenOption{
		TEAMSIZE, NTEAMS,
		SILENT,
		RATED,UNRATED,
		FORCEJOIN,
		OPEN,AUTO,
		TIME, INTERVAL,
		ARENA;

		public static String getValidList() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (EventOpenOption r: EventOpenOption.values()){
				if (!first) sb.append(", ");
				first = false;
				String val = "";
				switch (r){
				case TEAMSIZE:
				case NTEAMS:
					val = "=<int or range>";
					break;
				case TIME:
				case INTERVAL:
					val = "=<seconds>";
					break;
				case ARENA:
					val = "=<arena>";
					break;
				default:
					break;
				}
				sb.append(r+val);
			}
			return sb.toString();
		}
	}

	Map<EventOpenOption,Object> options = new EnumMap<EventOpenOption,Object>(EventOpenOption.class);
	int announceInterval = 0, secTillStart = 0;

	public static EventOpenOptions parseOptions(String[] args, Set<Integer> ignoreArgs) throws InvalidOptionException{
		EventOpenOptions eoo = new EventOpenOptions();
		Map<EventOpenOption,Object> ops = eoo.options;
		int i =0;
		for (String op: args){
			if ( ignoreArgs != null && ignoreArgs.contains(i++))
				continue;
			Object obj = null;
			String[] split = op.split("=");
			split[0] = split[0].trim().toUpperCase();
			EventOpenOption to = null;
			try{
				to = EventOpenOption.valueOf(split[0]);
			} catch(IllegalArgumentException e){
				throw new InvalidOptionException("&cThe option " + split[0]+" does not exist, \n&cvalid options=&6"+
						EventOpenOption.getValidList());
			}
			if (split.length == 1){
				ops.put(to,null);
				continue;
			}
			String val = split[1].trim();
			switch(to){
			case TEAMSIZE:{
				try{
					obj = MinMax.valueOf(val);
				}catch (Exception e){
					throw new InvalidOptionException("&cCouldnt parse teamSize &6"+val+" &e needs an int or range. &68, 2+, 2-10, etc");
				}
			}
			break;
			case NTEAMS: {
				try{
					obj = MinMax.valueOf(val);
				}catch (Exception e){
					throw new InvalidOptionException("&cCouldnt parse nTeams &6"+val+" &e needs an int or range. &68, 2+, 2-10, etc");
				}
			}
			break;
			case TIME:
				try {obj = Integer.valueOf(val) *60;}
				catch (Exception e){throw new InvalidOptionException("&cTime wasnt an integer: &6" +val);}
				eoo.secTillStart = (Integer) obj;
				break;
			case INTERVAL:
				try {obj = Integer.valueOf(val) *60;}
				catch (Exception e){throw new InvalidOptionException("&cTime interval wasnt an integer: &6" +val);}
				eoo.announceInterval = (Integer) obj;
				break;
			case ARENA:
				obj = BattleArena.getBAController().getArena(val);
				if (obj==null){
					throw new InvalidOptionException("&cCouldnt find the arena &6" +val);}
			default:
				break;
			}
			if (obj != null)
				ops.put(to, obj);
		}
		return eoo;
	}

	public Object getOption(EventOpenOption op) {
		return options.get(op);
	}

	public boolean hasOption(EventOpenOption op) {
		return options.containsKey(op);
	}

	public boolean isSilent(){
		return hasOption(EventOpenOption.SILENT);
	}

	public void updateParams(MatchParams mp){
		/// Rated
		if (hasOption(EventOpenOption.UNRATED))
			mp.setRating(Rating.UNRATED);
		/// By default lets make the teamSize the min team size if max # teams not specified as a finite range
		if (mp.getMaxTeams() == ArenaParams.MAX){
			mp.setMaxTeamSize(mp.getMinTeamSize());
		}

		/// Number of Teams
		if (hasOption(EventOpenOption.NTEAMS)){
			mp.setNTeams((MinMax)getOption(EventOpenOption.NTEAMS));
		}

		/// Team size
		if (hasOption(EventOpenOption.TEAMSIZE)){
			mp.setTeamSizes((MinMax)getOption(EventOpenOption.TEAMSIZE));}
	}

	public Arena getArena(MatchParams mp, JoinOptions jp) throws InvalidOptionException{
		BattleArenaController ac = BattleArena.getBAController();

		Arena arena;
		boolean autoFindArena = false;
		if (hasOption(EventOpenOption.ARENA)){
			arena = (Arena) getOption(EventOpenOption.ARENA);
		} else {
			arena = ac.getArenaByMatchParams(mp,jp);
			if (arena == null){
				Map<Arena,List<String>> reasons = ac.getNotMachingArenaReasons(mp,jp);
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (Arena a : reasons.keySet()){
					for (String reason: reasons.get(a)){
						if (!first) sb.append(", ");
						sb.append("&f"+a.getName()+":&e "+reason);
						first = false;
					}
				}
				throw new InvalidOptionException(
						"&cCouldnt find an arena matching the params &6"+mp +"\n" + sb.toString());
			}
			autoFindArena = true;
		}
		if (!arena.valid()){
			throw new InvalidOptionException("&c Arena is not valid.");}
		/// We have verified there is a valid arena.. now get a real one from the queue
		final String arenaName = arena.getName();
		if (autoFindArena){
			arena = ac.nextArenaByMatchParams(mp);
			if (arena == null){
				throw new InvalidOptionException("&cAll arenas matching those params are in use. wait till one is free ");}
		} else {
			arena = ac.reserveArena(arena);
			if (arena == null){
				throw new InvalidOptionException("&c Arena &6" +arenaName+"&c is currently in use, you'll have to wait till its free");}
		}
		ArenaParams ap = arena.getParameters();
		if (!ap.matches(mp)){
			throw new InvalidOptionException(StringUtils.join(ap.getInvalidMatchReasons(mp),"\n"));}

		return arena;
	}

	public int getInterval() {
		return announceInterval == 0 ? Defaults.ANNOUNCE_EVENT_INTERVAL : announceInterval;
	}

	public int getSecTillStart() {
		return secTillStart == 0 ? Defaults.AUTO_EVENT_COUNTDOWN_TIME : secTillStart;
	}

	public String getOpenCmd() {
		if (hasOption(EventOpenOption.AUTO)){
			return EventOpenOption.AUTO.toString().toLowerCase();
		} else {
			return EventOpenOption.OPEN.toString().toLowerCase();
		}
	}
}
