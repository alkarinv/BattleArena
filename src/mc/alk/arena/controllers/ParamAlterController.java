package mc.alk.arena.controllers;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.RegisteredCompetition;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.GameOption;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.MinMax;

import org.bukkit.command.CommandSender;

public class ParamAlterController {
	MatchParams params;
	public ParamAlterController(MatchParams params){
		this.params = params;
	}

	public boolean setOption(CommandSender sender, String[] args) {
		if (args.length < 3){
			sendMessage(sender, "&6/<game> setoption <option> <value>");
			return sendMessage(sender, "&6/<game> setoption <stage> <option> [value]");
		}
		RegisteredCompetition rc = CompetitionController.getCompetition(params.getName());
		if (rc == null){
			return sendMessage(sender, "&cGame &6" + params.getName() +"&c not found!");
		}
		GameOption go = GameOption.fromString(args[1]);

		if (go != null){
			try {
				setGameOption(go,args[2]);
				rc.saveParams(params);
				ParamController.addMatchParams(params);
				sendMessage(sender, "&2Game options &6"+go.toString()+"&2 changed to &6"+args[2]);
				switch(go){
				case COMMAND:
					sendMessage(sender, "&c[Info]&e This option will change after a restart");
					break;
				default:
					/* do nothing */
				}
				return true;
			} catch (Exception e) {
				Log.err(e.getMessage());
				sendMessage(sender, "&cCould not set game option &6" + args[1] +"&c bad value &6"+ args[2]);
				sendMessage(sender, e.getMessage());
				return false;
			}
		}
		MatchState state = MatchState.fromString(args[1]);
		if (state != null){
			final String key = args[2].trim().toUpperCase();
			final String value = args.length > 3 ? args[3].trim() : null;
			try{
				setTransitionOption(state, key,value);
				rc.saveParams(params);
				ParamController.addMatchParams(params);
				ParamController.setTransitionOptions(params, params.getTransitionOptions());
				if (value == null){
					return sendMessage(sender, "&2Game option "+state +"&2 changed!");
				} else {
					return sendMessage(sender, "&2Game option &6"+state +"&2 changed to &6" + value);
				}
			} catch (Exception e) {
				sendMessage(sender, "&cCould not set game option " + args[1] +"  value="+args[2]);
				sendMessage(sender, e.getMessage());
				return false;
			}
		}
		sendMessage(sender, "&cGame option &6" + args[1] +"&c not found!");
		return false;
	}

	public boolean deleteOption(CommandSender sender, String[] args) {
		if (args.length < 2){
			sendMessage(sender, "&6/<game> deleteOption <option>");
			return sendMessage(sender, "&6/<game> deleteOption <stage> <option>");
		}
		RegisteredCompetition rc = CompetitionController.getCompetition(params.getName());
		if (rc == null){
			return sendMessage(sender, "&cGame &6" + params.getName() +"&c not found!");}
		GameOption go = GameOption.fromString(args[1]);

		if (go != null){
			try {
				deleteGameOption(go);
				params.getTransitionOptions();
				rc.saveParams(params);
				ParamController.addMatchParams(params);
				sendMessage(sender, "&2Game option &6"+go.toString()+"&2 removed");
				switch(go){
				case COMMAND:
					sendMessage(sender, "&c[Info]&e This option will change after a restart");
					break;
				default:
					/* do nothing */
				}
				return true;
			} catch (Exception e) {
				Log.err(e.getMessage());
				sendMessage(sender, "&cCould not renive game option &6" + args[1]);
				sendMessage(sender, e.getMessage());
				return false;
			}
		}
		MatchState state = MatchState.fromString(args[1]);
		if (state != null){
			final String key = args[2].trim().toUpperCase();
			try{
				deleteTransitionOption(state, key);
				rc.saveParams(params);
				ParamController.addMatchParams(params);
				ParamController.setTransitionOptions(params, params.getTransitionOptions());
				return sendMessage(sender, "&2Game option &6"+state +"&2 removed");
			} catch (Exception e) {
				sendMessage(sender, "&cCould not remove game option " + args[1]);
				sendMessage(sender, e.getMessage());
				return false;
			}
		}
		sendMessage(sender, "&cGame option &6" + args[1] +"&c not found!");
		return false;
	}

