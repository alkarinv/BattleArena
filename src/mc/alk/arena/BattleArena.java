package mc.alk.arena;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.APIRegistrationController;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.events.AlwaysJoinRAE;
import mc.alk.arena.events.Event;
import mc.alk.arena.events.ReservedArenaEvent;
import mc.alk.arena.events.TournamentEvent;
import mc.alk.arena.executors.ArenaEditorExecutor;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.BattleArenaDebugExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.executors.TeamExecutor;
import mc.alk.arena.executors.TournamentExecutor;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.listeners.BAPluginListener;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.victoryconditions.HighestKills;
import mc.alk.arena.objects.victoryconditions.LastManStanding;
import mc.alk.arena.objects.victoryconditions.NDeaths;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.serializers.ArenaControllerSerializer;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.BAConfigSerializer;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.util.FileLogger;
import mc.alk.arena.util.Log;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.alk.battleEventTracker.BattleEventTracker;
import com.alk.massDisguise.MassDisguise;

public class BattleArena extends JavaPlugin{
	static private String pluginname; 
	static private String version;
	static private BattleArena plugin;
	static public MassDisguise md = null;
	public static BattleEventTracker bet = null;

	private final static BattleArenaController arenaController = new BattleArenaController();
	private final static TeamController tc = new TeamController(arenaController);
	private final static EventController ec = new EventController();
	private final static ArenaEditor aac = new ArenaEditor();
	private final static BAExecutor commandExecutor = new BAExecutor();
	private final static APIRegistrationController apiRegistrationController = new APIRegistrationController();
	
	private final BAPlayerListener playerListener = new BAPlayerListener(arenaController);

	private final BAPluginListener pluginListener = new BAPluginListener();
	private ArenaControllerSerializer yacs;
	private static final BAConfigSerializer cc = new BAConfigSerializer();

	public void onEnable() {
		plugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		pluginname = pdfFile.getName();
		version = pdfFile.getVersion();
		Log.info(getVersion() + " starting enable!");

		pluginListener.loadAll(); /// try and load plugins we want

		/// Create our plugin folder if its not there
		File dir = getDataFolder();
		if (!dir.exists()){
			dir.mkdirs();}
		yacs = new ArenaControllerSerializer();

		// Register our events
		Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(pluginListener, this);
		Bukkit.getServer().getPluginManager().registerEvents(tc, this);
		TeleportController.setup(this);

		/// Register our different arenas
		ArenaType.register("Any", Arena.class, this);
		ArenaType.register("BattleGround", Arena.class, this);
		ArenaType.register("Colliseum", Arena.class, this);
		ArenaType.register("DeathMatch", Arena.class, this);
		ArenaType.register("FFA", Arena.class, this);
		ArenaType.register("Versus", Arena.class, this);
		ArenaType.addCompatibleTypes("DeathMatch", "FFA"); /// Deathmatch arenas and FFAs are interchangeable

		VictoryType.register("LastManStanding", LastManStanding.class, this);
		VictoryType.register("HighestKills", HighestKills.class, this);
		VictoryType.register("NDeaths", NDeaths.class, this);

		MethodController.addMethods(Match.class, Match.class.getMethods());

		/// After registering our arenas and victories, load our configs
		ArenaSerializer.setBAC(arenaController);
		ArenaSerializer as = new ArenaSerializer(this, getDataFolder()+"/arenas.yml");
		as.loadArenas(this);
		
		SpawnSerializer ss = new SpawnSerializer();
		ss.setConfig(load("/default_files/spawns.yml",dir.getPath() +"/spawns.yml"));
		yacs.load();
		
		cc.setConfig(null,load("/default_files/config.yml",dir.getPath() +"/config.yml"));
		cc.loadAll();
		MoneyController.setup();

		MessageController.setConfig(load(Defaults.DEFAULT_MESSAGES_FILE, Defaults.MESSAGES_FILE));
		MessageController.load();

		/// Set our commands
		getCommand("arena").setExecutor(commandExecutor);
		getCommand("skirmish").setExecutor(commandExecutor);
		getCommand("colliseum").setExecutor(commandExecutor);
		getCommand("battleground").setExecutor(commandExecutor);
		getCommand("watch").setExecutor(commandExecutor);
		getCommand("team").setExecutor(new TeamExecutor(commandExecutor));
		getCommand("arenaAlter").setExecutor(new ArenaEditorExecutor());
		getCommand("battleArenaDebug").setExecutor(new BattleArenaDebugExecutor());

		createEvents();

		/// Start listening for players queuing
		new Thread(arenaController).start();
		Log.info(getVersion()+ " initialized!");
	}

