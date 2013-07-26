package mc.alk.arena.objects.messaging;



public interface Channel {
	public void broadcast(String msg);
	public static final Channel NullChannel = new NullChannel();
	public static final Channel ServerChannel = new ServerChannel();

	public class NullChannel implements Channel {
		private NullChannel(){}
		@Override
		public void broadcast(String msg) {
			/** Literally do nothing */
		}
		@Override
		public String toString(){
			return "[NullChannel]";
		}
	}
}
