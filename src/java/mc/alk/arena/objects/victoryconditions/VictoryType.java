package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.util.CaseInsensitiveMap;
import mc.alk.arena.util.Log;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;


public class VictoryType {
	static public LinkedHashMap<String,Class<?>> classes = new LinkedHashMap<String,Class<?>>();
	static public CaseInsensitiveMap<VictoryType> types = new CaseInsensitiveMap<VictoryType>();

	static int count =0;
	final String name;
	final Plugin ownerPlugin;
	final int id = count++;

	private VictoryType(final String name,final Plugin plugin){
		this.name = name.toUpperCase();
		this.ownerPlugin = plugin;

		if (!types.containsKey(name))
			types.put(name,this);
	}

	public static VictoryType fromString(final String type) {
        if (type.equalsIgnoreCase("none")) {return types.get("custom");}
        return type == null ? null : types.get(type);
	}

	public static VictoryType getType(VictoryCondition vc) {
		return vc == null ? null : types.get(vc.getClass().getSimpleName());
	}

	public static VictoryType getType(Class<? extends VictoryCondition> vc) {
		return vc == null ? null : types.get(vc.getSimpleName());
	}

	public static String getValidList() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (VictoryType at: types.values()){
			if (!first) sb.append(", ");
			first = false;
			sb.append(at.name);
		}
		return sb.toString();
	}
	@Override
	public String toString(){
		return name;
	}
	public String getName() {
		return name;
	}

	public static VictoryCondition createVictoryCondition(Match match) {
		VictoryType vt = match.getParams().getVictoryType();
		Class<?> vcClass = classes.get(vt.getName());
		if (vcClass == null)
			return null;
		Class<?>[] args = {Match.class};
		try {
			Constructor<?> constructor = vcClass.getConstructor(args);
			VictoryCondition newVC = (VictoryCondition) constructor.newInstance(match);
			if (newVC instanceof NLives){
				Integer nlives = match.getParams().getNLives();
				((NLives)newVC).setMaxLives(nlives==null?1: nlives);
			}
			return newVC;
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
		return null;
	}

	public static void register(Class<? extends VictoryCondition> vc, Plugin plugin) {
		final String vcName = vc.getSimpleName().toUpperCase();
		if (!classes.containsKey(vcName))
			classes.put(vcName, vc);
		if (!types.containsKey(vcName)){
			new VictoryType(vcName,plugin);}
	}

	public static boolean registered(VictoryCondition vc){
		final String vcName = vc.getClass().getSimpleName().toUpperCase();
		return classes.containsKey(vcName) && types.containsKey(vcName);
	}
	public int ordinal() {
		return id;
	}
}
