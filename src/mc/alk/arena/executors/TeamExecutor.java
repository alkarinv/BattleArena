package mc.alk.arena.executors;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.events.Event;
import mc.alk.arena.objects.teams.FormingTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alk.virtualPlayer.VirtualPlayers;

public class TeamExecutor extends CustomCommandExecutor {
	TeamController teamc; 
	BAExecutor bae;
	public TeamExecutor(BAExecutor bae) {
		super();
		this.teamc = BattleArena.getTC();
		this.bae = bae;
	}

	@MCCommand(cmds={"list"},admin=true,usage="list")
	public boolean teamList(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		StringBuilder sb = new StringBuilder();
		Map<Team,List<TeamHandler>> teams = teamc.getTeams();

		for (Team t: teams.keySet()){
			sb.append(t.getTeamInfo(null)+"\n");}
		sb.append("&e# of inEvent = &6" + teams.size());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"listDetailed"},op=true,usage="listDetailed")
	public boolean teamListDetails(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		StringBuilder sb = new StringBuilder();
		Map<Team,List<TeamHandler>> teams = teamc.getTeams();

		for (Team t: teams.keySet()){
			sb.append(t.getTeamInfo(null));
			sb.append(" &5Handlers:");
			if (teams.get(t) == null){
				sb.append("null");
				continue;
			}
			for (TeamHandler th: teams.get(t)){
				sb.append(th);}
			sb.append("\n");
		}
		sb.append("&e# of inEvent = &6" + teams.size());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"join"},inGame=true,usage="join")
	public boolean teamJoin(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		Player p = (Player) sender;
		Team t = teamc.getSelfTeam(p);
		if (t != null && t.size() >1){
			return sendMessage(sender, "&cYou are already part of a team with &6" + t.getOtherNames(p));			
		}
		List<TeamHandler> handlers = teamc.getHandlers(t);
		if (handlers != null && !handlers.isEmpty()){
			for (TeamHandler th: handlers){
				return sendMessage(sender, "&cYou cant join until the &6"+th+"&c ends");}
		}
		
//		Event ae = EventController.insideEvent(p);
//		if (ae != null){
//			return sendMessage(sender, "&eYou need to leave the bukkitEvent first. &6/" + ae.getCommand()+" leave");
//		}

		if (!teamc.inFormingTeam(p)){
			sendMessage(sender,ChatColor.RED + "You are not part of a forming team");
			return sendMessage(sender,ChatColor.YELLOW + "Usage: &6/team create <player2> [player3]...");
		}
		FormingTeam ft = teamc.getFormingTeam(p);

		ft.sendJoinedPlayersMessage(ChatColor.YELLOW + p.getName() + " has joined the team");
		ft.joinTeam(p);
		sendMessage(sender,ChatColor.YELLOW + "You have joined the team with");
		sendMessage(sender,ChatColor.GOLD + ft.toString());
		//		teamc.teamJoin(p);
		if (ft.hasAllPlayers()){
			ft.sendMessage("&2Your team is now complete.  you can now join an event or arena");
			teamc.removeFormingTeam(ft);
			teamc.addSelfTeam(ft);
//			TeamController.createTeam(ft.getPlayers(), teamc);
		}
		return true;
	}

	@MCCommand(cmds={"create"},inGame=true,usage="create <player 1> <player 2>...<player x>")
	public boolean teamCreate(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		Player p = (Player)sender;
		if (args.length<2){
			showTeamHelp(sender, p);
			sendMessage(sender,ChatColor.YELLOW + "You need to have at least 1 person in the team");
			return true;
		}
		if (!bae.canJoin(sender,p)){
			return true;
		}
		Set<String> players = new HashSet<String>();
		Set<Player> foundplayers = new HashSet<Player>();
		Set<String> unfoundplayers = new HashSet<String>();
		for (int i=1;i<args.length;i++){
			players.add((String)args[i]);}
		if (players.contains(p.getName()))
			return sendMessage(sender,ChatColor.YELLOW + "You can not invite yourself to a team");

		findOnlinePlayers(players, foundplayers,unfoundplayers);
		if (foundplayers.size() < players.size()){
			sendMessage(sender,ChatColor.YELLOW + "The following teammates were not found or were not online");
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String n : unfoundplayers){
				if (!first) sb.append(",");
				sb.append(n);
				first = false;
			}
			sendMessage(sender,ChatColor.YELLOW + sb.toString());
			return true;
		}
		for (Player player: foundplayers){
			if (Defaults.DEBUG){System.out.println("player=" + player.getName());}
			Team t = teamc.getSelfTeam(player);
			if (t!= null || !bae.canJoin(sender,player)){
				sendMessage(sender,"&6"+ player.getName() + "&e is already part of a team or is in an bukkitEvent");
				return sendMessage(sender,"&eCreate team &4cancelled!");
			}
			if (teamc.inFormingTeam(player)){
				sendMessage(sender,"&6"+ player.getName() + "&e is already part of a forming team");
				return sendMessage(sender,"&eCreate team &4cancelled!");
			}
		}
		foundplayers.add(p);
		if (Defaults.DEBUG){System.out.println(p.getName() + "  players=" + foundplayers.size());}

		if (!ac.hasArenaSize(foundplayers.size())){
			sendMessage(sender,"&6[Warning]&eAn arena for that many players has not been created yet!");
		}
		/// Finally ready to create a team
		FormingTeam ft = new FormingTeam(p, foundplayers);
		teamc.addFormingTeam(ft);
		sendMessage(sender,ChatColor.YELLOW + "You are now forming a team. The others must accept by using &6/team join");

		/// Send a message to the other teammates
		for (Player player: ft.getPlayers()){
			if (player.equals(p))
				continue;
			sendMessage(player, "&eYou have been invited to a team with &6" + ft.getOtherNames(player));
			sendMessage(player, "&6/team join&e : to accept: &6/team decline&e to refuse ");
		}
		return true;
	}

	@MCCommand(cmds={"info"},exact=1, inGame=true, usage="info")
	public boolean teamInfo(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		Player p = (Player) sender;
		Map<TeamHandler,Team> teams = teamc.getTeamMap(p);
		if (teams == null || teams.isEmpty()){
			return sendMessage(sender,"&eYou are not in a team");}
		final int size = teams.size();
		for (TeamHandler th : teams.keySet()){
			if (size == 1){
				return sendMessage(sender,teams.get(th).getTeamInfo(null));}
			else {
				sendMessage(sender,teams.get(th).getTeamInfo(null));
			}
		}
		return true;
	}

	@MCCommand(cmds={"info"}, min=2, admin=true, usage="info <player>", order=1)
	public boolean teamInfoOther(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		OfflinePlayer pl = Util.findPlayer((String)args[1]); 
		if (pl == null){
			sendMessage(sender,"&ePlayer &6" + args[1] +"&e not online.  checking offline player="+args[1]);}
		pl = Bukkit.getOfflinePlayer((String)args[1]); 
		Map<TeamHandler,Team> teams = teamc.getTeamMap(pl);
		if (teams == null || teams.isEmpty()){
			return sendMessage(sender,"&ePlayer &6" + pl.getName() +"&e is not in a team");}

		for (TeamHandler th : teams.keySet()){
			sendMessage(sender,th +" " + teams.get(th).getTeamInfo(null));
		}
		return true;
	}

	@MCCommand(cmds={"disband","leave"},usage="disband")
	public boolean teamDisband(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		Player p = sender instanceof Player ? (Player) sender: null;
		boolean admin = (p == null || p.hasPermission(Defaults.ADMIN_NODE));
		if (args.length < 1){
			return sendMessage(sender,"&6/team disband");}

		OfflinePlayer pl = p;
		boolean other = false;
		if (args.length > 1 && admin){
			pl = Util.findPlayer((String)args[1]);
			if (pl == null){
				sendMessage(sender,"&ePlayer &6" + args[1] +"&e not online.  checking offline player="+args[1]);}
			pl = Bukkit.getOfflinePlayer((String)args[1]); 
			other = true;
		}

		if (pl == null){
			return sendMessage(sender,"&eYou need to be in game to disband your own team");}
		FormingTeam ft = teamc.getFormingTeam(pl);
		if (ft != null){
			teamc.removeFormingTeam(ft);
			ft.sendMessage("&eYour team has been disbanded by " + pl.getName());
			if (other)
				sendMessage(sender, "&eYou have disbanded the forming team " + ft.getDisplayName());
			return true;
		}
		/// If in a self made team, let them disband it regardless
		/// This will cause the team to try and leave the bukkitEvent, queue, or whatever
		Team t = teamc.getSelfTeam(pl);
		if (t== null){
			if (other)
				return sendMessage(sender,"&6"+pl.getName() + " &eis not part of a team");
			else 
				return sendMessage(sender,"&eYou aren't part of a team");
		}

		teamc.removeSelfTeam(t);
		t.sendMessage("&eYour team has been disbanded by " + pl.getName());
		if (other){
			sendMessage(sender, "&eYou have disbanded the team " + t.getDisplayName());}

		/// Iterate over the handlers
		/// try and leave each one (hopefully they are only in 1)
		List<TeamHandler> handlers = teamc.getHandlers(t);
		if (handlers != null && !handlers.isEmpty()){
			for (TeamHandler th: handlers){
				for (Player player : t.getPlayers()){
					if (!th.canLeave(player)){
						sendMessage(player, "&cYou have disbanded your team but still cant leave the &6" + th);
					} else {
						sendMessage(player, "&2You are leaving the &6" + th);
						th.leave(p);
					}
				}				
			}
		}
//		if (ac.removeFromQue(t) != null){
//			t.sendMessage(ChatColor.YELLOW + "You have left the queue");
//		}

//		Event ae = EventController.insideEvent(pl);
//		if (ae != null){
//			if (ae.canLeave(pl)){
//				ae.leave(pl);
//				t.sendMessage("&eThe team has left the &6" + ae.getName());
//			} else {
//				return MC.sendMessage(pl, "&eYou can't leave the &6" +ae.getName()+"&e while its " + ae.getState());
//			}
//		} 
//		if (!bae.canLeave(sender, pl)){
//			return true;
//		}

		return true;
	}

	@MCCommand(cmds={"delete"},admin=true,online={1}, usage="delete")
	public boolean teamDelete(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		if (args.length < 2){
			return sendMessage(sender,"&6/team delete <playername>");}

		Player pl = (Player) args[1];
		Team t = teamc.getSelfTeam(pl);
		if (t== null){
			return sendMessage(sender,ChatColor.YELLOW + pl.getName() + " is not part of a team");}
		Event ae = EventController.insideEvent(pl);
		if (ae != null){
			ae.leave(pl);
		} else {
			teamc.removeSelfTeam(t);	
		}
		t.sendMessage(ChatColor.YELLOW + "The team has been disbanded ");
		return true;
	}


	@MCCommand(cmds={"decline"},inGame=true, usage="decline")
	public boolean teamDecline(CommandSender sender,Command cmd, String commandLabel, Object[] args) {
		Player p = (Player) sender;
		FormingTeam t = teamc.getFormingTeam(p);
		if (t== null){
			sendMessage(sender,ChatColor.YELLOW + "You are not part of a forming team");
			return true;
		} 
		t.sendMessage(ChatColor.YELLOW + "The team has been disbanded as " + p.getDisplayName() +" has declined");
		teamc.removeFormingTeam(t);
		return true;
	}



	public boolean showTeamHelp(CommandSender sender, Player p){
		sendMessage(sender,MessageController.getMessage("help","team_create"));
		sendMessage(sender,MessageController.getMessage("help","team_join"));
		sendMessage(sender,MessageController.getMessage("help","team_leave"));
		return true;
	}

	protected void findOnlinePlayers(Set<String> names, Set<Player> foundplayers,Set<String> unfoundplayers) {
		Player[] online = Bukkit.getOnlinePlayers();
		if (Defaults.DEBUG_VIRTUAL){online =  VirtualPlayers.getOnlinePlayers();}
		for (String name : names){
			Player lastPlayer = null;
			for (Player player : online) {
				String playerName = player.getName();
				if (playerName.equalsIgnoreCase(name)) {
					lastPlayer = player;
					break;
				}

				if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) { /// many names match the one given
					if (lastPlayer != null) {
						lastPlayer = null;
						break;
					}
					lastPlayer = player;
				}
			}
			if (lastPlayer != null){
				foundplayers.add(lastPlayer);
			} else{
				unfoundplayers.add(name);
			}
		}
	}

	public static boolean sendMessage(CommandSender sender, String msg){
		return MessageController.sendMessage(sender, msg);
	}

	public void showHelp(CommandSender sender, Command command){
		help(sender,command,null,null);
	}

	@MCCommand( cmds = {"help","?"})
	public void help(CommandSender sender, Command command, String label, Object[] args){
		super.help(sender, command, args);
	}

}
