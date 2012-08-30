package mc.alk.arena.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Vector;

import mc.alk.arena.BattleArena;



public class FileLogger {
	static final String version ="1.0.2";
	static Vector<String> msgs = new Vector<String>();

	public FileLogger() {}
	public static Integer count = 0;
	public static final Integer saveEvery = 100;
	public static final Integer maxFileSize = 10000; /// in lines
	public static final Integer reduceToSize = 20000; /// reduce to this many lines when it exceeds maxFileSize
	public static synchronized int log(String msg) {
		try {
			Calendar cal = new GregorianCalendar();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd,hh:mm:ss");
			msgs.add(sdf.format(cal.getTime()).toString() + ","+msg+"\n");
		} catch(Exception e){
			e.printStackTrace();
		}
		if (saveEvery != null){
			if (count++ % saveEvery == 0)
				saveAll();
		}
		return -1;
	}	
	public static synchronized int log(String node, Object... varArgs) {
		try {
			Calendar cal = new GregorianCalendar();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd,hh:mm:ss");
			StringBuilder buf = new StringBuilder();
			Formatter form = new Formatter(buf);
			form.format(node, varArgs);
			msgs.add(sdf.format(cal.getTime()).toString() + "," + buf.toString() +"\n");
			return msgs.size();
		} catch(Exception e){
			e.printStackTrace();
		}
		if (saveEvery != null){
			if (count++ % saveEvery == 0)
				saveAll();
		}
		return -1;
	}

	public static synchronized void saveAll() {
		try {
			File f = new File(BattleArena.getSelf().getDataFolder()+"/log.txt");
			int lineCount = count(f.getAbsolutePath());
			if (lineCount > maxFileSize){
				f = trimFile(f,lineCount);
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(f,true));
			for (String msg : msgs){
				out.write(msg);	
			}
			msgs.clear();
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static File trimFile(File f, int lineCount) {
		File f2 = new File(BattleArena.getSelf().getDataFolder()+"/log2.txt");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f2,true));
			BufferedReader br = new BufferedReader(new FileReader(f));
			int count = 0;
			String line;
			while (count < maxFileSize - lineCount){
				br.readLine();
			}
			while ((line = br.readLine()) != null){
				out.write(line+"\n");
			}
			f2.renameTo(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f2;
	}
	/**
	 * Code from 
	 * http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static int count(String filename) throws IOException {
		File f = new File(filename);
		if (!f.exists())
			return 0;
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        return count;
	    } finally {
	        is.close();
	    }
	}


}

