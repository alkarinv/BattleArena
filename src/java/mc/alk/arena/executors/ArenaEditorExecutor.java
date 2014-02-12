package mc.alk.arena.executors;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.ArenaAlterController;
import mc.alk.arena.controllers.ArenaDebugger;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaEditorExecutor extends CustomCommandExecutor {
    public static String idPrefix = "ar_";

    WorldEditPlugin wep;
    Map<String, ArenaEditor> editing = new HashMap<String, ArenaEditor>();
    public ArenaEditorExecutor(){
        super();
    }

    @MCCommand(cmds={"select","sel"}, admin=true)
    public boolean arenaSelect(CommandSender sender, Arena arena) {
        ArenaEditor aac = new ArenaEditor();
        aac.setCurrentArena(sender, arena);
        editing.put(sender.getName(), aac);
        return MessageUtil.sendMessage(sender, "&2You have selected arena &6" + arena.getName());
    }

    @MCCommand(cmds={"ds","deletespawn"}, selection=true, admin=true,
            usage="/aa deleteSpawn <index>")
    public boolean arenaDeleteSpawn(CommandSender sender, Integer number) {
        if (number <= 0 || number > 10000){
            return MessageUtil.sendMessage(sender, "&cYou need to specify an index within the range &61-10000");}
        Arena a = editing.get(sender.getName()).getArena(sender);
        TimedSpawn ts = a.deleteTimedSpawn(new Long(number));
        if (ts != null){
            ac.updateArena(a);
            BattleArena.saveArenas();
            return MessageUtil.sendMessage(sender, "&6"+a.getName()+ "&e has deleted index=&4" + number+"&e that had spawn="+ts);
        } else {
            return MessageUtil.sendMessage(sender, "&cThere was no spawn at that index");
        }
    }

    @MCCommand(cmds={"as","addspawn"}, selection=true, admin=true, min=2,
            usage="/aa addspawn <mob/item/block/spawnGroup> [buffs or effects] [number] [fs=first spawn time] [rt=respawn time] [trigger=<trigger type>]")
    public boolean arenaAddSpawn(Player sender, String[] args) {
        Long index = parseIndex(sender, args);
        if (index == -1)
            return true;
        ArenaEditor ae = editing.get(sender.getName());
        Arena a = ae.getArena(sender);

        TimedSpawn spawn = parseSpawn(Arrays.copyOfRange(args, 0, args.length-1));
        if (spawn == null){
            return MessageUtil.sendMessage(sender,"Couldnt recognize spawn " + args[1]);
        }
        Location l = sender.getLocation();
        spawn.getSpawn().setLocation(l);


        a.addTimedSpawn(index,spawn);
        ac.updateArena(a);
        BattleArena.saveArenas();
        return MessageUtil.sendMessage(sender, "&6"+a.getName()+ "&e now has spawn &6" + spawn +"&2  index=&4" + index);
    }

    @MCCommand(cmds={"as","addBlock"}, selection=true, admin=true,
            usage="/aa addBlock [number] [fs=first spawn time] [rt=respawn time] [trigger=<trigger type>] [resetTo=<air|block>]")
    public boolean arenaAddBlock(Player sender, String[] args) {
        Long index = parseIndex(sender, args);
        if (index == -1)
            return true;
        Arena a = editing.get(sender.getName()).getArena(sender);

        TimedSpawn spawn = parseSpawn(Arrays.copyOfRange(args, 0, args.length-1));
        if (spawn == null){
            return MessageUtil.sendMessage(sender,"Couldnt recognize spawn " + args[1]);
        }
        Location l = sender.getLocation();
        spawn.getSpawn().setLocation(l);


        a.addTimedSpawn(index,spawn);
        ac.updateArena(a);
        BattleArena.saveArenas();
        return MessageUtil.sendMessage(sender, "&6"+a.getName()+ "&e now has spawn &6" + spawn +"&2  index=&4" + index);
    }

    private Long parseIndex(CommandSender sender, String[] args) {
        Long number;
        try {
            number = Long.parseLong(args[args.length - 1]);
        } catch (Exception e) {
            MessageUtil.sendMessage(sender, "&cYou need to specify an index as the final value. &61-10000");
            return -1L;
        }

        if (number <= 0 || number > 10000) {
            MessageUtil.sendMessage(sender, "&cYou need to specify an index within the range &61-10000");
            return -1L;
        }
        return number;
    }

    @MCCommand(cmds = {}, admin = true, perm = "arena.alter")
    public boolean arenaGeneric(CommandSender sender, String args[]) {
        Arena arena = editing.get(sender.getName()).getArena(sender);
        String newargs[] = new String[args.length + 2];
        newargs[0] = "setOption";
        newargs[1] = arena.getName();
        System.arraycopy(args, 0, newargs, 2, args.length);
        return alter(sender, getArena(sender), newargs);
    }

    @MCCommand(cmds = {"spawn"}, admin = true, perm = "arena.alter")
    public boolean arenaSetSpawn(CommandSender sender, String index) {
        return alter(sender, getArena(sender), new String[]{"","spawn", index});
    }

    @MCCommand(cmds = {"wr","waitRoom"}, admin = true, perm = "arena.alter")
    public boolean arenaSetWaitroom(CommandSender sender, String index) {
        return alter(sender, getArena(sender), new String[]{"", "wr", index});
    }

    @MCCommand(cmds = {"s","Spectate"}, admin = true, perm = "arena.alter")
    public boolean arenaSetSpectate(CommandSender sender, String index) {
        return alter(sender, getArena(sender), new String[]{"", "spectate", index});
    }

    @MCCommand(cmds = {"l","lobby"}, admin = true, perm = "arena.alter")
    public boolean arenaSetLobby(CommandSender sender, String index) {
        return alter(sender, getArena(sender), new String[]{"", "lobby", index});
    }

    private boolean alter(CommandSender sender, Arena a, String[] args) {
        try {
            ArenaAlterController.setArenaOption(sender, a,true, args);
            return true;
        } catch (IllegalStateException e) {
            return sendMessage(sender, "&c" + e.getMessage());
        }
    }

    private Arena getArena(CommandSender sender){
        return editing.get(sender.getName()).getArena(sender);
    }


    @MCCommand(cmds = { "setArenaOption", "setOption", "alter", "edit" }, admin = true, perm = "arena.alter")
    public boolean arenaSetOption(CommandSender sender, String[] args) {
        try {
            Arena arena = editing.get(sender.getName()).getArena(sender);
            String newargs[] = new String[args.length + 1];
            newargs[1] = arena.getName();
            for (int i = 0; i < args.length; i++) {
                if (i < 1){
                    newargs[i] = args[i];
                } else if (i >= 1){
                    newargs[i + 1] = args[i];
                }
            }
            ArenaAlterController.setArenaOption(sender, arena, true, args);
        } catch (IllegalStateException e) {
            return sendMessage(sender, "&c" + e.getMessage());
        }
        return true;
    }

    private TimedSpawn parseSpawn(String[] args) {
        List<String> spawnArgs = new ArrayList<String>();
        //		List<EditOption> optionArgs = new ArrayList<EditOption>();
        Integer fs = 0; /// first spawn time
        Integer rs = 30; /// Respawn time
        Integer ds = 0; /// Despawn time
        for (int i=1;i< args.length;i++){
            String arg = args[i];
            if (arg.contains("=")){
                String as[] = arg.split("=");
                Integer time = null;
                try{
                    time = Integer.valueOf(as[1]);
                } catch (Exception e){}
                if (as[0].equalsIgnoreCase("fs")){
                    fs = time;
                } else if (as[0].equalsIgnoreCase("rs") || as[0].equalsIgnoreCase("rt")){
                    rs = time;
                } else if (as[0].equalsIgnoreCase("ds")){
                    ds = time;
                }
            } else {
                spawnArgs.add(arg);
            }
        }
        int number = -1;
        if (spawnArgs.size() > 1){
            try {number = Integer.parseInt(spawnArgs.get(spawnArgs.size()-1));} catch(Exception e){}
        }
        if (number == -1){
            spawnArgs.add("1");}
        List<SpawnInstance> spawn = SpawnSerializer.parseSpawnable(spawnArgs);
        if (spawn == null){
            return null;
        }
        SpawnInstance si = spawn.get(0);
        if (si == null)
            return null;
        TimedSpawn ts = new TimedSpawn(fs,rs,ds,si);
        return ts;
    }

    @MCCommand(cmds={"hidespawns"}, admin=true, selection=true, usage="hidespawns")
    public boolean arenaHideSpawns(Player sender) {
        Arena arena = editing.get(sender.getName()).getArena(sender);
        ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
        ad.hideSpawns(sender);
        ArenaDebugger.removeDebugger(ad);
        return sendMessage(sender,ChatColor.YELLOW+ "You are hiding spawns for &6" + arena.getName());
    }

    @MCCommand(cmds={"showspawns"}, admin=true, selection=true, usage="showspawns")
    public boolean arenaShowSpawns(Player sender) {
        Arena arena = editing.get(sender.getName()).getArena(sender);

        ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
        ad.hideSpawns(sender);
        ad.showSpawns(sender);
        return sendMessage(sender,ChatColor.GREEN+ "You are showing spawns for &6" + arena.getName());
    }

}
