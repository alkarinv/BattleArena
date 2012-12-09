package mc.alk.arena.util;

import java.util.Vector;


/**
 *
 * @author alkarin
 *
 */
public class ErrLogger {
	static final String version ="1.0.3";
	static Vector<String> msgs = new Vector<String>();

	public ErrLogger() {}
	public static Integer count = 0;
//
//	public static synchronized int log(Exception e) {
//		try {
//			Calendar cal = new GregorianCalendar();
//			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd,hh:mm:ss");
//			msgs.add(sdf.format(cal.getTime()).toString() + ","+e.getMessage()+"\n");
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//		if (saveEvery != null){
//			if (count++ % saveEvery == 0)
//				saveAll();
//		}
//		return -1;
//	}
//	public static synchronized int log(String node, Object... varArgs) {
//		try {
//			Calendar cal = new GregorianCalendar();
//			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd,hh:mm:ss");
//			StringBuilder buf = new StringBuilder();
//			Formatter form = new Formatter(buf);
//			form.format(node, varArgs);
//			msgs.add(sdf.format(cal.getTime()).toString() + "," + buf.toString() +"\n");
//			form.close();
//			return msgs.size();
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//		if (saveEvery != null){
//			if (count++ % saveEvery == 0)
//				saveAll();
//		}
//		return -1;
//	}
//
//	public static synchronized void saveAll() {
//		BufferedWriter out = null;
//		try {
//			File f = new File(BattleArena.getSelf().getDataFolder()+"/log.txt");
//			out = new BufferedWriter(new FileWriter(f,true));
//			for (String msg : msgs){
//				out.write(msg);
//			}
//			msgs.clear();
//
//		} catch (Exception e){
//			e.printStackTrace();
//		} finally{
//			if (out != null)
//				try {out.close();} catch (IOException e) {}
//		}
//	}
//
//	private static File trimFile(File f, int lineCount) {
//		File f2 = new File(BattleArena.getSelf().getDataFolder()+"/log2.txt");
//		BufferedWriter out = null;
//		BufferedReader br = null;
//		try {
//			out = new BufferedWriter(new FileWriter(f2,true));
//			br = new BufferedReader(new FileReader(f));
//			int count = 0;
//			String line;
//			while (count < maxFileSize - lineCount){
//				br.readLine();
//			}
//			while ((line = br.readLine()) != null){
//				out.write(line+"\n");
//			}
//			f2.renameTo(f);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally{
//			if (out != null)
//				try {out.close();} catch (IOException e) {}
//			if (br != null)
//				try {br.close();} catch (IOException e) {}
//		}
//		return f2;
//	}
//
//	/**
//	 * Code from
//	 * http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
//	 *
//	 * @param filename
//	 * @return
//	 * @throws IOException
//	 */
//	public static int count(String filename) throws IOException {
//		File f = new File(filename);
//		if (!f.exists())
//			return 0;
//	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
//	    try {
//	        byte[] c = new byte[1024];
//	        int count = 0;
//	        int readChars = 0;
//	        while ((readChars = is.read(c)) != -1) {
//	            for (int i = 0; i < readChars; ++i) {
//	                if (c[i] == '\n')
//	                    ++count;
//	            }
//	        }
//	        return count;
//	    } finally {
//	        try{is.close();} catch(Exception e){}
//	    }
//	}


}

