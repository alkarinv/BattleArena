package mc.alk.arena;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.APIRegistrationController;
import mc.alk.arena.controllers.ArenaEditor;
import mc.alk.arena.controllers.BAEventController;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.BukkitInterface;
import mc.alk.arena.controllers.DuelController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.EventScheduler;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.SignController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.executors.ArenaEditorExecutor;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.BattleArenaDebugExecutor;
import mc.alk.arena.executors.BattleArenaExecutor;
import mc.alk.arena.executors.BattleArenaSchedulerExecutor;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.TeamExecutor;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.listeners.BAPluginListener;
import mc.alk.arena.listeners.BASignListener;
import mc.alk.arena.listeners.competition.InArenaListener;
import mc.alk.arena.listeners.competition.MatchListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.victoryconditions.AllKills;
import mc.alk.arena.objects.victoryconditions.HighestKills;
import mc.alk.arena.objects.victoryconditions.InfiniteLives;
import mc.alk.arena.objects.victoryconditions.LastManStanding;
import mc.alk.arena.objects.victoryconditions.MobKills;
import mc.alk.arena.objects.victoryconditions.NLives;
import mc.alk.arena.objects.victoryconditions.NoTeamsLeft;
import mc.alk.arena.objects.victoryconditions.OneTeamLeft;
import mc.alk.arena.objects.victoryconditions.PlayerKills;
import mc.alk.arena.objects.victoryconditions.TimeLimit;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.serializers.ArenaControllerSerializer;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.BAClassesSerializer;
import mc.alk.arena.serializers.BAConfigSerializer;
import mc.alk.arena.serializers.EventScheduleSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.serializers.SignSerializer;
import mc.alk.arena.serializers.SpawnSerializer;
import mc.alk.arena.serializers.StateFlagSerializer;
import mc.alk.arena.serializers.TeamHeadSerializer;
import mc.alk.arena.serializers.YamlFileUpdater;
import mc.alk.arena.util.FileLogger;
import mc.alk.arena.util.FileUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.plugin.updater.v1r2.FileUpdater;
import mc.alk.plugin.updater.v1r2.PluginUpdater;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleArena extends JavaPlugin {
	static private String pluginname;
	static private String version;
	static private BattleArena plugin;

	private static BattleArenaController arenaController;
	static BAEventController eventController;
	private final static TeamController tc = TeamController.INSTANCE;
	private final static EventController ec = new EventController();
	private final static ArenaEditor aac = new ArenaEditor();
	private final static DuelController dc = new DuelController();
	private static BAExecutor commandExecutor;
	private final BAPlayerListener playerListener = new BAPlayerListener(arenaController);
	private final BAPluginListener pluginListener = new BAPluginListener();
	private final SignController signController = new SignController();
	private final BASignListener signListener = new BASignListener(signController);
	private final MatchListener matchListener = new MatchListener(signController);

	private ArenaControllerSerializer arenaControllerSerializer;
	private static final BAConfigSerializer baConfigSerializer = new BAConfigSerializer();
	private static final BAClassesSerializer classesSerializer = new BAClassesSerializer();
	private static final EventScheduleSerializer eventSchedulerSerializer = new EventScheduleSerializer();
	private static final SignSerializer signSerializer = new SignSerializer();

	@Override
	public void onEnable() {
		BattleArena.plugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		BattleArena.pluginname = pdfFile.getName();
		BattleArena.version = pdfFile.getVersion();
		Class<?> clazz = this.getClass();
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		MessageUtil.sendMessage(sender,"&4["+pluginname+"] &6v"+version+"&f enabling!");

		BukkitInterface.setServer(Bukkit.getServer()); /// Set the server
		arenaController = new BattleArenaController(signController);

		/// Create our plugin folder if its not there
		File dir = getDataFolder();
		FileUpdater.makeIfNotExists(dir);
		FileUpdater.makeIfNotExists(new File(dir+"/competitions"));
		FileUpdater.makeIfNotExists(new File(dir+"/messages"));
		FileUpdater.makeIfNotExists(new File(dir+"/saves"));
		FileUpdater.makeIfNotExists(new File(dir+"/modules"));

		/// For potential updates to default yml files
		YamlFileUpdater yfu = new YamlFileUpdater(this);

		/// Set up our messages first before other initialization needs messages
		MessageSerializer defaultMessages = new MessageSerializer("default",null);
		defaultMessages.setConfig(FileUtil.load(clazz,dir.getPath()+"/messages.yml","/default_files/messages.yml"));
		yfu.updateMessageSerializer(plugin,defaultMessages); /// Update our config if necessary
		defaultMessages.loadAll();
		MessageSerializer.setDefaultConfig(defaultMessages);

		commandExecutor = new BAExecutor();
		eventController = new BAEventController();

		pluginListener.loadAll(); /// try and load plugins we want

		arenaControllerSerializer = new ArenaControllerSerializer();

		// Register our events
		Bukkit.getPluginManager().registerEvents(playerListener, this);
		Bukkit.getPluginManager().registerEvents(pluginListener, this);
		Bukkit.getPluginManager().registerEvents(signListener, this);
		Bukkit.getPluginManager().registerEvents(matchListener, this);
		Bukkit.getPluginManager().registerEvents(tc, this);
		Bukkit.getPluginManager().registerEvents(new TeleportController(), this);

		/// Register our different Victory Types
		VictoryType.register(LastManStanding.class, this);
		VictoryType.register(NLives.class, this);
		VictoryType.register(InfiniteLives.class, this);
		VictoryType.register(TimeLimit.class, this);
		VictoryType.register(OneTeamLeft.class, this);
		VictoryType.register(NoTeamsLeft.class, this);
		VictoryType.register(HighestKills.class, this);
		VictoryType.register(PlayerKills.class, this);
		VictoryType.register(MobKills.class, this);
		VictoryType.register(AllKills.class, this);

		/// Load our configs, then arenas
		baConfigSerializer.setConfig(FileUtil.load(clazz,dir.getPath() +"/config.yml","/default_files/config.yml"));
		try{
			YamlFileUpdater.updateBaseConfig(this,baConfigSerializer); /// Update our config if necessary
		} catch (Exception e){
			Log.printStackTrace(e);
		}

		baConfigSerializer.loadDefaults(); /// Load our defaults for BattleArena, has to happen before classes are loaded

		classesSerializer.setConfig(FileUtil.load(clazz,dir.getPath() +"/classes.yml","/default_files/classes.yml")); /// Load classes
		classesSerializer.loadAll();

		/// Spawn Groups need to be loaded before the arenas
		SpawnSerializer ss = new SpawnSerializer();
		ss.setConfig(FileUtil.load(clazz,dir.getPath() +"/spawns.yml","/default_files/spawns.yml"));

		TeamHeadSerializer ts = new TeamHeadSerializer();
		ts.setConfig(FileUtil.load(clazz,dir.getPath() +"/teamConfig.yml","/default_files/teamConfig.yml")); /// Load team Colors
		ts.loadAll();

		baConfigSerializer.loadCompetitions(); /// Load our competitions, has to happen after classes and teams

		/// persist our disabled arena types
		StateFlagSerializer sfs = new StateFlagSerializer();
		sfs.setConfig(dir.getPath() +"/saves/state.yml");
		commandExecutor.setDisabled(sfs.load());

		ArenaSerializer.setBAC(arenaController);

		arenaControllerSerializer.load();

		/// Load up our signs
		signSerializer.setConfig(dir.getPath()+"/saves/signs.yml");
		signSerializer.loadAll(signController);
		signController.updateAllSigns();

		/// Set our commands
		getCommand("watch").setExecutor(commandExecutor);
		getCommand("team").setExecutor(new TeamExecutor(commandExecutor));
		getCommand("arenaAlter").setExecutor(new ArenaEditorExecutor());
		getCommand("battleArena").setExecutor(new BattleArenaExecutor());
		getCommand("battleArenaDebug").setExecutor(new BattleArenaDebugExecutor());
		final EventScheduler es = new EventScheduler();
		getCommand("battleArenaScheduler").setExecutor(new BattleArenaSchedulerExecutor(es));

		/// Reload our scheduled events
		eventSchedulerSerializer.setConfig(dir.getPath() +"/saves/scheduledEvents.yml");
		eventSchedulerSerializer.addScheduler(es);

		createMessageSerializers();
		FileLogger.init(); /// shrink down log size
		/// Start listening for players queuing into matches
		new Thread(arenaController).start();

		/// Other plugins using BattleArena are going to be registering
		/// Lets hold off on loading the scheduled events until those plugins have registered
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			@Override
			public void run() {
				eventSchedulerSerializer.loadAll();
				if (Defaults.START_NEXT)
					es.startNext();
				else if (Defaults.START_CONTINUOUS)
					es.start();
			}
		});
		if (Defaults.AUTO_UPDATE)
			PluginUpdater.downloadPluginUpdates(this);

		MessageUtil.sendMessage(sender,"&4["+pluginname+"] &6v"+BattleArena.version+"&f enabled!");
	}

	private void createMessageSerializers() {
		File f = new File(getDataFolder()+"/messages");
		if (!f.exists())
			f.mkdir();
		for (MatchParams mp: ParamController.getAllParams()){
			String fileName = "defaultMessages.yml";
			MessageSerializer ms = new MessageSerializer(mp.getName(),null);
			ms.setConfig(FileUtil.load(this.getClass(),f.getAbsolutePath()+"/"+mp.getName()+"Messages.yml","/default_files/"+fileName));
			ms.loadAll();
			MessageSerializer.addMessageSerializer(mp.getName(),ms);
		}
	}

	@Override
	public void onDisable() {
		StateFlagSerializer sfs = new StateFlagSerializer();
		sfs.setConfig(getDataFolder().getPath() +"/saves/state.yml");
		sfs.save(commandExecutor.getDisabled());

		BattleArena.getSelf();
		arenaController.stop();
		/// we no longer save arenas as those get saved after each alteration now
		arenaControllerSerializer.save();
		eventSchedulerSerializer.saveScheduledEvents();
		signSerializer.saveAll(signController);

		if (Defaults.AUTO_UPDATE)
			PluginUpdater.updatePlugin(this);
		FileLogger.saveAll();
	}

	/**
	 * Return the BattleArena plugin
	 * @return BattleArena
	 */
	public static BattleArena getSelf() {return plugin;}

	/**
	 * Return the BattleArenaController, which handles queuing and arenas
	 * @return BattleArenaController
	 */
	public static BattleArenaController getBAController(){return arenaController;}

	/**
	 * Return the BAEventController, which handles Events
	 * @return BAEventController
	 */
	public static BAEventController getBAEventController(){return eventController;}

	/**
	 * Get the TeamController, deals with self made teams
	 * @return TeamController
	 */
	public static TeamController getTeamController(){return tc;}

	/**
	 * Get the DuelController, deals with who is currently trying to duel other people/teams
	 * @return
	 */
	public static DuelController getDuelController(){return dc;}

	/**
	 * Get the EventController, deals with what events can be run
	 * @return
	 */
	public static EventController getEventController(){return ec;}

	/**
	 * Get the Arena Editor, deals with Altering and changing Arenas
	 * @return ArenaEditor
	 */
	public static ArenaEditor getArenaEditor() {return aac;}

	/**
	 * Get the BAExecutor, deals with the Arena related commands
	 * @return BAExecutor
	 */
	public static BAExecutor getBAExecutor() {return commandExecutor;}

	/**
	 * Is the player inside of the BattleArena system
	 * This means one of the following
	 * Player is in a queue, in a competition, being challenged, inside MobArena,
	 * being challenged to a duel, being invited to a team
	 *
	 * If a player is in an Arena or in a Competition this is always true
	 *
	 * @param player: the player you want to check
	 * @param showReasons: if player is in system, show the player a message about how to exit
	 * @return true or false: whether they are in the system
	 */
	public static boolean inSystem(Player player, boolean showReasons){
		return !getBAExecutor().canJoin(BattleArena.toArenaPlayer(player), showReasons);
	}

	/**
	 * Is the player currently inside a competition
	 * @param player: the player you want to check
	 * @return true or false: whether they are in a competition
	 */
	public static boolean inCompetition(Player player){
		return BattleArena.toArenaPlayer(player).getCompetition() != null;
	}

	/**
	 * Is the player physically inside an arena
	 * @param player: the player you want to check
	 * @return true or false: whether they are in inside an arena
	 */
	public static boolean inArena(Player player){
		return InArenaListener.inArena(player.getName());
	}

	@Override
	/**
	 * Reload our own config
	 */
	public void reloadConfig(){
		super.reloadConfig();
		baConfigSerializer.loadDefaults();
		classesSerializer.loadAll();
		baConfigSerializer.loadCompetitions();
		MessageSerializer.loadDefaults();
	}

	/**
	 * Get the a versioning String
	 * @return
	 */
	public static String getNameAndVersion() {
		return "[" + BattleArena.pluginname + " v" + BattleArena.version +"]";
	}

	/**
	 * Get the plugin name
	 * @return
	 */
	public static String getPluginName() {
		return "[" + BattleArena.pluginname+"]";
	}

	/**
	 * Save the arenas for a given plugin
	 * @param plugin
	 */
	public static void saveArenas(Plugin plugin) {ArenaSerializer.saveArenas(plugin);}

	/**
	 * Save all the arenas for all plugins
	 */
	public static void saveArenas() {ArenaSerializer.saveAllArenas(false);	}

	/**
	 * Save all arenas for all plugins and log to server.log
	 * @param log
	 */
	public static void saveArenas(boolean log) {ArenaSerializer.saveAllArenas(log);	}

	/**
	 * Load all arenas
	 */
	public void loadArenas() {
		ArenaSerializer.loadAllArenas();
	}

	/**
	 * Convert a bukkit Player into an ArenaPlayer
	 * @param player: player to convert
	 * @return ArenaPlayer: corresponding to the player
	 */
	public static ArenaPlayer toArenaPlayer(Player player) {return PlayerController.toArenaPlayer(player);}

	/**
	 * Convert bukkit Players to ArenaPlayers
	 * @param players
	 * @return
	 */
	public static Set<ArenaPlayer> toArenaPlayerSet(Collection<Player> players) {return PlayerController.toArenaPlayerSet(players);}

	/**
	 * Convert bukkit Players to ArenaPlayers
	 * @param players
	 * @return
	 */
	public static List<ArenaPlayer> toArenaPlayerList(Collection<Player> players) {return PlayerController.toArenaPlayerList(players);}

	/**
	 * Convert ArenaPlayers to BukkitPlayers
	 * @param players
	 * @return
	 */
	public static Set<Player> toPlayerSet(Collection<ArenaPlayer> players) {return PlayerController.toPlayerSet(players);}

	/**
	 * Convert ArenaPlayers to BukkitPlayers
	 * @param players
	 * @return
	 */
	public static List<Player> toPlayerList(Collection<ArenaPlayer> players) {return PlayerController.toPlayerList(players);}

	/**
	 * Get the arena a player is inside (if any)
	 * @param arenaName
	 * @return An arena, or null if player is not inside an arena
	 */
	public static Arena getArena(String arenaName) {return BattleArena.getBAController().getArena(arenaName);}


	/**
	 * Register a competiton with BattleArena
	 * @param plugin: The plugin that is registering the Arena
	 * @param name: Name of the competition
	 * @param cmd: The cmd you would like to use (can be an alias)
	 * @param arenaClass: The Arena Class for your competition
	 */
	public static void registerCompetition(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass){
		new APIRegistrationController().registerCompetition(plugin,name,cmd,arenaClass);
	}

	/**
	 * Register a competiton with BattleArena
	 * @param plugin: The plugin that is registering the Arena
	 * @param name: Name of the competition
	 * @param cmd: The cmd you would like to use (can be an alias)
	 * @param arenaClass: The Arena Class for your competition
	 * @param executor: The executor you would like to receive commands
	 */
	public static void registerCompetition(JavaPlugin plugin, String name, String cmd, Class<? extends Arena> arenaClass, CustomCommandExecutor executor){
		new APIRegistrationController().registerCompetition(plugin,name,cmd,arenaClass, executor);
	}

	public File getModuleDirectory() {
		return new File(this.getDataFolder()+"/modules");
	}
}
