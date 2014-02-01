package mc.alk.arena.util.plugins;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Permissions;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.util.Log;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPermUtil {
	static Permission perm = null;
	public static boolean hasPermissions(){
		return perm != null;
	}

	public static boolean setPermission(Plugin plugin){
		/// Load Vault Perms
		try{
			RegisteredServiceProvider<Permission> provider = Bukkit.getServer().
					getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (provider != null && provider.getProvider() != null) {
				perm = provider.getProvider();
			} else if (AnnouncementOptions.chatPlugin == null){
				Log.info("[BattleArena] Vault Perms not detected");
				return false;
			}
		} catch (Error e){
			Log.err(BattleArena.getPluginName() +" exception loading permissions through Vault");
			Log.printStackTrace(e);
			return false;
		}
		return true;
	}

	public static boolean giveAdminPerms(Player player, Boolean enable) {
		if (enable) {
			return perm.playerAdd(player, Permissions.ADMIN_NODE);
		} else {
			return perm.playerRemove(player, Permissions.ADMIN_NODE);
		}
	}

	public static boolean giveWorldGuardPerms(Player player, Boolean enable) {
		String perms[] = {"worldguard.region.wand", "worldguard.region.info.*","worldguard.region.list",
				"worldguard.region.flag.regions.*","worldguard.region.flag.flags.*",
				"worldguard.region.setpriority.*"};
		for (String p: perms){
			if (enable) {
				perm.playerAdd(player, p);
			} else {
				perm.playerRemove(player, p);
			}
		}
		return true;
	}

}
