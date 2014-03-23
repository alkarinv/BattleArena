package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class Log {
	private static Logger log;
    public static void setLogger(Logger log) {
        Log.log = log;
    }

    public static void info(String msg){
		if (msg == null) return;
		try{
			MessageUtil.sendMessage(Bukkit.getConsoleSender(),colorChat(msg));
		} catch (Exception e){
			if (log != null)
				log.info(colorChat(msg));
			else
				System.out.println(colorChat(msg));
		}
        NotifierUtil.notify("info", msg);
    }

	public static void warn(String msg){
		if (msg == null) return;
        try{
            MessageUtil.sendMessage(Bukkit.getConsoleSender(),colorChat(msg));
        } catch (Exception e){
            if (log != null)
                log.warning(colorChat(msg));
            else
                System.out.println(colorChat(msg));
        }
        NotifierUtil.notify("warn", msg);
	}

	public static void err(String msg){
		if (msg == null) return;
        try{
            MessageUtil.sendMessage(Bukkit.getConsoleSender(),colorChat(msg));
        } catch (Exception e){
            if (log != null)
                log.severe(colorChat(msg));
            else
                System.err.println(colorChat(msg));
        }
		NotifierUtil.notify("errors", msg);
	}

	public static String colorChat(String msg) {
		return msg.replace('&', (char) 167);
	}

	public static void debug(String msg){
		msg = MessageUtil.colorChat(msg);
		if (Defaults.DEBUG_MSGS){
			try{
				MessageUtil.sendMessage(Bukkit.getConsoleSender(),msg);
			} catch (Exception e){
				System.out.println(msg);
			}
		}
		if (NotifierUtil.hasListener("debug")){
			NotifierUtil.notify("debug", msg);}
	}

	public static void printStackTrace(Throwable e) {
		e.printStackTrace();
		if (NotifierUtil.hasListener("errors")){
			NotifierUtil.notify("errors", e);}
	}
}
