package mc.alk.arena.controllers.containers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.PerformTransition;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.listeners.competition.InArenaListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.util.CommandUtil;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public abstract class PlayerContainer implements PlayerHolder, TeamHandler{
	protected String name;
	protected String displayName;

	HashSet<String> disabledCommands = new HashSet<String>();
	final MethodController methodController = new MethodController();
	MatchParams params;

	protected Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();

	protected Map<ArenaPlayer,Location> oldLocs = new ConcurrentHashMap<ArenaPlayer,Location>();

	ArrayList<Location> spawns = new ArrayList<Location>();

	/** Our teams */
	protected List<ArenaTeam> teams = Collections.synchronizedList(new ArrayList<ArenaTeam>());

	/** our values for the team index, only used if the Team.getIndex is null*/
	final Map<ArenaTeam,Integer> teamIndexes = new ConcurrentHashMap<ArenaTeam,Integer>();
	Random r = new Random();
	public PlayerContainer(){
		methodController.addAllEvents(this);
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
	}

	public void callEvent(BAEvent event){
		event.callEvent();
		methodController.callEvent(event);
	}

	public void playerLeaving(ArenaPlayer player){
		methodController.updateEvents(MatchState.ONLEAVE, player);
	}
	protected void updateBukkitEvents(MatchState matchState,ArenaPlayer player){
		methodController.updateEvents(matchState, player);
	}

	protected void teamLeaving(ArenaTeam team){
		if (TeamController.removeTeamHandler(team, this)){
			methodController.updateEvents(MatchState.ONLEAVE, team.getPlayers());
		}
		if (team instanceof CompositeTeam){
			for (ArenaTeam t: ((CompositeTeam)team).getOldTeams()){
				TeamController.removeTeamHandler(t, this);
			}
		}
		teams.remove(team);
	}

	protected boolean teamJoining(ArenaTeam team){
		teams.add(team);
		teamIndexes.put(team, teams.size());
		players.addAll(team.getPlayers());
		return true;
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (!event.isCancelled() && InArenaListener.inQueue(event.getPlayer().getName()) &&
				CommandUtil.shouldCancel(event, disabledCommands)){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED+"You cannot use that command when you are in the "+displayName);
			if (PermissionsUtil.isAdmin(event.getPlayer())){
				MessageUtil.sendMessage(event.getPlayer(),"&cYou can set &6/bad allowAdminCommands true: &c to change");}
		}
	}

	public void setDisabledCommands(List<String> commands) {
		for (String s: commands){
			disabledCommands.add("/" + s.toLowerCase());}
	}

	protected static void doTransition(PlayerHolder match, MatchState state, ArenaPlayer player, ArenaTeam team, boolean onlyInMatch){
		if (player != null){
			PerformTransition.transition(match, state, player,team, onlyInMatch);
		} else {
			PerformTransition.transition(match, state, team, onlyInMatch);
		}
	}

	@Override
	public boolean canLeave(ArenaPlayer p) {
		return false;
	}

	@Override
	public boolean leave(ArenaPlayer p) {
		return players.remove(p);
	}

	@Override
	public void addArenaListener(ArenaListener arenaListener) {
		this.methodController.addListener(arenaListener);
	}

	@Override
	public MatchParams getParams() {
		return params;
	}

	public void setParams(MatchParams mp) {
		this.params= mp;
	}

	@Override
	public MatchState getMatchState() {
		return MatchState.INLOBBY;
	}

	@Override
	public boolean isHandled(ArenaPlayer player) {
		return players.contains(player);
	}

	@Override
	public CompetitionState getState() {
		return null;
	}

	@Override
	public int indexOf(ArenaTeam team) {
		return teamIndexes.containsKey(team) ? teamIndexes.get(team) : -1;
	}

	@Override
	public boolean checkReady(ArenaPlayer player, ArenaTeam team, TransitionOptions mo, boolean b) {
		return params.getTransitionOptions().playerReady(player, null);
	}

	@Override
	public Location getSpawn(int index, LocationType type, boolean random) {
		if (random){
			return spawns.get(r.nextInt(spawns.size()));
		} else{
			return index >= spawns.size() ? spawns.get(index % spawns.size()) : spawns.get(index);
		}
	}

	@Override
	public Location getSpawn(ArenaPlayer player, LocationType type, boolean random) {
		return oldLocs.get(player);
	}

	public List<Location> getSpawns(){
		return spawns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
