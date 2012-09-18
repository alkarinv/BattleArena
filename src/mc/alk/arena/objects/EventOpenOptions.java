package mc.alk.arena.objects;

import java.util.HashMap;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.objects.Exceptions.InvalidOptionException;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

import org.apache.commons.lang3.StringUtils;

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
				}
				sb.append(r+val);
			}
			return sb.toString();
		}
	}

	HashMap<EventOpenOption,Object> options = new HashMap<EventOpenOption,Object>();
	int announceInterval = 0, secTillStart = 0;

	public static EventOpenOptions parseOptions(String[] args) throws InvalidOptionException{
		EventOpenOptions eoo = new EventOpenOptions();
		HashMap<EventOpenOption,Object> ops = eoo.options;
		for (String op: args){
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
				MinMax teamSize = Util.getMinMax(val);
				if (teamSize == null){
					throw new InvalidOptionException("&cCouldnt parse teamSize &6"+val+" &e needs an int or range. &68, 2+, 2-10, etc");}
				obj = teamSize;
			}
			break;
			case NTEAMS: {
				MinMax nTeams = Util.getMinMax(val);
				if (nTeams == null){
					throw new InvalidOptionException("&cCouldnt parse nTeams &6"+val+" &e needs an int or range. &68, 2+, 2-10, etc");}
				obj = nTeams;
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
				obj = BattleArena.getBAC().getArena(val);
				if (obj==null){
					throw new InvalidOptionException("&cCouldnt find the arena &6" +val);}

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
		Rating rated = hasOption(EventOpenOption.UNRATED) ? Rating.UNRATED : Rating.RATED;

		/// Team size
		MinMax teamSize = hasOption(EventOpenOption.TEAMSIZE) ?  (MinMax)getOption(EventOpenOption.TEAMSIZE) : 
			new MinMax(1,1);

		/// Number of Teams
		MinMax nTeams = hasOption(EventOpenOption.NTEAMS) ? (MinMax)getOption(EventOpenOption.NTEAMS) : 
			new MinMax(2,ArenaParams.MAX);

		mp.setTeamSizes(teamSize);
		mp.setNTeams(nTeams);
		mp.setRating(rated);
	}

	public Arena getArena(MatchParams mp) throws InvalidOptionException{
		BattleArenaController ac = BattleArena.getBAC();

		Arena arena;
		boolean autoFindArena = false;
		if (hasOption(EventOpenOption.ARENA)){
			arena = (Arena) getOption(EventOpenOption.ARENA);
		} else {
			arena = ac.getArenaByMatchParams(mp);
			if (arena == null){
				List<String> reasons = ac.getNotMachingArenaReasons(mp);
				throw new InvalidOptionException(
						"&cCouldnt find an arena matching the params &6"+mp +"\n" + StringUtils.join(reasons,"\n"));
			}
			autoFindArena = true;
		}
		if (!arena.valid()){
			throw new InvalidOptionException("&c Arena is not valid.");}

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
