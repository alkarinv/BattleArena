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
		if (Defaults.PLUGIN_MULTI_INV){ /// Give the multiinv permission node to ignore this player
			p.getPlayer().addAttachment(BattleArena.getSelf(), Defaults.MULTI_INV_IGNORE_NODE, true, ticks);}
		if (Defaults.PLUGIN_MULITVERSE_CORE){ /// Give the multiverse-core permission node to ignore this player
			p.getPlayer().addAttachment(BattleArena.getSelf(), Defaults.MULTIVERSE_CORE_IGNORE_NODE, true, ticks);}			
		if (Defaults.PLUGIN_MULITVERSE_INV){ /// Give the multiverse-inventories permission node to ignore this player
			p.getPlayer().addAttachment(BattleArena.getSelf(), Defaults.MULTIVERSE_INV_IGNORE_NODE, true, ticks);}
	}
}
