package mc.alk.arena.objects.scoreboard.base;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.MessageUtil;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;
import mc.alk.scoreboardapi.scoreboard.bukkit.BScoreboard;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ArenaBukkitScoreboard extends ArenaScoreboard{

	HashMap<ArenaTeam,STeam> teams = new HashMap<ArenaTeam,STeam>();
	final BScoreboard bboard;
	final boolean colorPlayerNames;

	public ArenaBukkitScoreboard(Match match, MatchParams params) {
		super(match, params);
		this.colorPlayerNames = Defaults.USE_COLORNAMES &&
				!params.getTransitionOptions().hasAnyOption(TransitionOption.NOTEAMNAMECOLOR);
		bboard = (BScoreboard) board;
	}

	@Override
	public ArenaObjective createObjective(String id, String criteria, String displayName) {
		return createObjective(id,criteria,displayName,SAPIDisplaySlot.SIDEBAR);
	}

	@Override
	public ArenaObjective createObjective(String id, String criteria, String displayName,
			SAPIDisplaySlot slot) {
		return createObjective(id,criteria,displayName,slot, 50);
	}

	@Override
	public ArenaObjective createObjective(String id, String criteria, String displayName,
			SAPIDisplaySlot slot, int priority) {
		ArenaObjective o = new ArenaObjective(id,criteria,displayName,slot,priority);
		addObjective(o);
		return o;
	}

	@Override
	public void addObjective(ArenaObjective objective) {
		bboard.registerNewObjective(objective);
		bboard.addAllEntries(objective);
	}

	@Override
	public void removeTeam(ArenaTeam team) {
		STeam t = teams.remove(team);
		if (t != null){
			super.removeEntry(t);
			for (SObjective o : this.getObjectives()){
				o.removeEntry(t);
				for (OfflinePlayer player: t.getPlayers()){
					o.removeEntry(player);
				}
			}
		}
	}

	@Override
	public STeam addTeam(ArenaTeam team) {
		STeam t = teams.get(team);
		if (t != null)
			return t;
		t = createTeamEntry(team.getIDString(), team.getScoreboardDisplayName());
		t.addPlayers(team.getBukkitPlayers());
		for (Player p: team.getBukkitPlayers()){
			bboard.setScoreboard(p);
		}
		if (colorPlayerNames)
			t.setPrefix(MessageUtil.colorChat(team.getTeamChatColor()+""));
		teams.put(team, t);

		for (SObjective o : this.getObjectives()){
			o.addTeam(t, 0);
			if (o.isDisplayPlayers()){
				for (ArenaPlayer player: team.getPlayers()){
					o.addEntry(player.getName(), 0);
				}
			}
		}
		return t;
	}

	private void addToTeam(STeam team, ArenaPlayer player){
		team.addPlayer(player.getPlayer());
		bboard.setScoreboard(player.getPlayer());
	}

	private void removeFromTeam(STeam team, ArenaPlayer player){
		team.removePlayer(player.getPlayer());
		bboard.removeScoreboard(player.getPlayer());
	}

	@Override
	public void addedToTeam(ArenaTeam team, ArenaPlayer player) {
		STeam t = teams.get(team);
		if (t == null){
			t = addTeam(team);}
		addToTeam(t,player);
	}

	@Override
	public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
		STeam t = teams.get(team);
		if (t == null)
			throw new IllegalStateException("Removing from a team that doesn't exist");
		removeFromTeam(t,player);
	}

	@Override
	public void leaving(ArenaTeam team, ArenaPlayer player) {
		removedFromTeam(team,player);
	}

	@Override
	public void setDead(ArenaTeam team, ArenaPlayer player) {
		removedFromTeam(team,player);
	}

	@Override
	public List<STeam> getTeams() {
		return new ArrayList<STeam>(teams.values());
	}

	@Override
	public String toString(){
		return getPrintString();
	}
}
