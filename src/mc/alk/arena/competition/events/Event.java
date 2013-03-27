package mc.alk.arena.competition.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinStatus;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.messaging.EventMessageHandler;
import mc.alk.arena.controllers.messaging.EventMessageImpl;
import mc.alk.arena.controllers.messaging.EventMessager;
import mc.alk.arena.events.events.EventCancelEvent;
import mc.alk.arena.events.events.EventCompletedEvent;
import mc.alk.arena.events.events.EventFinishedEvent;
import mc.alk.arena.events.events.EventOpenEvent;
import mc.alk.arena.events.events.EventResultEvent;
import mc.alk.arena.events.events.EventStartEvent;
import mc.alk.arena.events.events.TeamJoinedEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionResult;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.EventState;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.queues.TeamQObject;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.ServerUtil;
import mc.alk.arena.util.TimeUtil;

import org.bukkit.entity.Player;


public abstract class Event extends Competition implements CountdownCallback, ArenaListener {
	static int eventCount = 0;
	final int id = eventCount++;

	final String name; /// Name of this event

	BattleArenaController ac; /// The BattleArenaController for adding removing matches

	EventMessager mc = null; /// Our message handler

	EventParams eventParams= null; /// The parameters for this event

	Countdown timer = null; /// Timer till Event starts, think about moving this to executor, or eventcontroller

	final ArrayList<Round> rounds = new ArrayList<Round>(); /// The list of matchups for each round

	TeamJoinHandler joinHandler; /// Specify how teams are allocated

	EventState state = null; /// The current state of this event

	/// When did each transition occur
	final Map<EventState, Long> times = Collections.synchronizedMap(new EnumMap<EventState,Long>(EventState.class));

	/**
	 * Create our event from the specified paramaters
	 * @param params
	 */
	public Event(EventParams params) {
		transitionTo(EventState.CLOSED);
		setParamInst(params);
		this.ac = BattleArena.getBAController();
		this.name = params.getName();
	}

	public void openEvent() {
		try {
			openEvent(eventParams);
		} catch (NeverWouldJoinException e) {
			e.printStackTrace();
		}
	}

	public void openEvent(EventParams params) throws NeverWouldJoinException {
		setParamInst(params);
		teams.clear();
		joinHandler = TeamJoinFactory.createTeamJoinHandler(params, this);
		EventOpenEvent event = new EventOpenEvent(this);

		callEvent(event);
		if (event.isCancelled())
			return;

		stopTimer();
		transitionTo(EventState.OPEN);

		mc.sendEventOpenMsg();
	}

	public void autoEvent(EventParams params,int secondsTillStart,int announcementInterval) throws NeverWouldJoinException {
		openEvent(params);
		TimeUtil.testClock();
		mc.sendCountdownTillEvent(secondsTillStart);
		timer = new Countdown(BattleArena.getSelf(),secondsTillStart, announcementInterval, this);
	}

	public void addAllOnline() {
		Player[] online = ServerUtil.getOnlinePlayers();
		for (Player p: online){
			Team t = TeamController.createTeam(BattleArena.toArenaPlayer(p));
			TeamQObject tqo = new TeamQObject(t,eventParams,null);
			this.joining(tqo);
		}
	}

	public void setParamInst(EventParams eventParams) {
		if (this.eventParams != eventParams)
			this.eventParams = new EventParams(eventParams);
		if (mc == null)
			mc = new EventMessager(this);
		mc.setMessageHandler(new EventMessageImpl(this));
	}

	public void startEvent() {
		List<Team> improper = joinHandler.removeImproperTeams();
		for (Team t: improper){
			t.sendMessage("&cYour team has been excluded to having an improper team size");
		}
		/// TODO rebalance teams
		Set<ArenaPlayer> excludedPlayers = getExcludedPlayers();
		for (ArenaPlayer p : excludedPlayers){
			p.sendMessage(Log.colorChat(eventParams.getPrefix()+
					"&6 &5There werent enough players to create a &6" + eventParams.getMinTeamSize() +"&5 person team"));
		}
		transitionTo(EventState.RUNNING);

		callEvent(new EventStartEvent(this,teams));
	}

	protected void setEventResult(CompetitionResult result) {
		if (result.hasVictor()){
			mc.sendEventVictory(result.getVictors(), result.getLosers());
		} else {
			mc.sendEventDraw(result.getDrawers(), result.getLosers());
		}
		callEvent(new EventResultEvent(this,result));
	}

	public void stopTimer(){
		if (timer != null){
			timer.stop();
			timer = null;
		}
	}

	public void cancelEvent() {
		eventCancelled();
	}

