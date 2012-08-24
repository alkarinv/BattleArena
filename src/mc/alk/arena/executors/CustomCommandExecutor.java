package mc.alk.arena.executors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

	static interface Converter{
		Object convert(Object arg);
	}
	private static Map<Class<?>,Converter> cmdMap = new HashMap<Class<?>,Converter>();
	static {
		cmdMap.put(CommandSender.class, new Converter() {public Object convert(Object arg) { return arg; }});
		cmdMap.put(Command.class, new Converter() {public Object convert(Object arg) { return arg; }});

	}

	/**
	 * Custom arguments class so that we can return a modified arguments
	 */
	public static class Arguments{
		MatchParams mp;
		Object[] args;
	}

	protected static class MethodWrapper{
		public MethodWrapper(Object obj, Method method){
			this.obj = obj; this.method = method;
		}
		Object obj; /// Object the method belongs to
		Method method; /// Method
	}

	/**
	 * When no arguments are supplied, no method is found
	 * What to display when this happens
	 * @param sender
	 */
	protected void showHelp(CommandSender sender, Command command){
		help(sender,command,null);
	}

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
				Arguments newArgs= verifyArgs(mwrapper,mccmd,sender,command, label, args);
				//				Object[] lists = new Object[5];
				//				lists[0] = sender;
				//				lists[1] = newArgs.mp;
				//				lists[2] = command;
				//				lists[3] = label;
				//				lists[4] = newArgs.args;
				//				for (int i=4;i<lists.length;i++){
				//					lists[i] = newArgs.args[i-4];
				//				}
				mwrapper.method.invoke(mwrapper.obj,newArgs.args);
				/// Invoke our arenaMethods
				//				if (newArgs.mp != null){
				//					mwrapper.method.invoke(mwrapper.obj,sender,newArgs.mp, command,label, newArgs.args);					
				//				} else {
				//					mwrapper.method.invoke(mwrapper.obj,sender,command,label, newArgs.args);					
				//				}
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
	private Arguments verifyArgs(MethodWrapper mwrapper, MCCommand cmd, 
			CommandSender sender, Command command, String label, String[] args) throws InvalidArgumentException{
		if (DEBUG)System.out.println("verifyArgs " + cmd +" sender=" +sender+", label=" + label+" args="+args);
		int strIndex = 1/*skip the label*/, objIndex = 0;

		/// Check our permissions
		if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()))
			throw new InvalidArgumentException(MessageController.getMessage("main", "no_permission"));

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

		/// the first ArenaPlayer or Player parameter is the sender
