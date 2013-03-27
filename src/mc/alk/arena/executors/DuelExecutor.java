package mc.alk.arena.executors;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;

import org.bukkit.command.CommandSender;



public class DuelExecutor extends BAExecutor{

	@Override
	public boolean join(ArenaPlayer player, MatchParams mp, String args[]) {
		return sendMessage(player, "&cYou can only duel with this type");
	}

	@Override
	public boolean arenaForceStart(CommandSender sender, MatchParams mp) {
		return sendMessage(sender, "&cThis command doesn't work for duel only arenas");
	}
}