	public void eventCompleted(){
		callEvent(new EventCompletedEvent(this));
		endEvent();
	}

	protected void eventCancelled(){
		stopTimer();
		List<Team> newTeams = new ArrayList<Team>(teams);
		callEvent(new EventCancelEvent(this));
		mc.sendEventCancelled(newTeams);
		endEvent();
	}

	protected void endEvent() {
		if (state == EventState.CLOSED)
			return;
		transitionTo(EventState.CLOSED);
		if (Defaults.DEBUG_TRACE) System.out.println("BAEvent::endEvent");
		stopTimer();

		removeAllTeams();
		teams.clear();
		joinHandler.deconstruct();
		joinHandler = null;
		callEvent(new EventFinishedEvent(this));
	}

	public boolean canJoin(){
		return isOpen();
	}

	public boolean canJoin(Team t){
		return isOpen();
	}

	public abstract boolean canLeave(ArenaPlayer p);

	@Override
	public EventState getState() {return state;}

	@Override
	protected void transitionTo(CompetitionState state){
		this.state = (EventState) state;
		times.put(this.state, System.currentTimeMillis());
	}

	@Override
	public Long getTime(CompetitionState state){
		return times.get(state);
	}

	/**
	 * Called when a player leaves minecraft.. we cant stop them so deal with it
	 */
	public boolean leave(ArenaPlayer p) {
		Team t = getTeam(p);
		p.removeCompetition(this);
		if (t==null) /// they arent in this Event
			return false;
		t.playerLeft(p);
		return true;
	}

	public void removeAllTeams(){
		for (Team t: teams){
			TeamController.removeTeamHandler(t,this);
			for (ArenaPlayer p: t.getPlayers()){
				p.removeCompetition(this);
			}
		}
		teams.clear();
	}

	@Override
	public boolean removeTeam(Team team){
		if (teams.remove(team)){
			TeamController.removeTeamHandler(team,this);
			for (ArenaPlayer p: team.getPlayers()){
				p.removeCompetition(this);
			}
			return true;
		}
		return false;
	}


	@Override
	public void addTeam(Team team){
		if (teams.contains(team)) /// adding a team twice is bad mmkay
			return;
		TeamController.addTeamHandler(team, this);
		new TeamJoinedEvent(this,team).callEvent();
		teams.add(team);
	}

	public void addTeam(Player p){
		ArenaPlayer ap = BattleArena.toArenaPlayer(p);
		Team t = TeamController.getTeam(ap);
		if (t == null){
			t = TeamController.createTeam(ap);
		}
		addTeam(t);
	}

	/**
	 * Called when a team wants to join
	 * @param team that is joining
	 * @return where the team ended up
	 */
	public TeamJoinResult joining(TeamQObject tqo){
		TeamJoinResult tjr = null;
		Team team = tqo.getTeam();
		if (joinHandler == null)
			tjr = TeamJoinHandler.NOTOPEN;
		else
			tjr = joinHandler.joiningTeam(tqo);
		if (tjr.status != TeamJoinStatus.CANT_FIT){
//			mc.sendEventTeamJoiningMessage(tqo.getTeam()); /// TODO
		}
		switch(tjr.status){
		case WAITING_FOR_PLAYERS:
			mc.sendWaitingForMorePlayers(team, tjr.remaining);
			/* drop down into ADDED, to add the players competition */
		case ADDED_TO_EXISTING: /* drop down into added */
		case ADDED:
			for (ArenaPlayer player: tqo.getTeam().getPlayers()){
				player.addCompetition(this);}
			break;
		case CANT_FIT:
			mc.sendCantFitTeam(team);
			break;
		default:
			break;
		}

		return tjr;
	}

	@Override
	public String getName(){
		return name;
	}

	public String getCommand(){return eventParams.getCommand();}
	public String getDisplayName() {
		return getName();
	}

	public boolean isRunning() {return state == EventState.RUNNING;}
	public boolean isOpen() {return state == EventState.OPEN;}
	public boolean isClosed() {return state == EventState.CLOSED;}
	public boolean isFinished() {return state== EventState.FINISHED;}

	@Override
	public EventParams getParams() {return eventParams;}

	public int getNTeams() {
		int size = 0;
		for (Team t: teams){
			if (t.size() > 0)
				size++;
		}
		return size;
	}

	public void setTeamJoinHandler(TeamJoinHandler tjh){
		this.joinHandler = tjh;
	}

	/**
	 * Set a Message handler to override default Event messages
	 * @param mc
	 */
	public void setMessageHandler(EventMessageHandler handler){
		this.mc.setMessageHandler(handler);
	}

