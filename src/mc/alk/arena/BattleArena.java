package mc.alk.arena;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.competition.events.TournamentEvent;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.APIRegistrationController;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.BAEventController;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.DuelController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.EventScheduler;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.executors.ArenaEditorExecutor;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.BattleArenaDebugExecutor;
import mc.alk.arena.executors.BattleArenaSchedulerExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.executors.TeamExecutor;
import mc.alk.arena.executors.TournamentExecutor;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.listeners.BAPluginListener;
import mc.alk.arena.listeners.BASignListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.victoryconditions.HighestKills;
import mc.alk.arena.objects.victoryconditions.LastManStanding;
import mc.alk.arena.objects.victoryconditions.NDeaths;
import mc.alk.arena.objects.victoryconditions.TimeLimit;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.serializers.ArenaControllerSerializer;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.BAClassesSerializer;
import mc.alk.arena.serializers.BAConfigSerializer;
import mc.alk.arena.serializers.EventScheduleSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.serializers.StateFlagSerializer;
import mc.alk.arena.serializers.TeamHeadSerializer;
import mc.alk.arena.serializers.YamlFileUpdater;
import mc.alk.arena.util.FileLogger;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.plugin.updater.PluginUpdater;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleArena extends JavaPlugin{
	static private String pluginname;
	static private String version;
	static private BattleArena plugin;

	private final static BattleArenaController arenaController = new BattleArenaController();
	static BAEventController eventController;
	private final static TeamController tc = TeamController.INSTANCE;
	private final static EventController ec = new EventController();
	private final static ArenaEditor aac = new ArenaEditor();
	private final DuelController dc = new DuelController();
	private static BAExecutor commandExecutor;
	private final BAPlayerListener playerListener = new BAPlayerListener(arenaController);
	private final BAPluginListener pluginListener = new BAPluginListener();
	private final BASignListener signListener = new BASignListener();
	private ArenaControllerSerializer yacs;
	private static final BAConfigSerializer cc = new BAConfigSerializer();
	private static final BAClassesSerializer bacs = new BAClassesSerializer();
	private static final EventScheduleSerializer ess = new EventScheduleSerializer();

	@Override
	public void onEnable() {
		plugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		pluginname = pdfFile.getName();
		version = pdfFile.getVersion();
		ColouredConsoleSender.getInstance().sendMessage(MessageUtil.colorChat("&4["+pluginname+"] &6v"+version+"&f enabling!"));
		/// Create our plugin folder if its not there
		File dir = getDataFolder();
		if (!dir.exists()){
			dir.mkdirs();}

		/// For potential updates to default yml files
		YamlFileUpdater yfu = new YamlFileUpdater();

		/// Set up our messages first before other initialization needs messages
		MessageSerializer defaultMessages = new MessageSerializer("default");
		defaultMessages.setConfig(load("/default_files/messages.yml", dir.getPath()+"/messages.yml"));
		yfu.updateMessageSerializer(defaultMessages); /// Update our config if necessary
		defaultMessages.loadAll();
		MessageSerializer.setDefaultConfig(defaultMessages);

		commandExecutor = new BAExecutor();
		eventController = new BAEventController();

		pluginListener.loadAll(); /// try and load plugins we want

		yacs = new ArenaControllerSerializer();

		// Register our events
		Bukkit.getPluginManager().registerEvents(playerListener, this);
		Bukkit.getPluginManager().registerEvents(pluginListener, this);
		Bukkit.getPluginManager().registerEvents(signListener, this);
		Bukkit.getPluginManager().registerEvents(tc, this);
		Bukkit.getPluginManager().registerEvents(new TeleportController(), this);

		/// Register our different arenas
		ArenaType.register("Any", Arena.class, this);
		ArenaType.register("BattleGround", Arena.class, this);
		ArenaType.register("Colliseum", Arena.class, this);
		ArenaType.register("DeathMatch", Arena.class, this);
		ArenaType.register("FFA", Arena.class, this);
		ArenaType.register("Arena", Arena.class, this);
		ArenaType.register("Skirmish", Arena.class, this);
		ArenaType.register("Versus", Arena.class, this);

		VictoryType.register(HighestKills.class, this);
		VictoryType.register(NDeaths.class, this);
		VictoryType.register(LastManStanding.class, this);
		VictoryType.register(TimeLimit.class, this);

		MethodController.addMethods(Match.class, Match.class.getMethods());
		MethodController.addMethods(ArenaMatch.class, ArenaMatch.class.getMethods());

		/// Load our configs, then arenas
		cc.setConfig(null,load("/default_files/config.yml",dir.getPath() +"/config.yml"));
		YamlFileUpdater.updateConfig(cc); /// Update our config if necessary

		bacs.setConfig(load("/default_files/classes.yml",dir.getPath() +"/classes.yml")); /// Load classes
		bacs.loadAll();

		TeamHeadSerializer ts = new TeamHeadSerializer();
		ts.setConfig(load("/default_files/teamHeads.yml",dir.getPath() +"/teamHeads.yml")); /// Load team heads
		ts.loadAll();

		cc.loadAll(); /// Load our defaults for BattleArena

		/// persist our disabled arena types
		StateFlagSerializer sfs = new StateFlagSerializer();
		sfs.setConfig(dir.getPath() +"/state.yml");
		commandExecutor.setDisabled(sfs.load());

		ArenaSerializer.setBAC(arenaController);
		ArenaSerializer as = new ArenaSerializer(this, dir.getPath()+"/arenas.yml");
		as.loadArenas(this);

		SpawnSerializer ss = new SpawnSerializer();
		ss.setConfig(load("/default_files/spawns.yml",dir.getPath() +"/spawns.yml"));
		yacs.load();

		/// Set our commands
		getCommand("arena").setExecutor(commandExecutor);
		getCommand("skirmish").setExecutor(commandExecutor);
		getCommand("colliseum").setExecutor(commandExecutor);
		getCommand("battleground").setExecutor(commandExecutor);
		getCommand("watch").setExecutor(commandExecutor);
		getCommand("team").setExecutor(new TeamExecutor(commandExecutor));
		getCommand("arenaAlter").setExecutor(new ArenaEditorExecutor());
		getCommand("battleArenaDebug").setExecutor(new BattleArenaDebugExecutor());
		final EventScheduler es = new EventScheduler();
		getCommand("battleArenaScheduler").setExecutor(new BattleArenaSchedulerExecutor(es));

		/// Create our events
		createEvents();

		/// Reload our scheduled events
		ess.setConfig(dir.getPath() +"/scheduledEvents.yml");
		ess.addScheduler(es);

		createMessageSerializers();
		FileLogger.init(); /// shrink down log size
		/// Start listening for players queuing into matches
		new Thread(arenaController).start();

		/// Other plugins using BattleArena are going to be registering
		/// Lets hold off on loading the scheduled events until those plugins have registered
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			@Override
			public void run() {
				ess.loadAll();
				if (Defaults.START_NEXT)
					es.startNext();
				else if (Defaults.START_CONTINUOUS)
					es.start();
			}
		});
		if (Defaults.AUTO_UPDATE)
			PluginUpdater.downloadPluginUpdates(this);
		ColouredConsoleSender.getInstance().sendMessage(MessageUtil.colorChat("&4["+pluginname+"] &6v"+version+"&f enabled!"));
	}

	private void createMessageSerializers() {
		File f = new File(getDataFolder()+"/messages");
		if (!f.exists())
			f.mkdir();
		HashSet<String> events = new HashSet<String>(Arrays.asList("freeforall","deathmatch","tourney"));
		for (MatchParams mp: ParamController.getAllParams()){
			String fileName = events.contains(mp.getName().toLowerCase()) ? "defaultEventMessages.yml": "defaultMatchMessages.yml";
			MessageSerializer ms = new MessageSerializer(mp.getName());
			ms.setConfig(load("/default_files/"+fileName, f.getAbsolutePath()+"/"+mp.getName()+"Messages.yml"));
			ms.loadAll();
			MessageSerializer.addMessageSerializer(mp.getName(),ms);
		}
	}

	@Override
	public void onDisable() {
		StateFlagSerializer sfs = new StateFlagSerializer();
		sfs.setConfig(getDataFolder().getPath() +"/state.yml");
		sfs.save(commandExecutor.getDisabled());

		BattleArena.getSelf();
		arenaController.stop();
		ArenaSerializer.saveAllArenas(true);
		yacs.save();
		ess.saveScheduledEvents();
		if (Defaults.AUTO_UPDATE)
			PluginUpdater.updatePlugin(this);
		FileLogger.saveAll();
	}

	private void createEvents() {
		EventParams mp = null;

		/// Tournament, multi round matches
		mp = ParamController.getEventParamCopy("tourney");
		if (mp != null){
			TournamentEvent tourney = new TournamentEvent(mp);
			EventController.addEvent(tourney);
			try{
				EventExecutor executor = new TournamentExecutor(tourney);
				getCommand("tourney").setExecutor(executor);
				EventController.addEventExecutor(tourney.getParams(), executor);
			} catch (Exception e){
				Log.err("command tourney not found");
			}
		} else {
			Log.err("Tournament type not found");
		}

		/// Reserve an arena.  Hold people in the area till ffa starts
		mp = ParamController.getEventParamCopy("FreeForAll");
		if (mp != null){
			try{
				EventExecutor executor = new ReservedArenaEventExecutor(null);
				getCommand("ffa").setExecutor(executor);
				EventController.addEventExecutor(mp, executor);
			} catch (Exception e){
				Log.err("command ffa not found");
			}
		} else {
			Log.err("FFA type not found");
		}

		/// Reserve an arena.  Let people join and enter at will
		mp = ParamController.getEventParamCopy("DeathMatch");
		if (mp != null){
			try{
				EventExecutor executor = new ReservedArenaEventExecutor(null);
				getCommand("dm").setExecutor(executor);
				EventController.addEventExecutor(mp, executor);
			} catch (Exception e){
				Log.err("command dm not found");
			}
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
	public static BattleArenaController getBAController(){return arenaController;}
	public static BAEventController getBAEventController(){return eventController;}
	public static TeamController getTeamController(){return tc;}
	public DuelController getDuelController(){return dc;}
	public static EventController getEventController(){return ec;}
	public static ArenaEditor getArenaEditor() {return aac;}
	public static BAExecutor getBAExecutor() {return commandExecutor;}

	@Override
	/**
	 * Reload our own config
	 */
	public void reloadConfig(){
		super.reloadConfig();
		cc.loadAll();
		bacs.loadAll();
	}

	public static String getVersion() {
		return "[" + pluginname + " v" + version +"]";
	}
	public static String getPName() {
		return "[" + pluginname+"]";
	}

	public static void saveArenas() {ArenaSerializer.saveAllArenas(false);	}
	public static void saveArenas(boolean log) {ArenaSerializer.saveAllArenas(log);	}

	public void loadArenas() {
		ArenaSerializer.loadAllArenas();
	}

	public static ArenaPlayer toArenaPlayer(Player player) {return PlayerController.toArenaPlayer(player);}
	public static Set<ArenaPlayer> toArenaPlayerSet(Collection<Player> players) {return PlayerController.toArenaPlayerSet(players);}
	public static List<ArenaPlayer> toArenaPlayerList(Collection<Player> players) {return PlayerController.toArenaPlayerList(players);}
	public static Set<Player> toPlayerSet(Collection<ArenaPlayer> players) {return PlayerController.toPlayerSet(players);}
	public static List<Player> toPlayerList(Collection<ArenaPlayer> players) {return PlayerController.toPlayerList(players);}

	public static void registerMatchType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass){
		new APIRegistrationController().registerMatchType(plugin, name, cmd, arenaClass);
	}
	public static void registerMatchType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, BAExecutor executor){
		new APIRegistrationController().registerMatchType(plugin,name,cmd,arenaClass,executor);
	}

	public static void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass){
		new APIRegistrationController().registerEventType(plugin,name,cmd,arenaClass);
	}

	public static void registerEventType(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, EventExecutor executor){
		new APIRegistrationController().registerEventType(plugin,name,cmd,arenaClass,executor);
	}

	public static Arena getArena(String arenaName) {return BattleArena.getBAC().getArena(arenaName);}

	public static void saveArenas(Plugin plugin) {ArenaSerializer.saveArenas(plugin);}
}
