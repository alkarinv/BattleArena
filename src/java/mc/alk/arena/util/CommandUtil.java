package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class CommandUtil {

	public static boolean shouldCancel(PlayerCommandPreprocessEvent event, boolean allDisabled,
                                       Set<String> disabledCommands, Set<String> enabledCommands){
		if (Defaults.DEBUG_COMMANDS){
			event.getPlayer().sendMessage("event Message=" + event.getMessage() +"   isCancelled=" + event.isCancelled());}
		if (!allDisabled && (disabledCommands == null || disabledCommands.isEmpty()))
			return false;
		if (Defaults.ALLOW_ADMIN_CMDS_IN_Q_OR_MATCH && PermissionsUtil.isAdmin(event.getPlayer())){
			return false;}
        if (allDisabled && (enabledCommands.isEmpty()))
            return true;

		String cmd = event.getMessage();
		final int index = cmd.indexOf(' ');
		if (index != -1){
			cmd = cmd.substring(0, index);
		}
        cmd = cmd.toLowerCase();
//        boolean enabled = ;
        return !(enabledCommands != null && enabledCommands.contains(cmd)) &&
                !cmd.equals("/bad") && (allDisabled || disabledCommands.contains(cmd));
    }
}
