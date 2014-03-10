package mc.alk.arena.objects;

import mc.alk.arena.util.MinMax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArenaSize implements CompetitionSize{
	int minTeamSize = 1;
	int maxTeamSize = MAX;
	int minTeams = 2;
	int maxTeams = MAX;

	public ArenaSize(){}

	public ArenaSize(CompetitionSize size) {
		minTeamSize = size.getMinTeamSize();
		maxTeamSize = size.getMaxTeamSize();
		minTeams = size.getMinTeams();
		maxTeams = size.getMaxTeams();
	}

	@Override
	public int getMinPlayers(){
		return minTeams* minTeamSize;
	}

	@Override
	public int getMaxPlayers(){
		return (maxTeams == MAX || maxTeamSize == MAX) ? MAX : maxTeams * maxTeamSize;
	}

	@Override
	public boolean matchesTeamSize(int i) {
		return minTeamSize <= i && i <= maxTeamSize;
	}

	public boolean matches(ArenaSize size){
		return matchesTeamSize(this,size) && matchesNTeams(this,size);
	}

	public static boolean matchesTeamSize(ArenaSize size1, ArenaSize size2) {
        return size1 == null && size2 == null ||
                !(size1 == null || size2 == null) && size1.matchesTeamSize(size2);
    }
	public static boolean matchesNTeams(ArenaSize size1, ArenaSize size2) {
        return size1 == null && size2 == null ||
                !(size1 == null || size2 == null) && size1.matchesNTeams(size2);
    }

	public static boolean lower(MinMax child, MinMax parent) {
        return child == null || parent == null || child.max < parent.max;
    }

	@Override
	public void setTeamSize(int size) {
		minTeamSize = maxTeamSize = size;
	}

	@Override
	public void setTeamSizes(MinMax mm) {
		minTeamSize = mm.min;
		maxTeamSize = mm.max;
	}
	@Override
	public void setNTeams(MinMax mm) {
		minTeams = mm.min;
		maxTeams = mm.max;
	}

	@Override
	public int getMinTeams() {return minTeams;}

	@Override
	public int getMaxTeams() {return maxTeams;}

	@Override
	public void setMinTeams(int nteams) {this.minTeams = nteams;}

	@Override
	public void setMaxTeams(int nteams) {this.maxTeams = nteams;}

	@Override
	public void setMinTeamSize(int size) {minTeamSize=size;}

	@Override
	public void setMaxTeamSize(int size) {maxTeamSize=size;}

	@Override
	public int getMinTeamSize() {return minTeamSize;}

	@Override
	public int getMaxTeamSize() {return maxTeamSize;}

	@Override
	public boolean matchesNTeams(final CompetitionSize csize) {
		final int min = Math.max(csize.getMinTeams(), minTeams);
		final int max = Math.min(csize.getMaxTeams(), maxTeams);
		return min <= max;
	}

	@Override
	public boolean matchesNTeams(int nteams) {
		return minTeams<= nteams && nteams<=maxTeams;
	}

	@Override
	public boolean matchesTeamSize(final CompetitionSize csize) {
		final int min = Math.max(csize.getMinTeamSize(), minTeamSize);
		final int max = Math.min(csize.getMaxTeamSize(), maxTeamSize);
		return min <= max;
	}


	public static boolean intersect(CompetitionSize size1, CompetitionSize size2) {
		return size1.intersect(size2);
	}

	@Override
	public boolean intersect(CompetitionSize csize) {
		minTeams = Math.max(csize.getMinTeams(), minTeams);
		maxTeams = Math.min(csize.getMaxTeams(), maxTeams);
		minTeamSize = Math.max(csize.getMinTeamSize(), minTeamSize);
		maxTeamSize = Math.min(csize.getMaxTeamSize(), maxTeamSize);
		return (minTeams <= maxTeams && minTeamSize <= maxTeamSize);
	}


	public boolean intersectMax(CompetitionSize csize) {
		maxTeams = Math.min(csize.getMaxTeams(), maxTeams);
		maxTeamSize = Math.min(csize.getMaxTeamSize(), maxTeamSize);
		return (minTeams <= maxTeams && minTeamSize <= maxTeamSize);
	}

	@Override
	public boolean intersectTeamSize(int size) {
		if (minTeamSize > size || maxTeamSize < size)
			return false;
		minTeamSize = size;
		maxTeamSize = size;
		return true;
	}

	public static String toString(int size){
		return size == ArenaSize.MAX ? "infinite" : String.valueOf(size);
	}

    public static int toInt(String size) {
        return size.equalsIgnoreCase("infinite") ? MAX : Integer.valueOf(size);
    }

    public static int toInt(String size, int defValue) {
        if (size == null || size.isEmpty())
            return defValue;
        return size.equalsIgnoreCase("infinite") ? MAX : Integer.valueOf(size);
    }

    @Override
	public String toString(){
		return "["+rangeString(minTeamSize,maxTeamSize)+" <-> "+rangeString(minTeams,maxTeams)+"]";
	}


	public static String rangeString(final int min,final int max){
		if (max == MAX){ return min+"+";} /// Example: 2+
		if (min == max){ return min+"";} /// Example: 2
		return min + "-" + max; //Example 2-4
	}

	public boolean valid() {
		return minTeamSize <= maxTeamSize && minTeams <= maxTeams;
	}

	public Collection<String> getInvalidReasons() {
		List<String> reasons = new ArrayList<String>();
		if (minTeamSize <= 0) reasons.add("Min Team Size is <= 0");
		if (maxTeamSize <= 0) reasons.add("Max Team Size is <= 0");
		if (minTeamSize > maxTeamSize) reasons.add("Min Team Size is greater than Max Team Size " + minTeamSize+":"+ maxTeamSize);
		if (minTeams > maxTeams) reasons.add("Min Teams is greater than Max Teams" + minTeams+":"+ maxTeams);
		return reasons;
	}

}
