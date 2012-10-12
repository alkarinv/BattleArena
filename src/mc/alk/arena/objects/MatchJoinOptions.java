package mc.alk.arena.objects;

import java.util.HashMap;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.Exceptions.InvalidOptionException;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

public class MatchJoinOptions {
	public static enum MatchJoinOption{
		TEAMSIZE,
		ARENA;

		public static String getValidList() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (MatchJoinOption r: MatchJoinOption.values()){
				if (!first) sb.append(", ");
				first = false;
				String val = "";
				switch (r){
				case TEAMSIZE:
					val = "=<int or intvint>";
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

	HashMap<MatchJoinOption,Object> options = new HashMap<MatchJoinOption,Object>();

	public static MatchJoinOptions parseOptions(String[] args, Set<Integer> ignoreArgs) throws InvalidOptionException{
		MatchJoinOptions mjo = new MatchJoinOptions();
		HashMap<MatchJoinOption,Object> ops = mjo.options;
		int i =0;
		for (String op: args){
			if ( ignoreArgs != null && ignoreArgs.contains(i++))
				continue;
			Object obj = null;
			String[] split = op.split("=");
			split[0] = split[0].trim().toUpperCase();
			MatchJoinOption to = null;
			try{
				to = MatchJoinOption.valueOf(split[0]);
			} catch(IllegalArgumentException e){
				throw new InvalidOptionException("&cThe option " + split[0]+" does not exist, \n&cvalid options=&6"+
						MatchJoinOption.getValidList());
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
			case ARENA:
				obj = BattleArena.getBAC().getArena(val);
				if (obj==null){
					throw new InvalidOptionException("&cCouldnt find the arena &6" +val);}
			default:
				break;
			}
			if (obj != null)
				ops.put(to, obj);
		}
		return mjo;
	}

	public Object getOption(MatchJoinOption op) {
		return options.get(op);
	}

	public boolean hasOption(MatchJoinOption op) {
		return options.containsKey(op);
	}

	public void updateParams(MatchParams mp){

		/// Team size
		if (hasOption(MatchJoinOption.TEAMSIZE)){
			mp.setTeamSizes((MinMax)getOption(MatchJoinOption.TEAMSIZE));}		
	}
}
