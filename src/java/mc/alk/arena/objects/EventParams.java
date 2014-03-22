package mc.alk.arena.objects;

import mc.alk.arena.objects.arenas.ArenaType;

import java.util.ArrayList;
import java.util.List;


public class EventParams extends MatchParams{
	Integer secondsTillStart;
	Integer announcementInterval;
	List<String> openOptions;
	EventParams eparent;

	public EventParams(MatchParams mp) {
		super(mp);
	}

    @Override
    public void copy(ArenaParams ap){
        if (this == ap)
            return;
        super.copy(ap);
        if (ap instanceof EventParams){
            EventParams ep = (EventParams) ap;
            this.secondsTillStart = ep.secondsTillStart;
            this.announcementInterval = ep.announcementInterval;
            this.eparent = ep.eparent;
            if (ep.openOptions != null)
                this.openOptions = new ArrayList<String>(ep.openOptions);
        }
    }

	@Override
	public void flatten() {
		if (eparent != null){
			if (this.secondsTillStart == null) this.secondsTillStart = eparent.getSecondsTillStart();
			if (this.announcementInterval == null) this.announcementInterval = eparent.getAnnouncementInterval();
			if (this.openOptions == null) this.openOptions = eparent.getPlayerOpenOptions();
			this.eparent = null;
		}
		super.flatten();
	}

	public EventParams(ArenaType at) {
		super(at);
	}

	public Integer getSecondsTillStart() {
        return secondsTillStart ==null && eparent!=null ? eparent.getSecondsTillStart() : secondsTillStart;
    }

	public void setSecondsTillStart(Integer secondsTillStart) {
		this.secondsTillStart = secondsTillStart;
    }

	public Integer getAnnouncementInterval() {
        return announcementInterval ==null && eparent!=null ? eparent.getAnnouncementInterval() : announcementInterval;
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
        this.eparent = (parent instanceof EventParams) ? (EventParams) parent : null;
	}

}