//		boolean getSenderAsPlayer = cmd.playerSender();
		boolean getSenderAsPlayer = cmd.inGame();

		/// In game check
		if (cmd.inGame() && !isPlayer || getSenderAsPlayer && !isPlayer){
			throw new InvalidArgumentException(ONLY_INGAME);			
		}

		Arguments newArgs = new Arguments(); /// Our return value
		Object[] objs = new Object[mwrapper.method.getParameterTypes().length]; /// Our new array of castable arguments
		//		System.arraycopy( args, 0, objs, 0, args.length );
		newArgs.args = objs; /// Set our return object with the new castable arguments
		for (Class<?> theclass : mwrapper.method.getParameterTypes()){
			if (strIndex > args.length) /// out of arguments
				break;
			//			System.out.println(objIndex + " : " + strIndex +"  !!!!!!!!!!!!!!!!!!!!!!!!!!! Cs = " + theclass.getCanonicalName());
			if (CommandSender.class == theclass){
				objs[objIndex] = sender;
			} else if (Command.class == theclass){
				objs[objIndex] = command;				
			} else if (Player.class ==theclass){
				if (getSenderAsPlayer){
					objs[objIndex] = sender;
					getSenderAsPlayer = false;
				} else {
					objs[objIndex] = verifyPlayer(args[strIndex++]);
				}
			} else if (OfflinePlayer.class ==theclass){
				objs[objIndex] = verifyOfflinePlayer(args[strIndex++]);
			} else if (ArenaPlayer.class == theclass){
				if (getSenderAsPlayer){
					objs[objIndex] = BattleArena.toArenaPlayer((Player)sender);
					getSenderAsPlayer = false;
				} else {
					objs[objIndex] = verifyArenaPlayer(args[strIndex++]);
				}
			} else if (MatchParams.class == theclass){
				objs[objIndex] = verifyMatchParams(command);
			} else if (Arena.class == theclass){
				objs[objIndex] = verifyArena(args[strIndex++]);
			} else if (String.class == theclass){
				objs[objIndex] = args[strIndex++]; 
			} else if (Integer.class == theclass){
				objs[objIndex] = verifyInteger(args[strIndex++]);
			} else if (String[].class == theclass){
				objs[objIndex] = args;
			} else if (Object[].class == theclass){
				objs[objIndex] = args;
			} else if (Boolean.class == theclass){
				objs[objIndex] = Boolean.parseBoolean(args[strIndex++]);
			} else if (Object.class == theclass){
				objs[objIndex] = args[strIndex++];
			}
//			System.out.println(objIndex + " : " + strIndex + "  " + objs[objIndex] +" !!!!!!!!!!!!!!!!!!!!!!!!!!! Cs = " + theclass.getCanonicalName());

			objIndex++;
		}

		/// Verify alphanumeric
		if (cmd.alphanum().length > 0){
			for (int index: cmd.alphanum()){
				if (index >= args.length)
					throw new InvalidArgumentException("String Index out of range. ");
				if (!args[index].matches("[a-zA-Z0-9_]*")) {
					throw new InvalidArgumentException("&eargument '"+args[index]+"' can only be alphanumeric with underscores");
				}
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

		return newArgs; /// Success
	}

	private OfflinePlayer verifyOfflinePlayer(String name) throws InvalidArgumentException {
		OfflinePlayer p = Util.findOfflinePlayer(name);
		if (p == null)
			throw new InvalidArgumentException("Player " + name+" can not be found");
		return p;
	}

	private ArenaPlayer verifyArenaPlayer(String name) throws InvalidArgumentException {
		Player p = Util.findPlayer(name);
		if (p == null || !p.isOnline())
			throw new InvalidArgumentException(name+" is not online ");
		return BattleArena.toArenaPlayer(p);
	}

	private Player verifyPlayer(String name) throws InvalidArgumentException {
		Player p = Util.findPlayer(name);
		if (p == null || !p.isOnline())
			throw new InvalidArgumentException(name+" is not online ");
		return p;
	}

	private Arena verifyArena(String name) throws InvalidArgumentException {
		Arena arena = ac.getArena(name);
		if (arena == null){
			throw new InvalidArgumentException("Arena '" +name+"' doesnt exist" );}
		return arena;
	}

	private MatchParams verifyMatchParams(Command command) throws InvalidArgumentException {
		MatchParams mp = ParamController.getMatchParams(command.getName());
		System.out.println("verfiy mp = " + mp);
		if (mp != null){
			return mp;
		} else {
			for (String alias : command.getAliases()){
				mp = ParamController.getMatchParams(alias);
				if (mp != null)
					return mp;
			}
		}

		throw new InvalidArgumentException(ChatColor.RED + "Match parameters for a &6" + command.getName()+"&c can't be found");
	}

	private Integer verifyInteger(Object object) throws InvalidArgumentException {
		/// Verify ints
		try {
			return Integer.parseInt(object.toString());
		}catch (NumberFormatException e){
			throw new InvalidArgumentException(ChatColor.RED+(String)object+" is not a valid integer.");
		}
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

	public static boolean sendMessage(CommandSender sender, String msg){
		return MessageController.sendMessage(sender, msg);
	}

	public static boolean sendMessage(ArenaPlayer player, String msg){
		return MessageController.sendMessage(player, msg);
	}
}
