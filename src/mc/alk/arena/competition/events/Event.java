package mc.alk.arena.competition.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.messaging.EventMessageHandler;
import mc.alk.arena.controllers.messaging.EventMessageImpl;
import mc.alk.arena.controllers.messaging.EventMessager;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.events.EventCancelEvent;
import mc.alk.arena.events.events.EventCompletedEvent;
import mc.alk.arena.events.events.EventFinishedEvent;
import mc.alk.arena.events.events.EventOpenEvent;
import mc.alk.arena.events.events.EventStartEvent;
import mc.alk.arena.events.events.EventVictoryEvent;
import mc.alk.arena.events.events.TeamJoinedEvent;
import mc.alk.arena.listeners.TransitionListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.EventState;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.queues.TeamQObject;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.TimeUtil;
import mc.alk.arena.util.Util;

import org.bukkit.entity.Player;


public abstract class Event extends Competition implements CountdownCallback, TeamHandler, TransitionListener {
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
		this.ac = BattleArena.getBAC();
		this.name = params.getName();
	}

	/**
	 * Notify Bukkit Listeners and specific listeners to this match
	 * @param BAevent event
	 */
	protected void notifyListeners(BAEvent event) {
		tmc.callListeners(event); /// Call our listeners listening to only this match
		event.callEvent(); /// Call bukkit listeners for this event
	}

	public void openEvent() {
		EventParams mp = ParamController.getEventParamCopy(eventParams.getName());
		mp.setMinTeams(2); /// TODO do I need this anymore?
		mp.setMaxTeams(2);
		mp.setMinTeamSize(1);
		mp.setMaxTeamSize(Integer.MAX_VALUE);
		try {
			openEvent(mp);
		} catch (NeverWouldJoinException e) {
			e.printStackTrace();
		}
	}

	public void openEvent(EventParams params) throws NeverWouldJoinException {
		setParamInst(params);
		teams.clear();
		joinHandler = TeamJoinFactory.createTeamJoinHandler(params, this);

		EventOpenEvent event = new EventOpenEvent(this);
		tmc.callListeners(event);
		if (event.isCancelled())
			return;

		event.callEvent();
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

	public void openAllPlayersEvent(EventParams params) throws NeverWouldJoinException {
		openEvent(params);
		TimeUtil.testClock();
		Player[] online = Util.getOnlinePlayers();
		for (Player p: online){
			Team t = TeamController.createTeam(BattleArena.toArenaPlayer(p));
			TeamQObject tqo = new TeamQObject(t,params,null);
			this.joining(tqo);
		}
		startEvent();
	}

	public void setParamInst(EventParams eventParams) {
		this.eventParams = new EventParams(eventParams);
		if (mc == null)
			mc = new EventMessager(this);
		mc.setMessageHandler(new EventMessageImpl(this));
	}

	public void removeEmptyTeams(){
		Iterator<Team> iter = teams.iterator();
		while(iter.hasNext()){
			Team t = iter.next();
			if (t.size() == 0){
				iter.remove();}
		}
	}

	public void startEvent() {
		removeEmptyTeams();
		/// TODO rebalance teams
		Set<ArenaPlayer> excludedPlayers = getExcludedPlayers();
		for (ArenaPlayer p : excludedPlayers){
			p.sendMessage(Log.colorChat(eventParams.getPrefix()+
					"&6 &5There werent enough players to create a &6" + eventParams.getMinTeamSize() +"&5 person team"));
		}
		joinHandler.deconstruct();
		joinHandler = null;
		transitionTo(EventState.RUNNING);

		notifyListeners(new EventStartEvent(this,teams));
	}

	protected void eventVictory(Collection<Team> victors, Collection<Team> losers) {
		if (victors != null)
			mc.sendEventVictory(victors, losers);
		else
			mc.sendEventDraw(losers, new HashSet<Team>());
		notifyListeners(new EventVictoryEvent(this,victors,losers));
	}

	public void stopTimer(){
		if (timer != null){
			timer.stop();
			timer = null;
		}
	}

	public void eventCompleted(){
		notifyListeners(new EventCompletedEvent(this));
		endEvent();
	}

	public void cancelEvent() {
		List<Team> newTeams = new ArrayList<Team>(teams);
		for (Team tt : newTeams){ /// for anyone in a match, cancel them
			if (!ac.cancelMatch(tt)){
				tt.sendMessage("&cEvent was cancelled");}
		}
		notifyListeners(new EventCancelEvent(this));
		mc.sendEventCancelled();
		endEvent(); /// now call the method to clean everything else up
	}

	protected void eventCancelled(){
		notifyListeners(new EventCancelEvent(this));
		mc.sendEventCancelled();
		endEvent();
	}

	protected void endEvent() {
		if (Defaults.DEBUG_TRACE) System.out.println("BAEvent::endEvent");
		stopTimer();
		transitionTo(EventState.CLOSED);
		removeAllTeams();
		teams.clear();
		notifyListeners(new EventFinishedEvent(this));
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
		if (t==null) /// they arent in this Event
			return true;
		if (t.size() == 1){
			removeTeam(t);
		} else {
			t.playerLeft(p);
		}
		return true;
	}

	public void removeAllTeams(){
		for (Team t: teams){
			TeamController.removeTeamHandler(t,this);}
		teams.clear();
	}

	@Override
	public boolean removeTeam(Team team){
		if (teams.remove(team)){
			TeamController.removeTeamHandler(team,this);
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
		mc.sendTeamJoinedEvent(team);
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
		switch(tjr.status){
		case ADDED:
			break;
		case CANT_FIT:
			mc.sendCantFitTeam(team);
			break;
		case WAITING_FOR_PLAYERS:
			mc.sendWaitingForMorePlayers(team, tjr.remaining);
			break;
		default:
			break;
		}

		return tjr;
	}

	public String getName(){
		return name;
	}

	public String getCommand(){return eventParams.getCommand();}
	public String getDetailedName() {
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

	public Team getTeam(ArenaPlayer p) {
		for (Team tt : teams){
			if (tt.hasMember(p)){
				return tt;}
		}

		return null;
	}

	public Matchup getMatchup(Team t,int round){
		if (rounds == null)
			return null;
		Round tr = rounds.get(round);
		if (tr == null)
			return null;
		for (Matchup m : tr.getMatchups()){
			for (Team team: m.getTeams()){
				for (ArenaPlayer ap: t.getPlayers()){
					if (team.hasMember(ap))
						return m;
				}
			}
		}
		return null;
	}

	protected Set<ArenaPlayer> getExcludedPlayers() {
		return joinHandler == null ? null :  joinHandler.getExcludedPlayers();
	}

	public boolean hasPlayer(ArenaPlayer p) {
		for (Team t: teams){
			if (t.hasMember(p))
				return true;
		}
		return false;
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

	@Override
	public List<Team> getTeams(){return teams;}
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
			if (this.hasEnoughTeams()){
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
	private Set<ArenaPlayer> getPlayers() {
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

	public void addTransitionListener(TransitionListener transitionListener) {
		tmc.addListener(transitionListener);
	}
	public void removeTransitionListener(TransitionListener transitionListener) {
		tmc.removeListener(transitionListener);
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
