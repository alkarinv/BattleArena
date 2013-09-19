package mc.alk.arena.objects.options;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.MinMax;
import mc.alk.arena.util.TeamUtil;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;

public class JoinOptions {
	public static enum JoinOption{
		ARENA("<arena>",false), TEAM("<team>",false),
		WANTEDTEAMSIZE("<teamSize>",false);

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

	MatchParams params;

	MinMax teamSize;

	/** When the player joined, (defaults to when the JoinOptions was created) */
	long joinTime;

	public JoinOptions(){
		joinTime = System.currentTimeMillis();
	}

	public boolean matches(Arena arena) {
		if (options.containsKey(JoinOption.ARENA)){
			Arena a = (Arena) options.get(JoinOption.ARENA);
			return a != null ? arena.matches(a) : false;
		}
		return true;
	}

	public boolean matches(MatchParams params) {
//		return ArenaSize.matchesNTeams(nTeams, params.getNTeams());
		return ArenaSize.lower(teamSize, params.getTeamSizes());
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

		for (Location l : arena.getSpawns()){
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

		for (Location l : arena.getSpawns()){
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
		String lastArg = args.length > 0 ? args[args.length-1] : "";
		int length = args.length;

		for (int i=0;i<length;i++){
			String op = args[i];
			if (op.isEmpty())
				continue;
			Object obj = null;
			Arena a = BattleArena.getBAController().getArena(op);
			if (a != null){
				if (arena != null){
					throw new InvalidOptionException("&cYou specified 2 arenas!");}
				if (!a.valid()){
					throw new InvalidOptionException("&cThe specified arena is not valid!");}
				arena = a;
				ops.put(JoinOption.ARENA, arena);
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
				a = (Arena) obj;
				if (!a.valid()){
					throw new InvalidOptionException("&cThe specified arena is not valid!");}
				arena = a;
			default:
				break;
			}
			ops.put(jo, obj);
		}
		if (arena != null && !arena.matchesIgnoreSize(mp, jos)){
			throw new InvalidOptionException("&cThe arena &6" +arena.getName() +
					"&c doesn't match your join requirements. "  +
					StringUtils.join( arena.getInvalidMatchReasons(mp, jos), '\n'));
		} else if (arena == null){
			arena = BattleArena.getBAController().getNextArena(mp.getType());
			jos.setArena(arena);
		}
		if (arena != null){
			MatchParams old = ParamController.copyParams(mp);
			mp = arena.getParams();
			mp.setParent(old);
			if (!arena.matchesIgnoreSize(mp, jos)){
				throw new InvalidOptionException("&cThe arena &6" +arena.getName() +
						"&c doesn't match your join requirements. "  +
						StringUtils.join( arena.getInvalidMatchReasons(mp, jos), '\n'));
			}
		}
		MinMax mm = null;
		try{mm = MinMax.valueOf(lastArg);} catch (Exception e){}

//		final WantedTeamSizePair teamSize = WantedTeamSizePair.getWantedTeamSize(player,t,mp,lastArg);
//		if (teamSize.manuallySet){
//			length = args.length -1;
//			jos.setTeamSize(teamSize.size);
//		}
		if (mm != null)
			ops.put(JoinOption.WANTEDTEAMSIZE, mm);
//		mp.flatten();
		jos.params=mp;
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

//	public boolean hasTeamSize(){
//		if (options.containsKey(JoinOption.WANTEDTEAMSIZE)){
//			return ((WantedTeamSizePair)options.get(JoinOption.WANTEDTEAMSIZE)).manuallySet;}
//		return false;
//	}
//
//	public Integer getTeamSize(){
//		return ((WantedTeamSizePair)options.get(JoinOption.WANTEDTEAMSIZE)).size;
//	}

	public boolean hasWantedTeamSize() {
		return options.containsKey(JoinOption.WANTEDTEAMSIZE);
	}

	public boolean hasOption(JoinOption option) {
		return options.containsKey(option);
	}

	public Object getOption(JoinOption option) {
		return options.get(option);
	}

	public boolean hasArena() {
		return options.containsKey(JoinOption.ARENA);
	}

	public Arena getArena() {
		return hasArena() ? (Arena) options.get(JoinOption.ARENA) : null;
	}

	public void setArena(Arena arena) {
		options.put(JoinOption.ARENA, arena);
	}

	public MatchParams getMatchParams() {
		return params;
	}

}
