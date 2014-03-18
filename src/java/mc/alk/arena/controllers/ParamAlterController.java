package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.plugins.WorldGuardController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.CommandLineString;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.RegisteredCompetition;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.GameOption;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.MinMax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ParamAlterController {
    MatchParams params;

    public ParamAlterController(MatchParams params){
        this.params = ParamController.getMatchParams(params.getType());
    }

    private static MatchParams getOrCreateTeamParams(Integer teamIndex, MatchParams params){
        Map<Integer, MatchParams> map = params.getThisTeamParams();
        if (map == null) {
            map = new HashMap<Integer, MatchParams>();
            params.setTeamParams(map);
        }
        MatchParams tp = map.get(teamIndex);
        if (tp == null) {
            tp = new MatchParams();
            map.put(teamIndex, tp);
        }
        tp.setParent(params);
        return tp;
    }

    public static boolean setTeamParams(CommandSender sender, Integer teamIndex, MatchParams params, GameOption option,
                                        Object value) throws InvalidOptionException {
        RegisteredCompetition rc = CompetitionController.getCompetition(params.getName());
        if (rc == null){
            throw new InvalidOptionException("&cGame &6" + params.getName() +"&c not found!");}

        MatchParams tp = getOrCreateTeamParams(teamIndex, params);
        setOption(sender, tp, option, value);
        saveParamsAndUpdate(rc, params);
        return true;
    }

    public static boolean setGameOption(CommandSender sender, MatchParams params, Integer teamIndex, GameOption option, Object value)
            throws InvalidOptionException {
        RegisteredCompetition rc = CompetitionController.getCompetition(params.getName());
        if (rc == null){
            throw new InvalidOptionException("&cGame &6" + params.getName() +"&c not found!");}
        if (teamIndex != null){
            MatchParams tp = getOrCreateTeamParams(teamIndex, params);
            setOption(sender, tp, option, value);
        } else {
            setOption(sender, params, option, value);
        }
        saveParamsAndUpdate(rc, params);
        return true;
    }

    public static boolean setGameOption(CommandSender sender, MatchParams params, Integer teamIndex,
                                        CompetitionState state, TransitionOption to, Object value) throws InvalidOptionException {
        RegisteredCompetition rc = CompetitionController.getCompetition(params.getName());
        if (rc == null){
            throw new InvalidOptionException("&cGame &6" + params.getName() +"&c not found!");}
        if (teamIndex != null){
            MatchParams tp = getOrCreateTeamParams(teamIndex, params);
            setOption(sender, tp, state, to, value);
        } else {
            setOption(sender, params, state, to, value);
        }

        saveParamsAndUpdate(rc, params);
        return true;

    }

    private static void saveParamsAndUpdate(RegisteredCompetition rc, MatchParams params) {
        rc.saveParams(params);
        ParamController.addMatchParams(params);
    }



    public static boolean setOption(CommandSender sender, MatchParams params, GameOption option, Object value)
            throws IllegalStateException {
        int iv;
        switch(option){
            case NLIVES: params.setNLives((Integer)value); break;
            case NTEAMS: params.setNTeams((MinMax) value);  break;
            case FORCESTARTTIME: params.setForceStartTime((Integer) value);  break;
            case TEAMSIZE: params.setTeamSizes((MinMax) value);  break;
            case PREFIX: params.setPrefix((String)value); break;
            case SIGNDISPLAYNAME: params.setSignDisplayName((String)value); break;
            case DISPLAYNAME: params.setDisplayName((String)value); break;
            case COMMAND: params.setCommand((String)value); break;
            case MATCHTIME: params.setMatchTime((Integer)value);break;
            case CLOSEWAITROOMWHILERUNNING: params.setWaitroomClosedWhileRunning((Boolean)value); break;
            case CANCELIFNOTENOUGHPLAYERS: params.setCancelIfNotEnoughPlayers((Boolean)value); break;
            case ALLOWEDTEAMSIZEDIFFERENCE: params.setAllowedTeamSizeDifference((Integer)value); break;
            case NCUMONCURRENTCOMPETITIONS: params.setNConcurrentCompetitions((Integer)value); break;
            case PRESTARTTIME:
                iv = (Integer) value;
                checkGreater(iv,0, true);
                params.setSecondsTillMatch(iv);
                break;
            case VICTORYTIME:
                iv = (Integer) value;
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
        switch(option){
            case COMMAND:
                sendMessage(sender, "&c[Info]&e This option will change after a restart");
                break;
            default: /* do nothing */
        }
        return true;
    }

    public static boolean setOption(CommandSender sender, MatchParams params, CompetitionState state, TransitionOption to, Object value)
            throws InvalidOptionException {

        if (to.hasValue() && value == null)
            throw new InvalidOptionException("Transition Option " + to +" needs a value! " + to+"=<value>");
        MatchTransitions tops = params.getTransitionOptions();
        if (to == TransitionOption.GIVEITEMS){
            if (!(sender instanceof Player)){
                throw new InvalidOptionException("&cYou need to be in game to set this option");}
            value = InventoryUtil.getItemList((Player) sender);
        } else if (to == TransitionOption.ENCHANTS){
            List<PotionEffect> list = tops.hasOptionAt(state, to) ?
                    tops.getOptions(state).getEffects() : new ArrayList<PotionEffect>();
            list.add((PotionEffect) value);
            value = list;
        } else if (to == TransitionOption.DOCOMMANDS){
            List<CommandLineString> list = tops.hasOptionAt(state, to) ?
                    tops.getOptions(state).getDoCommands() : new ArrayList<CommandLineString>();
            list.add((CommandLineString)value);
            value = list;
        } else if (to == TransitionOption.GIVECLASS){
            Map<Integer, ArenaClass> map = tops.hasOptionAt(state, to) ?
                    tops.getOptions(state).getClasses() : new HashMap<Integer, ArenaClass>();
            map.put(ArenaClass.DEFAULT, (ArenaClass) value);
            value = map;
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
        if (state == MatchState.DEFAULTS){
            if (to == TransitionOption.WGNOENTER){
                for (Arena a: BattleArena.getBAController().getArenas(params)){
                    if (a.getWorldGuardRegion()!=null){
                        WorldGuardController.setFlag(a.getWorldGuardRegion(), "entry", false);}
                }
            } else if (to == TransitionOption.WGNOLEAVE){
                for (Arena a: BattleArena.getBAController().getArenas(params)){
                    if (a.getWorldGuardRegion()!=null){
                        WorldGuardController.setFlag(a.getWorldGuardRegion(), "exit", false);}
                }
            }
        }
        /// if we removed teleportIn, then we should put it back in the most logical place
        if ((state == MatchState.ONPRESTART || state == MatchState.ONJOIN) &&
                tpOps.contains(to) && to!=TransitionOption.TELEPORTIN &&
                !tops.hasOptionAt(MatchState.ONPRESTART,TransitionOption.TELEPORTIN)){
            tops.addTransitionOption(MatchState.ONSTART, TransitionOption.TELEPORTIN);
        }

        tops.addTransitionOption(state, to, value);
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
                saveParamsAndUpdate(rc, params);
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
        CompetitionState state = StateController.fromString(args[1]);
        if (state != null){
            if (args.length < 3){
                MatchTransitions tops = params.getTransitionOptions();
                tops.deleteOptions(state);
                return  sendMessage(sender, "&2Options at &6"+state +"&2 are now empty");
            } else {
                final String key = args[2].trim().toUpperCase();
                try{
                    deleteTransitionOption(state, key);
                    rc.saveParams(params);
                    sendMessage(sender, "&2Game option &6"+state +" "+key+" &2 removed");
                    MatchTransitions tops = params.getTransitionOptions();
                    TransitionOptions ops = tops.getOptions(state);
                    if (ops == null){
                        sendMessage(sender, "&2Options at &6"+state +"&2 are empty");
                    } else {
                        sendMessage(sender, "&2Options at &6"+state +"&2 are &6" + ops.toString());
                    }
                    return true;
                } catch (Exception e) {
                    sendMessage(sender, "&cCould not remove game option " + args[1]);
                    sendMessage(sender, e.getMessage());
                    return false;
                }

            }
        }
        sendMessage(sender, "&cGame option &6" + args[1] +"&c not found!");
        return false;
    }

    private boolean deleteTransitionOption(CompetitionState state, String key) throws Exception{
        TransitionOption to = TransitionOption.fromString(key);
        MatchTransitions tops = params.getTransitionOptions();
        return tops.removeTransitionOption(state,to);
    }

    private boolean deleteGameOption(GameOption go) throws Exception {
        switch(go){
            case NLIVES: params.setNLives(null); break;
            case NTEAMS: params.setNTeams(null);  break;
            case TEAMSIZE: params.setTeamSizes(null);  break;
            case PREFIX: params.setPrefix(null); break;
            case SIGNDISPLAYNAME: params.setSignDisplayName(null); break;
            case COMMAND: params.setCommand(null); break;
            case MATCHTIME: params.setMatchTime(null);break;
            case PRESTARTTIME: params.setSecondsTillMatch(null);break;
            case VICTORYTIME: params.setSecondsToLoot(null); break;
            case VICTORYCONDITION: params.setVictoryCondition(null); break;
            case CLOSEWAITROOMWHILERUNNING: params.setWaitroomClosedWhileRunning(null);
            case RATED: params.setRated(false); break;
            default:
                break;
        }
        return true;
    }

    private static void checkGreater(int iv, int bound, boolean inclusive) throws IllegalStateException {
        if (inclusive && iv < bound) throw new IllegalStateException(iv +"  must be greater or equal to " + bound);
        else if (iv <= bound) throw new IllegalStateException(iv +"  must be greater than " + bound);
    }

    private static boolean sendMessage(CommandSender sender, String msg){
        return MessageUtil.sendMessage(sender, msg);
    }


}
