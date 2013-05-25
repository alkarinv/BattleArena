package mc.alk.arena;

import java.io.File;

import org.bukkit.Material;

public class Defaults {

	/// Use auto updating
	public static boolean AUTO_UPDATE = false;
	public static boolean REPORT_ERRORS = false;

	public static double TICK_MULT = 1.0;
	public static String MONEY_STR = "Gold";
	public static boolean MONEY_SET = false;
    public static final double DEFAULT_ELO = 1250.0;

    /// How long can we keep appending player names together
    /// before reverting to team 1, team 2, etc
    public static final int MAX_TEAM_NAME_APPEND = 32;
	public static final int MAX_SCOREBOARD_NAME_SIZE = 16;

	/// ARENA QUEUING OPTIONS
	public static boolean USE_ARENAS_ONLY_IN_ORDER = false;


	/// MATCH OPTIONS
	public static int SECONDS_TILL_MATCH = 15;
	public static int SECONDS_TO_LOOT = 15;

	public static int MATCH_TIME = 2*60; /// matchEndTime
	public static int MATCH_UPDATE_INTERVAL = 30;
	public static int JOIN_CUTOFF_TIME = 15;

	public static boolean MATCH_FORCESTART_ENABLED = true;
	public static boolean MATCH_FORCESTART_START_ONJOIN = true;
	public static long MATCH_FORCESTART_TIME = 180;

	public static boolean USE_SCOREBOARD = true;
	public static boolean USE_COLORNAMES = true;

	/// EVENT OPTIONS
	public static int AUTO_EVENT_COUNTDOWN_TIME = 180;
	public static int ANNOUNCE_EVENT_INTERVAL = 60;

	/// TOURNEY OPTIONS
	public static final int TIME_BETWEEN_ROUNDS = 20;

	/// WORLDGUARD OPTIONS
	public static final int MAX_REGION_SIZE = 5000000;

	/// DUEL OPTIONS
	/// 30 minutes before people can duel a players that has rejected it once before
	public static int DUEL_CHALLENGE_INTERVAL = 60*30;
	public static boolean DUEL_ALLOW_RATED = false;

	/// EVENT OPTIONS
	public static boolean ALLOW_PLAYER_EVENT_CREATION = false;

	/// Scheduled Event Options
	public static boolean START_CONTINUOUS = false;
	public static boolean START_NEXT = false;
	public static int TIME_BETWEEN_SCHEDULED_EVENTS = 30;
	public static boolean SCHEDULER_ANNOUNCE_TIMETILLNEXT = false;

	/// Workaround for gamemode switching and teleporting
	public static boolean PLUGIN_MULTI_INV = false; /// workarounds for multiinv and tping
	public static boolean PLUGIN_MULITVERSE_INV = false;
	public static boolean PLUGIN_MULITVERSE_CORE = false;

	/// MISC OPTIONS
	public static int NUM_INV_SAVES = 5; /// number of inventory saves

	public static double TELEPORT_Y_OFFSET = 1.0; /// offset for teleportation

	public static Material READY_BLOCK = Material.IRON_BLOCK;

	/// SIGN OPTIONS
	public static final String SIGN_PREFIX = "*";

	public static boolean ITEMS_IGNORE_STACKSIZE = true;
	public static boolean ITEMS_UNSAFE_ENCHANTMENTS = false;

    /// DEBUG OPTIONS
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_TRACE = false;
	public static final boolean DEBUG_EVENTS = false;
	public static final boolean DEBUG_TEVENTS = false;
	public static final boolean DEBUG_DAMAGE = false;
	public static final boolean DEBUG_SCHEDULER = false;
	public static final boolean TESTSERVER = false;
	public static final String DEFAULT_CONFIG_NAME = "Default";

	public static boolean DEBUG_COMMANDS = false;

	public static int TIME_BETWEEN_CLASS_CHANGE = 3;

	public static boolean DEBUG_VIRTUAL = false;
	public static boolean DEBUG_TRANSITIONS = false;
	public static boolean DEBUG_STORAGE = false;
	public static boolean DEBUG_TRACKING = false;
	public static boolean DEBUG_MATCH_TEAMS = false;

	public static boolean ALLOW_ADMIN_CMDS_IN_Q_OR_MATCH = false;
	public static boolean ENABLE_TELEPORT_FIX = false;
	public static boolean ENABLE_PLAYER_READY_BLOCK = true;

	public static File DIR_SAVES;
	public static File DIR_COMPETITIONS;

}
