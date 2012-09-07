package mc.alk.arena.events;

public class CancellableEvent extends BAEvent{
	/** Cancel the event */
	boolean cancelled = false;

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
