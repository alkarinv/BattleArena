package mc.alk.arena.util;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionsUtil {
	static final int ticks = 2;
	public static void givePlayerInventoryPerms(ArenaPlayer p){
		givePlayerInventoryPerms(p.getPlayer());
	}

	public static void givePlayerInventoryPerms(Player p){
		if (BattleArena.getSelf().isEnabled()){
			if (Defaults.PLUGIN_MULTI_INV){ /// Give the multiinv permission node to ignore this player
				p.getPlayer().addAttachment(BattleArena.getSelf(), Defaults.MULTI_INV_IGNORE_NODE, true, ticks);}
			if (Defaults.PLUGIN_MULITVERSE_CORE){ /// Give the multiverse-core permission node to ignore this player
				p.getPlayer().addAttachment(BattleArena.getSelf(), Defaults.MULTIVERSE_CORE_IGNORE_NODE, true, ticks);}
			if (Defaults.PLUGIN_MULITVERSE_INV){ /// Give the multiverse-inventories permission node to ignore this player
				p.getPlayer().addAttachment(BattleArena.getSelf(), Defaults.MULTIVERSE_INV_IGNORE_NODE, true, ticks);}
		}
	}

	public static int getPriority(Player player) {
		if (player.hasPermission("arena.priority.lowest")){ return 1000;}
		else if (player.hasPermission("arena.priority.low")){ return 800;}
		else if (player.hasPermission("arena.priority.normal")){ return 600;}
		else if (player.hasPermission("arena.priority.high")){ return 400;}
		else if (player.hasPermission("arena.priority.highest")){ return 200;}
		return 1000;
	}
	public static boolean isAdmin(CommandSender sender){
		return sender.isOp() || sender.hasPermission(Defaults.ARENA_ADMIN);
	}

}
