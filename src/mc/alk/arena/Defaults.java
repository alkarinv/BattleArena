package mc.alk.arena;

public class Defaults {

	public static final String ADMIN_NODE = "arena.admin";

	/// Use auto updating
	public static boolean AUTO_UPDATE;
	public static boolean REPORT_ERRORS;

	public static double TICK_MULT = 1.0;
	public static String MONEY_STR = "Gold";
    public static final double DEFAULT_ELO = 1250.0;

    /// How long can we keep appending player names together
    /// before reverting to team 1, team 2, etc
    public static final int MAX_TEAM_NAME_APPEND = 4;

	public static final boolean DEBUG = false;
	public static final boolean DEBUG_TRACE = false;
	public static final boolean DEBUG_EVENTS = false;
	public static final boolean DEBUG_TEVENTS = false;
	public static boolean DEBUG_VIRTUAL = false;
	public static boolean DEBUG_TRANSITIONS = false;
	public static boolean DEBUG_STORAGE = false;
	public static boolean DEBUG_TRACKING = false;
	public static final boolean DEBUG_DAMAGE = false;


	public static final String ARENA_ADMIN = "arena.admin";

	/// MATCH OPTIONS
	public static int SECONDS_TILL_MATCH = 20;
	public static int SECONDS_TO_LOOT = 20;

	public static int MATCH_TIME = 2*60; /// matchEndTime
	public static int MATCH_UPDATE_INTERVAL = 30;

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

	/// Scheduled Event Options
	public static boolean START_CONTINUOUS = false;
	public static boolean START_NEXT = false;
	public static int TIME_BETWEEN_SCHEDULED_EVENTS = 30;

	/// Workaround for gamemode switching and teleporting
	public static boolean PLUGIN_MULTI_INV = false; /// workarounds for multiinv and tping
	public static boolean PLUGIN_MULITVERSE_INV = false;
	public static boolean PLUGIN_MULITVERSE_CORE = false;

	public static final String MULTI_INV_IGNORE_NODE = "multiinv.exempt";
	public static final String MULTIVERSE_INV_IGNORE_NODE = "mvinv.bypass.*";
	public static final String MULTIVERSE_CORE_IGNORE_NODE = "mv.bypass.gamemode.*";
	public static final String WORLDGUARD_BYPASS_NODE = "worldguard.region.bypass.";

	/// MISC OPTIONS
	public static int NUM_INV_SAVES = 5; /// number of inventory saves

	public static double TELEPORT_Y_OFFSET = 1.0; /// offset for teleportation

	/// SIGN OPTIONS
	public static final String SIGN_PREFIX = "*";

	public static boolean IGNORE_STACKSIZE = true;

}
