package mc.alk.arena.objects;


public class ContainerState {
	public static final ContainerState OPEN = new ContainerState(AreaContainerState.OPEN);

	public static final ContainerState CLOSED = new ContainerState(AreaContainerState.CLOSED);

	public enum AreaContainerState{
		CLOSED, OPEN;
	}

	final AreaContainerState state;
	String msg;

	public ContainerState(AreaContainerState state){
		this.state = state;
	}

	public ContainerState(AreaContainerState state, String msg){
		this.state = state;
		this.msg = msg;
	}

	public AreaContainerState getState(){
		return state;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public static ContainerState toState(AreaContainerState state) {
		switch(state){
		case CLOSED: return ContainerState.CLOSED;
		case OPEN: return ContainerState.OPEN;
		default: return null;
		}
	}

	public boolean isOpen() {
		return state == AreaContainerState.OPEN;
	}

	public boolean isClosed() {
		return state == AreaContainerState.CLOSED;
	}

}
