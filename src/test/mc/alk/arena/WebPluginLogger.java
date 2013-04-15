package test.mc.alk.arena;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.LogRecord;

import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class WebPluginLogger extends PluginLogger{
	final String name;
	String websiteURL = "http://battleplugins.com/grabber/error.php";

	public WebPluginLogger(JavaPlugin context) {
		super(context);
		name = context.getDescription().getName();
		try {
			Field type = context.getClass().getSuperclass().getDeclaredField("logger");
			type.setAccessible(true);
			type.set(context, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void log(LogRecord logRecord) {
		super.log(logRecord);
		weblog(logRecord);
	}

	public void weblog(LogRecord logRecord){
		new Thread(new WebSender(websiteURL,name,logRecord)).start();
	}

	static class WebSender implements Runnable{
		final String websiteURL;
		final String pluginName;
		final LogRecord logRecord;
		public WebSender(String websiteURL, String pluginName, LogRecord logRecord){
			this.websiteURL = websiteURL;
			this.pluginName = pluginName;
			this.logRecord = logRecord;
		}
		@Override
		public void run() {
			try {
				StringBuilder sb = new StringBuilder(websiteURL);
				sb.append("?");
				sb.append("server=64.237.34.226:25567&");
				sb.append("plugin="+pluginName+"&");
				sb.append("error="+encodeHex(logRecord.getMessage()));
				URL dataurl = new URL(sb.toString());
				dataurl.openConnection();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String encodeUTF(String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}

	/**
	 * Code from Apache commons-codec
	 */
	private static final char[] DIGITS = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	/**
	 * Code from Apache commons-codec
	 */
	public static char[] encodeHex(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & data[i]) >>> 4 ];
			out[j++] = DIGITS[ 0x0F & data[i] ];
		}
		return out;
	}

	/**
	 * Code modified slightly from Apache commons-codec
	 */
	public static Object encodeHex(Object object) {
		try {
			byte[] byteArray = object instanceof String ? ((String) object).getBytes() : (byte[]) object;
			return encodeHex(byteArray);
		} catch (ClassCastException e) {
			return null;
		}
	}
}
