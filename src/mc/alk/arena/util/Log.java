package mc.alk.arena.util;

import java.util.logging.Logger;

public class Log {
	public static boolean debug = false;

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
		if (debug)
			System.out.println(msg);
		if (NotifierUtil.hasListener("debug")){
			NotifierUtil.notify("debug", msg);}
	}

	public static void printStackTrace(Throwable e) {
		e.printStackTrace();
		if (NotifierUtil.hasListener("errors")){
			NotifierUtil.notify("errors", e);}
	}
	public static void setDebug(Boolean on) {
		debug = on;
	}
}