	public void onDisable() {
		arenaController.stop();
		ArenaSerializer.saveAllArenas(true);
		yacs.save();
		FileLogger.saveAll();
	}

	private void createEvents() {
		MatchParams mp = null;

		/// Tournament, multi round matches
		mp = ParamController.findParamInst("tourney");
		if (mp != null){
			TournamentEvent tourney = new TournamentEvent(mp);
			EventController.addEvent(tourney);
			getCommand("tourney").setExecutor(new TournamentExecutor(tourney));
		} else {
			Log.err("Tournament type not found");
		}

		/// Reserve an arena.  Hold people in the area till ffa starts
		mp = ParamController.findParamInst("FreeForAll");
		if (mp != null){
			ReservedArenaEvent event = new ReservedArenaEvent(mp);
			EventController.addEvent(event);
			getCommand("ffa").setExecutor(new ReservedArenaEventExecutor(event));
		} else {
			Log.err("FFA type not found");
		}

		/// Reserve an arena.  Let people join and enter at will
		mp = ParamController.findParamInst("DeathMatch");
		if (mp != null){
			ReservedArenaEvent event = new AlwaysJoinRAE(mp);
			EventController.addEvent(event);
			getCommand("dm").setExecutor(new ReservedArenaEventExecutor(event));
		} else {
			Log.err("DM type not found");
		}
	}

	public File load(String default_file, String config_file) {
		File file = new File(config_file);
		if (!file.exists()){ /// Create a new file from our default example
			try{
				InputStream inputStream = getClass().getResourceAsStream(default_file);
				OutputStream out=new FileOutputStream(config_file);
				byte buf[]=new byte[1024];
				int len;
				while((len=inputStream.read(buf))>0){
					out.write(buf,0,len);}
				out.close();
				inputStream.close();
			} catch (Exception e){
			}
		}
		return file;
	}


	public static BattleArena getSelf() {return plugin;}
	public static BattleArenaController getBAC(){return arenaController;}
	public static TeamController getTC(){return tc;}
	public static ArenaEditor getAE(){return aac;}
	public static EventController getEC(){return ec;}
	public static Event getEvent(String name){return EventController.getEvent(name);}
	public static ArenaEditor getArenaEditor() {return aac;}
	public static BAExecutor getBAExecutor() {return commandExecutor;}
	
	@Override
	/**
	 * Reload our own config
	 */
	public void reloadConfig(){
		super.reloadConfig();
		cc.loadAll();
	}

	public static String getVersion() {
		return "[" + pluginname + " v" + version +"]";
	}
	public static String getPName() {
		return "[" + pluginname+"]";
	}

	public void saveArenas() {ArenaSerializer.saveAllArenas(false);	}
	public void saveArenas(boolean log) {ArenaSerializer.saveAllArenas(log);	}

	public void loadArenas() {
		ArenaSerializer.loadAllArenas();
	}

	public static ArenaPlayer toArenaPlayer(Player player) {return PlayerController.toArenaPlayer(player);}
	public static Set<ArenaPlayer> toArenaPlayerSet(Collection<Player> players) {return PlayerController.toArenaPlayerSet(players);}
	public static List<ArenaPlayer> toArenaPlayerList(Collection<Player> players) {return PlayerController.toArenaPlayerList(players);}
	public static Set<Player> toPlayerSet(Collection<ArenaPlayer> players) {return PlayerController.toPlayerSet(players);}
	public static List<Player> toPlayerList(Collection<ArenaPlayer> players) {return PlayerController.toPlayerList(players);}
	
	public static void registerMatchType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass){
		apiRegistrationController.registerMatchType(plugin, name, cmd, arenaClass);
	}
	public static void registerMatchType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, BAExecutor executor){
		apiRegistrationController.registerMatchType(plugin,name,cmd,arenaClass,executor);
	}

	public static void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass){
		apiRegistrationController.registerEventType(plugin,name,cmd,arenaClass);
	}
	
	public static void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, EventExecutor executor){
		apiRegistrationController.registerEventType(plugin,name,cmd,arenaClass,executor);
	}
}
