package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.RegisteredCompetition;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.GameOption;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.MinMax;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class ParamAlterController {
	MatchParams params;

	public ParamAlterController(MatchParams params){
		this.params = params;
	}

	public boolean setOption(CommandSender sender, String[] args) {
		String help1 = "&6/<game> setoption <option> [value]";
		String help2 = "&6/<game> setoption <stage> [option] [value]";
		if (args.length < 2){
			sendMessage(sender, help1);
			return sendMessage(sender, help2);
		}
		RegisteredCompetition rc = CompetitionController.getCompetition(params.getName());
		if (rc == null){
			return sendMessage(sender, "&cGame &6" + params.getName() +"&c not found!");}
		MatchState stage = MatchState.fromString(args[1]);
		GameOption go = null;
		TransitionOption to = null;
		Object value = null;
		try{
			if (stage != null){
				if (args.length < 3){
					return sendMessage(sender, help2);}
				to = TransitionOption.fromString(args[2]);
				if (to != null){
					if (to.hasValue() || to == TransitionOption.ENCHANTS){
						if (args.length < 4){
							sendMessage(sender, "&c"+to.name()+" needs a value");
							return sendMessage(sender, help2);
						} else {
							value = to.parseValue(args[3]);
						}
					}
					setOption(sender,stage, to,value);
				}
			} else {
				go = GameOption.fromString(args[1]);
				if (go != null){
					if (go.hasValue()){
						if (args.length < 3){
							sendMessage(sender, "&c"+go.name()+" needs a value");
							return sendMessage(sender, help1);
						} else {
							value = GameOption.getValue(go, args[2]);
						}
					}
					setOption(sender,go,value);
				}
			}
		} catch (Exception e) {
			sendMessage(sender, "&cCould not set game option");
			sendMessage(sender, "&c" +e.getMessage());
			return false;
		}

		if (go==null && to == null)
			return sendMessage(sender, "&cOption &6" + args[1] + "&c was not found");
		String opString = go != null ? go.name() : to.name();

		rc.saveParams(params);
		ParamController.addMatchParams(params);
		if (to != null){
			ParamController.setTransitionOptions(params, params.getTransitionOptions());
		}
		if (value != null)
			sendMessage(sender, "&2Game options &6"+opString+"&2 changed to &6"+value);
		else
			sendMessage(sender, "&2Game options &6"+opString+"&2 changed");
		if (go != null){
			switch(go){
			case COMMAND:
				sendMessage(sender, "&c[Info]&e This option will change after a restart");
				break;
			default: /* do nothing */
			}
		}
		return true;
	}

	public boolean setOption(CommandSender sender, GameOption option, Object value)
			throws InvalidOptionException {
		int iv;
		switch(option){
		case NLIVES: params.setNLives((Integer)value); break;
		case NTEAMS: params.setNTeams((MinMax) value);  break;
		case TEAMSIZE: params.setTeamSizes((MinMax) value);  break;
		case PREFIX: params.setPrefix((String)value); break;
		case COMMAND: params.setCommand((String)value); break;
		case MATCHTIME: params.setMatchTime((Integer)value);break;
		case CLOSEWAITROOMWHILERUNNING: params.setWaitroomClosedWhileRunning((Boolean)value); break;
		case CANCELIFNOTENOUGHPLAYERS: params.setCancelIfNotEnoughPlayers((Boolean)value); break;
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

	public boolean setOption(CommandSender sender, MatchState state, TransitionOption to, Object value)
			throws InvalidOptionException {
		if (to.hasValue() && value == null)
			throw new InvalidOptionException("Transition Option " + to +" needs a value! " + to+"=<value>");
		MatchTransitions tops = params.getTransitionOptions();
		if (to == TransitionOption.GIVEITEMS){
			if (!(sender instanceof Player)){
				throw new InvalidOptionException("&cYou need to be in game to set this option");}
			value = InventoryUtil.getItemList((Player) sender);
		} else if (to == TransitionOption.ENCHANTS){
			List<PotionEffect> effects = tops.hasOptionAt(state, to) ?
					tops.getOptions(state).getEffects() : new ArrayList<PotionEffect>();
			effects.add((PotionEffect)value);
			value = effects;
		}
		/// For teleport options, remove them from other places where they just dont make sense
		HashSet<TransitionOption> tpOps =
				new HashSet<TransitionOption>(Arrays.asList(
						TransitionOption.TELEPORTIN,TransitionOption.TELEPORTWAITROOM ,
						TransitionOption.TELEPORTCOURTYARD, TransitionOption.TELEPORTLOBBY,
						TransitionOption.TELEPORTMAINLOBBY, TransitionOption.TELEPORTMAINWAITROOM,
						TransitionOption.TELEPORTSPECTATE
						));
		if ((state == MatchState.ONPRESTART || state == MatchState.ONSTART || state == MatchState.ONJOIN) &&
				tpOps.contains(to)){
			tops.removeTransitionOption(MatchState.ONPRESTART, to);
			tops.removeTransitionOption(MatchState.ONSTART, to);
			tops.removeTransitionOption(MatchState.ONJOIN, to);
			for (TransitionOption op: tpOps){
				tops.removeTransitionOption(state, op);}
		}
		tops.addTransitionOption(state, to,value);
		return true;
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

	private boolean deleteTransitionOption(MatchState state, String key) throws Exception{
		TransitionOption to = null;
		to = TransitionOption.fromString(key);
		MatchTransitions tops = params.getTransitionOptions();
		return tops.removeTransitionOption(state,to);
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

	private boolean sendMessage(CommandSender sender, String msg){
		return MessageUtil.sendMessage(sender, msg);
	}
}
