package mc.alk.arena.util;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.Permissions;
import mc.alk.arena.objects.ArenaPlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionsUtil {
	static final int ticks = 2;
	static boolean hasVaultPerms = false;

	public static void setPermission(Plugin plugin){
		hasVaultPerms = VaultPermUtil.setPermission(plugin);
	}

	public static void givePlayerInventoryPerms(ArenaPlayer p){
		givePlayerInventoryPerms(p.getPlayer());
	}

	public static void givePlayerInventoryPerms(Player p){
		if (BattleArena.getSelf().isEnabled()){
			if (Defaults.DEBUG_TRACE) Log.info("Giving inventory perms=" + p.getName());

			if (Defaults.PLUGIN_MULTI_INV){ /// Give the multiinv permission node to ignore this player
				p.getPlayer().addAttachment(BattleArena.getSelf(), Permissions.MULTI_INV_IGNORE_NODE, true, ticks);}
			if (Defaults.PLUGIN_MULITVERSE_CORE){ /// Give the multiverse-core permission node to ignore this player
				p.getPlayer().addAttachment(BattleArena.getSelf(), Permissions.MULTIVERSE_CORE_IGNORE_NODE, true, ticks);}
			if (Defaults.PLUGIN_MULITVERSE_INV){ /// Give the multiverse-inventories permission node to ignore this player
				p.getPlayer().addAttachment(BattleArena.getSelf(), Permissions.MULTIVERSE_INV_IGNORE_NODE, true, ticks);}
			if (Defaults.DEBUG_TRACE) Log.info("End giving inventory perms=" + p.getName());
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
		return sender.isOp() || sender.hasPermission(Permissions.ADMIN_NODE);
	}

	public static boolean giveAdminPerms(Player player, Boolean enable) {
		if (!hasVaultPerms)
			return false;
		return VaultPermUtil.giveAdminPerms(player,enable);
	}

}
