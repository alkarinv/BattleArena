package mc.alk.arena.executors;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.FormingTeam;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.ServerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TeamExecutor extends CustomCommandExecutor {
	private TeamController teamc;
    private BAExecutor bae;

	public TeamExecutor(BAExecutor bae) {
		super();
		this.teamc = BattleArena.getTeamController();
		this.bae = bae;
	}

	@MCCommand(cmds={"list"},admin=true,usage="list")
	public boolean teamList(CommandSender sender) {
		StringBuilder sb = new StringBuilder();
        Collection<ArenaTeam> teams = TeamController.getSelfFormedTeams();
        for (ArenaTeam t: teams){
			sb.append(t.getTeamInfo(null)).append("\n");}
		sb.append("&e# of players = &6").append(teams.size());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"join"}, usage="join", perm="arena.team.join")
	public boolean teamJoin(ArenaPlayer player) {

		ArenaTeam t = teamc.getSelfFormedTeam(player);
		if (t != null && t.size() >1){
			return sendMessage(player, "&cYou are already part of a team with &6" + t.getOtherNames(player));
		}

		if (!teamc.inFormingTeam(player)){
			sendMessage(player,ChatColor.RED + "You are not part of a forming team");
			return sendMessage(player,ChatColor.YELLOW + "Usage: &6/team create <player2> [player3]...");
		}
		FormingTeam ft = teamc.getFormingTeam(player);

		ft.sendJoinedPlayersMessage(ChatColor.YELLOW + player.getName() + " has joined the team");
		ft.joinTeam(player);
		sendMessage(player,ChatColor.YELLOW + "You have joined the team with");
		sendMessage(player,ChatColor.GOLD + ft.toString());

		if (ft.hasAllPlayers()){
			ft.sendMessage("&2Your team is now complete.  you can now add an event or arena");
			teamc.removeFormingTeam(ft);
			teamc.addSelfFormedTeam(ft);
		}
		return true;
	}

	@MCCommand(cmds={"create"}, usage="create <player 1> <player 2>...<player x>", perm="arena.team.create")
	public boolean teamCreate(ArenaPlayer player, String[] args) {
		if (args.length<2){
			sendMessage(player,ChatColor.YELLOW + "create <player 1> <player 2>...<player x>");
			sendMessage(player,ChatColor.YELLOW + "You need to have at least 1 person in the team");
			return true;
		}
		if (!bae.canJoin(player)){
			return true;
		}
		Set<String> players = new HashSet<String>();
		Set<Player> foundplayers = new HashSet<Player>();
		Set<String> unfoundplayers = new HashSet<String>();
        players.addAll(Arrays.asList(args).subList(1, args.length));
		if (players.contains(player.getName()))
			return sendMessage(player,ChatColor.YELLOW + "You can not invite yourself to a team");

		ServerUtil.findOnlinePlayers(players, foundplayers,unfoundplayers);
		if (foundplayers.size() < players.size()){
			sendMessage(player,ChatColor.YELLOW + "The following teammates were not found or were not online");
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String n : unfoundplayers){
				if (!first) sb.append(",");
				sb.append(n);
				first = false;
			}
			sendMessage(player,ChatColor.YELLOW + sb.toString());
			return true;
		}
		Set<ArenaPlayer> foundArenaPlayers = PlayerController.toArenaPlayerSet(foundplayers);
		for (ArenaPlayer p: foundArenaPlayers){
			if (Defaults.DEBUG){Log.info("player=" + player.getName());}
			ArenaTeam t = teamc.getSelfFormedTeam(p);
			if (t!= null || !bae.canJoin(p,false)){
				sendMessage(player,"&6"+ p.getName() + "&e is already part of a team or is in an Event");
				return sendMessage(player,"&eCreate team &4cancelled!");
			}
			if (teamc.inFormingTeam(p)){
				sendMessage(player,"&6"+ p.getName() + "&e is already part of a forming team");
				return sendMessage(player,"&eCreate team &4cancelled!");
			}
		}
		foundArenaPlayers.add(player);
		if (Defaults.DEBUG){Log.info(player.getName() + "  players=" + foundArenaPlayers.size());}

		if (!ac.hasArenaSize(foundArenaPlayers.size())){
			sendMessage(player,"&6[Warning]&eAn arena for that many players has not been created yet!");
		}
		/// Finally ready to create a team
		FormingTeam ft = new FormingTeam(player, foundArenaPlayers);
		teamc.addFormingTeam(ft);
		sendMessage(player,ChatColor.YELLOW + "You are now forming a team. The others must accept by using &6/team add");

		/// Send a message to the other teammates
		for (ArenaPlayer p: ft.getPlayers()){
			if (player.equals(p))
				continue;
			sendMessage(p, "&eYou have been invited to a team with &6" + ft.getOtherNames(player));
			sendMessage(p, "&6/team add&e : to accept: &6/team decline&e to refuse ");
		}
		return true;
	}

	@MCCommand(cmds={"info"}, usage="info")
	public boolean teamInfo(ArenaPlayer player) {
        ArenaTeam team = TeamController.getTeam(player);
        if (team == null) {
            return sendMessage(player, "&eYou are not in a team");
        }
        return sendMessage(player, team.getTeamInfo(null));
    }

	@MCCommand(cmds={"info"}, min=2, admin=true, usage="info <player>", order=1)
	public boolean teamInfoOther(CommandSender sender,ArenaPlayer player) {
        ArenaTeam team = TeamController.getTeam(player);
        if (team == null) {
            return sendMessage(sender,"&ePlayer &6" + player.getName() +"&e is not in a team");
        }
        return sendMessage(sender, team.getTeamInfo(null));
	}

	@MCCommand(cmds={"disband","leave"},usage="disband")
	public boolean teamDisband(ArenaPlayer player) {
		/// Try to disband a forming team first
		FormingTeam ft = teamc.getFormingTeam(player);
		if (ft != null){
			teamc.removeFormingTeam(ft);
			ft.sendToOtherMembers(player,"&eYour team has been disbanded by " + player.getName());
			sendMessage(player, "&2You have disbanded your team with " + ft.getName());
			return true;
		}

		/// If in a self made team, let them disband it regardless
		/// This will cause the team to try and leave the event, queue, or whatever
		ArenaTeam t = teamc.getSelfFormedTeam(player);
		if (t== null){
			return sendMessage(player,"&eYou aren't part of a team");}

		if (HeroesController.enabled()){
			HeroesController.removedFromTeam(t, player.getPlayer());}

		teamc.removeSelfFormedTeam(t);
		t.sendToOtherMembers(player,"&eYour team has been disbanded by " + player.getName());
		sendMessage(player, "&2You have disbanded your team with " + t.getName());

		return true;
	}

	@MCCommand(cmds={"delete"}, usage="delete")
	public boolean teamDelete(CommandSender sender, ArenaPlayer player) {
		ArenaTeam t = teamc.getSelfFormedTeam(player);
		if (t== null){
			return sendMessage(sender,ChatColor.YELLOW + player.getName() + " is not part of a team");}
		Event ae = EventController.insideEvent(player);
		if (ae != null){
			ae.leave(player);
		} else {
			teamc.removeSelfFormedTeam(t);
		}
		t.sendMessage(ChatColor.YELLOW + "The team has been disbanded ");
		return true;
	}


	@MCCommand(cmds={"decline"}, usage="decline")
	public boolean teamDecline(ArenaPlayer p) {
		FormingTeam t = teamc.getFormingTeam(p);
		if (t== null){
			sendMessage(p,ChatColor.YELLOW + "You are not part of a forming team");
			return true;
		}
		t.sendMessage(ChatColor.YELLOW + "The team has been disbanded as " + p.getDisplayName() +" has declined");
		teamc.removeFormingTeam(t);
		return true;
	}
}
