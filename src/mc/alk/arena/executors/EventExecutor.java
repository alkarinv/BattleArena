package mc.alk.arena.executors;

import java.util.Arrays;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.competition.events.ReservedArenaEvent;
import mc.alk.arena.controllers.BAEventController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidEventException;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.objects.options.EventOpenOptions.EventOpenOption;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.KeyValue;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;

import org.bukkit.command.CommandSender;


public class EventExecutor extends BAExecutor{
	protected final BAEventController controller;

	public EventExecutor(){
		super();
		controller = BattleArena.getBAEventController();
	}

	@Deprecated
	public EventExecutor(Event event){
		super();
		controller = BattleArena.getBAEventController();
	}

	@MCCommand(cmds={"options"},admin=true, usage="options", order=2)
	public boolean eventOptions(CommandSender sender,EventParams eventParams) {
		MatchTransitions tops = eventParams.getTransitionOptions();
		StringBuilder sb = new StringBuilder(tops.getOptionString());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"cancel"},admin=true, order=1)
	public boolean eventCancel(CommandSender sender, EventParams eventParams, Arena arena) {
		Event event = controller.getEvent(arena);
		if (event == null){
			return sendMessage(sender, "&cThere was no event in " + arena.getName());}
		return cancelEvent(sender,eventParams,event);
	}

	@MCCommand(cmds={"cancel"},admin=true, order=2)
	public boolean eventCancel(CommandSender sender, EventParams eventParams) {
		Event event = findUnique(sender, eventParams);
		if (event == null){
			return true;}
		return cancelEvent(sender,eventParams,event);
	}

	protected Event findUnique(CommandSender sender, EventParams eventParams) {
		KeyValue<Boolean,Event> result = controller.getUniqueEvent(eventParams);
		if (result.key == false){
			sendMessage(sender, "&cThere are no events open/running of this type");}
		else if (result.value == null){
			sendMessage(sender, "&cThere are multiple events ongoing, please specify the arena of the event. \n&6/"+
					eventParams.getCommand()+" ongoing &c for a list");}
		return result.value;
	}

	@MCCommand(cmds={"cancel"},admin=true, order=4)
	public boolean eventCancel(CommandSender sender, EventParams eventParams, ArenaPlayer player) {
		Event event = controller.getEvent(player);
		if (event == null){
			return sendMessage(sender, "&cThere was no event with " + player.getName() +" inside");}
		return cancelEvent(sender,eventParams,event);
	}

	public boolean cancelEvent(CommandSender sender, EventParams eventParams, Event event){
		if (!event.isRunning() && !event.isOpen()){
			return sendMessage(sender,"&eA "+event.getCommand()+" is not running");
		}
		controller.cancelEvent(event);
		return sendMessage(sender,"&eYou have canceled the &6" + event.getName());
	}

	@MCCommand(cmds={"start"},admin=true,usage="start", order=2)
	public boolean eventStart(CommandSender sender, EventParams eventParams, String[] args) {
		Event event = controller.getOpenEvent(eventParams);
		if (event == null){
			return sendMessage(sender, "&cThere are no open events right now");
		}

		final String name = event.getName();
		if (!event.isOpen()){
			sendMessage(sender,"&eYou need to open a "+name+" before starting one");
			return sendMessage(sender,"&eType &6/"+event.getCommand()+" open <params>&e : to open one");
		}
		boolean forceStart = args.length > 1 && args[1].equalsIgnoreCase("force");
		if (!forceStart && !event.hasEnoughTeams()){
			final int nteams = event.getNteams();
			final int neededTeams = event.getParams().getMinTeams();
			sendMessage(sender,"&cThe "+name+" only has &6" + nteams +" &cteams and it needs &6" +neededTeams);
			return sendMessage(sender,"&cIf you really want to start the bukkitEvent anyways. &6/"+event.getCommand()+" start force");
		}
		try {
			controller.startEvent(event);
			return sendMessage(sender,"&2You have started the &6" + name);
		} catch (Exception e) {
			sendMessage(sender,"&cError Starting the &6" + name);
			return sendMessage(sender,"&c" +e.getMessage());
		}
	}

	@MCCommand(cmds={"announce"},admin=true,usage="announce")
	public boolean eventAnnounce(CommandSender sender,String[] args) {
		return true;
	}

	@MCCommand(cmds={"info"},usage="info", order=2)
	public boolean eventInfo(CommandSender sender, EventParams eventParams){
		Event event = findUnique(sender, eventParams);
		if (event == null){
			return true;}

		if (!event.isOpen() && !event.isRunning()){
			return sendMessage(sender,"&eThere is no open "+event.getCommand()+" right now");}
		int size = event.getNteams();
		String teamOrPlayers = MessageUtil.getTeamsOrPlayers(event.getTeamSize());
		String arena =event instanceof ReservedArenaEvent? " &eArena=&5"+((ReservedArenaEvent) event).getArena().getName() : "";
		sendMessage(sender,"&eThere are currently &6" + size +"&e "+teamOrPlayers+arena);
		StringBuilder sb = new StringBuilder(event.getInfo());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"leave"},inGame=true,usage="leave", order=2)
	public boolean eventLeave(ArenaPlayer p) {
		Event event = controller.getEvent(p);
		if (event == null || !event.waitingToJoin(p) && !event.hasPlayer(p)){
			return sendMessage(p,"&eYou aren't inside the &6" + event.getName());}
		event.leave(p);
		return sendMessage(p,"&eYou have left the &6" + event.getName());
	}

	@MCCommand(cmds={"check"},usage="check", order=2)
	public boolean eventCheck(CommandSender sender, EventParams eventParams) {
		Event event = findUnique(sender, eventParams);
		if (event == null){
			return true;}

		if (!event.isOpen()){
			return sendMessage(sender,"&eThere is no open &6"+event.getCommand()+"&e right now");}
		int size = event.getNteams();
		String teamOrPlayers = MessageUtil.getTeamsOrPlayers(event.getTeamSize());
		return  sendMessage(sender,"&eThere are currently &6" + size +"&e "+teamOrPlayers+" that have joined");
	}

	@MCCommand(cmds={"join"},inGame=true,usage="join", order=2)
	public boolean eventJoin(ArenaPlayer p, EventParams eventParams, String[] args) {
		eventJoin(p, eventParams, args, false);
		return true;
	}

	public boolean eventJoin(ArenaPlayer p, EventParams eventParams, String[] args, boolean adminCommand) {

		if (!(p.hasPermission("arena."+eventParams.getCommand().toLowerCase()+".join"))){
			return sendMessage(p, "&eYou don't have permission to join a &6" + eventParams.getCommand());}
		Event event = controller.getOpenEvent(eventParams);
		/// If we allow players to start their own events
		if (event == null && Defaults.ALLOW_PLAYER_EVENT_CREATION){
			EventExecutor ee = EventController.getEventExecutor(eventParams.getName());
			if (ee == null){
				return true;}
			if (ee instanceof ReservedArenaEventExecutor){
				ReservedArenaEventExecutor exe = (ReservedArenaEventExecutor) ee;
				try {
					event = exe.openIt(eventParams, new String[]{"auto","silent"});
				} catch (Exception e) {
					sendMessage(p, "&cThe event can not be joined at this time");
					return true;
				}
			}
		}
		/// perform join checks
		if (event == null){
			return sendMessage(p, "&cThere is no event currently open");}

		if (!event.canJoin()){
			return sendMessage(p,"&eYou can't join the &6"+event.getCommand()+"&e while its "+event.getState());}

		if (!canJoin(p)){
			return true;}

		if (event.waitingToJoin(p)){
			return sendMessage(p,"&eYou have already joined the and will enter when you get matched up with a team");}

		EventParams sq = event.getParams();
		MatchTransitions tops = sq.getTransitionOptions();
		/// Perform is ready check
		if(!tops.playerReady(p)){
			String notReadyMsg = tops.getRequiredString("&eYou need the following to compete!!!\n");
			return MessageUtil.sendMessage(p,notReadyMsg);
		}
		/// Get the team
		Team t = teamc.getSelfFormedTeam(p);
		if (t==null){
			t = TeamController.createTeam(p); }
		/// Check any options specified in the join
		JoinOptions jp;
		try {
			jp = JoinOptions.parseOptions(sq,t, p, Arrays.copyOfRange(args, 1, args.length));
			t.setJoinPreferences(jp);
		} catch (InvalidOptionException e) {
			return sendMessage(p, e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			jp = null;
		}
		if (sq.getMaxTeamSize() < t.size()){
			return sendMessage(p,"&cThis Event can only support up to &6" + sq.getSize()+"&e your team has &6"+t.size());}

		/// Now that we have options and teams, recheck the team for joining
		if (!event.canJoin(t)){
			return true;}
		/// Check fee
		if (!checkFee(sq, p)){
			return true;}

		/// Finally actually join the event
		event.joining(t);
		if (sq.getSecondsTillStart() != null){
			Long time = event.getTimeTillStart();
			if (time != null)
				sendMessage(p,"&2The event will start in &6" + TimeUtil.convertMillisToString(time));
		}
		return true;
	}

	@MCCommand(cmds={"status"}, usage="status", order=2)
	public boolean eventStatus(CommandSender sender, EventParams eventParams) {
		Event event = findUnique(sender, eventParams);
		if (event == null){
			return true;}

		StringBuilder sb = new StringBuilder(event.getStatus());
		if (sender==null || sender.isOp() || sender.hasPermission(Defaults.ARENA_ADMIN)){
			for (Team t: event.getTeams()){
				sb.append("\n" + t.getTeamInfo(null)); }
		}

		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"status"}, usage="status", order=1)
	public boolean eventStatus(CommandSender sender, EventParams eventParams, Arena arena) {
		Event event = controller.getEvent(arena);
		if (event == null){
			return sendMessage(sender, "&cNo event could be found using that arena!");}
		StringBuilder sb = new StringBuilder(event.getStatus());
		if (sender==null || sender.isOp() || sender.hasPermission(Defaults.ARENA_ADMIN)){
			for (Team t: event.getTeams()){
				sb.append("\n" + t.getTeamInfo(null)); }
		}

		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"result"},usage="result", order=2)
	public boolean eventResult(CommandSender sender, EventParams eventParams) {
		Event event = findUnique(sender, eventParams);
		if (event == null){
			return true;}

		StringBuilder sb = new StringBuilder(event.getResultString());
		if (sb.length() == 0){
			return sendMessage(sender,"&eThere are no results for a previous &6" +event.getDetailedName() +"&e right now");
		}
		return sendMessage(sender,"&eResults for the &6" + event.getDetailedName() + "&e\n" + sb.toString());
	}

	public static boolean checkOpenOptions(Event event, MatchParams mp, String[] args) throws InvalidEventException {
		if (mp == null){
			throw new InvalidEventException("&cMatch params were null");
		}
		final String cmd = mp.getCommand();
		if (event.isRunning() || event.isOpen()){
			throw new InvalidEventException("&cA "+cmd+" is already &6" + event.getState());
		}
		if (args.length < 1){
			throw new InvalidEventException("&6/"+cmd+" <open|auto|server> [options]\n"+
					"&eExample &6/ "+cmd+" auto\n" +
					"&eExample &6/ "+cmd+" auto rated teamSize=1 nTeams=2+ arena=<arenaName>");
		}
		return true;
	}

	public static void openEvent(BAEventController controller, Event te, EventParams ep, EventOpenOptions eoo) throws InvalidOptionException, InvalidEventException{
		eoo.updateParams(ep);
		//		System.out.println("mp = " + mp + "   sq = " + specificparams +"   teamSize="+teamSize +"   nTeams="+nTeams);
		te.setSilent(eoo.isSilent());
		controller.addOpenEvent(te);
		if (eoo.hasOption(EventOpenOption.FORCEJOIN)){
			te.openAllPlayersEvent(ep);
		} else if (eoo.hasOption(EventOpenOption.AUTO)){
			ep.setSecondsTillStart(eoo.getSecTillStart());
			ep.setAnnouncementInterval(eoo.getInterval());
			te.autoEvent(ep, eoo.getSecTillStart(), eoo.getInterval());
		} else {
			te.openEvent(ep);
		}
	}


}
