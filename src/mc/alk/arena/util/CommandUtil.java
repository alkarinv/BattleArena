package mc.alk.arena.util;

import java.util.Set;

import mc.alk.arena.Defaults;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandUtil {

	public static boolean shouldCancel(PlayerCommandPreprocessEvent event, Set<String> disabledCommands){
		if (Defaults.DEBUG_COMMANDS){
			event.getPlayer().sendMessage("event Message=" + event.getMessage() +"   isCancelled=" + event.isCancelled());}
		if (disabledCommands == null || disabledCommands.isEmpty())
			return false;
		final Player p = event.getPlayer();
		if (PermissionsUtil.isAdmin(p) && Defaults.ALLOW_ADMIN_CMDS_IN_Q_OR_MATCH){
			return false;}

		String cmd = event.getMessage();
		final int index = cmd.indexOf(' ');
		if (index != -1){
			cmd = cmd.substring(0, index);
		}
		cmd = cmd.toLowerCase();
		if(disabledCommands.contains(cmd)){
			event.setCancelled(true);
			return true;
		}
		return false;
	}
}
