package mc.alk.arena.util;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Permissions;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
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
			} else if (AnnouncementOptions.hc == null){
				Log.info("[BattleArena] Vault Perms not detected");
				return false;
			}
		} catch (Error e){
			Log.err(BattleArena.getPluginName() +" exception loading permissions through Vault");
			e.printStackTrace();
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

}
