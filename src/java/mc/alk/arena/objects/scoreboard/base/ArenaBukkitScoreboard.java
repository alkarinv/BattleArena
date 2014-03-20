package mc.alk.arena.objects.scoreboard.base;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;
import mc.alk.scoreboardapi.scoreboard.bukkit.BScoreboard;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class ArenaBukkitScoreboard extends ArenaScoreboard{

    final HashMap<ArenaTeam,STeam> teams = new HashMap<ArenaTeam,STeam>();
    final BScoreboard bboard;
    final boolean colorPlayerNames;

    public ArenaBukkitScoreboard(String scoreboardName) {
        super(scoreboardName);
        this.colorPlayerNames = Defaults.USE_COLORNAMES;
        bboard = (BScoreboard) board;
    }

    public ArenaBukkitScoreboard(String scoreboardName, MatchParams params) {
        super(scoreboardName);
        this.colorPlayerNames = Defaults.USE_COLORNAMES &&
                (!params.getStateGraph().hasAnyOption(TransitionOption.NOTEAMNAMECOLOR));
        bboard = (BScoreboard) board;
    }

    @Deprecated
    /**
     * Use 'public ArenaBukkitScoreboard(String scoreboardName, MatchParams params)' instead
     */
    public ArenaBukkitScoreboard(Match match, MatchParams params) {
        this(match.getName(),params);
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
    public STeam removeTeam(ArenaTeam team) {
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
        return t;
    }

    @Override
    public STeam addTeam(ArenaTeam team) {
        STeam t = teams.get(team);
        if (t != null)
            return t;
        t = createTeamEntry(team.getIDString(), team.getScoreboardDisplayName());
        Set<Player> bukkitPlayers = team.getBukkitPlayers();

        t.addPlayers(bukkitPlayers);
        for (Player p: bukkitPlayers){
            bboard.setScoreboard(p);
        }
        if (colorPlayerNames)
            t.setPrefix(team.getTeamChatColor()+"");
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


    @Override
    public STeam addedToTeam(ArenaTeam team, ArenaPlayer player) {
        STeam t = teams.get(team);
        if (t == null){
            t = addTeam(team);}
        addedToTeam(t,player);
        return t;
    }

    @Override
    public void addedToTeam(STeam team, ArenaPlayer player){
        team.addPlayer(player.getPlayer());
        bboard.setScoreboard(player.getPlayer());
    }

    @Override
    public STeam removedFromTeam(ArenaTeam team, ArenaPlayer player) {
        STeam t = teams.get(team);
        if (t == null) {
            Log.err(teams.size() + "  Removing from a team that doesn't exist player=" + player.getName() + "   team=" + team + "  " + team.getId());
            return null;
        }
        removedFromTeam(t,player);
        return t;
    }

    @Override
    public void removedFromTeam(STeam team, ArenaPlayer player){
        team.removePlayer(player.getPlayer());
        bboard.removeScoreboard(player.getPlayer());
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
