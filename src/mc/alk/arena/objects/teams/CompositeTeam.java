package mc.alk.arena.objects.teams;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * Class that is a collection of other teams 
 * @author alkarin
 *
 */
public class CompositeTeam extends Team{
	final List<Team> oldTeams = new ArrayList<Team>();
	boolean nameSet = false;
	
	public CompositeTeam() {
		super();
		isPickupTeam = true;
	}

	protected CompositeTeam(Set<Player> tplayers) {
		super(tplayers);
		isPickupTeam=true;
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		nameSet = true;
	}

	public void addTeam(Team t) {
		if (t instanceof CompositeTeam){
			CompositeTeam ct = (CompositeTeam) t;
			oldTeams.addAll(ct.oldTeams);
			players.addAll(ct.getPlayers());
		} else if (oldTeams.add(t)){
			players.addAll(t.getPlayers());
		}
	}
	
	public boolean removeTeam(Team t) {
		boolean has = oldTeams.remove(t);
		if (has){
			players.removeAll(t.getPlayers());}
		return has;
	}

	public void finish() {
		if (!nameSet)
			createName();
	}

	public void removePlayer(Player p) {
		for (Team t: oldTeams){
			if (t.hasMember(p)){
				oldTeams.remove(t);
				break;
			}
		}
	}

}
