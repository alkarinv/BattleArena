package mc.alk.arena.executors;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Permissions;
import mc.alk.arena.controllers.ArenaAlterController;
import mc.alk.arena.controllers.ArenaAlterController.ArenaOptionPair;
import mc.alk.arena.controllers.ArenaAlterController.ChangeType;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.ArenaEditor.CurrentSelection;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.StateController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CommandLineString;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.options.AlterParamOption;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.pairs.ParamAlterOptionPair;
import mc.alk.arena.objects.pairs.TransitionOptionTuple;
import mc.alk.arena.objects.spawns.SpawnIndex;
import mc.alk.arena.objects.teams.TeamIndex;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.ServerUtil;
import mc.alk.arena.util.TeamUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class CustomCommandExecutor extends BaseExecutor{

    protected final BattleArenaController ac;
    protected final EventController ec;
    protected final ArenaEditor aec;

    protected CustomCommandExecutor(){
        super();
        this.ac = BattleArena.getBAController();
        this.ec = BattleArena.getEventController();
        this.aec = BattleArena.getArenaEditor();
    }

    @Override
    protected boolean validCommandSenderClass(Class<?> clazz){
        return super.validCommandSenderClass(clazz) || clazz == ArenaPlayer.class;
    }

    @Override
    protected boolean hasAdminPerms(CommandSender sender){
        return super.hasAdminPerms(sender) || sender.hasPermission(Permissions.ADMIN_NODE);
    }

    @Override
    protected Object verifySender(CommandSender sender, Class<?> clazz) {
        if (clazz == ArenaPlayer.class){
            if (!(sender instanceof Player))
                throw new IllegalArgumentException(ONLY_INGAME);
            return BattleArena.toArenaPlayer((Player)sender);
        }
        return super.verifySender(sender,clazz);
    }

    @Override
    protected String getUsageString(Class<?> clazz) {
        if (ArenaPlayer.class == clazz){
            return "<player> ";
        } else if (Arena.class.isAssignableFrom(clazz)){
            return "<arena> ";
        } else if (ChangeType.class == clazz){
            return "<Arena | Lobby | Waitroom>";
        } else if (ParamAlterOptionPair.class == clazz){
            return "<GameOption> [value]";
        } else if (TransitionOptionTuple.class == clazz){
            return "<GameStage> <Option> [value]";
        } else if (TeamIndex.class == clazz){
            return "<team>";
        } else if (SpawnIndex.class == clazz){
            return "<team> [spawn #]";
        } else if (MatchParams.class == clazz){
            return "";
        } else if (EventParams.class == clazz){
            return "";
        } else if (ArenaOptionPair.class == clazz){
            return "<ArenaOption> [value]";
        }
        return super.getUsageString(clazz);
    }

    @Override
    protected Object verifyArg(CommandSender sender, Class<?> clazz, Command command, String[] args,int curIndex, AtomicInteger numUsedStrings) {
        if (EventParams.class == clazz){
            return verifyEventParams(command);
        } else if (MatchParams.class == clazz){
            return verifyMatchParams(command);
        } else if (CurrentSelection.class == clazz){
            return verifyCurrentSelection(sender);
        }
        if (args[curIndex] == null)
            throw new ArrayIndexOutOfBoundsException();
        numUsedStrings.set(1);
        String string = args[curIndex];
        if (ArenaPlayer.class == clazz){
            return verifyArenaPlayer(string);
        } else if (Arena.class.isAssignableFrom(clazz)){
            return verifyArena(clazz, string);
        } else if (ChangeType.class == clazz){
            return verifyChangeType(string);
        } else if (ParamAlterOptionPair.class == clazz){
            return verifyGameOption(sender,args,curIndex,numUsedStrings);
        } else if (TransitionOptionTuple.class == clazz){
            return verifyTransitionOptionTuple(sender, args, curIndex, numUsedStrings);
        } else if (ArenaOptionPair.class == clazz){
            return verifyArenaOptionPair(sender, args, curIndex, numUsedStrings);
        } else if (TeamIndex.class == clazz){
            return verifyTeamIndex(string);
        } else if (SpawnIndex.class == clazz){
            return verifySpawnIndex(args, curIndex, numUsedStrings);
        }
        return super.verifyArg(sender, clazz, command, args, curIndex, numUsedStrings);
    }

    private SpawnIndex verifySpawnIndex(String[] args, int curIndex, AtomicInteger numUsedStrings) {
        Integer ti = TeamUtil.getFromHumanTeamIndex(args[curIndex]);
        if (ti == null) {
            throw new IllegalArgumentException(ChatColor.RED + "SpawnIndex for team &6" + args[curIndex] + "&c isn't valid");
        }
        if (args.length > curIndex+1){
            Integer ti2 = TeamUtil.getFromHumanTeamIndex(args[curIndex+1]);
            if (ti2 !=null) {
                numUsedStrings.set(2);
                return new SpawnIndex(ti, ti2);
            }
        }
        return new SpawnIndex(ti);
    }

    private TeamIndex verifyTeamIndex(String string) {
        Integer ti = TeamUtil.getFromHumanTeamIndex(string);
        if (ti == null) {
            throw new IllegalArgumentException(ChatColor.RED + "TeamIndex &6" + string + "&c isn't valid");
        }
        return new TeamIndex(ti);
    }

    private ArenaOptionPair verifyArenaOptionPair(CommandSender sender, String[] args, int curIndex, AtomicInteger numUsedStrings) {
        ChangeType ct = ChangeType.fromName(args[curIndex]);
        if (ct == null){
            throw new IllegalArgumentException(ChatColor.RED + "Option: &6" + args[curIndex]+
                    "&c does not exist. \n&cValid options are &6"+ChangeType.getValidList());
        }
        if (ct.needsPlayer() && !(sender instanceof Player))
            throw new IllegalArgumentException(ChatColor.RED + "You need to be online to change the option " + ct.name());
        ArenaOptionPair aop = new ArenaOptionPair();
        aop.ao = ct;
        if (ct == ChangeType.SPAWNLOC) {
            aop.value = ChangeType.getValue(ct, curIndex, args);
            if (aop.value == null){
                throw new IllegalArgumentException(ChatColor.RED + "Option " + ct.name()+" couldn't parse value "+args[curIndex]);
            }
            numUsedStrings.set(2);
            return aop;
        }
        if (ct.hasValue() && args.length < curIndex+2){
            throw new IllegalArgumentException(ChatColor.RED + "Option " + ct.name()+" needs a value");
        }
        if (ct.hasValue()){
            try {
                aop.value = ChangeType.getValue(ct, curIndex+1, args);
                if (aop.value == null){
                    throw new IllegalArgumentException(ChatColor.RED + "Option " + ct.name()+" couldn't parse value "+args[curIndex+1]);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(ChatColor.RED + "Option " + ct.name()+" couldn't parse value "+args[curIndex+1]);
            }
            numUsedStrings.set(2);
        } else {
            numUsedStrings.set(1);
        }

        return aop;
    }

    private TransitionOptionTuple verifyTransitionOptionTuple(
            CommandSender sender, String[] args, int curIndex, AtomicInteger numUsedStrings) {
        CompetitionState stage = StateController.fromString(args[curIndex]);
        if (stage==null){
            throw new IllegalArgumentException(ChatColor.RED + "You need to specify a Game Stage : [onJoin, onStart,...]");
        }

        if (args.length < curIndex+2){
            throw new IllegalArgumentException(ChatColor.RED + "Game stage " + stage+" needs a value");
        }
        TransitionOption to = TransitionOption.fromString(args[curIndex + 1]);
        if (to == null){
            throw new IllegalArgumentException(ChatColor.RED + "Couldn't recognize option " + args[curIndex+1]);
        }
        TransitionOptionTuple top = new TransitionOptionTuple();
        top.op = to;
        top.state = stage;
        if (to.hasValue() && args.length < curIndex+3){
            /// Special exceptions
            if (to == TransitionOption.TELEPORTTO){
                if (!(sender instanceof Player)){
                    throw new IllegalArgumentException(ChatColor.RED + "You need to be online to change the option " +
                            to.name());}
                top.value = ((Player) sender).getLocation();
                return top;
            } else {
                throw new IllegalArgumentException(ChatColor.RED + "Option " + to.name()+" needs a value");
            }

        }
        if (to == TransitionOption.GIVEITEMS && !(sender instanceof Player)){
            throw new IllegalArgumentException(ChatColor.RED + "You need to be online to change the option " +
                    to.name());}
        if (to.hasValue()){
            try {
                numUsedStrings.set(3);
                if (to == TransitionOption.DOCOMMANDS) {
                    String s = StringUtils.join(args, " ", curIndex + 2, args.length);
                    top.value = CommandLineString.parse(s);
                    numUsedStrings.set(args.length-curIndex);
                } else {
                    top.value = to.parseValue(args[curIndex+2]);
                }
                if (top.value == null){
                    throw new IllegalArgumentException(ChatColor.RED + "Option " + to.name()+" couldn't parse value "+args[curIndex+2]);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(ChatColor.RED + "Option " + to.name()+" couldn't parse value "+args[curIndex+2]);
            }
        } else {
            numUsedStrings.set(2);
        }
        return top;
    }

    private ParamAlterOptionPair verifyGameOption(CommandSender sender, String[] args, int curIndex, AtomicInteger numUsedStrings) {
        AlterParamOption go = AlterParamOption.fromString(args[curIndex]);
        if (go==null)
            throw new IllegalArgumentException(ChatColor.RED + "You need to specify a AlterParamOption");
        if (go.needsPlayer() && !(sender instanceof Player))
            throw new IllegalArgumentException(ChatColor.RED + "You need to be online to change the option " + go.name());

        ParamAlterOptionPair gop = new ParamAlterOptionPair();
        gop.alterParamOption = go;
        if (go.hasValue()){
            if (args.length < curIndex+2){
                throw new IllegalArgumentException(ChatColor.RED + "Game Option "+ go.name()+" needs a value");
            }
            numUsedStrings.set(2);

            try {
                gop.value = AlterParamOption.getValue(go, args[curIndex + 1]);
                if (gop.value == null){
                    throw new IllegalArgumentException(ChatColor.RED + "Option " + go.name()+" couldn't parse value "+args[curIndex+1]);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(ChatColor.RED + "Option " + go.name()+" couldn't parse value "+args[curIndex+1]);
            }
        }
        return gop;
    }


    private CurrentSelection verifyCurrentSelection(CommandSender sender) {
        CurrentSelection cs = aec.getCurrentSelection(sender);
        if (cs == null)
            throw new IllegalArgumentException(ChatColor.RED + "You need to select an arena first");
        if (System.currentTimeMillis() - cs.lastUsed > 5*60*1000){
            throw new IllegalArgumentException(ChatColor.RED + "its been over a 5 minutes since you selected an arena, reselect it");
        }
        cs.updateCurrentSelection();
        return cs;
    }

    private ChangeType verifyChangeType(String name) {
        ChangeType cs = ArenaAlterController.ChangeType.fromName(name);
        if (cs == null){
            throw new IllegalArgumentException(name+" is not a valid type. Waitroom, Lobby, Arena ");}
        return cs;
    }

    private ArenaPlayer verifyArenaPlayer(String name) throws IllegalArgumentException {
        Player p = ServerUtil.findPlayer(name);
        if (p == null || !p.isOnline())
            throw new IllegalArgumentException(name+" is not online ");
        return BattleArena.toArenaPlayer(p);
    }

    private Arena verifyArena(Class<?> arenaClass, String name) throws IllegalArgumentException {
        Arena arena = ac.getArena(name);
        if (arena == null){
            throw new IllegalArgumentException("Arena '" +name+"' doesnt exist" );}
        if (!arenaClass.isAssignableFrom(arena.getClass())){
            throw new IllegalArgumentException("Arena '" +name+"' isn't a "+arenaClass.getSimpleName()+" arena" );}
        return arena;
    }

    private MatchParams verifyMatchParams(Command command) throws IllegalArgumentException {
        MatchParams mp = ParamController.getMatchParams(command.getName());
        if (mp != null){
            return mp;
        } else {
            for (String alias : command.getAliases()){
                mp = ParamController.getMatchParams(alias);
                if (mp != null)
                    return mp;
            }
        }

        throw new IllegalArgumentException(ChatColor.RED + "Match parameters for a &6" + command.getName()+"&c can't be found");
    }

    private EventParams verifyEventParams(Command command) throws IllegalArgumentException {
        MatchParams mp = ParamController.getEventParamCopy(command.getName());
        if (mp != null){
            return (EventParams)mp;
        } else {
            for (String alias : command.getAliases()){
                mp = ParamController.getEventParamCopy(alias);
                if (mp != null)
                    return (EventParams) mp;
            }
        }

        throw new IllegalArgumentException(ChatColor.RED + "Event parameters for a &6" + command.getName()+"&c can't be found");
    }

    public static boolean sendMessage(ArenaPlayer player, String msg){
        return MessageUtil.sendMessage(player, msg);
    }


}

