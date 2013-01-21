package mc.alk.arena.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.executors.ArenaExecutor;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.ConfigException;
import mc.alk.arena.objects.exceptions.ExtensionPluginException;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.BaseSerializer;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.serializers.YamlFileUpdater;
import mc.alk.arena.util.FileUtil;
import mc.alk.arena.util.Log;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class APIRegistrationController {
	static Set<String> delayedInits = Collections.synchronizedSet(new HashSet<String>());

	static class DelayedRegistrationHandler implements Runnable{
		JavaPlugin plugin;

		public DelayedRegistrationHandler(JavaPlugin plugin) {
			this.plugin = plugin;
		}

		@Override
		public void run() {
			if (!plugin.isEnabled()) /// lets not try to register plugins that aren't loaded
				return;
			File dir = plugin.getDataFolder();
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File file) {return file.toString().contains("Config.yml");}
			};
			for (File file : dir.listFiles(fileFilter)){
				String n = file.getName().substring(0, file.getName().length()-"Config.yml".length());
				if (ArenaType.contains(n)){ /// we already loaded this type
					continue;}
				registerCustomCompetition(plugin, file);
			}
		}
	}

	public static void registerCustomCompetition(Plugin plugin, File configFile){
		BaseSerializer bs = new BaseSerializer();
		bs.setConfig(configFile);
		FileConfiguration config = bs.getConfig();
		/// Initialize custom matches or events
		Set<String> keys = config.getKeys(false);
		for (String key: keys){
			registerCustomCompetition(plugin, config, key);
		}
	}

	private static void registerCustomCompetition(Plugin plugin, FileConfiguration config, String key) {
		ConfigurationSection cs = config.getConfigurationSection(key);
		if (cs == null)
			return;
		try {
			/// A new match/event needs the params, an executor, and the command to use
			boolean isMatch = !config.getBoolean(key+".isEvent",false);
			MatchParams mp = ConfigSerializer.setTypeConfig(plugin,key,cs, isMatch);
			ArenaExecutor executor = isMatch ? BattleArena.getBAExecutor() : new ReservedArenaEventExecutor();
			ArenaCommand arenaCommand = new ArenaCommand(mp.getCommand(),"","", new ArrayList<String>(), BattleArena.getSelf());
			arenaCommand.setExecutor(executor);
			CommandController.registerCommand(arenaCommand);
		} catch (Exception e) {
			Log.err("Couldnt configure arenaType " + key+". " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void init(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, boolean match) throws ExtensionPluginException{
		if (plugin == null){
			throw new ExtensionPluginException(plugin, "Plugin can not be null");}
		/// Create our plugin folder if its not there
		File dir = plugin.getDataFolder();
		if (!dir.exists()){
			dir.mkdirs();}

		/// Register our arenas
		ArenaType at = ArenaType.register(name, arenaClass, plugin);
		Log.info(plugin.getName() +" registering arena type " +name +" using arenaClass " +arenaClass.getName());

		/// Load our configs
		ArenaSerializer as = new ArenaSerializer(plugin, plugin.getDataFolder()+File.separator+"arenas.yml"); /// arena config
		as.loadArenas(plugin,at);

		loadem(plugin, name, cmd, match, dir, at);

		if (!delayedInits.contains(plugin.getName())){
			delayedInits.add(plugin.getName());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DelayedRegistrationHandler(plugin));
		}
	}

	private void loadem(JavaPlugin plugin, String name, String cmd, boolean match, File dir, ArenaType at) {
		ConfigSerializer cc = new ConfigSerializer(); /// Our config.yml

		String configFileName = name+"Config.yml";
		String fileName = match ? "defaultMatchTypeConfig.yml" : "defaultEventTypeConfig.yml";

		File pluginFile = new File(dir.getPath()+File.separator+configFileName);
		File defaultPluginFile = new File(configFileName);
		File defaultFile = new File("default_files"+File.separator+fileName);

		if (!loadConfigFile(plugin, defaultFile, defaultPluginFile, pluginFile, name,cmd)){
			Log.err(plugin.getName() + " " + pluginFile.getName() + " could not be loaded");
			return;
		}
		cc.setConfig(at, pluginFile);
		YamlFileUpdater.updateAllConfig(plugin, cc);
		cc.setConfig(at, pluginFile);

		/// Make a message serializer for this event, and make the messages.yml file if it doesnt exist
		MessageSerializer ms = new MessageSerializer(name);

		String messagesFileName = name+"Messages.yml";
		fileName = match ? "defaultMatchMessages.yml": "defaultEventMessages.yml";

		pluginFile = new File(dir.getPath()+File.separator+messagesFileName);
		defaultPluginFile = new File(messagesFileName);
		defaultFile = new File("default_files"+File.separator+fileName);

		if (!loadFile(plugin, defaultFile, defaultPluginFile, pluginFile)){
			pluginFile = FileUtil.load(BattleArena.getSelf(), pluginFile.getAbsolutePath(),"/default_files/"+fileName);
			if (pluginFile == null){
				Log.err(plugin.getName() + " " + messagesFileName+" could not be loaded");
				return;
			}
		}

		ms.setConfig(pluginFile);
		ms.loadAll();
		MessageSerializer.addMessageSerializer(name,ms);

		try {
			Log.info("["+plugin.getName()+ "] Loading config from " + cc.getFile().getAbsolutePath());
			ConfigSerializer.setTypeConfig(plugin, name,cc.getConfigurationSection(name), match);
		} catch (Exception e){
			System.err.println("Error trying to load "+name+" config");
			e.printStackTrace();
		}
	}

	private static boolean loadFile(Plugin plugin, File defaultFile, File defaultPluginFile, File pluginFile){
		if (pluginFile.exists())
			return true;

		InputStream inputStream = FileUtil.getInputStream(plugin, defaultFile, defaultPluginFile);
		if (inputStream == null){
			return false;}

		OutputStream out = null;
		try{
			out=new FileOutputStream(pluginFile);
			byte buf[]=new byte[1024];
			int len;
			while((len=inputStream.read(buf))>0){
				out.write(buf,0,len);}
		} catch (Exception e){
			e.printStackTrace();
			return false;
		} finally{
			if (out != null)
				try {out.close();} catch (IOException e) {}
			if (inputStream!=null)
				try {inputStream.close();} catch (IOException e) {}
		}

		return true;
	}

	private boolean loadConfigFile(Plugin plugin, File defaultFile, File defaultPluginFile, File pluginFile,
			String name, String cmd) {
		if (pluginFile.exists())
			return true;
		InputStream inputStream = FileUtil.getInputStream(plugin, defaultFile, defaultPluginFile);
		if (inputStream == null){
			return false;
		}

		String line =null;
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		BufferedWriter fw =null;
		try {
			fw = new BufferedWriter(new FileWriter(pluginFile));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			while ((line = br.readLine()) != null){
				line = line.replaceAll("<name>", name).replaceAll("<cmd>", cmd);
				fw.write(line+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void registerMatchType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass) {
		registerMatchType(plugin,name,cmd,arenaClass,new BAExecutor());
	}

	public void registerMatchType(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, BAExecutor executor) {
		try {
			init(plugin,name,cmd,arenaClass,true);
			/// Set up command executors
			setCommandToExecutor(plugin, cmd, executor);
		} catch (ExtensionPluginException e) {
			e.printStackTrace();
		}

	}

	private void setCommandToExecutor(JavaPlugin plugin, String wantedCommand, CommandExecutor executor) {
		if (!setCommandToExecutor(plugin,wantedCommand, executor, true)){
			Log.err("Searching for alternative commands to register");
			Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
			for (String cmd: commands.keySet()){
				if (setCommandToExecutor(plugin, cmd, executor,false)){ /// we found one!
					Log.info("Alternative command found cmd=" + cmd);
					return;
				}
				Map<String,Object> aliases = commands.get(cmd);
				for (String alias: aliases.keySet()){
					if (setCommandToExecutor(plugin, alias, executor,false)){ /// we found one!
						return;
					}
				}
			}
		}
	}

	private boolean setCommandToExecutor(JavaPlugin plugin, String command, CommandExecutor executor, boolean displayError){
		try{
			plugin.getCommand(command).setExecutor(executor);
			return true;
		} catch(Exception e){
			if (displayError)
				Log.err(plugin.getName() + " command " + command +" was not found. Did you register it in your plugin.yml?");
			return false;
		}
	}

	public void createMessageSerializer(Plugin plugin, String name, boolean match, File dir) throws ConfigException {
		File pluginFile;
		/// Make a message serializer for this match/event, and make the messages.yml file if it doesnt exist
		MessageSerializer ms = new MessageSerializer(name);

		pluginFile = createMessageFile(plugin, name, match, dir);
		ms.setConfig(pluginFile);
		ms.loadAll();
		MessageSerializer.addMessageSerializer(name,ms);
	}

	private static File createMessageFile(Plugin plugin, String name, boolean match, File dir) throws ConfigException {
		String messagesFileName = name+"Messages.yml";
		String fileName = match ? "defaultMatchMessages.yml": "defaultEventMessages.yml";

		File pluginFile = new File(dir.getPath()+File.separator+messagesFileName);
		File defaultPluginFile = new File(messagesFileName);
		File defaultFile = new File("default_files"+File.separator+fileName);

		if (!loadFile(plugin, defaultFile, defaultPluginFile, pluginFile)){
			pluginFile = FileUtil.load(BattleArena.getSelf(), pluginFile.getAbsolutePath(),"/default_files/"+fileName);
			if (pluginFile == null){
				throw new ConfigException(plugin.getName() + " " + messagesFileName+" could not be loaded");
			}
		}
		return pluginFile;
	}

	public void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass) {
		try{
			registerEventType(plugin,name,cmd,arenaClass,new ReservedArenaEventExecutor());
		} catch (ExtensionPluginException e) {
			e.printStackTrace();
		}
	}

	public void registerEventType(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, EventExecutor executor) throws ExtensionPluginException {
		init(plugin,name,cmd,arenaClass,false);
		EventParams mp = ParamController.getEventParamCopy(name);
		if (mp != null){
			setCommandToExecutor(plugin, cmd, executor);
			EventController.addEventExecutor(mp, executor);
		} else {
			throw new ExtensionPluginException(plugin, name+" type not found");
		}
	}

	public void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass,
			Event event, EventExecutor executor) throws ExtensionPluginException {
		init(plugin,name,cmd,arenaClass,false);
		EventParams mp = ParamController.getEventParamCopy(name);
		if (mp != null){
			plugin.getCommand(cmd).setExecutor(executor);
			EventController.addEventExecutor(mp, executor);
		} else {
			throw new ExtensionPluginException(plugin, name+" type not found");
		}
	}


	public boolean registerCompetition(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass) {
		return registerCompetition(plugin,name,cmd, arenaClass,null);
	}

	public boolean registerCompetition(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, CustomCommandExecutor executor) {
		try{
			if (executor != null && executor.getClass() != CustomCommandExecutor.class){

			}
			File dir = plugin.getDataFolder();
			File configFile = new File(dir.getAbsoluteFile()+"/"+name+"Config.yml");
			if (!configFile.exists()){
				throw new ExtensionPluginException(plugin, "Error loading config file " + configFile);
			}
			registerCompetition(plugin, name, cmd, arenaClass, executor, configFile);
			return true;
		} catch (Exception e){
			return false;
		}
	}

	public void registerCompetition(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, CustomCommandExecutor executor, File configFile)
			throws Exception {
		registerCompetition(plugin,name,cmd,arenaClass,executor, configFile,true,false);
	}

	public void registerCompetition(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, CustomCommandExecutor executor, File configFile,
			boolean defaultIsMatch, boolean defaultCompetition)
			throws Exception{
		BaseSerializer bs = new BaseSerializer();
		bs.setConfig(configFile);
		FileConfiguration config = bs.getConfig();

		boolean isMatch = !config.getBoolean(name+".isEvent",!defaultIsMatch);
		isMatch = config.getBoolean(name+".queue",isMatch);
		if (isMatch){
			BAExecutor exe = new BAExecutor();
			if (executor != null){
				exe.addMethods(executor, executor.getClass().getMethods());}
			if (!defaultCompetition){
				this.registerMatchType(plugin, name, cmd, arenaClass,exe);
			} else {
				/// SetTypeConfig doesn't register ArenaType or commands
				ArenaType.register(name, Arena.class, BattleArena.getSelf());
				ConfigSerializer.setTypeConfig(plugin, name,config.getConfigurationSection(name), true);
				/// Set up command executors
				setCommandToExecutor(plugin, cmd, exe);
			}
		} else {
			ReservedArenaEventExecutor exe = new ReservedArenaEventExecutor();
			if (executor != null){
				exe.addMethods(executor, executor.getClass().getMethods());}
			if (!defaultCompetition){
				this.registerEventType(plugin, name, cmd, arenaClass,exe);
			} else {
				/// SetTypeConfig doesn't register ArenaType or commands
				ArenaType.register(name, Arena.class, BattleArena.getSelf());
				MatchParams mp = ConfigSerializer.setTypeConfig(plugin, name,config.getConfigurationSection(name), false);
				setCommandToExecutor(plugin, cmd, exe);
				EventController.addEventExecutor((EventParams) mp, exe);
			}
		}
	}
}