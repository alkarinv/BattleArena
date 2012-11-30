package mc.alk.arena.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.ConfigException;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.serializers.YamlFileUpdater;
import mc.alk.arena.util.FileUtil;
import mc.alk.arena.util.Log;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class APIRegistrationController {

	private void init(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, boolean match){
		if (plugin == null){
			Log.err("Plugin can not be null");
			return;
		}
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
		YamlFileUpdater.updateAllConfig(cc);
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

	public void registerMatchType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, BAExecutor executor) {
		init(plugin,name,cmd,arenaClass,true);

		/// Set up command executors
		registerCommand(plugin, cmd, executor);
	}

	private void registerCommand(JavaPlugin plugin, String cmd, CommandExecutor executor) {
		try{
			plugin.getCommand(cmd).setExecutor(executor);
		} catch(Exception e){
			Log.err(plugin.getName() + " command " + cmd +" was not found. Did you register it in your plugin.yml?");
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
		registerEventType(plugin,name,cmd,arenaClass,new ReservedArenaEventExecutor());
	}

	public void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, EventExecutor executor) {
		init(plugin,name,cmd,arenaClass,false);
		EventParams mp = ParamController.getEventParamCopy(name);
		if (mp != null){
			registerCommand(plugin, cmd, executor);
			EventController.addEventExecutor(mp, executor);
		} else {
			Log.err(name+" type not found");
		}
	}

	public void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass,
			Event event, EventExecutor executor) {
		init(plugin,name,cmd,arenaClass,false);
		EventParams mp = ParamController.getEventParamCopy(name);
		if (mp != null){
			plugin.getCommand(cmd).setExecutor(executor);
			EventController.addEventExecutor(mp, executor);
		} else {
			Log.err(name+" type not found");
		}
	}

}