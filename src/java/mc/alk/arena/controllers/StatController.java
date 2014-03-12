package mc.alk.arena.controllers;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.WinLossDraw;
import mc.alk.arena.objects.stats.ArenaStat;
import mc.alk.arena.objects.stats.BlankArenaStat;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.BTInterface;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Set;

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
        return enabled && BTInterface.hasInterface(mp);
    }

	public static void resumeTracking(ArenaPlayer p) {
		if (enabled)
			BTInterface.resumeTracking(p);
	}

	public static void stopTracking(ArenaPlayer p) {
		if (enabled)
			BTInterface.stopTracking(p);
	}

	public static void stopTrackingMessages(ArenaPlayer p) {
		if (enabled)
			BTInterface.stopTrackingMessages(p);
	}
	public static void resumeTrackingMessages(ArenaPlayer p) {
		if (enabled)
			BTInterface.resumeTrackingMessages(p);
	}

	public void addRecord(ArenaPlayer victor,ArenaPlayer loser, WinLossDraw wld) {
		if (!enabled)
			return;
		BTInterface.addRecord(mp, victor, loser, wld);
	}

	public void addRecord(Set<ArenaTeam> victors,Set<ArenaTeam> losers,
			Set<ArenaTeam> drawers, WinLossDraw wld, boolean teamRating) {
		if (!enabled)
			return;

		BTInterface.addRecord(mp, victors, losers, drawers, wld, teamRating);
	}

	public ArenaStat loadRecord(ArenaTeam t) {
		return loadRecord(mp,t);
	}

	public static ArenaStat loadRecord(MatchParams mp, ArenaTeam t) {
		if (!enabled || mp == null) return BLANK_STAT;
		return BTInterface.loadRecord(mp.getDBName(),t);
	}

	public ArenaStat loadRecord(ArenaPlayer ap) {
		return loadRecord(mp,ap);
	}

	public static ArenaStat loadRecord(MatchParams mp, ArenaPlayer ap) {
		if (!enabled) return BLANK_STAT;
		return BTInterface.loadRecord(mp.getDBName(),ap);
	}

	public void resetStats() {
		if (!enabled) return;
		BTInterface bti = new BTInterface(mp);
		if (bti.isValid())
			 bti.resetStats();
	}

	public boolean setRating(OfflinePlayer player, int rating) {
		BTInterface bti = new BTInterface(mp);
		return bti.isValid() && bti.setRating(player, rating);
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
