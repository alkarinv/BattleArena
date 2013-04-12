package mc.alk.arena.controllers;

import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.stats.ArenaStat;
import mc.alk.arena.objects.stats.BlankArenaStat;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.BTInterface;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class StatController {
	static boolean enabled = false;
	final MatchParams mp;
	public static final BlankArenaStat BLANK_STAT = BlankArenaStat.BLANK_STAT;

	public StatController(MatchParams mp) {
		this.mp = mp;
	}

	public static boolean enabled() {
		return enabled;
	}

	public static void setPlugin(Plugin plugin) {
		BTInterface.setTrackerPlugin(plugin);
		enabled = true;
	}

	public static boolean hasInterface(MatchParams mp) {
		if (!enabled) return false;
		return BTInterface.hasInterface(mp);
	}


	public static void resumeTracking(ArenaPlayer p) {
		if (enabled)
			BTInterface.resumeTracking(p);
	}

	public static void stopTracking(ArenaPlayer p) {
		if (enabled)
			BTInterface.stopTracking(p);
	}

	public void addRecord(Set<ArenaTeam> victors,Set<ArenaTeam> losers,
			Set<ArenaTeam> drawers, WinLossDraw wld) {
		if (!enabled)
			return;

		BTInterface.addRecord(mp, victors, losers, drawers, wld);
	}

	public ArenaStat loadRecord(ArenaTeam t) {
		if (!enabled) return BLANK_STAT;
		return BTInterface.loadRecord(mp.getDBName(),t);
	}

	public void resetStats() {
		if (!enabled) return;
		BTInterface bti = new BTInterface(mp);
		if (bti.isValid())
			 bti.resetStats();
	}

	public boolean setRating(OfflinePlayer player, int rating) {
		BTInterface bti = new BTInterface(mp);
		return bti.isValid() ? bti.setRating(player, rating) : false;
	}

	public String getRankMessage(OfflinePlayer player) {
		BTInterface bti = new BTInterface(mp);
		return bti.isValid() ? bti.getRankMessage(player) : "";
	}

	public void printTopX(CommandSender sender, int x, int minTeamSize,String headerMsg, String bodyMsg) {
		BTInterface bti = new BTInterface(mp);
		if (!bti.isValid())
			return;
		bti.printTopX(sender, x, minTeamSize, headerMsg, bodyMsg);
	}

}
