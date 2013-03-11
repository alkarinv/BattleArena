package mc.alk.arena;

public class Permissions {
	/// BattleArena permissions
	/// many of these are dynamically created and aren't included here
	/// Examples
	///		arena.<type>.join
	public static final String ADMIN_NODE = "arena.admin";
	public static final String DUEL_EXEMPT = "arena.duel.exempt";
	public static final String TELEPORT_BYPASS_PERM = "arena.teleport.bypass";

	/// Permissions for other plugins
	public static final String MULTI_INV_IGNORE_NODE = "multiinv.exempt";
	public static final String MULTIVERSE_INV_IGNORE_NODE = "mvinv.bypass.*";
	public static final String MULTIVERSE_CORE_IGNORE_NODE = "mv.bypass.gamemode.*";
	public static final String WORLDGUARD_BYPASS_NODE = "worldguard.region.bypass.";

}
