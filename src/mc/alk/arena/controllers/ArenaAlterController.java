package mc.alk.arena.controllers;

import java.util.Arrays;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;
import mc.alk.arena.util.WorldEditUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class ArenaAlterController {
	public enum ChangeType{
		NTEAMS, TEAMSIZE, WAITROOM, SPAWN, VLOC, TYPE, ADDREGION;

		public static ChangeType fromName(String str){
			str = str.toUpperCase();
			ChangeType ct = null;
			try {ct = ChangeType.valueOf(str);} catch (Exception e){}
			if (ct != null)
				return ct;
			try{
				if (Integer.valueOf(str) != null)
					return SPAWN;
			} catch (Exception e){}
			if (str.equalsIgnoreCase("wr")) return WAITROOM;
			if (str.equalsIgnoreCase("v")) return VLOC;
			return null;
		}

		public static String getValidList() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (ChangeType r: ChangeType.values()){
				if (!first) sb.append(", ");
				sb.append(r);
			}
			return sb.toString();
		}
	}

	public static boolean alterArena(CommandSender sender, Arena arena, String[] args) {
		if (args.length < 3){
			showAlterHelp(sender);
			//			sendMessage(sender,ChatColor.YELLOW+ "      or /arena alter MainArena spawnitem <itemname>:<matchEndTime between spawn> ");
			return false;
		}
		BattleArenaController ac = BattleArena.getBAC();
		String arenaName = arena.getName();
		String changetype = args[2];
		String value = null;
		if (args.length > 3)
			value = (String) args[3];
		String[] otherOptions = args.length > 4 ? Arrays.copyOfRange(args, 4, args.length) : null;
		if (Defaults.DEBUG) System.out.println("alterArena " + arenaName +":" + changetype + ":" + value);

		boolean success = false;
		ChangeType ct = ChangeType.fromName(changetype);
		if (ct == null){
			sendMessage(sender,ChatColor.RED+ "Option: &6" + changetype+"&c does not exist. \n&cValid options are &6"+ChangeType.getValidList());
			showAlterHelp(sender);
		}
		switch(ct){
		case TEAMSIZE: success = changeTeamSize(sender, arena, ac, value); break;
		case NTEAMS: success = changeNTeams(sender, arena, ac, value); break;
		case TYPE: success = changeType(sender, arena, ac, value); break;
		case SPAWN: success = changeSpawn(sender, arena, ac, changetype, value, otherOptions); break;
		case VLOC: success = changeVisitorSpawn(sender,arena,ac,changetype,value,otherOptions); break;			
		case WAITROOM: success = changeWaitroomSpawn(sender,arena,ac,changetype,value,otherOptions); break;
		case ADDREGION: success = addWorldGuardRegion(sender,arena,ac,value); break;
		default:
			sendMessage(sender,ChatColor.RED+ "Option: &6" + changetype+"&c does not exist. \n&cValid options are &6"+ChangeType.getValidList());
			break;
		}
		if (success)
			BattleArena.saveArenas();	
		return success;
	}

	private static boolean checkWorldGuard(CommandSender sender){
		if (!WorldGuardInterface.hasWorldGuard()){
			sendMessage(sender,"&cWorldGuard is not enabled");
			return false;
		}
		if (!(sender instanceof Player)){
			sendMessage(sender,"&cYou need to be in game to use this command");
			return false;
		}
		return true;
	}

	private static boolean addWorldGuardRegion(CommandSender sender, Arena arena, BattleArenaController ac, String value) {
		if (!checkWorldGuard(sender)){
			return false;}
		Player p = (Player)sender;
		WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
		Selection sel = wep.getSelection(p);
		if (sel == null){
			sendMessage(sender,"&cYou need to select a region to use this command.");
			return false;
		}

		String region = arena.getRegion();
		World w = sel.getWorld();
		try{
			String id = makeRegionName(arena);
			if (region != null){
				WorldGuardInterface.updateProtectedRegion(p,id);
				sendMessage(sender,"&2Region updated! ");
			} else {
				WorldGuardInterface.createProtectedRegion(p, id);
				sendMessage(sender,"&2Region added! ");
			}
			arena.addRegion(w.getName(), id);
		} catch (Exception e) {
			sendMessage(sender,"&cAdding WorldGuard region failed!");
			sendMessage(sender, "&c" + e.getMessage());
			e.printStackTrace();
		}	
		return true;
	}

	public static String makeRegionName(Arena arena){
		return "ba-"+arena.getName().toLowerCase();
	}

	private static int verifySpawnLocation(CommandSender sender, String value){
		if (!(sender instanceof Player)){
			sendMessage(sender,"&cYou need to be in game to use this command");
			return -1;
		}
		int locindex = -1;
		try{locindex = Integer.parseInt(value);}catch(Exception e){}
		if (locindex <= 0 || locindex > 100){
			sendMessage(sender,"&cspawn number must be in the range [1-100]");
			return -1;
		}
		return locindex;
	}

	private static boolean changeWaitroomSpawn(CommandSender sender, Arena arena, BattleArenaController ac, 
			String changetype, String value, String[] otherOptions) {
		if (!BAExecutor.checkPlayer(sender))
			return false;
		int locindex = verifySpawnLocation(sender,value);
		if (locindex == -1)
			return false;

		Player p = (Player) sender;
		Location loc = null;
		ac.removeArena(arena);
		loc = parseLocation(p,value);
		if (loc == null){
			loc = p.getLocation();}
		arena.setWaitRoomSpawnLoc(locindex-1,loc);			
		ac.addArena(arena);
		sendMessage(sender,"&2waitroom &6" + locindex +"&2 set to location=&6" + Util.getLocString(loc));
		return true;
	}

	private static boolean changeVisitorSpawn(CommandSender sender, Arena arena, BattleArenaController ac, 
			String changetype, String value, String[] otherOptions) {
		if (!BAExecutor.checkPlayer(sender))
			return false;

		int locindex = verifySpawnLocation(sender,value);
		if (locindex == -1)
			return false;
		Player p = (Player) sender;
		Location loc = null;
		ac.removeArena(arena);
		loc = parseLocation(p,value);
		if (loc == null){
			loc = p.getLocation();}
		arena.setSpawnLoc(locindex-1,loc);			
		ac.addArena(arena);
		sendMessage(sender,"&2team &6" + changetype +"&2 spawn set to location=&6" + Util.getLocString(loc));
		return true;
	}


	private static boolean changeSpawn(CommandSender sender, Arena arena, BattleArenaController ac, 
			String changetype, String value, String[] otherOptions) {
		if (!BAExecutor.checkPlayer(sender))
			return false;
		int locindex = verifySpawnLocation(sender,changetype);
		if (locindex == -1)
			return false;

		Player p = (Player) sender;
		Location loc = null;
		ac.removeArena(arena);
		loc = parseLocation(p,value);
		if (loc == null){
			loc = p.getLocation();}
		arena.setSpawnLoc(locindex-1,loc);			
		ac.addArena(arena);
		sendMessage(sender,"&2team &6" + changetype +"&2 spawn set to location=&6" + Util.getLocString(loc));
		return true;
	}


	private static boolean changeType(CommandSender sender, Arena arena, BattleArenaController ac, String value) {
		ArenaType t = ArenaType.fromString(value);
		if (t == null){
			sendMessage(sender,"&ctype &6"+ value + "&c not found. valid types=&6"+ArenaType.getValidList());
			return false;
		}
		ac.removeArena(arena);
		arena.getParameters().setType(t);
		ac.addArena(arena);
		sendMessage(sender,"&2Altered arena type to &6" + value);
		return true;
	}

	private static boolean changeNTeams(CommandSender sender, Arena arena, BattleArenaController ac, String value) {
		final MinMax mm = Util.getMinMax(value);
		if (mm == null){
			sendMessage(sender,"size "+ value + " not found");
			return false;
		} else {
			ac.removeArena(arena);
			arena.getParameters().setNTeams(mm);
			ac.addArena(arena);
			sendMessage(sender,"&2Altered arena number of teams to &6" + value);
		}
		return true;
	}


	private static void showAlterHelp(CommandSender sender) {
		sendMessage(sender,ChatColor.GOLD+ "Usage: /arena alter <arenaname> <teamSize|nTeams|type|1|2|3...|vloc|waitroom> <value> [option]");
		sendMessage(sender,ChatColor.GOLD+ "Example: /arena alter MainArena 1 &e: sets spawn location 1 to where you are standing");
		sendMessage(sender,ChatColor.GOLD+ "Example: /arena alter MainArena 1 wg &e: causes spawn 1 to have the worldguard area selected");
		sendMessage(sender,ChatColor.GOLD+ "Example: /arena alter MainArena teamSize 3+ ");
		sendMessage(sender,ChatColor.GOLD+ "Example: /arena alter MainArena nTeams 2 ");
		sendMessage(sender,ChatColor.GOLD+ "Example: /arena alter MainArena type deathmatch ");
		sendMessage(sender,ChatColor.GOLD+ "      or /arena alter MainArena waitroom 1 &e: sets waitroom 1 to your location");
	}


	private static boolean changeTeamSize(CommandSender sender, Arena arena,
			BattleArenaController ac, String value) {
		final MinMax mm = Util.getMinMax(value);
		if (mm == null){
			sendMessage(sender,"size "+ value + " not found");
			return false;
		} else {
			ac.removeArena(arena);
			arena.getParameters().setTeamSizes(mm);
			ac.addArena(arena);
			sendMessage(sender,"&2Altered arena team size to &6" + value);
		}
		return true;
	}


	public static Location parseLocation(CommandSender sender, String svl) {
		if (!(sender instanceof Player))
			return null;
		Player p = (Player) sender;
		if (svl == null)
			return null;
		String as[] = svl.split(":");
		if (svl.contains(",")){
			as = svl.split(",");
		}
		World w = p.getWorld();
		if (w == null){
			return null;}
		if (as.length < 3){
			return null;}
		try {
			int x = Integer.valueOf(as[0]);
			int y = Integer.valueOf(as[1]);
			int z = Integer.valueOf(as[2]);
			return new Location(w,x,y,z);
		} catch (Exception e){
			return null;
		}
	}

	public static boolean sendMessage(CommandSender sender, String msg){
		return MessageUtil.sendMessage(sender, msg);
	}

	public static boolean sendMessage(ArenaPlayer player, String msg){
		return MessageUtil.sendMessage(player, msg);
	}
}
