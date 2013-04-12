package mc.alk.arena.objects.options;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.pairs.WantedTeamSizePair;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.TeamUtil;

import org.bukkit.Location;

public class JoinOptions extends ArenaSize{
	public static enum JoinOption{
		ARENA("<arena>",false), TEAM("<team>",false), TEAMSIZE("<teamSize>",false);

		final public boolean needsValue;
		final String name;
		JoinOption(String name, boolean needsValue){
			this.needsValue = needsValue;
			this.name = name;
		}
		public String getName(){
			return name;
		}
		public static JoinOption fromName(String str){
			str = str.toUpperCase();
			try {return JoinOption.valueOf(str);} catch (Exception e){}
			throw new IllegalArgumentException();
		}
		public static String getValidList() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (JoinOption r: JoinOption.values()){
				if (!first) sb.append(", ");
				first = false;
				String val = "";
				switch (r){
				default: break;
				}
				sb.append(r.getName()+val);
			}
			return sb.toString();
		}
	}

	/** All options for joining */
	final Map<JoinOption,Object> options = new EnumMap<JoinOption,Object>(JoinOption.class);

	/** Location they have joined from */
	Location joinedLocation = null;

	/** Specific arena or match size.  Is the user requesting a special arena or match size */
	boolean specific = false;

	/** When the player joined, can be null */
	Long joinTime;

	public boolean matches(Arena arena) {
		if (options.containsKey(JoinOption.ARENA)){
			Arena a = (Arena) options.get(JoinOption.ARENA);
			return a != null ? arena.matches(a) : false;
		}
		return true;
	}

	public boolean matches(MatchParams params) {
		return matchesTeamSize(params);
	}

	public static boolean matches(JoinOptions jo, Match match) {
		return jo==null || (jo.matches(match.getArena()) && jo.matches(match.getParams()));
	}



	public boolean nearby(Arena arena, double distance) {
		UUID wid = joinedLocation.getWorld().getUID();
		Location arenajoinloc = arena.getJoinLocation();
		if (arenajoinloc != null){
			return (wid == arenajoinloc.getWorld().getUID() &&
					arenajoinloc.distance(joinedLocation) <= distance);
		}

		for (Location l : arena.getSpawnLocs().values()){
			if (l.getWorld().getUID() != wid)
				return false;
			if (l.distance(joinedLocation) <= distance)
				return true;
		}
		return false;
	}

	public boolean sameWorld(Arena arena) {
		UUID wid = joinedLocation.getWorld().getUID();
		Location arenajoinloc = arena.getJoinLocation();
		if (arenajoinloc != null){
			return (wid == arenajoinloc.getWorld().getUID());}

		for (Location l : arena.getSpawnLocs().values()){
			if (l.getWorld().getUID() != wid)
				return false;
		}
		return true;
	}

	public static JoinOptions parseOptions(MatchParams mp, ArenaTeam t, ArenaPlayer player, String[] args)
			throws InvalidOptionException, NumberFormatException{
		JoinOptions jos = new JoinOptions();
		jos.setJoinTime(System.currentTimeMillis());
		jos.joinedLocation = player.getLocation();
		Map<JoinOption,Object> ops = jos.options;
		Arena arena = null;
		String lastArg = args.length - 1 >= 0 ? args[args.length-1] : "";
		final WantedTeamSizePair teamSize = WantedTeamSizePair.getWantedTeamSize(player,t,mp,lastArg);
		int length = args.length;
		if (teamSize.manuallySet){
			length = args.length -1;
			jos.setTeamSize(teamSize.size);
			jos.specific = true;
		}

		ops.put(JoinOption.TEAMSIZE, teamSize);
		for (int i=0;i<length;i++){
			String op = args[i];
			if (op.isEmpty())
				continue;
			Object obj = null;
			Arena a = BattleArena.getBAController().getArena(op);
			if (a != null){
				if (!a.valid()){
					throw new InvalidOptionException("&cThe specified arena is not valid!");}
				if (arena != null){
					throw new InvalidOptionException("&cYou specified 2 arenas!");}
				arena = a;
				ops.put(JoinOption.ARENA, arena);
				jos.specific = true;
				continue;
			}
			Integer teamIndex = TeamUtil.getTeamIndex(op);
			if (teamIndex != null){
				ops.put(JoinOption.TEAM, teamIndex);
				continue;
			}
			JoinOption jo = null;
			try{
				jo = JoinOption.fromName(op);
				if (jo.needsValue && i+1 >= args.length){
					throw new InvalidOptionException("&cThe option " + jo.name()+" needs a value!");}
			} catch(IllegalArgumentException e){
				throw new InvalidOptionException("&cThe arena or option " + op+" does not exist, \n&cvalid options=&6"+
						JoinOption.getValidList());
			}
			switch(jo){
			default:
				break;
			}

			if (!jo.needsValue){
				ops.put(jo,null);
				continue;
			}
			String val = args[++i];
			switch(jo){
			case ARENA:
				obj = BattleArena.getBAController().getArena(val);
				if (obj==null){
					throw new InvalidOptionException("&cCouldnt find the arena &6" +val);}
			default:
				break;
			}
			ops.put(jo, obj);
		}
		return jos;
	}

	public void setJoinTime(Long currentTimeMillis) {
		this.joinTime = currentTimeMillis;
	}

	public Long getJoinTime(){
		return joinTime;
	}

	public String optionsString(MatchParams mp) {
		StringBuilder sb = new StringBuilder(mp.toPrettyString()+" ");
		for (JoinOption op: options.keySet()){
			sb.append(op.getName());
			if (op.needsValue){
				sb.append("=" + options.get(op));
			}
			sb.append(" ");
		}
		return sb.toString();
	}
	public boolean hasTeamSize(){
		if (options.containsKey(JoinOption.TEAMSIZE)){
			return ((WantedTeamSizePair)options.get(JoinOption.TEAMSIZE)).manuallySet;}
		return false;
	}

	public Integer getTeamSize(){
		return ((WantedTeamSizePair)options.get(JoinOption.TEAMSIZE)).size;
	}

	public boolean hasOption(JoinOption option) {
		return options.containsKey(option);
	}

	public Object getOption(JoinOption option) {
		return options.get(option);
	}

	public boolean isSpecific(){
		return specific;
	}
}
