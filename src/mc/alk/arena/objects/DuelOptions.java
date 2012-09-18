package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.EventOpenOptions.EventOpenOption;
import mc.alk.arena.objects.Exceptions.InvalidOptionException;
import mc.alk.arena.util.Util;

import org.bukkit.entity.Player;

public class DuelOptions {
	List<ArenaPlayer> challengedPlayers = new ArrayList<ArenaPlayer>();
	
	public static enum DuelOption{
		ARENA,RATED, UNRATED;

		public static String getValidList() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (EventOpenOption r: EventOpenOption.values()){
				if (!first) sb.append(", ");
				first = false;
				String val = "";
				switch (r){
				case ARENA:
					val = "=<arena>";
					break;
				}
				sb.append(r+val);
			}
			return sb.toString();
		}
	}

	HashMap<DuelOption,Object> options = new HashMap<DuelOption,Object>();


	public static DuelOptions parseOptions(String[] args) throws InvalidOptionException{
		DuelOptions eoo = new DuelOptions();
		HashMap<DuelOption,Object> ops = eoo.options;
		for (String op: args){
			Player p = Util.findPlayer(op);
			if (p != null){
				if (!p.isOnline())
					throw new InvalidOptionException("&cPlayer &6" + p.getDisplayName() +"&c is not online!");
				
				eoo.challengedPlayers.add(BattleArena.toArenaPlayer(p));
				continue;
			}
			Object obj = null;
			String[] split = op.split("=");
			split[0] = split[0].trim().toUpperCase();
			
			DuelOption to = null;
			try{
				to = DuelOption.valueOf(split[0]);
			} catch(IllegalArgumentException e){
				throw new InvalidOptionException("&cThe player or option " + split[0]+" does not exist, \n&cvalid options=&6"+
						EventOpenOption.getValidList());
			}
			switch(to){
			case RATED: 
				if (!Defaults.DUEL_ALLOW_RATED)
					throw new InvalidOptionException("&cRated duels are not allowed!");
				break;
			}
			
			if (split.length == 1){
				ops.put(to,null);
				continue;
			}
			String val = split[1].trim();
			switch(to){
			case ARENA:
				obj = BattleArena.getBAC().getArena(val);
				if (obj==null){
					throw new InvalidOptionException("&cCouldnt find the arena &6" +val);}
			}
			ops.put(to, obj);
		}
		if (eoo.challengedPlayers == null || eoo.challengedPlayers.isEmpty()){
			throw new InvalidOptionException("&cYou need to challenge at least one player!");
		}
		return eoo;
	}

	public String optionsString(MatchParams mp) {
		return mp.getType().getName()+" " + mp.toPrettyString();
	}

	public List<ArenaPlayer> getChallengedPlayers() {
		return challengedPlayers;
	}

	public String getChallengedTeamString() {
		return Util.playersToCommaDelimitedString(getChallengedPlayers());
	}

	public String getOtherChallengedString(ArenaPlayer ap) {
		List<ArenaPlayer> players = new ArrayList<ArenaPlayer>(challengedPlayers);
		players.remove(ap);
		return Util.playersToCommaDelimitedString(getChallengedPlayers());
	}

	public boolean hasOption(DuelOption option) {
		return options.containsKey(option);
	}

}
