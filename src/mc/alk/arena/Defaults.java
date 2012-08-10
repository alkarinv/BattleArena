package mc.alk.arena;

public class Defaults {

	public static final String DEFAULT_MESSAGES_FILE = "/default_files/messages.yml";
	public static final String MESSAGES_FILE = "plugins/BattleArena/messages.yml";
	public static final String ADMIN_NODE = "arena.admin";
	
	public static double TICK_MULT = 1.0;
	public static String MONEY_STR = "bc";
    public static final Double DEFAULT_ELO = 1250.0;

    /// How long can we keep appending player names together
    /// before reverting to team 1, team 2, etc
    public static final int MAX_TEAM_NAME_APPEND = 4;
 
	public static boolean DEBUG_VIRTUAL = false;
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_TRACE = false;
	public static final boolean DEBUG_EVENTS = false;

	public static boolean PLUGIN_MULTI_INV = false; /// workarounds for multiinv and tping
	public static final String MULTI_INV_IGNORE_NODE = "multiinv.exempt";
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
	
}