	private boolean setTransitionOption(MatchState state, String key, String value) throws Exception{
		Object ovalue = null;
		TransitionOption to = null;
		to = TransitionOption.fromString(key);
		if (to.hasValue() && value == null)
			throw new InvalidOptionException("Transition Option " + to +" needs a value! " + key+"=<value>");
		/// For teleport options, remove them from other places where they just dont make sense
		MatchTransitions tops = params.getTransitionOptions();
		if ((state == MatchState.ONPRESTART || state == MatchState.ONSTART || state == MatchState.ONJOIN) &&
				(to == TransitionOption.TELEPORTIN || to == TransitionOption.TELEPORTWAITROOM ||
				to == TransitionOption.TELEPORTCOURTYARD || to == TransitionOption.TELEPORTLOBBY ||
				to == TransitionOption.TELEPORTMAINLOBBY || to == TransitionOption.TELEPORTMAINWAITROOM)){
			tops.removeTransitionOption(MatchState.ONPRESTART, to);
			tops.removeTransitionOption(MatchState.ONSTART, to);
			tops.removeTransitionOption(MatchState.ONJOIN, to);
		}
		ovalue = to.parseValue(value);
		tops.addTransitionOption(state, to,ovalue);
		return true;
	}

	private boolean deleteTransitionOption(MatchState state, String key) throws Exception{
		TransitionOption to = null;
		to = TransitionOption.fromString(key);
		MatchTransitions tops = params.getTransitionOptions();
		return tops.removeTransitionOption(state,to);
	}

	private boolean setGameOption(GameOption go, String val) throws Exception {
		Object value = GameOption.getValue(go,val);
		if (value == null){
			throw new InvalidOptionException("No value specified for " + go);}
		int iv;

		switch(go){
		case NLIVES: params.setNLives((Integer)value); break;
		case NTEAMS: params.setNTeams((MinMax) value);  break;
		case TEAMSIZE: params.setTeamSizes((MinMax) value);  break;
		case PREFIX: params.setPrefix((String)value); break;
		case COMMAND: params.setCommand((String)value); break;
		case MATCHTIME: params.setMatchTime((Integer)value);break;
		case PRESTARTTIME:
			iv = ((Integer)value).intValue();
			checkGreater(iv,0, true);
			params.setSecondsTillMatch(iv);
			break;
		case VICTORYTIME:
			iv = ((Integer)value).intValue();
			checkGreater(iv,1, true);
			params.setSecondsToLoot(iv); break;
		case VICTORYCONDITION:
			params.setVictoryCondition((VictoryType)value);
			break;
		case RATED:
			params.setRated((Boolean)value);
			break;
		default:
			break;
		}

		return true;
	}

	private boolean deleteGameOption(GameOption go) throws Exception {
		switch(go){
		case NLIVES: params.setNLives(null); break;
		case NTEAMS: params.setNTeams(null);  break;
		case TEAMSIZE: params.setTeamSizes(null);  break;
		case PREFIX: params.setPrefix(null); break;
		case COMMAND: params.setCommand(null); break;
		case MATCHTIME: params.setMatchTime(null);break;
		case PRESTARTTIME: params.setSecondsTillMatch(null);break;
		case VICTORYTIME: params.setSecondsToLoot(null); break;
		case VICTORYCONDITION: params.setVictoryCondition(null); break;
		case RATED: params.setRated(false); break;
		default:
			break;
		}
		return true;
	}
	private void checkGreater(int iv, int bound, boolean inclusive) throws InvalidOptionException {
		if (inclusive && iv < bound) throw new InvalidOptionException(iv +"  must be greater or equal to " + bound);
		else if (iv <= bound) throw new InvalidOptionException(iv +"  must be greater than " + bound);
	}

	public static boolean sendMessage(CommandSender sender, String msg){
		return MessageUtil.sendMessage(sender, msg);
	}
}
