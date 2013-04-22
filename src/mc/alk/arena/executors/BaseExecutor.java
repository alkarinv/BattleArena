package mc.alk.arena.executors;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import mc.alk.arena.util.Log;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class BaseExecutor implements ArenaExecutor{
	public static final String version = "2.0.1";
	static final boolean DEBUG = false;

	private HashMap<String,TreeMap<Integer,MethodWrapper>> methods =
			new HashMap<String,TreeMap<Integer,MethodWrapper>>();
	private HashMap<String,Map<String,TreeMap<Integer,MethodWrapper>>> subCmdMethods =
			new HashMap<String,Map<String,TreeMap<Integer,MethodWrapper>>>();

	protected PriorityQueue<MethodWrapper> usage = new PriorityQueue<MethodWrapper>(2, new Comparator<MethodWrapper>(){
		public int compare(MethodWrapper mw1, MethodWrapper mw2) {
			MCCommand cmd1 = mw1.getCommand();
			MCCommand cmd2 = mw2.getCommand();

			int c = new Float(mw1.getHelpOrder()).compareTo(mw2.getHelpOrder());
			if (c!=0) return c;
			c = new Integer(cmd1.order()).compareTo(cmd2.order());
			return c != 0 ? c : new Integer(cmd1.hashCode()).compareTo(cmd2.hashCode());
		}
	});
	static final String DEFAULT_CMD = "_dcmd_";

	/**
	 * Custom arguments class so that we can return a modified arguments
	 */
	public static class Arguments{
		public Object[] args;
	}

	protected static class MethodWrapper{
		public MethodWrapper(Object obj, Method method){
			this.obj = obj; this.method = method;
		}

		public Object obj; /// Object instance the method belongs to
		public Method method; /// Method
		public String usage;
		Float helpOrder = null;
		public MCCommand getCommand(){
			return this.method.getAnnotation(MCCommand.class);
		}
		public float getHelpOrder(){
			return helpOrder != null ?
					helpOrder : this.method.getAnnotation(MCCommand.class).helpOrder();
		}
	}

	/**
	 * When no arguments are supplied, no method is found
	 * What to display when this happens
	 * @param sender
	 */
	protected void showHelp(CommandSender sender, Command command){
		showHelp(sender,command,null);
	}
	protected void showHelp(CommandSender sender, Command command, String[] args){
		help(sender,command,args);
	}

	protected BaseExecutor(){
		addMethods(this, getClass().getMethods());
	}

	protected boolean validCommandSenderClass(Class<?> clazz){
		return clazz != CommandSender.class || clazz != Player.class;
	}

	public void addMethods(Object obj, Method[] methodArray){

		for (Method method : methodArray){
			MCCommand mc = method.getAnnotation(MCCommand.class);
			if (mc == null)
				continue;
			Class<?> types[] = method.getParameterTypes();
			if (types.length == 0 || !validCommandSenderClass(types[0])){
				System.err.println("MCCommands must start with a CommandSender,Player, or ArenaPlayer");
				continue;
			}
			if (mc.cmds().length == 0){ /// There is no subcommand. just the command itself with arguments
				addMethod(obj, method, mc, DEFAULT_CMD);
			} else {
				/// For each of the cmds, store them with the method
				for (String cmd : mc.cmds()){
					addMethod(obj, method, mc, cmd.toLowerCase());}
			}
		}
	}

	private void addMethod(Object obj, Method method, MCCommand mc, String cmd) {
		int ml = method.getParameterTypes().length;
		if (mc.subCmds().length == 0){
			TreeMap<Integer,MethodWrapper> mthds = methods.get(cmd);
			if (mthds == null){
				mthds = new TreeMap<Integer,MethodWrapper>();
			}
			int order = (mc.order() != -1? mc.order()*100000 :Integer.MAX_VALUE) - ml*100 - mthds.size();
			MethodWrapper mw = new MethodWrapper(obj,method);
			mthds.put(order, mw);
			methods.put(cmd, mthds);
			addUsage(mw, mc);
		} else {
			Map<String,TreeMap<Integer,MethodWrapper>> basemthds = subCmdMethods.get(cmd);
			if (basemthds == null){
				basemthds = new HashMap<String,TreeMap<Integer,MethodWrapper>>();
				subCmdMethods.put(cmd, basemthds);
			}
			for (String subcmd: mc.subCmds()){
				TreeMap<Integer,MethodWrapper> mthds = basemthds.get(subcmd);
				if (mthds == null){
					mthds = new TreeMap<Integer,MethodWrapper>();
					basemthds.put(subcmd, mthds);
				}
				int order = (mc.order() != -1? mc.order()*100000 :Integer.MAX_VALUE) - ml*100-mthds.size();
				MethodWrapper mw = new MethodWrapper(obj,method);
				/// Set help order
				if (mc.helpOrder() == Integer.MAX_VALUE){
					mw.helpOrder = (float) (Integer.MAX_VALUE - usage.size());
				}
				mthds.put(order, mw);
				addUsage(mw, mc);
			}
		}
	}
	private void addUsage(MethodWrapper method, MCCommand mc) {
		/// save the usages, for showing help messages
		if (!mc.usage().isEmpty()){
			method.usage = mc.usage();
		} else { /// Generate an automatic usage string
			method.usage = createUsage(method.method);
		}
		usage.add(method);
	}

	private String createUsage(Method method) {
		MCCommand cmd = method.getAnnotation(MCCommand.class);
		StringBuilder sb = new StringBuilder(cmd.cmds().length > 0 ? cmd.cmds()[0] +" " : "");
		int startIndex = 1;
		if (cmd.subCmds().length > 0){
			sb.append(cmd.subCmds()[0] +" ");
			startIndex = 2;
		}
		Class<?> types[] = method.getParameterTypes();
		for (int i=startIndex;i<types.length;i++){
			Class<?> theclass = types[i];
			sb.append(getUsageString(theclass));
		}
		return sb.toString();
	}

	protected String getUsageString(Class<?> clazz) {
		if (Player.class ==clazz){
			return "<player> ";
		} else if (OfflinePlayer.class ==clazz){
			return "<player> ";
		} else if (String.class == clazz){
			return "<string> ";
		} else if (Integer.class == clazz || int.class == clazz){
			return "<int> ";
		} else if (Float.class == clazz || float.class == clazz){
			return "<number> ";
		} else if (Double.class == clazz || double.class == clazz){
			return "<number> ";
		} else if (Short.class == clazz || short.class == clazz){
			return "<int> ";
		} else if (Boolean.class == clazz || boolean.class == clazz){
			return "<true|false> ";
		} else if (String[].class == clazz || Object[].class == clazz){
			return "[string ... ] ";
		}
		return "<string> ";
	}

	public class CommandException{
		final IllegalArgumentException err;
		final MethodWrapper mw;
		public CommandException(IllegalArgumentException err, MethodWrapper mw){
			this.err = err; this.mw = mw;
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		TreeMap<Integer,MethodWrapper> methodmap = null;

		/// No method to handle, show some help
		if ((args.length == 0 && !methods.containsKey(DEFAULT_CMD))
				|| (args.length > 0 && (args[0].equals("?") || args[0].equals("help")))){
			showHelp(sender, command,args);
			return true;
		}
		final int length = args.length;
		final String cmd = length > 0 ? args[0].toLowerCase() : null;
		final String subcmd = length > 1 ? args[1].toLowerCase() : null;
		int startIndex = 0;

		/// check for subcommands
		if (subcmd!=null && subCmdMethods.containsKey(cmd) && subCmdMethods.get(cmd).containsKey(subcmd)){
			methodmap = subCmdMethods.get(cmd).get(subcmd);
			startIndex = 2;
		}
		if (methodmap == null && cmd != null){ /// Find our method, and verify all the annotations
			methodmap = methods.get(cmd);
			if (methodmap != null)
				startIndex =1;
		}

		if (methodmap == null){ /// our last attempt
			methodmap = methods.get(DEFAULT_CMD);
		}

		if (methodmap == null || methodmap.isEmpty()){
			return sendMessage(sender, "&cThat command does not exist!&6 /"+command.getLabel()+" help &c for help");}

		MCCommand mccmd = null;
		List<CommandException> errs =null;
		boolean success = false;
		for (MethodWrapper mwrapper : methodmap.values()){

			mccmd = mwrapper.method.getAnnotation(MCCommand.class);
			final boolean isOp = sender == null || sender.isOp() || sender instanceof ConsoleCommandSender;

			if (mccmd.op() && !isOp || mccmd.admin() && !hasAdminPerms(sender)) /// no op, no pass
				continue;
			Arguments newArgs = null;
			try {
				newArgs= verifyArgs(mwrapper,mccmd,sender,command, label, args, startIndex);
				Object completed = mwrapper.method.invoke(mwrapper.obj,newArgs.args);
				if (completed != null && completed instanceof Boolean){
					success = (Boolean)completed;
					if (!success){
						String usage = mwrapper.usage;
						if (usage != null && !usage.isEmpty()){
							sendMessage(sender, usage);}
					}
				} else {
					success = true;
				}
				break; /// success on one
			} catch (IllegalArgumentException e){ /// One of the arguments wasn't correct, store the message
				if (errs == null)
					errs = new ArrayList<CommandException>();
				errs.add(new CommandException(e,mwrapper));
			} catch (Exception e) { /// Just all around bad
				logInvocationError(e, mwrapper,newArgs);
			}
		}
		/// and handle all errors
		if (!success && errs != null && !errs.isEmpty()){
			HashSet<String> usages = new HashSet<String>();
			for (CommandException e: errs){
				usages.add(ChatColor.GOLD+command.getLabel() +" " +e.mw.usage+" &c:"+e.err.getMessage());
			}
			for (String msg : usages){
				sendMessage(sender, msg);}
		}
		return true;
	}

	private void logInvocationError(Exception e, MethodWrapper mwrapper, Arguments newArgs) {
		System.err.println("[CustomCommandExecutor Error] "+mwrapper.method +" : " + mwrapper.obj +"  : " + newArgs);
		if (newArgs!=null && newArgs.args != null){
			for (Object o: newArgs.args)
				System.err.println("[Error] object=" + (o!=null ? o.toString() : o));
		}
		System.err.println("[Error] Cause=" + e.getCause());
		if (e.getCause() != null){
			e.getCause().printStackTrace();
			Log.printStackTrace(e.getCause());
		}
		System.err.println("[Error] Trace Continued ");
		Log.printStackTrace(e);
	}

	public static final String ONLY_INGAME =ChatColor.RED+"You need to be in game to use this command";
	protected Arguments verifyArgs(MethodWrapper mwrapper, MCCommand cmd,
			CommandSender sender, Command command, String label, String[] args, int startIndex) throws IllegalArgumentException{
		if (DEBUG){
			System.out.println(" method="+mwrapper.method.getName() + " verifyArgs " + cmd +" sender=" +sender+", label=" + label+" args="+args);
			for (String arg: args){
				System.out.println(" -- arg=" +arg);}
			for (Class<?> t: mwrapper.method.getParameterTypes()){
				System.out.println(" -- type=" +t);}
		}
		final int paramLength = mwrapper.method.getParameterTypes().length;

		/// Check our permissions
		if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()) && !(cmd.admin() && hasAdminPerms(sender)))
			throw new IllegalArgumentException("You don't have permission to use this command");

		/// Verify min number of arguments
		if (args.length < cmd.min()){
			throw new IllegalArgumentException("You need at least "+cmd.min()+" arguments");
		}
		/// Verfiy max number of arguments
		if (args.length > cmd.max()){
			throw new IllegalArgumentException("You need less than "+cmd.max()+" arguments");
		}
		/// Verfiy max number of arguments
		if (cmd.exact()!= -1 && args.length != cmd.exact()){
			throw new IllegalArgumentException("You need exactly "+cmd.exact()+" arguments");
		}
		final boolean isPlayer = sender instanceof Player;
		final boolean isOp = (isPlayer && sender.isOp()) || sender == null || sender instanceof ConsoleCommandSender;

		if (cmd.op() && !isOp)
			throw new IllegalArgumentException("You need to be op to use this command");

		if (cmd.admin() && !isOp && (isPlayer && !hasAdminPerms(sender)))
			throw new IllegalArgumentException("You need to be an Admin to use this command");

		Class<?> types[] = mwrapper.method.getParameterTypes();

		//		/// In game check
		if (types[0] == Player.class && !isPlayer){
			throw new IllegalArgumentException(ONLY_INGAME);
		}
		int strIndex = startIndex/*skip the label*/, objIndex = 1;

		Arguments newArgs = new Arguments(); /// Our return value
		Object[] objs = new Object[paramLength]; /// Our new array of castable arguments

		newArgs.args = objs; /// Set our return object with the new castable arguments
		objs[0] = verifySender(sender, types[0]);
		AtomicBoolean usedString = new AtomicBoolean();
		for (int i=1;i<types.length;i++){
			Class<?> clazz = types[i];
			usedString.set(false);
			try{
				if (CommandSender.class == clazz){
					objs[objIndex] = sender;
				} else if (String[].class == clazz){
					objs[objIndex] = args;
				} else if (Object[].class == clazz){
					objs[objIndex] =args;
				} else {
					String str = strIndex < args.length ? args[strIndex] : null;
					objs[objIndex] = verifyArg(clazz, command, str, usedString);
					if (objs[objIndex] == null){
						throw new IllegalArgumentException("Argument " + args[strIndex] + " can not be null");
					}
				}
				if (DEBUG)System.out.println("   " + objIndex + " : " + strIndex + "  " +
						(args.length > strIndex ? args[strIndex] : null ) + " <-> " + objs[objIndex] +" !!!!!!!!!!!!!!!!!!!!!!!!!!! Cs = " + clazz.getCanonicalName());
				if (usedString.get()){
					strIndex++;}
			} catch (ArrayIndexOutOfBoundsException e){
				throw new IllegalArgumentException("You didnt supply enough arguments for this method");
			}
			objIndex++;
		}

		/// Verify alphanumeric
		if (cmd.alphanum().length > 0){
			for (int index: cmd.alphanum()){
				if (index >= args.length)
					throw new IllegalArgumentException("String Index out of range. ");
				if (!args[index].matches("[a-zA-Z0-9_]*")) {
					throw new IllegalArgumentException("argument '"+args[index]+"' can only be alphanumeric with underscores");
				}
			}
		}
		return newArgs; /// Success
	}

	protected Object verifySender(CommandSender sender, Class<?> clazz) {
		if (!clazz.isAssignableFrom(sender.getClass())){
			throw new IllegalArgumentException("sender must be a " + clazz.getSimpleName());}
		return sender;
	}

	protected Object verifyArg(Class<?> clazz, Command command, String string, AtomicBoolean usedString) {
		if (Command.class == clazz){
			usedString.set(false);
			return command;
		}
		if (string == null)
			throw new ArrayIndexOutOfBoundsException();
		usedString.set(true);
		if (Player.class ==clazz){
			return verifyPlayer(string);
		} else if (OfflinePlayer.class ==clazz){
			return verifyOfflinePlayer(string);
		} else if (String.class == clazz){
			return string;
		} else if (Integer.class == clazz || int.class == clazz){
			return verifyInteger(string);
		} else if (Boolean.class == clazz || boolean.class == clazz){
			return Boolean.parseBoolean(string);
		} else if (Object.class == clazz){
			return string;
		} else if (Float.class == clazz || float.class == clazz){
			return verifyFloat(string);
		} else if (Double.class == clazz || double.class == clazz){
			return verifyDouble(string);
		}
		return null;
	}

	private OfflinePlayer verifyOfflinePlayer(String name) throws IllegalArgumentException {
		OfflinePlayer p = findOfflinePlayer(name);
		if (p == null)
			throw new IllegalArgumentException("Player " + name+" can not be found");
		return p;
	}

	private Player verifyPlayer(String name) throws IllegalArgumentException {
		Player p = findPlayer(name);
		if (p == null || !p.isOnline())
			throw new IllegalArgumentException(name+" is not online ");
		return p;
	}

	private Integer verifyInteger(Object object) throws IllegalArgumentException {
		try {
			return Integer.parseInt(object.toString());
		}catch (NumberFormatException e){
			throw new IllegalArgumentException(ChatColor.RED+(String)object+" is not a valid integer.");
		}
	}

	private Float verifyFloat(Object object) throws IllegalArgumentException {
		try {
			return Float.parseFloat(object.toString());
		}catch (NumberFormatException e){
			throw new IllegalArgumentException(ChatColor.RED+(String)object+" is not a valid float.");
		}
	}

	private Double verifyDouble(Object object) throws IllegalArgumentException {
		try {
			return Double.parseDouble(object.toString());
		}catch (NumberFormatException e){
			throw new IllegalArgumentException(ChatColor.RED+(String)object+" is not a valid double.");
		}
	}

	protected boolean hasAdminPerms(CommandSender sender){
		return sender.isOp();
	}


	static final int LINES_PER_PAGE = 8;
	public void help(CommandSender sender, Command command, String[] args){
		Integer page = 1;

		if (args != null && args.length > 1){
			try{
				page = Integer.valueOf(args[1]);
			} catch (Exception e){
				sendMessage(sender, ChatColor.RED+" " + args[1] +" is not a number, showing help for page 1.");
			}
		}

		List<String> available = new ArrayList<String>();
		List<String> unavailable = new ArrayList<String>();
		List<String> onlyop = new ArrayList<String>();

		for (MethodWrapper mw : usage){
			MCCommand cmd = mw.getCommand();
			final String use = "&6/" + command.getName() +" " + mw.usage;
			if (cmd.op() && !sender.isOp())
				continue;
			else if (cmd.admin() && !hasAdminPerms(sender))
				continue;
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
			if (page <= 0){
				sendMessage(sender, "&4There are no methods for this command");
			} else {
				sendMessage(sender, "&4That page doesnt exist, try 1-"+npages);
			}
			return;
		}
		if (command != null && command.getAliases() != null && !command.getAliases().isEmpty()) {
			String aliases = StringUtils.join(command.getAliases(),", ");
			sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 : /"+command.getName()+" help <page number>");
			sendMessage(sender, "&e    command &6"+command.getName()+"&e has aliases: &6" + aliases);
		} else {
			sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 : /cmd help <page number>");
		}
		int i=0;
		for (String use : available){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page*LINES_PER_PAGE)
				continue;
			sendMessage(sender, use);
		}
		for (String use : unavailable){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
				continue;
			sendMessage(sender, ChatColor.RED+"[Insufficient Perms] " + use);
		}
		if (sender.isOp()){
			for (String use : onlyop){
				i++;
				if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
					continue;
				sendMessage(sender, ChatColor.AQUA+"[OP only] &6"+use);
			}
		}
	}

	public static boolean sendMessage(CommandSender p, String message){
		if (message ==null || message.isEmpty()) return true;
		if (message.contains("\n"))
			return sendMultilineMessage(p,message);
		if (p instanceof Player){
			if (((Player) p).isOnline())
				p.sendMessage(colorChat(message));
		} else {
			p.sendMessage(colorChat(message));
		}
		return true;
	}

	public static boolean sendMultilineMessage(CommandSender p, String message){
		if (message ==null || message.isEmpty()) return true;
		String[] msgs = message.split("\n");
		for (String msg: msgs){
			if (p instanceof Player){
				if (((Player) p).isOnline())
					p.sendMessage(colorChat(msg));
			} else {
				p.sendMessage(colorChat(msg));
			}
		}
		return true;
	}

	public static String colorChat(String msg) {return msg.replace('&', (char) 167);}


	public static Player findPlayer(String name) {
		if (name == null)
			return null;
		Player foundPlayer = Bukkit.getPlayer(name);
		if (foundPlayer != null)
			return foundPlayer;

		Player[] online = Bukkit.getOnlinePlayers();

		for (Player player : online) {
			String playerName = player.getName();

			if (playerName.equalsIgnoreCase(name)) {
				foundPlayer = player;
				break;
			}

			if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
				if (foundPlayer != null) {
					return null;}

				foundPlayer = player;
			}
		}

		return foundPlayer;
	}

	public static OfflinePlayer findOfflinePlayer(String name) {
		OfflinePlayer p = findPlayer(name);
		if (p != null){
			return p;
		} else{
			/// Iterate over the worlds to see if a player.dat file exists
			for (World w : Bukkit.getWorlds()){
				File f = new File(w.getName()+"/players/"+name+".dat");
				if (f.exists()){
					return Bukkit.getOfflinePlayer(name);
				}
			}
			return null;
		}
	}
}

