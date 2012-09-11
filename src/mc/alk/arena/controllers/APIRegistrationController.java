package mc.alk.arena.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.competition.events.ReservedArenaEvent;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
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
		ArenaSerializer as = new ArenaSerializer(plugin, plugin.getDataFolder()+"/arenas.yml"); /// arena config
		as.loadArenas(plugin,at);

		ConfigSerializer cc = new ConfigSerializer(); /// Our config.yml
		
		String configFileName = name+"Config.yml";
		File f = new File(dir.getPath()+"/"+configFileName);
		if (!f.exists()){
			loadDefaultConfig(plugin, name,cmd, f, match);}

		cc.setConfig(at, dir.getPath()+"/"+configFileName);

		/// Make a message serializer for this event, and make the messages.yml file if it doesnt exist
		String fileName = match ? "defaultMatchMessages.yml": "defaultEventMessages.yml";  
		MessageSerializer ms = new MessageSerializer(name);
		ms.setConfig(BattleArena.getSelf().load("/default_files/"+fileName, plugin.getDataFolder()+"/"+name+"Messages.yml"));
		ms.loadAll();
		MessageSerializer.addMessageSerializer(name,ms);

		try {
			ConfigSerializer.setTypeConfig(name,cc.getConfigurationSection(name));
		} catch (Exception e){
			System.err.println("Error trying to load "+name+" config");
			e.printStackTrace();
		}
	}

	private void loadDefaultConfig(Plugin plugin, String name, String cmd, File configFile, boolean match) {
		InputStream inputStream = null;
		File infile = new File("/"+name+"Config.yml");
		/// See if the plugin has supplied a default config already, if not get a default from BattleArena
		inputStream = plugin.getClass().getResourceAsStream(infile.getAbsolutePath());
		if (inputStream == null){
			String fileName = match ? "defaultMatchTypeConfig.yml" : "defaultEventTypeConfig.yml";
			infile = new File("/default_files/"+fileName);			
			inputStream = getClass().getResourceAsStream(infile.getAbsolutePath());
		}
		String line =null;
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		BufferedWriter fw =null;
		try {
			fw = new BufferedWriter(new FileWriter(configFile));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} 

		try {
			while ((line = br.readLine()) != null){
				line = line.replaceAll("<name>", name).replaceAll("<cmd>", cmd);
				fw.write(line+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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


	public void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass) {
		registerEventType(plugin,name,cmd,arenaClass,new ReservedArenaEventExecutor());
	}

	public void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, EventExecutor executor) {
		init(plugin,name,cmd,arenaClass,false);
		MatchParams mp = ParamController.getMatchParamCopy(name);
		if (mp != null){
			/// TODO this should probably get what event based off of what executor, maybe a map?
			ReservedArenaEvent event = new ReservedArenaEvent(mp);
			EventController.addEvent(event);
			executor.setEvent(event);
			registerCommand(plugin, cmd, executor);
		} else {
			Log.err(name+" type not found");
		}
	}

	public void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, 
			Event event, EventExecutor executor) {
		init(plugin,name,cmd,arenaClass,false);
		MatchParams mp = ParamController.getMatchParams(name);
		if (mp != null){
			EventController.addEvent(event);
			executor.setEvent(event);
			plugin.getCommand(cmd).setExecutor(executor);
		} else {
			Log.err(name+" type not found");
		}		
	}

}
