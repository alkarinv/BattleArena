package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.objects.arenas.ArenaType;


public class EventParams extends MatchParams{
	Integer secondsTillStart = null;
	Integer announcementInterval = null;
	List<String> openOptions = null;
	EventParams eparent = null;

	public EventParams(MatchParams mp) {
		super(mp);
		if (mp instanceof EventParams){
			EventParams ep = (EventParams) mp;
			this.secondsTillStart = ep.secondsTillStart;
			this.announcementInterval = ep.announcementInterval;
			if (ep.openOptions != null)
				this.openOptions = new ArrayList<String>(ep.openOptions);
			this.eparent = ep.eparent;
		}
	}

	public EventParams(ArenaType at) {
		super(at);
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

	public void setPlayerOpenOptions(List<String> playerOpenOptions){
		this.openOptions = playerOpenOptions;
	}
	public List<String> getPlayerOpenOptions(){
		return openOptions != null ? openOptions :
			(eparent != null ? eparent.getPlayerOpenOptions() : null);
	}
	@Override
	public void setParent(ArenaParams parent) {
		super.setParent(parent);
		if (parent instanceof EventParams){
			this.eparent = (EventParams) parent;}
		else
			this.eparent = null;

	}
}
