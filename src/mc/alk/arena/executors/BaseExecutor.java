package mc.alk.arena.executors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import mc.alk.arena.BattleArena;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.ServerUtil;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class BaseExecutor implements ArenaExecutor{
	static final boolean DEBUG = false;

	private HashMap<String,TreeMap<Integer,MethodWrapper>> methods = new HashMap<String,TreeMap<Integer,MethodWrapper>>();
	private HashMap<String,Map<String,TreeMap<Integer,MethodWrapper>>> subCmdMethods =
			new HashMap<String,Map<String,TreeMap<Integer,MethodWrapper>>>();
	public static final int SELF = -2; /// Which index defines the sender

	protected TreeMap<MCCommand, Set<String>> usage = new TreeMap<MCCommand, Set<String>>(new Comparator<MCCommand>(){
		@Override
		public int compare(MCCommand cmd1, MCCommand cmd2) {
			int c = new Float(cmd1.helpOrder()).compareTo(cmd2.helpOrder());
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
			int order = (mc.order() != -1? mc.order()*100000 :Integer.MAX_VALUE) - ml*100-mthds.size();
			mthds.put(order, new MethodWrapper(obj,method));
			methods.put(cmd, mthds);
			addUsage(method, mc);
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
				mthds.put(order, new MethodWrapper(obj,method));
				addUsage(method, mc);
			}
		}
	}
	private void addUsage(Method method, MCCommand mc) {
		Set<String> usages = usage.get(mc);
		if (usages == null){
			usages = new HashSet<String>();
			usage.put(mc, usages);
		}
		/// save the usages, for showing help messages
		if (mc.cmds().length > 0 && MessageSerializer.hasMessage("usage", mc.cmds()[0])){
			usages.add(MessageSerializer.getDefaultMessage("usage."+ mc.cmds()[0]).getMessage());
		} else if (!mc.usageNode().isEmpty()){
			usages.add(MessageSerializer.getDefaultMessage("usage."+mc.usageNode()).getMessage());
		} else if (!mc.usage().isEmpty()){
			usages.add(mc.usage());
		} else { /// Generate an automatic usage string
			usages.add(createUsage(method));
		}
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

	private List<String> getUsage(Command c, MCCommand cmd) {
		List<String> usages = new ArrayList<String>();
		for (String str : usage.get(cmd)){
			usages.add( "&6/"+c.getName()+" " + str);}
		return usages;
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
		final MCCommand cmd;
		public CommandException(IllegalArgumentException err, MCCommand cmd){
			this.err = err; this.cmd = cmd;
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		TreeMap<Integer,MethodWrapper> methodmap = methods.get(DEFAULT_CMD);
		/// No method to handle, show some help
		if (args.length == 0 && (methodmap == null || methodmap.isEmpty())
				|| (args.length > 0 && (args[0].equals("?") || args[0].equals("help")))){
			showHelp(sender, command,args);
			return true;
		}
		int startIndex = 1;
		final int length = args.length;
		final String cmd = length > 0 ? args[0].toLowerCase() : null;
		final String subcmd = length > 1 ? args[1].toLowerCase() : null;
		methodmap = null;
		/// check for subcommands
		if (subcmd!=null && subCmdMethods.containsKey(cmd) && subCmdMethods.get(cmd).containsKey(subcmd)){
			methodmap = subCmdMethods.get(cmd).get(subcmd);
			startIndex = 2;
		}
		if (methodmap == null && cmd != null){ /// Find our method, and verify all the annotations
			methodmap = methods.get(cmd);}

		if (methodmap == null || methodmap.isEmpty()){
			return sendMessage(sender, "&cThat command does not exist!&6 /"+command.getLabel()+" &c for help");}

		MCCommand mccmd = null;
		List<CommandException> errs =null;
		boolean success = false;
		for (MethodWrapper mwrapper : methodmap.values()){
			mccmd = mwrapper.method.getAnnotation(MCCommand.class);
//			final boolean isOp = sender == null || sender.isOp() || sender instanceof ConsoleCommandSender;
//
//			if (mccmd.op() && !isOp || mccmd.admin() && !hasAdminPerms(sender)) /// no op, no pass
//				continue;
			Arguments newArgs = null;
			try {
				newArgs= verifyArgs(mwrapper,mccmd,sender,command, label, args, startIndex);
				mwrapper.method.invoke(mwrapper.obj,newArgs.args);
				success = true;
				break; /// success on one
			} catch (IllegalArgumentException e){ /// One of the arguments wasn't correct, store the message
				if (errs == null)
					errs = new ArrayList<CommandException>();
				errs.add(new CommandException(e,mccmd));
			} catch (Exception e) { /// Just all around bad
				logInvocationError(e, mwrapper,newArgs);
			}
		}
		/// and handle all errors
		if (!success && errs != null && !errs.isEmpty()){
			if (errs.size() == 1){
				MessageUtil.sendMessage(sender, errs.get(0).err.getMessage());
				for (String usage: getUsage(command,mccmd)){
					MessageUtil.sendMessage(sender, usage);}
				return true;
			}
			HashSet<String> errstrings = new HashSet<String>();
			HashSet<String> usages = new HashSet<String>();
			for (CommandException e: errs){
				errstrings.add(e.err.getMessage());
				usages.addAll(getUsage(command,e.cmd));
			}
			for (String msg : errstrings){
				MessageUtil.sendMessage(sender, msg);}
			for (String msg : usages){
				MessageUtil.sendMessage(sender, msg);}
		}
		return true;
	}

	private void logInvocationError(Exception e, MethodWrapper mwrapper, Arguments newArgs) {
		Log.err("[BA Error] "+BattleArena.getNameAndVersion()+":"+mwrapper.method +" : " + mwrapper.obj +"  : " + newArgs);
		if (newArgs!=null && newArgs.args != null){
			for (Object o: newArgs.args)
				Log.err("[BA Error] object=" + (o!=null ? o.toString() : o));
		}
		Log.err("[BA Error] Cause=" + e.getCause());
		if (e.getCause() != null) e.getCause().printStackTrace();
		Log.err("[BA Error] Trace Continued ");
		e.printStackTrace();
	}

	public static final String ONLY_INGAME =ChatColor.RED+"You need to be in game to use this command";
	protected Arguments verifyArgs(MethodWrapper mwrapper, MCCommand cmd,
			CommandSender sender, Command command, String label, String[] args, int startIndex) throws IllegalArgumentException{
		if (DEBUG)System.out.println("verifyArgs " + cmd +" sender=" +sender+", label=" + label+" args="+args);
		final int paramLength = mwrapper.method.getParameterTypes().length;

		/// Verify min number of arguments
		if (args.length < cmd.min()){
			throw new IllegalArgumentException(ChatColor.RED+"You need at least "+cmd.min()+" arguments");
		}
		/// Verfiy max number of arguments
		if (args.length > cmd.max()){
			throw new IllegalArgumentException(ChatColor.RED+"You need less than "+cmd.max()+" arguments");
		}
		/// Verfiy max number of arguments
		if (cmd.exact()!= -1 && args.length != cmd.exact()){
			throw new IllegalArgumentException(ChatColor.RED+"You need exactly "+cmd.exact()+" arguments");
		}
		final boolean isPlayer = sender instanceof Player;
		final boolean isOp = (isPlayer && sender.isOp()) || sender == null || sender instanceof ConsoleCommandSender;
		final boolean isAdmin = isOp || hasAdminPerms(sender);
		if (cmd.op() && !isOp)
			throw new IllegalArgumentException(ChatColor.RED +"You need to be op to use this command");
		if (!cmd.perm().isEmpty() || cmd.admin()){
			boolean needsPerm = !cmd.perm().isEmpty();
			boolean hasPerm = sender.hasPermission(cmd.perm());
			/// Check our permissions
			if (needsPerm && !hasPerm && !(cmd.admin() && isAdmin)){
				throw new IllegalArgumentException(MessageSerializer.getDefaultMessage("main", "no_permission"));}

			if (cmd.admin() && !isAdmin && !(needsPerm && hasPerm))
				throw new IllegalArgumentException(ChatColor.RED +"You need to be an Admin to use this command");
		}

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
					throw new IllegalArgumentException("&eargument '"+args[index]+"' can only be alphanumeric with underscores");
				}
			}
		}
		return newArgs; /// Success
	}

	protected Object verifySender(CommandSender sender, Class<?> cla) {
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
		OfflinePlayer p = ServerUtil.findOfflinePlayer(name);
		if (p == null)
			throw new IllegalArgumentException("Player " + name+" can not be found");
		return p;
	}

	private Player verifyPlayer(String name) throws IllegalArgumentException {
		Player p = ServerUtil.findPlayer(name);
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
				MessageUtil.sendMessage(sender, ChatColor.RED+" " + args[1] +" is not a number, showing help for page 1.");
			}
		}

		List<String> available = new ArrayList<String>();
		List<String> unavailable = new ArrayList<String>();
		List<String> onlyop = new ArrayList<String>();

		for (MCCommand cmd : usage.keySet()){
			for (String str: usage.get(cmd)){
				final String use = "&6/" + command.getName() +" " + str;
				if (cmd.op() && !sender.isOp())
					onlyop.add(use);
				else if (cmd.admin() && !hasAdminPerms(sender))
					onlyop.add(use);
				else if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()))
					unavailable.add(use);
				else
					available.add(use);
			}
		}
		int npages = available.size()+unavailable.size();
		if (sender.isOp())
			npages += onlyop.size();
		npages = (int) Math.ceil( (float)npages/LINES_PER_PAGE);
		if (page > npages || page <= 0){
			MessageUtil.sendMessage(sender, "&4That page doesnt exist, try 1-"+npages);
			return;
		}
		if (command != null) {
			String aliases = StringUtils.join(command.getAliases(),", ");
			MessageUtil.sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 : /"+command.getName()+" help <page number>");
			MessageUtil.sendMessage(sender, "&e    command &6"+command.getName()+"&e has aliases: &6" + aliases);
		} else {
			MessageUtil.sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 : /cmd help <page number>");
		}
		int i=0;
		for (String use : available){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page*LINES_PER_PAGE)
				continue;
			MessageUtil.sendMessage(sender, use);
		}
		for (String use : unavailable){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
				continue;
			MessageUtil.sendMessage(sender, ChatColor.RED+"[Insufficient Perms] " + use);
		}
		if (sender.isOp()){
			for (String use : onlyop){
				i++;
				if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
					continue;
				MessageUtil.sendMessage(sender, ChatColor.AQUA+"[OP only] &6"+use);
			}
		}
	}

	public static boolean sendMessage(CommandSender sender, String msg){
		return MessageUtil.sendMessage(sender, msg);
	}
}

