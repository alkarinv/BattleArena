package mc.alk.arena.executors;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.events.Event;
import mc.alk.arena.events.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.command.CommandSender;


public class EventExecutor extends BAExecutor{
	protected Event event;
	public EventExecutor(){
		super();
	}
	public EventExecutor(Event event){
		super();
		setEvent(event);
	}

	public void setEvent(Event ae){
		if (event != null){
			event.cancelEvent();}
		this.event = ae;
	}

	@MCCommand(cmds={"options"},admin=true, usage="options", order=2)
	public boolean eventOptions(CommandSender sender) {
		MatchParams mp = event.getParams();
		if (mp == null){
			sendMessage(sender,"&eNo match options for this bukkitEvent yet");
			return true;
		}
		MatchTransitions tops = mp.getTransitionOptions();
		StringBuilder sb = new StringBuilder(tops.getOptionString());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"cancel"},admin=true,usage="cancel", order=2)
	public boolean eventCancel(CommandSender sender, String[] args) {
		if (!event.isRunning() && !event.isOpen()){
			return sendMessage(sender,"&eA "+event.getCommand()+" is not running");
		}
		boolean silent = args.length >1 && args[1].equalsIgnoreCase("silent");
		event.cancelEvent();
		if (!silent && !event.isSilent())
			event.getMessageHandler().sendEventCancelled();

		return sendMessage(sender,"&eYou have canceled the &6" + event.getName());		
	}

	@MCCommand(cmds={"start"},admin=true,usage="start", order=2)
	public boolean eventStart(CommandSender sender,String[] args) {
		final String name = event.getName();
		if (!event.isOpen()){
			sendMessage(sender,"&eYou need to open a "+name+" before starting one");
			return sendMessage(sender,"&eType &6/"+event.getCommand()+" open <params>&e : to open one");
		}
		boolean forceStart = args.length > 1 && args[1].equalsIgnoreCase("force");
		final int nteams = event.getNteams();
		final int neededTeams = event.getParams().getMinTeams();
		if (!forceStart && nteams < neededTeams){
			sendMessage(sender,"&cThe "+name+" only has &6" + nteams +" &cteams and it needs &6" +neededTeams);
			return sendMessage(sender,"&cIf you really want to start the bukkitEvent anyways. &6/"+event.getCommand()+" start force");
		}
		event.startEvent();
		return sendMessage(sender,"&2You have started the &6" + name);
	}

	@MCCommand(cmds={"info"},usage="info", order=2)
	public boolean eventInfo(CommandSender sender){
		if (!event.isOpen() && !event.isRunning()){
			return sendMessage(sender,"&eThere is no open "+event.getCommand()+" right now");}
		int size = event.getNteams();
		String teamOrPlayers = MessageController.getTeamsOrPlayers(event.getTeamSize());
		sendMessage(sender,"&eThere are currently &6" + size +"&e "+teamOrPlayers+" that have joined");
		StringBuilder sb = new StringBuilder(event.getInfo());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"leave"},inGame=true,usage="leave", order=2)
	public boolean eventLeave(ArenaPlayer p) {
		if (!event.waitingToJoin(p) && !event.hasPlayer(p)){
			return sendMessage(p,"&eYou aren't inside the &6" + event.getName());}
		if (!event.canLeave(p)){
			return sendMessage(p,"&eYou can't leave the &6"+event.getCommand()+"&e while its "+event.getState());}
		event.leave(p);
		return sendMessage(p,"&eYou have left the &6" + event.getName());
	}

	@MCCommand(cmds={"check"},usage="check", order=2)
	public boolean eventCheck(CommandSender sender) {
		if (!event.isOpen()){
			return sendMessage(sender,"&eThere is no open &6"+event.getCommand()+"&e right now");}
		int size = event.getNteams();
		String teamOrPlayers = MessageController.getTeamsOrPlayers(event.getTeamSize());
		return  sendMessage(sender,"&eThere are currently &6" + size +"&e "+teamOrPlayers+" that have joined");
	}

	@MCCommand(cmds={"join"},inGame=true,usage="join", order=2)
	public boolean eventJoin(ArenaPlayer p) {
		if (!(p.hasPermission("arena."+event.getCommand().toLowerCase()+".join"))){
			return sendMessage(p, "&eYou don't have permission to join a &6" + event.getCommand());}
		if (!event.canJoin()){
			return sendMessage(p,"&eYou can't join the &6"+event.getCommand()+"&e while its "+event.getState());}
		if (!canJoin(p)){
			return true;
		}
		if (event.waitingToJoin(p)){
			return sendMessage(p,"&eYou have already joined the and will enter when you get matched up with a team");			
		}

		MatchParams sq = event.getParams();
		MatchTransitions tops = sq.getTransitionOptions();
		if(!tops.playerReady(p)){
			String notReadyMsg = tops.getRequiredString("&eYou need the following to compete!!!\n"); 
			return MessageController.sendMessage(p,notReadyMsg);
		}
		Team t = teamc.getSelfTeam(p);
//		System.out.println(" t size=" + sq.getTeamSizeRange() +"   in team = " + t +"   ");
		if (t==null){
			t = TeamController.createTeam(p); }

		if (sq.getMaxTeamSize() < t.size()){
			return sendMessage(p,"&cThis bukkitEvent can only support inEvent up to &6" + sq.getSize()+"&e your team has &6"+t.size());}

		if (!event.canJoin(t)){
			return true;}

		if (!checkFee(sq, p)){
			return true;}
		
		TeamJoinResult ar = event.joining(t);
		
		switch(ar.a){
		case ADDED:
			event.getMessageHandler().sendPlayerJoinedEvent(p);
			break;
		case CANT_FIT:
			sendMessage(p,"&cThe &6" + event.getDetailedName()+"&c is full");
			break;
		case WAITING_FOR_PLAYERS:
			final int remaining = ar.n;
			sendMessage(p,"&eYou have joined the &6" + event.getDetailedName());
			sendMessage(p,"&eYou will enter the bukkitEvent when &6" +remaining+"&e more "+MessageUtil.playerOrPlayers(remaining)+
					"&e have joined to make your team");
			break;
		}
		return true;
	}

	@MCCommand(cmds={"status"}, usage="status", order=2)
	public boolean eventStatus(CommandSender sender) {
		StringBuilder sb = new StringBuilder(event.getStatus());
		if (sender==null || sender.isOp() || sender.hasPermission(Defaults.ARENA_ADMIN)){
			for (Team t: event.getTeams()){
				sb.append("\n" + t.getTeamInfo(null)); }
		}

		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"result"},usage="result", order=2)
	public boolean eventResult(CommandSender sender) {
		StringBuilder sb = new StringBuilder(event.getResultString());
		if (sb.length() == 0){
			return sendMessage(sender,"&eThere are no results for a previous &6" +event.getDetailedName() +"&e right now");			
		}
		return sendMessage(sender,"&eResults for the &6" + event.getDetailedName() + "&e\n" + sb.toString());
	}
}
