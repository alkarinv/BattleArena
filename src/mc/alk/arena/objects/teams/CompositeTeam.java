package mc.alk.arena.objects.teams;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;

/**
 * Class that is a collection of other teams
 * @author alkarin
 *
 */
public class CompositeTeam extends AbstractTeam{
	final Set<Team> oldTeams = new HashSet<Team>();

	public CompositeTeam() {
		super();
		isPickupTeam = true;
	}

	protected CompositeTeam(ArenaPlayer ap) {
		super(ap);
		isPickupTeam = true;
	}

	protected CompositeTeam(Collection<ArenaPlayer> players) {
		super(players);
		isPickupTeam = true;
	}

	protected CompositeTeam(Team team) {
		this();
		addTeam(team);
	}

	protected CompositeTeam(Set<ArenaPlayer> tplayers) {
		super(tplayers);
		isPickupTeam=true;
	}

	public void addTeam(Team t) {
		if (t instanceof CompositeTeam){
			CompositeTeam ct = (CompositeTeam) t;
			oldTeams.add(ct);
			oldTeams.addAll(ct.oldTeams);
			players.addAll(ct.getPlayers());
			nameChanged = true;
		} else if (oldTeams.add(t)){
			nameChanged = true;
			players.addAll(t.getPlayers());
		}
	}

	public boolean removeTeam(Team t) {
		if (t instanceof CompositeTeam){
			for (Team tt : ((CompositeTeam)t).getOldTeams()){
				if (oldTeams.remove(tt)){
					nameChanged = true;}
			}
		}
		boolean has = oldTeams.remove(t);
		if (has){
			players.removeAll(t.getPlayers());
			nameChanged = true;
		}
		return has;
	}

	@Override
	public boolean hasTeam(Team team){
		for (Team t: oldTeams){
			if (t.hasTeam(team))
				return true;
		}
		return false;
	}

	public void removePlayer(ArenaPlayer p) {
		for (Team t: oldTeams){
			if (t.hasMember(p)){
				oldTeams.remove(t);
				nameChanged = true;
				break;
			}
		}
	}
	public Collection<Team> getOldTeams(){
		return oldTeams;
	}

}
