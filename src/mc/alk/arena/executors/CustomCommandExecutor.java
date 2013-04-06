package mc.alk.arena.executors;

import java.util.concurrent.atomic.AtomicBoolean;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Permissions;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.ArenaEditor.CurrentSelection;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.ServerUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CustomCommandExecutor extends BaseExecutor{

	protected final BattleArenaController ac;
	protected final EventController ec;
	protected final ArenaEditor aec;

	protected CustomCommandExecutor(){
		super();
		this.ac = BattleArena.getBAController();
		this.ec = BattleArena.getEventController();
		this.aec = BattleArena.getArenaEditor();
	}

	@Override
	protected boolean validCommandSenderClass(Class<?> clazz){
		return super.validCommandSenderClass(clazz) || clazz == ArenaPlayer.class;
	}

	@Override
	protected boolean hasAdminPerms(CommandSender sender){
		return super.hasAdminPerms(sender) || sender.hasPermission(Permissions.ADMIN_NODE);
	}

	@Override
	protected Object verifySender(CommandSender sender, Class<?> clazz) {
		if (clazz == ArenaPlayer.class){
			if (!(sender instanceof Player))
				throw new IllegalArgumentException(ONLY_INGAME);
			return BattleArena.toArenaPlayer((Player)sender);
		}
		return super.verifySender(sender,clazz);
	}

	@Override
	protected Arguments verifyArgs(MethodWrapper mwrapper, MCCommand cmd,
			CommandSender sender, Command command, String label, String[] args, int startIndex)
					throws IllegalArgumentException{

		final boolean isPlayer = sender instanceof Player;
		if (cmd.selection()){
			if (!isPlayer){
				throw new IllegalArgumentException(ONLY_INGAME);
			}
			CurrentSelection cs = aec.getCurrentSelection((Player)sender);
			if (cs == null)
				throw new IllegalArgumentException(ChatColor.RED + "You need to select an arena first");

			if (System.currentTimeMillis() - cs.lastUsed > 5*60*1000){
				throw new IllegalArgumentException(ChatColor.RED + "its been over a 5 minutes since you selected an arena, reselect it");
			}
		}
		return super.verifyArgs(mwrapper,cmd,sender,command,label,args,startIndex);
	}

	@Override
	protected String getUsageString(Class<?> theclass) {
		if (ArenaPlayer.class == theclass){
			return "<player> ";
		} else if (Arena.class == theclass){
			return "<arena> ";
		} else if (MatchParams.class == theclass){
			return "";
		} else if (EventParams.class == theclass){
			return "";
		}
		return super.getUsageString(theclass);
	}

	@Override
	protected Object verifyArg(Class<?> clazz, Command command, String string, AtomicBoolean usedString) {
		if (EventParams.class == clazz){
			return verifyEventParams(command);
		} else if (MatchParams.class == clazz){
			return verifyMatchParams(command);
		}
		if (string == null)
			throw new ArrayIndexOutOfBoundsException();
		usedString.set(true);
		if (ArenaPlayer.class == clazz){
			return verifyArenaPlayer(string);
		} else if (Arena.class == clazz){
			return verifyArena(string);
		}

		return super.verifyArg(clazz, command, string, usedString);
	}

	private ArenaPlayer verifyArenaPlayer(String name) throws IllegalArgumentException {
		Player p = ServerUtil.findPlayer(name);
		if (p == null || !p.isOnline())
			throw new IllegalArgumentException(name+" is not online ");
		return BattleArena.toArenaPlayer(p);
	}

	private Arena verifyArena(String name) throws IllegalArgumentException {
		Arena arena = ac.getArena(name);
		if (arena == null){
			throw new IllegalArgumentException("Arena '" +name+"' doesnt exist" );}
		return arena;
	}

	private MatchParams verifyMatchParams(Command command) throws IllegalArgumentException {
		MatchParams mp = ParamController.getMatchParamCopy(command.getName());
		if (mp != null){
			return mp;
		} else {
			for (String alias : command.getAliases()){
				mp = ParamController.getMatchParamCopy(alias);
				if (mp != null)
					return mp;
			}
		}

		throw new IllegalArgumentException(ChatColor.RED + "Match parameters for a &6" + command.getName()+"&c can't be found");
	}

	private EventParams verifyEventParams(Command command) throws IllegalArgumentException {
		MatchParams mp = ParamController.getEventParamCopy(command.getName());
		if (mp != null && mp instanceof EventParams){
			return (EventParams)mp;
		} else {
			for (String alias : command.getAliases()){
				mp = ParamController.getEventParamCopy(alias);
				if (mp != null && mp instanceof EventParams)
					return (EventParams) mp;
			}
		}

		throw new IllegalArgumentException(ChatColor.RED + "Event parameters for a &6" + command.getName()+"&c can't be found");
	}

	public static boolean sendMessage(ArenaPlayer player, String msg){
		return MessageUtil.sendMessage(player, msg);
	}
}

