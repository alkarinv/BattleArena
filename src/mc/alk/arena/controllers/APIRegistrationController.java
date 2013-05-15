package mc.alk.arena.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.DuelExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.RegisteredCompetition;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.ConfigException;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.FileUtil;
import mc.alk.arena.util.Log;
import mc.alk.plugin.updater.v1r2.FileUpdater;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class APIRegistrationController {
	static Set<String> delayedInits = Collections.synchronizedSet(new HashSet<String>());

	static class DelayedRegistrationHandler implements Runnable{
		final JavaPlugin plugin;
		final File compDir;
		final File arenaFile;

		public DelayedRegistrationHandler(JavaPlugin plugin, File compDir, File arenaFile) {
			this.plugin = plugin;
			this.compDir = compDir;
			this.arenaFile = arenaFile;
		}

		@Override
		public void run() {
			if (!plugin.isEnabled()) /// lets not try to register plugins that aren't loaded
				return;
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File file) {return file.toString().matches(".*Config.yml$");}
			};
			if (!compDir.exists())
				return;
			for (File file : compDir.listFiles(fileFilter)){
				String n = file.getName().substring(0, file.getName().length()-"Config.yml".length());
				if (ArenaType.contains(n) || n.contains(".")){ /// we already loaded this type, or bad type
					continue;}
				File configFile = new File(compDir+"/"+n+"Config.yml");
				File msgFile = new File(compDir+"/"+n+"Messages.yml");
				if (!new APIRegistrationController().registerCompetition(
						plugin,n /*name*/,n /*command*/, null /*Arena class*/,
						null /*executor*/, configFile, msgFile, null,arenaFile)){
					Log.err("[BattleArena] Unable to load custom competition " + n);
				}

			}
		}
	}

	private boolean loadFile(Plugin plugin, File fullFile, String fileName, String name, String cmd){
		if (fullFile.exists())
			return true;
		InputStream inputStream = FileUtil.getInputStream(plugin.getClass(), new File(fileName));
		if (inputStream == null)
			return false;
		return createFile(fullFile, name, cmd, inputStream);
	}

	private boolean loadFile(Plugin plugin, File defaultFile, File defaultPluginFile, File pluginFile,
			String fullFileName, String name, String cmd){
		if (pluginFile.exists())
			return true;
		InputStream inputStream = FileUtil.getInputStream(plugin.getClass(), new File(fullFileName));
		if (inputStream == null && defaultFile!=null && defaultPluginFile!= null){
			inputStream = FileUtil.getInputStream(plugin.getClass(), defaultFile, defaultPluginFile);}
		if (inputStream == null){
			return false;
		}

		return createFile(pluginFile, name, cmd, inputStream);
	}

	private boolean createFile(File pluginFile, String name, String cmd, InputStream inputStream) {
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
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fw !=null) try {fw.close(); } catch (Exception e){}
			if (br !=null) try {br.close(); } catch (Exception e){}
		}
		return true;
	}

	private static void setCommandToExecutor(JavaPlugin plugin, String wantedCommand, CommandExecutor executor) {
		if (!setCommandToExecutor(plugin,wantedCommand, executor, false)){
			List<String> aliases = new ArrayList<String>();
			ArenaCommand arenaCommand = new ArenaCommand(wantedCommand,"","", aliases, BattleArena.getSelf(),executor);
			CommandController.registerCommand(arenaCommand);
		}
	}

	private static boolean setCommandToExecutor(JavaPlugin plugin, String command, CommandExecutor executor, boolean displayError){
		try{
			plugin.getCommand(command).setExecutor(executor);
			return true;
		} catch(Exception e){
			if (displayError)
				Log.err(plugin.getName() + " command " + command +" was not found. Did you register it in your plugin.yml?");
			return false;
		}
	}

	public boolean registerCompetition(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass) {
		return registerCompetition(plugin,name,cmd, arenaClass,null);
	}

	public boolean registerCompetition(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, CustomCommandExecutor executor) {
		File dir = plugin.getDataFolder();
		File configFile = new File(dir.getAbsoluteFile()+"/"+name+"Config.yml");
		File msgFile = new File(dir.getAbsoluteFile()+"/"+name+"Messages.yml");
		File defaultArenaFile = new File(dir.getAbsoluteFile()+"/arenas.yml");
		return registerCompetition(plugin, name, cmd, arenaClass, executor,
				configFile, msgFile, defaultArenaFile);
	}

	public boolean registerCompetition(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, CustomCommandExecutor executor,
			File configFile, File messageFile,File defaultArenaFile) {
		return registerCompetition(plugin, name, cmd, arenaClass, executor, configFile, messageFile,
				new File(plugin.getDataFolder()+"/"+name+"Config.yml"),defaultArenaFile);
	}

	public boolean registerCompetition(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, CustomCommandExecutor executor,
			File configFile, File messageFile, File defaultPluginConfigFile,File defaultArenaFile) {
		try {
			return _registerCompetition(plugin, name, cmd, arenaClass, executor,
					configFile, messageFile, defaultPluginConfigFile, defaultArenaFile);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private boolean _registerCompetition(JavaPlugin plugin, String name, String cmd,
			Class<? extends Arena> arenaClass, CustomCommandExecutor executor,
			File configFile, File messageFile, File defaultPluginConfigFile, File defaultArenaFile)
					throws Exception{

		/// Create our plugin folder if its not there
		File dir = plugin.getDataFolder();
		FileUpdater.makeIfNotExists(dir);

		/// Define our config files
		String configFileName = name+"Config.yml";
		String defaultConfigFileName = "defaultConfig.yml";
		File compDir = configFile.getParentFile().getAbsoluteFile();

		File pluginFile = new File(compDir.getPath()+File.separator+configFileName);
		File defaultFile = new File("default_files/competitions/"+File.separator+defaultConfigFileName);

		/// Set a delayed init on this plugin and folder to load custom types
		if (!delayedInits.contains(plugin.getName())){
			delayedInits.add(plugin.getName());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
					new DelayedRegistrationHandler(plugin, compDir,defaultArenaFile));
		}

		/// load config
		if (!loadFile(plugin, defaultFile, defaultPluginConfigFile, pluginFile,
				name+"Config.yml",name,cmd)){
			Log.err(plugin.getName() + " " + pluginFile.getName() + " could not be loaded!");
			Log.err("defaultFile="+defaultFile != null ? defaultFile.getAbsolutePath() : "null");
			Log.err("defaultPluginFile="+defaultPluginConfigFile != null ? defaultPluginConfigFile.getAbsolutePath() : "null");
			Log.err("pluginFile="+pluginFile != null ? pluginFile.getAbsolutePath() : "null");
			return false;
		}

		ConfigSerializer config = new ConfigSerializer(plugin, pluginFile,name);

		/// load or register our arena type
		ArenaType at;
		if (arenaClass == null){
			try{
				at = ConfigSerializer.getArenaType(plugin, config.getConfigurationSection(name));
			} catch (ConfigException e){
				Log.err(e.getMessage());
				return false;
			}
		} else {
			at = ArenaType.register(name, arenaClass, plugin);
		}

		/// Load our Match Params for this type
		MatchParams mp = config.loadMatchParams();

		ArenaType gameType = ConfigSerializer.getArenaGameType(plugin,config.getConfigurationSection(name));
		MessageSerializer ms = null;
		/// load messages
		if (loadFile(plugin, messageFile, name+"Messages.yml",name,cmd)){
			ms = new MessageSerializer(name,mp);
		} else if (gameType != null){
			RegisteredCompetition rc = CompetitionController.getCompetition(plugin, gameType.getName());
			if (rc != null){
				ms = MessageSerializer.getMessageSerializer(gameType.getName());}
		}

		if (ms != null){
			ms.setConfig(messageFile);
			ms.loadAll();
			MessageSerializer.addMessageSerializer(name,ms);
		}

		/// Everything nearly successful, register our competition
		RegisteredCompetition rc = new RegisteredCompetition(plugin,name);

		if (executor == null && gameType != null){
			RegisteredCompetition comp = CompetitionController.getCompetition(plugin, gameType.getName());
			if (comp != null){
				executor = comp.getCustomExecutor();}
		} else {
			rc.setCustomExeuctor(executor);
		}

		/// Create our Executor
		createExecutor(plugin, cmd, executor, mp);

		rc.setConfigSerializer(config);
		CompetitionController.addRegisteredCompetition(rc);

		/// Load our arenas
		ArenaSerializer as = new ArenaSerializer(plugin, defaultArenaFile); /// arena config
		as.loadArenas(plugin,at);
		rc.setArenaSerializer(as);
		return true;
	}

	private static void createExecutor(JavaPlugin plugin, String cmd, CustomCommandExecutor executor, MatchParams mp) {
		CustomCommandExecutor exe = null;
		if (mp instanceof EventParams){
			exe = new ReservedArenaEventExecutor();
			EventController.addEventExecutor((EventParams) mp, (EventExecutor) exe);
		} else {
			if (mp.isDuelOnly()){
				exe = new DuelExecutor();
			} else {
				exe = new BAExecutor();
			}
		}
		if (executor != null){
			exe.addMethods(executor, executor.getClass().getMethods());}

		/// Set command to the executor
		setCommandToExecutor(plugin, cmd.toLowerCase(), exe);
		if (!mp.getCommand().equalsIgnoreCase(cmd))
			setCommandToExecutor(plugin, mp.getCommand().toLowerCase(), exe);
	}
}