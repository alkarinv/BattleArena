package mc.alk.arena.executors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.ArenaDebugger;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.ProtectionController;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ArenaEditorExecutor extends CustomCommandExecutor {
	public static String idPrefix = "ar_";
	ProtectionController pc;
	WorldEditPlugin wep;
	ArenaEditor aac;
	public ArenaEditorExecutor(){
		super();
		this.ac = BattleArena.getBAC();
		this.aac = BattleArena.getArenaEditor();
	}

	@MCCommand(cmds={"select","sel"}, inGame=true, op=true)
	public boolean arenaSelect(CommandSender sender, Arena arena) {
		aac.setCurrentArena((Player) sender, arena);
		return MessageUtil.sendMessage(sender,"You have selected " + arena.getName());
	}

	@MCCommand(cmds={"as","addspawn"}, selection=true, inGame=true, op=true, min=2,
			usage="/aa addspawn <mob/item/block/spawnGroup> [buffs or effects] [number] [fs=first spawn time] [rt=respawn time] [trigger=<trigger type>]")
	public boolean arenaAddSpawn(Player sender, String[] args) {
		Long number = -1L;
		try {number = Long.parseLong(args[args.length-1].toString());} 
		catch(Exception e){
			return MessageUtil.sendMessage(sender, "&cYou need to specify an index as the final value. &61-10000");
		}
		if (number == -1){
			number = 1L;}
		if (number <= 0 || number > 10000){
			return MessageUtil.sendMessage(sender, "&cYou need to specify an index within the range &61-10000");}

		Arena a = aac.getArena(sender);
		SpawnInstance spawn = parseSpawn(Arrays.copyOfRange(args, 0, args.length-1));
		if (spawn == null){
			return MessageUtil.sendMessage(sender,"Couldnt recognize spawn " + args[1]);			
		}
		Location l = sender.getLocation();
		spawn.setLocation(l);
		TimedSpawn ts = new TimedSpawn(0,30,0,spawn);


		a.addTimedSpawn(number,ts);
		ac.updateArena(a);
		return MessageUtil.sendMessage(sender, "&6"+a.getName()+ "&e now has spawn &6" + spawn +"&2  index=&4" + number);
	}

	private SpawnInstance parseSpawn(String[] args) {
		List<String> spawnArgs = new ArrayList<String>();
		//		List<EditOption> optionArgs = new ArrayList<EditOption>();
		for (int i=1;i< args.length;i++){
			String arg = (String) args[i];
			if (arg.contains("=")){

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
		return spawn.get(0);
	}

	@MCCommand(cmds={"region","addregion","addr"}, selection=true, op=true)
	public boolean arenaAddWorldGuardRegion(CommandSender sender, Player p, Object[] args) {
		Arena a = aac.getArena(p);
		Selection sel = wep.getSelection(p);
		if (sel == null)
			return MessageUtil.sendMessage(sender, ChatColor.RED + "Please select the protection area first.");
		String regionName = idPrefix+a.getName();
		ProtectedRegion region = pc.addRegion(p, sel, regionName);
		if (region == null)
			return MessageUtil.sendMessage(sender, ChatColor.RED + "Selected region could not be made");
		a.addRegion(regionName);
		ac.updateArena(a);
		return MessageUtil.sendMessage(sender, ChatColor.GREEN + "Region added to " + a.getName());
	}

	@MCCommand(cmds={"hidespawns"}, admin=true, usage="hidespawns <arena>")
	public boolean arenaHideSpawns(CommandSender sender, Arena arena) {
		ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
		ad.hideSpawns();
		ArenaDebugger.removeDebugger(ad);
		return sendMessage(sender,ChatColor.YELLOW+ "You are hiding spawns for " + arena.getName());
	}

	@MCCommand(cmds={"showspawns"}, admin=true, usage="showspawns <arena>")
	public boolean arenaShowSpawns(CommandSender sender, Arena arena) {
		ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
		ad.hideSpawns();
		ad.showSpawns();
		return sendMessage(sender,ChatColor.GREEN+ "You are showing spawns for " + arena.getName());
	}

	@MCCommand( cmds = {"help","?"})
	public void help(CommandSender sender, Command command, String label, Object[] args){
		super.help(sender, command, args);
	}

}
