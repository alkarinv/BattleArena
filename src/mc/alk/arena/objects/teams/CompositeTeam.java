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
	boolean nameSet = false;

	protected CompositeTeam(ArenaPlayer ap) {
		super(ap);
		isPickupTeam = true;
	}

	protected CompositeTeam(Collection<ArenaPlayer> players) {
		super(players);
		isPickupTeam = true;
	}

	protected CompositeTeam() {
		super();
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

	@Override
	public int getPlayerIndex(ArenaPlayer p) {
		finish();
		return super.getPlayerIndex(p);
	}


	@Override
	public String getName() {
		finish();
		return name;
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		nameSet = true;
	}

	public void addTeam(Team t) {
		if (t instanceof CompositeTeam){
			CompositeTeam ct = (CompositeTeam) t;
			oldTeams.add(ct);
			oldTeams.addAll(ct.oldTeams);
			players.addAll(ct.getPlayers());
			nameSet = false;
		} else if (oldTeams.add(t)){
			nameSet = false;
			players.addAll(t.getPlayers());
		}
	}

	public boolean removeTeam(Team t) {
		if (t instanceof CompositeTeam){
			for (Team tt : ((CompositeTeam)t).getOldTeams()){
				if (oldTeams.remove(tt)){
					nameSet = false;}
			}
		}
		boolean has = oldTeams.remove(t);
		if (has){
			players.removeAll(t.getPlayers());
			nameSet = false;
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

	void finish() {
		if (!nameSet){
			nameSet = true;
			createName();
		}
	}

	public void removePlayer(ArenaPlayer p) {
		for (Team t: oldTeams){
			if (t.hasMember(p)){
				oldTeams.remove(t);
				nameSet = false;
				break;
			}
		}
	}
	public Collection<Team> getOldTeams(){
		return oldTeams;
	}
}
