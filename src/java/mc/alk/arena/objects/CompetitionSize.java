package mc.alk.arena.objects;

import mc.alk.arena.util.MinMax;

public interface CompetitionSize {
	public static final int MAX = Integer.MAX_VALUE;

	public void setTeamSize(int size);
	public void setTeamSizes(MinMax mm);
	public void setNTeams(MinMax mm);

	public int getMinPlayers();
	public int getMaxPlayers();

	public int getMinTeams();
	public int getMaxTeams();
	public void setMinTeams(int nteams);
	public void setMaxTeams(int nteams);

	public void setMinTeamSize(int size);
	public void setMaxTeamSize(int size);
	public int getMinTeamSize();
	public int getMaxTeamSize();

	public boolean matchesTeamSize(final CompetitionSize size);
	public boolean matchesTeamSize(int i);

	public boolean matchesNTeams(int nteams);
	public boolean matchesNTeams(CompetitionSize csize);

	public boolean intersect(CompetitionSize csize);
	public boolean intersectTeamSize(int size);
}
