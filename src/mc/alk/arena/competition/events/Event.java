package mc.alk.arena.competition.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.util.AddToLeastFullTeam;
import mc.alk.arena.competition.events.util.BinPackAdd;
import mc.alk.arena.competition.events.util.TeamJoinHandler;
import mc.alk.arena.competition.events.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.TransitionMethodController;
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
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.TransitionOptions;
import mc.alk.arena.objects.Exceptions.NeverWouldJoinException;
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


public abstract class Event implements CountdownCallback, TeamHandler, TransitionListener{
	static int eventCount = 0;
	final int id = eventCount++;

	final String name;
	BattleArenaController ac;
	EventMessager mc = null;

	enum EventState{CLOSED,OPEN,RUNNING, FINISHED};
	EventState state = EventState.CLOSED;
	EventParams eventParams= null;
	Countdown timer = null; /// Timer till Event starts, think about moving this to executor, or eventcontroller

	final List<Team> teams = new ArrayList<Team>();
	final ArrayList<Round> rounds = new ArrayList<Round>();
	TeamJoinHandler joinHandler; /// Specify how teams are allocated

	final TransitionMethodController tmc = new TransitionMethodController();

	public Event(EventParams params) {
		setParamInst(params);
		/// eventParams will change when an new Event is called
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
		mp.setMinTeams(2);
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
		if (params.getMaxTeams() != ArenaParams.MAX){ /// we have a finite set of players
			joinHandler = new AddToLeastFullTeam(this);	/// lets try and add players to all players first
		} else { /// finite team size
			joinHandler = new BinPackAdd(this);
		}
		EventOpenEvent event = new EventOpenEvent(this);
		tmc.callListeners(event);
		if (event.isCancelled())
			return;

		event.callEvent();
		if (event.isCancelled())
			return;

		stopTimer();
		state = EventState.OPEN;

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
			this.joining(t);
		}
		startEvent();
	}

	public void setParamInst(EventParams eventParams) {
		this.eventParams = new EventParams(eventParams);
		if (mc == null)
			mc = new EventMessager(this);
		mc.setMessageHandler(new EventMessageImpl(this));
	}

	public void startEvent() {
		/// TODO rebalance teams
		Set<ArenaPlayer> excludedPlayers = getExcludedPlayers();
		for (ArenaPlayer p : excludedPlayers){
			p.sendMessage(Log.colorChat(eventParams.getPrefix()+"&6 &5There werent enough players to create a &6" + getTeamSize() +"&5 person team"));
		}
		joinHandler.deconstruct();
		joinHandler = null;
		state = EventState.RUNNING;
		notifyListeners(new EventStartEvent(this,teams));
	}

	protected void eventVictory(Team victor, Collection<Team> losers) {
		if (victor != null)
			mc.sendEventVictory(victor, losers);
		else 
			mc.sendEventDraw(losers);
		notifyListeners(new EventVictoryEvent(this,victor,losers));
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
		for (Team tt : teams){ /// for anyone in a match, cancel them
			ac.cancelMatch(tt);}
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

		state = EventState.CLOSED;
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

	/**
	 * Called when a player leaves minecraft.. we cant stop them so deal with it
	 */	
	public boolean leave(ArenaPlayer p) {
		Team t = getTeam(p);
		if (t==null) /// they arent in this Event
			return true;
//		if (!isRunning()){
			removeTeam(t);
//		} else {
			/// do nothing, they should be part of a match somewhere which will handle
			/// removing them
//		}
		return true;
	}

	public void removeAllTeams(){
		for (Team t: teams){
			TeamController.removeTeam(t,this);}
		teams.clear();
	}
	public void removeTeam(Team t){
		teams.remove(t);
		TeamController.removeTeam(t,this);
	}


	public void addTeam(Team t){
		TeamController.addTeamHandler(t, this);
		new TeamJoinedEvent(this,t).callEvent();
		teams.add(t);
		mc.sendTeamJoinedEvent(t);
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
	public TeamJoinResult joining(Team t){
		TeamJoinResult tjr = null;
		if (joinHandler == null)
			tjr = TeamJoinHandler.NOTOPEN;
		else 
			tjr = joinHandler.joiningTeam(t);
		switch(tjr.a){
		case ADDED:
			break;
		case CANT_FIT:
			mc.sendCantFitTeam(t);
			break;
		case WAITING_FOR_PLAYERS:
			mc.sendWaitingForMorePlayers(t, tjr.n);
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
	public EventParams getParams() {return eventParams;}

	public int getNteams() {return teams.size();}
	public int getTeamSize() {return eventParams.getSize();}

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
				if (team.equals(t))
					return m;
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
		if (eventParams != null) sb.append("&eTeam size=" + eventParams.getTeamSizeRange() );
		//		sb.append("&e Teams=&6 " + inEvent.size()+" &e. Alive Teams: &6" + aliveTeams.size());
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
				if (result == null || result.getVictor() == null){
					for (Team t: m.getTeams()){
						sb.append(t.getTeamSummary()+" "); }
					sb.append("\n");
				} else {
					sb.append(result.toPrettyString()+"\n");}
			}
		}

		return sb.toString();
	}

	public List<Team> getTeams(){return teams;}
	public boolean canLeaveTeam(ArenaPlayer p) {return canLeave(p);}
	public String getState() {return state.toString();}

	/**
	 * Broadcast to all players in the Event
	 */
	public void broadcast(String msg){for (Team t : teams){t.sendMessage(msg);}}


	public boolean intervalTick(int remaining){
		if (!isOpen())
			return false;
		if (remaining == 0){
			if (eventParams.matchesNTeams(teams.size()) && this.hasEnoughTeams()){
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

	public String toString(){
		return "[" + getName()+":"+id+"]";
	}

	public boolean waitingToJoin(ArenaPlayer p) {
		return joinHandler == null ? false : joinHandler.getExcludedPlayers().contains(p);
	}

	public boolean hasEnoughTeams() {
		int nteams = 0;
		for (Team t: teams){
			if (t.size() > 0)
				nteams++;
		}
		return nteams >= eventParams.getMinTeams();
	}

	public void addTransitionListener(TransitionListener transitionListener) {
		tmc.addListener(transitionListener);
	}
	public void removeTransitionListener(TransitionListener transitionListener) {
		tmc.removeListener(transitionListener);
	}
}
