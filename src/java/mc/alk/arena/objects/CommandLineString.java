package mc.alk.arena.objects;



public class CommandLineString {
	private enum SenderType{ CONSOLE, PLAYER};
	String raw;
	SenderType sender;
	String command;

	public static CommandLineString parse(String line) throws IllegalArgumentException{
		try{
			CommandLineString cls = new CommandLineString();
			final int index = line.indexOf(' ');
			cls.sender = SenderType.valueOf(line.substring(0,index).toUpperCase());
			cls.command = line.substring(index).trim();
			cls.raw = line;
			return cls;
		} catch (Exception e){
			throw new IllegalArgumentException("Format for commands must be: <player or console> <commands> ... <commands>");
		}
	}
	public boolean isConsoleSender(){
		return sender == SenderType.CONSOLE;
	}
	public SenderType getSenderType() {
		return sender;
	}

	public String getCommand(String playerName){
		return command.contains("player") ? command.replaceAll("player", playerName) : command;
	}
	public String getRawCommand() {
		return raw;
	}
}
