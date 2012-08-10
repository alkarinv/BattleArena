package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.match.Match;
import mc.alk.arena.objects.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NDeaths extends VictoryCondition{

	final int ndeaths; /// number of deaths before teams are eliminated

	public NDeaths(Match match, Integer ndeaths) {
		super(match);
		this.ndeaths = ndeaths;
	}

	@MatchEventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		killPlayer(event.getPlayer());		
	}
	@MatchEventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		killPlayer(event.getPlayer());		
	}
	
	@Override
	public void playerLeft(Player p) {
		killPlayer(p);
	}

	protected void killPlayer(Player p){
		Team team = match.getTeam(p);
		if (team == null)
			return;
		team.killMember(p);
		playerDeath(p,team);
	}
	
	@MatchEventHandler
	public void playerDeathEvent(PlayerDeathEvent event, Player p) {
//		System.out.println("DEAD Player " + event.getEntity());
		Team team = match.getTeam(p);
		playerDeath(p,team);		
	}

	
	private void playerDeath(Player p,Team team) {
		if (match.isWon()){
			match.unregister(this);
			return;
		}
		if (!match.isStarted())
			return;
		
//		System.out.println("checkPlayerDeath Team t =" + p.getName() +"  "+ team +"  dead="+team.isDead());
//		System.out.println("IDSDKFJDKFJDKJF " +team+ "  " + team.getDeadPlayers().size() +"   " + team.getPlayers().size());
		team.addDeath(p);
		if (team.getNDeaths(p) >= ndeaths){
			team.killMember(p);}
		
		if (!team.isDead()){ /// killing the player didnt even kill the team, definitely no victory here
			return;}

		/// Killing this player killed the team
		Team leftAlive = null;
		/// Iterate over the inEvent to see if we have one team left standing
		
		for (Team t: match.getTeams()){
//			System.out.println("    checkPlayerDeath Team t =" + t +"  dead="+t.isDead());
//			System.out.println("     SDKFJDKFJDKJF " +t+ "  " + t.getDeadPlayers().size() +"   " + t.getPlayers().size());

			if (t.isDead())
				continue;
			if (leftAlive != null) /// obviously more than one team is still in the match
				return;
			leftAlive = t;				
		}
		/// One team left alive = victory
		match.setVictor(leftAlive);
	}

	@Override
	public boolean hasTimeVictory() {
		return false;
	}


}
