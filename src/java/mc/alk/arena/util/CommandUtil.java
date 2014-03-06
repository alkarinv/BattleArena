package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class CommandUtil {

	public static boolean shouldCancel(PlayerCommandPreprocessEvent event, boolean allDisabled,
                                       Set<String> disabledCommands, Set<String> enabledCommands){
		if (Defaults.DEBUG_COMMANDS){
			event.getPlayer().sendMessage("event Message=" + event.getMessage() +"   isCancelled=" + event.isCancelled());}
		if (disabledCommands.isEmpty())
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
        return !cmd.equals("/bad") && ( allDisabled ? !enabledCommands.contains(cmd) :
                disabledCommands.contains(cmd) && !enabledCommands.contains(cmd) );
    }
}