	/**
	 * Return the Message Handler for this Event
	 * @return
	 */
	public EventMessageHandler getMessageHandler(){
		return mc.getMessageHandler();
	}


	public static class TeamSizeComparator implements Comparator<Team>{
		public int compare(Team arg0, Team arg1) {
			if (arg0.size() == arg1.size() ) return 0;
			return (arg0.size() < arg1.size()) ? -1 : 1;
		}
	}


	protected Set<ArenaPlayer> getExcludedPlayers() {
		return joinHandler == null ? null :  joinHandler.getExcludedPlayers();
	}

	public String getStatus() {
		StringBuilder sb = new StringBuilder();
		boolean rated = eventParams.isRated();
		sb.append((rated? "&4Rated" : "&aUnrated") +"&e "+name+". " );
		sb.append("&e(&6" + state+"&e)");
		if (eventParams != null){
			sb.append("&eTeam size=" + eventParams.getTeamSizeRange() );
			sb.append("&e Teams=&6 " + teams.size());
		}
		if (state == EventState.OPEN && joinHandler != null){
			sb.append("\n&eJoiningTeams: " + MessageUtil.joinPlayers(joinHandler.getExcludedPlayers(), ", "));
		}
		return sb.toString();
	}

	public String getInfo() {
		return TransitionOptions.getInfo(eventParams, eventParams.getName());
	}

	/**
	 * Show Results from the previous Event
	 * @return
	 */
	public String getResultString() {
		StringBuilder sb = new StringBuilder();
		if (rounds == null){
			return "&eThere are no results from the previous Event";
		}
		if (!isFinished() && !isClosed()){
			sb.append("&eEvent is still &6" + state + "\n");
		}

		//		boolean useRounds = rounds.size() > 1 || isTourney;
		boolean useRounds = rounds.size() > 1;
		for (int r = 0;r<rounds.size();r++){
			Round round = rounds.get(r);
			if (useRounds) sb.append("&5***&4 Round "+(r+1)+"&5 ***\n");
			//			boolean useMatchups = round.getMatchups().size() > 1 || isTourney;
			boolean useMatchups = round.getMatchups().size() > 1;
			for (Matchup m: round.getMatchups()){
				if (useMatchups) sb.append("&4Matchup :");
				MatchResult result = m.getResult();
				if (result == null || result.getVictors() == null){
					for (Team t: m.getTeams()){
						sb.append(t.getTeamSummary()+" "); }
					sb.append("\n");
				} else {
					sb.append(result.toPrettyString()+"\n");}
			}
		}

		return sb.toString();
	}

	public boolean canLeaveTeam(ArenaPlayer p) {return canLeave(p);}

	/**
	 * Broadcast to all players in the Event
	 */
	public void broadcast(String msg){for (Team t : teams){t.sendMessage(msg);}}

	public Long getTimeTillStart() {
		if (timer == null)
			return null;
		return timer.getTimeRemaining();
	}

	public boolean intervalTick(int remaining){
		if (!isOpen())
			return false;
		if (remaining == 0){
			if (this.hasEnough() ){
				startEvent();
			} else {
				mc.sendEventCancelledDueToLackOfPlayers(getPlayers());
				cancelEvent();
			}
		} else {
			mc.sendCountdownTillEvent(remaining);
		}
		return true;
	}

	/**
	 * Get all players in the Event
	 * if Event is open will return those players still waiting for a team as well
	 * @return
	 */
	@Override
	public Set<ArenaPlayer> getPlayers() {
		Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		for (Team t: getTeams()){
			players.addAll(t.getPlayers());}
		if (isOpen() && joinHandler != null){
			players.addAll(joinHandler.getExcludedPlayers());
		}
		return players;
	}

	public void setSilent(boolean silent) {
		mc.setSilent(silent);
	}

	@Override
	public String toString(){
		return "[" + getName()+":"+id+"]";
	}

	public boolean waitingToJoin(ArenaPlayer p) {
		return joinHandler == null ? false : joinHandler.getExcludedPlayers().contains(p);
	}

	public boolean hasEnoughTeams() {
		return getNTeams() >= eventParams.getMinTeams();
	}

	public boolean hasEnough() {
		return joinHandler != null ? joinHandler.hasEnough(true) : false;
	}

	@Override
	public void addedToTeam(Team team, Collection<ArenaPlayer> players) {/* do nothing */}

	@Override
	public void addedToTeam(Team team, ArenaPlayer player) {/* do nothing */}

	@Override
	public void removedFromTeam(Team team, Collection<ArenaPlayer> players) {/* do nothing */}

	@Override
	public void removedFromTeam(Team team, ArenaPlayer player) {/* do nothing */}

	@Override
	public int getID(){
		return id;
	}
}
