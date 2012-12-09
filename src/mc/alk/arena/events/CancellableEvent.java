package mc.alk.arena.events;

public interface CancellableEvent {
	public boolean isCancelled();
	public void setCancelled(boolean cancelled);
}
