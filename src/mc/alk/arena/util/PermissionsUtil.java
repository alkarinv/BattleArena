package mc.alk.arena.util;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;

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

	/// TODO CLEANUP OR REMOVE
	public static void setWorldGuardBypassPerms(ArenaPlayer p, String world, boolean newState) {
////		final String perm = +world;
//		boolean state = p.getPlayer().hasPermission(perm);
//		if (state != newState){
//			p.getPlayer().addAttachment(BattleArena.getSelf(), perm, newState);
//		}
	}

	public static int getPriority(Player player) {
		if (player.hasPermission("arena.priority.lowest")){ return 1000;} 
		else if (player.hasPermission("arena.priority.low")){ return 900;}
		else if (player.hasPermission("arena.priority.normal")){ return 800;}
		else if (player.hasPermission("arena.priority.high")){ return 700;}
		else if (player.hasPermission("arena.priority.highest")){ return 600;}
		return 1000;
	}
}
