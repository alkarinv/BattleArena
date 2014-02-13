package mc.alk.arena.executors;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.ArenaAlterController;
import mc.alk.arena.controllers.ArenaDebugger;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.ArenaEditor.CurrentSelection;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.options.SpawnOptions;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArenaEditorExecutor extends CustomCommandExecutor {
    public static String idPrefix = "ar_";

    WorldEditPlugin wep;
    public ArenaEditorExecutor(){
        super();
    }

    @MCCommand(cmds={"select","sel"}, admin=true)
    public boolean arenaSelect(CommandSender sender, Arena arena) {
        ArenaEditor aac = BattleArena.getArenaEditor();
        aac.setCurrentArena(sender, arena);
        return MessageUtil.sendMessage(sender, "&2You have selected arena &6" + arena.getName());
    }

    @MCCommand(cmds={"ds","deletespawn"}, admin=true, usage="/aa deleteSpawn <index>")
    public boolean arenaDeleteSpawn(CommandSender sender, CurrentSelection cs, Integer number) {
        if (number <= 0 || number > 10000){
            return MessageUtil.sendMessage(sender, "&cYou need to specify an index within the range &61-10000");}

        Arena a = cs.getArena();
        TimedSpawn ts = a.deleteTimedSpawn((long) number);
        if (ts != null){
            ac.updateArena(a);
            BattleArena.saveArenas();
            return MessageUtil.sendMessage(sender, "&6"+a.getName()+ "&e has deleted index=&4D" + number+"&e that had spawn="+ts);
        } else {
            return MessageUtil.sendMessage(sender, "&cThere was no spawn at that index");
        }
    }

    @MCCommand(cmds={"as","addspawn"}, admin=true, min=2,
            usage="/aa addspawn <mob/item/spawnGroup> [buffs or effects] [number] [fs=first spawn time] [rt=respawn time] [trigger=<trigger type>] [index|i=<index>]")
    public boolean arenaAddSpawn(Player sender, CurrentSelection cs, String[] args) {
        Long index = parseIndex(sender, cs.getArena(), args);
        if (index == -1)
            return true;
        Arena a = cs.getArena();

        TimedSpawn spawn = SpawnSerializer.parseSpawn(Arrays.copyOfRange(args, 0, args.length - 1));
        if (spawn == null){
            return MessageUtil.sendMessage(sender,"Couldnt recognize spawn " + args[1]);
        }
        Location l = sender.getLocation();
        spawn.getSpawn().setLocation(l);


        a.addTimedSpawn(index,spawn);
        ac.updateArena(a);
        ArenaSerializer.saveArenas(a.getArenaType().getPlugin());
        return MessageUtil.sendMessage(sender, "&6"+a.getName()+ "&e now has spawn &6" + spawn +"&2  index=&5" + index);
    }

    @MCCommand(cmds={"ab","addBlock"}, admin=true,
            usage="/aa addBlock [number] [fs=first spawn time] [rt=respawn time] [trigger=<trigger type>] [resetTo=<block>] [index]")
    public boolean arenaAddBlock(Player sender, CurrentSelection cs, String[] args) {
        Long index = parseIndex(sender, cs.getArena(), args);
        if (index == -1)
            return true;
        SpawnOptions po = SpawnOptions.parseSpawnOptions(args);
        cs.setStartListening(index, po);

        return MessageUtil.sendMessage(sender, "&2Success: &eClick a block to add the block spawn");
    }

    private Long parseIndex(CommandSender sender, Arena arena, String[] args) {
        Long number = -1L;
        String last = args[args.length - 1];
        String split = "index=|i=";
        try {
            String[] s = last.split(split);
            if (s.length==2)
                number = Long.parseLong(s[1]);
        } catch (Exception e) {
            MessageUtil.sendMessage(sender, "&cindex "+last+" was bad");
            return -1L;
        }
        long nextIndex = 1;
        if (number == -1L) {
            Map<Long, TimedSpawn> spawns = arena.getTimedSpawns();
            if (spawns==null) {
                return 1L;
            }
            List<Long> keys = new ArrayList<Long>(spawns.keySet());
            Collections.sort(keys);
            for (Long k : keys){
                if (k!= nextIndex)
                    break;
                nextIndex++;
            }
            number = nextIndex;
        }

        if (number <= 0 || number > 10000) {
            MessageUtil.sendMessage(sender, "&cYou need to specify an index within the range &61-10000");
            return -1L;
        }
        return number;
    }

    @MCCommand(cmds = {}, admin = true, perm = "arena.alter")
    public boolean arenaGeneric(CommandSender sender,CurrentSelection cs,  String args[]) {
        Arena arena = cs.getArena();
        String newargs[] = new String[args.length + 2];
        newargs[0] = "setOption";
        newargs[1] = arena.getName();
        System.arraycopy(args, 0, newargs, 2, args.length);
        return alter(sender, arena, newargs);
    }

    @MCCommand(cmds = {"spawn"}, admin = true, perm = "arena.alter")
    public boolean arenaSetSpawn(CommandSender sender,CurrentSelection cs,  String index) {
        return alter(sender, cs.getArena(), new String[]{"","spawn", index});
    }

    @MCCommand(cmds = {"wr","waitRoom"}, admin = true, perm = "arena.alter")
    public boolean arenaSetWaitroom(CommandSender sender,CurrentSelection cs,  String index) {
        return alter(sender, cs.getArena(), new String[]{"", "","wr", index});
    }

    @MCCommand(cmds = {"s","Spectate"}, admin = true, perm = "arena.alter")
    public boolean arenaSetSpectate(CommandSender sender, CurrentSelection cs, String index) {
        return alter(sender, cs.getArena(), new String[]{"", "","spectate", index});
    }

    @MCCommand(cmds = {"l","lobby"}, admin = true, perm = "arena.alter")
    public boolean arenaSetLobby(CommandSender sender, CurrentSelection cs, String index) {
        return alter(sender, cs.getArena(), new String[]{"", "","lobby", index});
    }

    private boolean alter(CommandSender sender, Arena a, String[] args) {
        try {
            ArenaAlterController.setArenaOption(sender, a,true, args);
            return true;
        } catch (IllegalStateException e) {
            return sendMessage(sender, "&c" + e.getMessage());
        }
    }

    @MCCommand(cmds = { "setArenaOption", "setOption", "alter", "edit" }, admin = true, perm = "arena.alter")
    public boolean arenaSetOption(CommandSender sender, CurrentSelection cs, String[] args) {
        try {
            Arena arena = cs.getArena();
            String newargs[] = new String[args.length + 1];
            newargs[1] = arena.getName();
            for (int i = 0; i < args.length; i++) {
                if (i < 1){
                    newargs[i] = args[i];
                } else if (i >= 1){
                    newargs[i + 1] = args[i];
                }
            }
            ArenaAlterController.setArenaOption(sender, arena, true, newargs);
        } catch (IllegalStateException e) {
            return sendMessage(sender, "&c" + e.getMessage());
        }
        return true;
    }

    @MCCommand(cmds={"hidespawns"}, admin=true, usage="hidespawns")
    public boolean arenaHideSpawns(Player sender, CurrentSelection cs) {
        Arena arena = cs.getArena();
        ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
        ad.hideSpawns(sender);
        ArenaDebugger.removeDebugger(ad);
        return sendMessage(sender,ChatColor.YELLOW+ "You are hiding spawns for &6" + arena.getName());
    }

    @MCCommand(cmds={"showspawns"}, admin=true,  usage="showspawns")
    public boolean arenaShowSpawns(Player sender,CurrentSelection cs) {
        Arena arena = cs.getArena();

        ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
        ad.hideSpawns(sender);
        ad.showSpawns(sender);
        return sendMessage(sender, ChatColor.GREEN + "You are showing spawns for &6" + arena.getName());
    }

    @MCCommand(cmds={"listspawns"}, admin=true)
    public boolean arenaListSpawns(Player sender,CurrentSelection cs) {
        Arena arena = cs.getArena();
        sendMessage(sender, ChatColor.GREEN + "You are listing spawns for &6" + arena.getName());
        Map<Long, TimedSpawn> spawns = arena.getTimedSpawns();
        if (spawns==null) {
            return sendMessage(sender, ChatColor.RED+ "Arena has no spawns");}
        List<Long> keys = new ArrayList<Long>(spawns.keySet());
        Collections.sort(keys);
        for (Long k : keys) {
            sendMessage(sender, "&5"+k+"&e: "+spawns.get(k).getDisplayName());
        }
        return true;
    }

}
