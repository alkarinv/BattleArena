package mc.alk.arena.events;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.EventMessageHandler;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.events.util.AddToLeastFullTeam;
import mc.alk.arena.events.util.BinPackAdd;
import mc.alk.arena.events.util.NeverWouldJoinException;
import mc.alk.arena.events.util.TeamJoinHandler;
import mc.alk.arena.events.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.listeners.MatchListener;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.TransitionOptions;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.TimeUtil;


public abstract class Event implements MatchListener, CountdownCallback, TeamHandler{
	static int eventCount = 0;
	final int id = eventCount++;

	final String name;
	final String prefix;
	final String command;
	BattleArenaController ac;
	EventMessageHandler mc = null;

	enum EventState{CLOSED,OPEN,RUNNING, FINISHED};
	EventState state = EventState.CLOSED;
	MatchParams matchParams= null;
	Countdown timer = null; /// Timer till Event starts, think about moving this to executor, or eventcontroller

	Set<Team> teams = new HashSet<Team>();
	ArrayList<Round> rounds = new ArrayList<Round>();
	TeamJoinHandler joinHandler; /// Specify out teams are allocated
	boolean silent = false;

	public Event(MatchParams params) {
		setParamInst(params);
		 /// matchParams will change when an new Event is called
		this.ac = BattleArena.getBAC();
		this.prefix = params.getPrefix();
		this.command = params.getCommand();
		this.name = params.getName();
	}

	public void openEvent(MatchParams params) throws NeverWouldJoinException {
		setParamInst(params);
		if (params.getMaxTeams() != ArenaParams.MAX){ /// we have a finite set of players
			joinHandler = new AddToLeastFullTeam(this);	/// lets try and add players to all players first
		} else { /// finite team size
			joinHandler = new BinPackAdd(this);
		}
		ac.addMatchListener(this);
		stopTimer();
		teams.clear();
		state = EventState.OPEN;
		if (!silent)
			mc.sendEventOpenMsg();
	}

	public void autoEvent(MatchParams params,int secondsTillStart,int announcementInterval) throws NeverWouldJoinException {
		openEvent(params);
		TimeUtil.testClock();
		if (!silent)
			mc.sendCountdownTillEvent(secondsTillStart);
	
		timer = new Countdown(BattleArena.getSelf(),secondsTillStart, announcementInterval, this);
	}

	public void setParamInst(MatchParams matchParams) {
		this.matchParams = new MatchParams(matchParams);
		mc = new MessageController(this);
	}

	public void startEvent() {
		Set<ArenaPlayer> excludedPlayers = getExcludedPlayers();
		for (ArenaPlayer p : excludedPlayers){
			p.sendMessage(Log.colorChat(prefix+"&6 &5There werent enough players to create a &6" + getTeamSize() +"&5 person team"));
		}
		joinHandler.deconstruct();
		joinHandler = null;
		state = EventState.RUNNING;
	}

	public abstract void matchCancelled(Match am);

	public abstract void matchComplete(Match am);

	public void stopTimer(){
		if (timer != null){
			timer.stop();
			timer = null;
		}		
	}

	public void cancelEvent() {
		ac.removeMatchListener(this); /// we no longer want events from ourself, we are dealing with it
		for (Team tt : teams){ /// for anyone in a match, cancel them
			ac.cancelMatch(tt);}
		endEvent(); /// now call the method to clean everything else up
	}

	protected void endEvent() {
		if (Defaults.DEBUG_TRACE) System.out.println("ArenaEvent::endEvent");
		stopTimer();

		ac.removeMatchListener(this);

		state = EventState.FINISHED;
		removeAllTeams();
		teams.clear();
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
		if (!isRunning()){
			removeTeam(t);
			return true;
		} else {
			return false; /// we wont let them go.. they are part of this Event
		}
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
		teams.add(t);
	}

	/**
	 * Called when a team wants to join
	 * @param team that is joining
	 * @return where the team ended up
	 */
	public TeamJoinResult joining(Team t){
		if (joinHandler == null)
			return TeamJoinHandler.NOTOPEN;
		return joinHandler.joiningTeam(t);
	}

	public String getName(){
		return name;
	}

	public String getCommand(){return command;}
	public String getDetailedName() {
		return getName();
	}

	public boolean isRunning() {return state == EventState.RUNNING;}
	public boolean isOpen() {return state == EventState.OPEN;}
	public boolean isClosed() {return state == EventState.CLOSED;}
	public boolean isFinished() {return state== EventState.FINISHED;}
	public MatchParams getParams() {return matchParams;}

	public int getNteams() {return teams.size();}
	public int getTeamSize() {return matchParams.getSize();}

	public void setTeamJoinHandler(TeamJoinHandler tjh){
		this.joinHandler = tjh;
	}
	
	/**
	 * Set a Message handler to override default Event messages
	 * @param mc
	 */
	public void setMessageHandler(EventMessageHandler mc){
		this.mc = mc;
	}

	/**
	 * Return the Message Handler for this Event
	 * @return
	 */
	public EventMessageHandler getMessageHandler(){
		return mc;
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
		boolean rated = matchParams.isRated();
		sb.append((rated? "&4Rated" : "&aUnrated") +"&e "+name+". " );
		sb.append("&e(&6" + state+"&e)");
		if (matchParams != null) sb.append("&eTeam size=" + matchParams.getMinTeamSize() );
		//		sb.append("&e Teams=&6 " + inEvent.size()+" &e. Alive Teams: &6" + aliveTeams.size());
		return sb.toString();
	}

	public String getInfo() {
		return TransitionOptions.getInfo(matchParams, matchParams.getName());
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

	public Set<Team> getTeams(){return teams;}
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
			if (matchParams.matchesNTeams(teams.size())){
				startEvent();							
			} else {
				if (!silent) mc.sendEventCancelledDueToLackOfPlayers(getPlayers());
				cancelEvent();
			}
		} else {
			if (!silent) mc.sendCountdownTillEvent(remaining);
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
		this.silent = silent;
	}
	public boolean isSilent(){
		return silent;
	}
	public String toString(){
		return "[" + getName()+":"+id+"]";
	}

	public boolean waitingToJoin(ArenaPlayer p) {
		return joinHandler == null ? false : joinHandler.getExcludedPlayers().contains(p);
	}
}
