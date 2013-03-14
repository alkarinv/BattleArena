package mc.alk.arena.objects;

import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.victoryconditions.VictoryType;


public class EventParams extends MatchParams{
	Integer secondsTillStart = null;
	Integer announcementInterval = null;

	public EventParams(MatchParams mp) {
		super(mp);
		if (mp instanceof EventParams){
			EventParams ep = (EventParams) mp;
			this.secondsTillStart = ep.secondsTillStart;
			this.announcementInterval = ep.announcementInterval;
		}
	}

	public EventParams(ArenaType at, Rating rating, VictoryType vc) {
		super(at, rating, vc);
	}

	public Integer getSecondsTillStart() {
		return secondsTillStart;
	}

	public void setSecondsTillStart(Integer secondsTillStart) {
		this.secondsTillStart = secondsTillStart;
	}

	public Integer getAnnouncementInterval() {
		return announcementInterval;
	}

	public void setAnnouncementInterval(Integer announcementInterval) {
		this.announcementInterval = announcementInterval;
	}

	@Override
	public JoinType getJoinType() {
		return JoinType.JOINPHASE;
	}
}
