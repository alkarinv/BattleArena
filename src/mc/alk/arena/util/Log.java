package mc.alk.arena.util;

import java.util.logging.Logger;

public class Log {
	public static final boolean debug = true;

	private static Logger log = Logger.getLogger("Arena");

	public static void info(String msg){
		if (msg == null) return;
		if (log != null)
			log.info(colorChat(msg));
		else
			System.out.println(colorChat(msg));
	}
	public static void warn(String msg){
		if (msg == null) return;
		if (log != null)
			log.warning(colorChat(msg));
		else
			System.err.println(colorChat(msg));
	}
	public static void err(String msg){
		if (msg == null) return;
		if (log != null)
			log.severe(colorChat(msg));
		else
			System.err.println(colorChat(msg));
		NotifierUtil.notify("errors", msg);
	}

	public static String colorChat(String msg) {
		return msg.replace('&', (char) 167);
	}

	public static void debug(String msg){
		System.out.println(msg);
	}

	public static void printStackTrace(Throwable e) {
		e.printStackTrace();
		NotifierUtil.notify("errors", e);
	}
}
