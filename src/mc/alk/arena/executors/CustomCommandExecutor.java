package mc.alk.arena.executors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.ArenaEditor.CurrentSelection;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class CustomCommandExecutor implements CommandExecutor{
	static final boolean DEBUG = false;

	private HashMap<String,TreeMap<Integer,MethodWrapper>> methods = new HashMap<String,TreeMap<Integer,MethodWrapper>>();
	public static final int SELF = -2; /// Which index defines the sender
	protected HashMap<MCCommand, String> usage = new HashMap<MCCommand, String>();
	
	protected BattleArenaController ac;
	protected EventController ec;
	protected ArenaEditor aec;
	
	/**
	 * Custom arguments class so that we can return a modified arguments
	 */
	public static class Arguments{
		MatchParams mp;
		Object[] args;
	}

	protected static class MethodWrapper{
		public MethodWrapper(Object obj, Method method){this.obj = obj; this.method = method;}
		Object obj; /// Object the method belongs to
		Method method; /// Method
	}

	/**
	 * When no arguments are supplied, no method is found
	 * What to display when this happens
	 * @param sender
	 */
	protected abstract void showHelp(CommandSender sender, Command command);

	protected CustomCommandExecutor(){
		addMethods(this, getClass().getMethods());
		this.ac = BattleArena.getBAC();
		this.ec = BattleArena.getEC();
		this.aec = BattleArena.getArenaEditor();
	}

	protected void addMethods(Object obj, Method[] methodArray){
		for (Method method : methodArray){
			MCCommand mc = method.getAnnotation(MCCommand.class);
			if (mc == null)
				continue;
//			System.out.println("adding method " + method);
			/// For each of the cmds, store them with the method
			for (String cmd : mc.cmds()){
				cmd = cmd.toLowerCase();
				TreeMap<Integer,MethodWrapper> mthds = methods.get(cmd);
				if (mthds == null){
					mthds = new TreeMap<Integer,MethodWrapper>();
				}
				int order = mc.order() != -1? mc.order() : Integer.MAX_VALUE-mthds.size();
				mthds.put(order, new MethodWrapper(obj,method));
				methods.put(cmd, mthds);
			}
			/// TeamJoinResult in the usages, for showing help messages
			if (MessageController.hasMessage("usage", mc.cmds()[0])){
				usage.put(mc,MessageController.getMessage("usage", mc.cmds()[0]));
			} else if (!mc.usageNode().isEmpty()){
				usage.put(mc, MessageController.getMessage("usage",mc.usageNode()));
			} else if (!mc.usage().isEmpty()){
				usage.put(mc, mc.usage());
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/// No method to handle, show some help
		if (args.length == 0){
			showHelp(sender, command);
			return true;
		}
		/// Find our method, and verify all the annotations
		TreeMap<Integer,MethodWrapper> methodmap = methods.get(args[0].toLowerCase());
		if (methodmap == null || methodmap.isEmpty()){
			return MessageController.sendMessage(sender, "That command does not exist!");
		}

		MCCommand mccmd = null;
		List<InvalidArgumentException> errs =null;
		boolean success = false;
		for (MethodWrapper mwrapper : methodmap.values()){
			mccmd = mwrapper.method.getAnnotation(MCCommand.class);
			final boolean isOp = sender == null || sender.isOp() || sender instanceof ConsoleCommandSender;

			if (mccmd.op() && !isOp || mccmd.admin() && !sender.hasPermission(Defaults.ARENA_ADMIN)) /// no op, no pass
				continue;
			try {
				Arguments newArgs= verifyArgs(mccmd,sender,command, label, args);
				/// Invoke our arenaMethods
				if (newArgs.mp != null){
					mwrapper.method.invoke(mwrapper.obj,sender,newArgs.mp, command,label, newArgs.args);					
				} else {
					mwrapper.method.invoke(mwrapper.obj,sender,command,label, newArgs.args);					
				}
				success = true;
				break; /// success on one
			} catch (InvalidArgumentException e){
				if (errs == null)
					errs = new ArrayList<InvalidArgumentException>();
				errs.add(e);
			} catch (Exception e) { /// Just all around bad
				e.printStackTrace();
			}
		}
		/// and handle all errors
		if (!success && errs != null && !errs.isEmpty()){
			if (errs.size() == 1){
				MessageController.sendMessage(sender, errs.get(0).getMessage());
				MessageController.sendMessage(sender, getUsage(command, mccmd));
				return true;
			}
			HashSet<String> errstrings = new HashSet<String>();
			for (InvalidArgumentException e: errs){
				errstrings.add(e.getMessage());}
			for (String msg : errstrings){
				MessageController.sendMessage(sender, msg);}
			MessageController.sendMessage(sender, getUsage(command, mccmd));
		}
		return true;
	}

	static final String ONLY_INGAME =ChatColor.RED+"You need to be in game to use this command";
	private Arguments verifyArgs(MCCommand cmd, CommandSender sender, Command command, String label, String[] args) 
			throws InvalidArgumentException{
		if (DEBUG)System.out.println("verifyArgs " + cmd +" sender=" +sender+", label=" + label+" args="+args);

		Arguments newArgs = new Arguments(); /// Our return value
		Object[] objs = new Object[args.length]; /// Our new array of castable arguments
		System.arraycopy( args, 0, objs, 0, args.length );
		newArgs.args = objs; /// Set our return object with the new castable arguments

		/// Verify min number of arguments
		if (args.length < cmd.min()){
			throw new InvalidArgumentException(ChatColor.RED+"You need at least "+cmd.min()+" arguments");
		}
		/// Verfiy max number of arguments
		if (args.length > cmd.max()){
			throw new InvalidArgumentException(ChatColor.RED+"You need less than "+cmd.max()+" arguments");
		}
		/// Verfiy max number of arguments
		if (cmd.exact()!= -1 && args.length != cmd.exact()){
			throw new InvalidArgumentException(ChatColor.RED+"You need exactly "+cmd.exact()+" arguments");
		}
		final boolean isPlayer = sender instanceof Player;
		final boolean isOp = (isPlayer && sender.isOp()) || sender == null || sender instanceof ConsoleCommandSender;
		if (cmd.op() && !isOp)
			throw new InvalidArgumentException(ChatColor.RED +"You need to be op to use this command");
		if (cmd.admin() && !isOp && (isPlayer && !sender.hasPermission(Defaults.ARENA_ADMIN)))
			throw new InvalidArgumentException(ChatColor.RED +"You need to be an Admin to use this command");

		/// In game check
		if (cmd.inGame() && !isPlayer){
			throw new InvalidArgumentException(ONLY_INGAME);			
		}

		/// Verify ints
		if (cmd.ints().length > 0){
			for (int intIndex: cmd.ints()){
				if (intIndex >= args.length)
					throw new InvalidArgumentException("IntegerIndex out of range. ");
				try {
					objs[intIndex] = Integer.parseInt(args[intIndex]);
				}catch (NumberFormatException e){
					throw new InvalidArgumentException(ChatColor.RED+(String)args[1]+" is not a valid integer.");
				}
			}
		}
		/// Verify alphanumeric
		if (cmd.alphanum().length > 0){
			for (int index: cmd.alphanum()){
				if (index >= args.length)
					throw new InvalidArgumentException("String Index out of range. ");
				if (!args[index].matches("[a-zA-Z0-9_]*")) {
					throw new InvalidArgumentException("&earguments can be only alphanumeric with underscores");
				}
			}
		}
		
		/// Verify arena
		if (cmd.arenas().length > 0){
			for (int index: cmd.arenas()){
				if (index >= args.length)
					throw new InvalidArgumentException("Arena Index out of range. ");
				Arena arena = ac.getArena(args[index]);
				if (arena == null){
					throw new InvalidArgumentException("That arena doesnt exist ");}
				objs[index] = arena;
			}
		}

		if (cmd.selection()){
			if (!isPlayer){
				throw new InvalidArgumentException(ONLY_INGAME);
			}
			CurrentSelection cs = aec.getCurrentSelection((Player)sender);
			if (cs == null)
				throw new InvalidArgumentException(ChatColor.RED + "You need to select an arena first");

			if (System.currentTimeMillis() - cs.lastUsed > 60000){
				throw new InvalidArgumentException(ChatColor.RED + "its been over a minute since you selected an arena, reselect it");
			}
		}

		/// Check to see if the players are online
		if (cmd.online().length > 0){
			if (DEBUG)System.out.println("isPlayer " + cmd.online());

			for (int playerIndex : cmd.online()){
				if (playerIndex == SELF){
					if (!isPlayer)
						throw new InvalidArgumentException(ChatColor.RED + "You can only use this command in game");
				} else {
					if (playerIndex >= args.length)
						throw new InvalidArgumentException("PlayerIndex out of range. ");
					Player p = Util.findPlayer(args[playerIndex]);
					if (p == null || !p.isOnline())
						throw new InvalidArgumentException(args[playerIndex]+" must be online ");
					/// Change over our string to a player
					objs[playerIndex] = p;
				}
			}
		}
		
		if (cmd.selection()){
			if (!isPlayer){
				throw new InvalidArgumentException(ChatColor.RED + "You can only use this command in game");
			}
			CurrentSelection cs = aec.getCurrentSelection((Player)sender);
			if (cs == null)
				throw new InvalidArgumentException(ChatColor.RED + "You need to select an arena first");

			if (System.currentTimeMillis() - cs.lastUsed > 60000){
				throw new InvalidArgumentException(ChatColor.RED + "its been over a minute since you selected an arena, reselect it");
			}
		}

		/// Check our permissions
		if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()))
			throw new InvalidArgumentException(MessageController.getMessage("main", "no_permission"));
		
		if (cmd.mp()){
			MatchParams mp = ParamController.getMatchParams(command.getName());
			if (mp == null){
				for (String alias : command.getAliases()){
					mp = ParamController.getMatchParams(alias);
					if (mp != null)
						break;
				}
			}					
			if (mp == null){
				throw new InvalidArgumentException(ChatColor.RED + "Match parameters for a &6" + command.getName()+"&c can't be found");				
			}
			newArgs.mp = mp;
		}
		return newArgs; /// Success
	}

	private String getUsage(Command c, MCCommand cmd) {
		if (!cmd.usageNode().isEmpty())
			return MessageController.getMessage("usage",cmd.usageNode());
		if (!cmd.usage().isEmpty())
			return "&6"+c.getName()+":&e" + cmd.usage();
		/// By Default try to return the message under this commands name in "usage.cmd"
		return MessageController.getMessage("usage", cmd.cmds()[0]);
	}


	public class InvalidArgumentException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidArgumentException(String string) {
			super(string);
		}
	}

	static final int LINES_PER_PAGE = 8;
	public void help(CommandSender sender, Command command, Object[] args){
		Integer page = 1;

		if (args != null && args.length > 1){
			try{
				page = Integer.valueOf((String) args[1]);
			} catch (Exception e){
				MessageController.sendMessage(sender, ChatColor.RED+" " + args[1] +" is not a number, showing help for page 1.");
			}
		}

		Set<String> available = new HashSet<String>();
		Set<String> unavailable = new HashSet<String>();
		Set<String> onlyop = new HashSet<String>();

		for (MCCommand cmd : usage.keySet()){
			final String use = "&6/" + command.getName() +" " + usage.get(cmd);
			if (cmd.op() && !sender.isOp())
				onlyop.add(use);
			else if (cmd.admin() && !sender.hasPermission(Defaults.ARENA_ADMIN))
				onlyop.add(use);
			else if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()))
				unavailable.add(use);
			else 
				available.add(use);
		}
		int npages = available.size()+unavailable.size();
		if (sender.isOp())
			npages += onlyop.size();
		npages = (int) Math.ceil( (float)npages/LINES_PER_PAGE);
		if (page > npages || page <= 0){
			MessageController.sendMessage(sender, "&4That page doesnt exist, try 1-"+npages);
			return;
		}
		if (command != null)
			MessageController.sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 :[Usage] /"+command.getName()+" help <page number>");
		else 
			MessageController.sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 :[Usage] /cmd help <page number>");
		int i=0;
		for (String use : available){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page*LINES_PER_PAGE)
				continue;
			MessageController.sendMessage(sender, use);
		}
		for (String use : unavailable){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
				continue;
			MessageController.sendMessage(sender, ChatColor.RED+"[Insufficient Perms] " + use);
		}
		if (sender.isOp()){
			for (String use : onlyop){
				i++;
				if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
					continue;
				MessageController.sendMessage(sender, ChatColor.AQUA+"[OP only] &6"+use);
			}			
		}
	}

}
