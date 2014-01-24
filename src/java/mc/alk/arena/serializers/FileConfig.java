package mc.alk.arena.serializers;

import java.io.File;
import java.util.List;

public interface FileConfig {
	public boolean getBoolean(String node, boolean defaultValue);
	public String getString(String node,String defaultValue);
	public int getInt(String node,int defaultValue);
	public double getDouble(String node, double defaultValue);
	public List<String> getStringList(String node);
	public void load(File file);
}
