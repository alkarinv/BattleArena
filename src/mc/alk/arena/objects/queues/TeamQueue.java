package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.entity.Player;


public class TeamQueue extends LinkedList<Team>{
	private static final long serialVersionUID = 1L;
	MatchParams mp;
	
	public TeamQueue(MatchParams q){
		super();
		this.mp = new MatchParams(q);
	}

	public boolean contains(Player p){
		for (Team t: this){
			if (t.hasMember(p)) 
				return true;
		}
		return false;
	}

	public Team remove(Player p){
		for (Team t: this){
			if (t.hasMember(p)){
				this.remove(t);
				return t;
			}
		}
		return null;
	}
	public int indexOf(Player p){
		for (int i=0;i < this.size();i++){
			Team t = this.get(i);
			if (t.hasMember(p))
				return i;
		}
		return -1;
	}
	public MatchParams getMatchParams() {return mp;}
	public int getMinTeams() {
		return mp.getMinTeams();
	}
	
	public List<Team> sortBySize(){return TeamQueue.sortBySize(this);}	
	/**
	 * This is a semi stable sort, inEvent of the same size will retain their order in the queue
	 * Preference is given to larger inEvent
	 * @param tq
	 * @return
	 */
	public static List<Team> sortBySize(TeamQueue tq){
		ArrayList<Team> teams = new ArrayList<Team>(tq);
		Collections.sort(teams, new Comparator<Team>(){
			public int compare(Team arg0, Team arg1) {
				Integer size = arg0.size();
				return size.compareTo(arg1.size());
			}
		});
		return teams;
	}
}
