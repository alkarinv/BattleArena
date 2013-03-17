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
import java.util.List;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.CustomCommandExecutor;
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
import mc.alk.plugin.updater.FileUpdater;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class APIRegistrationController {
	static Set<String> delayedInits = Collections.synchronizedSet(new HashSet<String>());

	static class DelayedRegistrationHandler implements Runnable{
		JavaPlugin plugin;
		File compDir;

		public DelayedRegistrationHandler(JavaPlugin plugin, File compDir) {
			this.plugin = plugin;
			this.compDir = compDir;
		}

		@Override
		public void run() {
			if (!plugin.isEnabled()) /// lets not try to register plugins that aren't loaded
				return;
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File file) {return file.toString().contains("Config.yml");}
			};
			if (!compDir.exists())
				return;
			for (File file : compDir.listFiles(fileFilter)){
				String n = file.getName().substring(0, file.getName().length()-"Config.yml".length());
				if (ArenaType.contains(n) || n.contains(".")){ /// we already loaded this type, or bad type
					continue;}
				new APIRegistrationController().registerCompetition(plugin, n,n,Arena.class);
			}
		}
	}

	public static boolean loadFile(Plugin plugin, File defaultFile, File defaultPluginFile, File pluginFile){
		if (pluginFile.exists())
			return true;

		InputStream inputStream = FileUtil.getInputStream(plugin.getClass(), defaultFile, defaultPluginFile);
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
			if (out != null) try {out.close();} catch (IOException e) {}
			if (inputStream!=null) try {inputStream.close();} catch (IOException e) {}
		}

		return true;
	}

	public boolean loadConfigFile(Plugin plugin, File defaultFile, File defaultPluginFile, File pluginFile,
			String name, String cmd) {
		if (pluginFile.exists())
			return true;
		InputStream inputStream = FileUtil.getInputStream(plugin.getClass(), new File(name+"Config.yml"));
		if (inputStream == null){
			inputStream = FileUtil.getInputStream(plugin.getClass(), defaultFile, defaultPluginFile);}
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
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fw !=null) try {fw.close(); } catch (Exception e){}
			if (br !=null) try {br.close(); } catch (Exception e){}
		}
		return true;
	}

	private void setCommandToExecutor(JavaPlugin plugin, String wantedCommand, CommandExecutor executor) {
		if (!setCommandToExecutor(plugin,wantedCommand, executor, false)){
			List<String> aliases = new ArrayList<String>();
			ArenaCommand arenaCommand = new ArenaCommand(wantedCommand,"","", aliases, BattleArena.getSelf(),executor);
			CommandController.registerCommand(arenaCommand);
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

	public boolean hasMessageFile(Plugin plugin, String name, File dir){
		try {
			return createMessageFile(plugin, name, dir) != null;
		} catch (ConfigException e) {
			return false;
		}
	}

	public void createMessageSerializer(Plugin plugin, String name, File dir) throws ConfigException {
		File pluginFile;
		/// Make a message serializer for this match/event, and make the messages.yml file if it doesnt exist
		MessageSerializer ms = new MessageSerializer(name);

		pluginFile = createMessageFile(plugin, name, dir);
		ms.setConfig(pluginFile);
		ms.loadAll();
		MessageSerializer.addMessageSerializer(name,ms);
	}

	private static File createMessageFile(Plugin plugin, String name, File dir) throws ConfigException {
		String messagesFileName = name+"Messages.yml";
		String fileName = "defaultMessages.yml";

		File pluginFile = new File(dir.getPath()+File.separator+messagesFileName);
		File defaultPluginFile = new File(messagesFileName);
		File defaultFile = new File("default_files"+File.separator+fileName);

		if (!loadFile(plugin, defaultFile, defaultPluginFile, pluginFile)){
			pluginFile = FileUtil.load(BattleArena.getSelf().getClass(), pluginFile.getAbsolutePath(),"/default_files/"+fileName);
			if (pluginFile == null){
				throw new ConfigException(plugin.getName() + " " + messagesFileName+" could not be loaded\n"+
						"pluginFile was null");
			}
		}
		return pluginFile;
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

		String configFileName = name+"Config.yml";
		String defaultConfigFileName = "defaultConfig.yml";
		File compDir = configFile.getParentFile().getAbsoluteFile();

		File pluginFile = new File(compDir.getPath()+File.separator+configFileName);
		File defaultFile = new File("default_files/competitions/"+File.separator+defaultConfigFileName);

		if (!loadConfigFile(plugin, defaultFile, defaultPluginConfigFile, pluginFile, name,cmd)){
			Log.err(plugin.getName() + " " + pluginFile.getName() + " could not be loaded!");
			Log.err("defaultFile="+defaultFile != null ? defaultFile.getAbsolutePath() : "null");
			Log.err("defaultPluginFile="+defaultPluginConfigFile != null ? defaultPluginConfigFile.getAbsolutePath() : "null");
			Log.err("pluginFile="+pluginFile != null ? pluginFile.getAbsolutePath() : "null");
			return false;
		}

		ConfigSerializer bs = new ConfigSerializer(configFile,name);

		/// SetTypeConfig doesn't register ArenaType or commands
		ArenaType at = ArenaType.register(name, arenaClass, plugin);
		CustomCommandExecutor exe = null;
		if (!delayedInits.contains(plugin.getName())){
			delayedInits.add(plugin.getName());
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
					new DelayedRegistrationHandler(plugin, compDir));
		}
		MatchParams mp = bs.loadType(plugin);

		if (mp instanceof EventParams){
			exe = new ReservedArenaEventExecutor();
			EventController.addEventExecutor((EventParams) mp, (EventExecutor) exe);
		} else {
			exe = new BAExecutor();
		}
		if (executor != null){
			exe.addMethods(executor, executor.getClass().getMethods());}

		/// Set up command executors
		setCommandToExecutor(plugin, cmd, exe);

		RegisteredCompetition rc = new RegisteredCompetition(plugin,name);
		rc.setConfigSerializer(bs);
		CompetitionController.addRegisteredCompetition(rc);

		/// Load our arenas
		ArenaSerializer as = new ArenaSerializer(plugin, defaultArenaFile); /// arena config
		as.loadArenas(plugin,at);
		rc.setArenaSerializer(as);
		return true;
	}
}