package mc.alk.arena.objects.tournament;

import java.util.HashMap;

import mc.alk.arena.objects.teams.Team;


public class TournamentTeam extends Team{
	boolean pickupTeam = false;
	private HashMap<Integer,Integer> results = new HashMap<Integer,Integer>();
	public TournamentTeam(Team t){
		super(t.getPlayers());
	}
//	public TournamentTeam(Team t) {
//		
//		this.team = t;
//	}
//	public Team getTeam() {
//		return team;
//	}
	public void setResult(int round, int result) {
		results.put(round, result);
	}
	public void setPickupTeam(boolean pickupTeam) {
		this.pickupTeam = pickupTeam;
	}
	public boolean isPickupTeam() {
		return pickupTeam;
	}
	

}
