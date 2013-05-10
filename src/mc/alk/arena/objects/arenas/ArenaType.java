package mc.alk.arena.objects.arenas;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.util.CaseInsensitiveMap;
import mc.alk.arena.util.Log;

import org.bukkit.plugin.Plugin;


public class ArenaType implements Comparable<ArenaType>{
	static public CaseInsensitiveMap<Class<? extends Arena>> classes = new CaseInsensitiveMap<Class<? extends Arena>>();
	static public CaseInsensitiveMap<ArenaType> types = new CaseInsensitiveMap<ArenaType>();

	static int count = 0;

	final String name;
	final Plugin ownerPlugin;
	final int id = count++;
	Set<ArenaType> compatibleTypes = null;

	private ArenaType(final String name,Plugin plugin){
		this.name = name;
		this.ownerPlugin = plugin;
		if (!types.containsKey(name))
			types.put(name,this);
	}
	@Override
	public String toString(){
		return name;
	}

	public boolean matches(ArenaType arenaType) {
		if (this == arenaType) return true;
		return (compatibleTypes==null) ? false : compatibleTypes.contains(arenaType);
	}

	public Collection<String> getInvalidMatchReasons(ArenaType arenaType) {
		List<String> reasons = new ArrayList<String>();
		if (this != arenaType) reasons.add("Arena type is " + this +". You requested " + arenaType);
		return reasons;
	}

	public String toPrettyString(int min, int max) {
		if (this.name.equals("ARENA") || this.name.equals("SKIRMISH")){
			return min +"v" + max;
		} else {
			return toString();
		}
	}

	public String getCompatibleTypes(){
		if (compatibleTypes == null || compatibleTypes.isEmpty())
			return name;
		StringBuilder sb = new StringBuilder(name);
		for (ArenaType at: compatibleTypes){
			sb.append(", " +at.name);}
		return sb.toString();
	}

	public int ordinal() {
		return id;
	}

	public String getName() {
		return name;
	}

	private void addCompatibleType(ArenaType at) {
		if (compatibleTypes == null){
			compatibleTypes = new HashSet<ArenaType>();
		}
		compatibleTypes.add(at);
	}

	@Override
	public int compareTo(ArenaType arg0) {
		Integer ord = ordinal();
		return ord.compareTo(arg0.ordinal());
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		return compareTo((ArenaType)obj) == 0;
	}

	@Override
	public int hashCode(){
		return id;
	}

	public Plugin getPlugin() {
		return ownerPlugin;
	}

	public static ArenaType fromString(final String arenatype) {
		if (arenatype==null)
			return null;
		return types.get(arenatype.toUpperCase());
	}


	public static String getValidList() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ArenaType at: types.values()){
			if (!first) sb.append(", ");
			first = false;
			sb.append(at.name);
		}
		return sb.toString();
	}

	public static ArenaType register(String arenaType, Class<? extends Arena> arenaClass, Plugin plugin) {
		final String uarenaType = arenaType.toUpperCase();
		if (!classes.containsKey(uarenaType))
			classes.put(uarenaType, arenaClass);
		if (!types.containsKey(uarenaType)){
			new ArenaType(arenaType,plugin);
		}
//		MethodController.addBukkitMethods(c,c.getMethods());
		return types.get(uarenaType);
	}

	/**
	 * Create an arena from a name and parameters
	 * This will not load persistable objects, which must be done by the caller
	 * @param arenaName
	 * @param arenaParams
	 * @return
	 */
	public static Arena createArena(String arenaName, ArenaParams arenaParams) {
		ArenaType arenaType = arenaParams.getType();
		return createArena(arenaType, arenaName, arenaParams, true);
	}

	/**
	 * Create an arena from a name and parameters
	 * This will not load persistable objects, which must be done by the caller
	 * @param arenaName
	 * @param arenaParams
	 * @param init : whether we should call init directly after arena creation
	 * @return
	 */
	public static Arena createArena(String arenaName, ArenaParams arenaParams, boolean init) {
		ArenaType arenaType = arenaParams.getType();
		return createArena(arenaType, arenaName, arenaParams, true);
	}

	private static Arena createArena(ArenaType arenaType, String arenaName, ArenaParams arenaParams, boolean init){
		Class<?> arenaClass = classes.get(arenaType.name);
		if (arenaClass == null){
			Log.err("[BA Error] arenaClass " + arenaType.name +" is not found");
			return null;
		}

		Class<?>[] args = {};
		try {
			Constructor<?> constructor = arenaClass.getConstructor(args);
			Arena arena = (Arena) constructor.newInstance((Object[])args);
			arena.setName(arenaName);
			arena.setParameters(arenaParams);
			if (init)
				arena.privateInit();
			return arena;
		} catch (NoSuchMethodException e){
			Log.err("If you have custom constructors for your class you must also have a public default constructor");
			Log.err("Add the following line to your Arena Class '" + arenaClass.getSimpleName()+".java'");
			Log.err("public " + arenaClass.getSimpleName()+"(){}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void addCompatibleTypes(String type1, String type2) {
		ArenaType at1 = fromString(type1);
		ArenaType at2 = fromString(type2);
		if (at1 == null || at2==null)
			return;
		at1.addCompatibleType(at2);
		at2.addCompatibleType(at1);
	}

	public static void addAliasForType(String type, String alias) {
		type = type.toUpperCase();
		alias = alias.toUpperCase();
		if (type.equals(alias))
			return;
		ArenaType at = fromString(type);
		if (at == null)
			return;
		types.put(alias, at);
		classes.put(alias, getArenaClass(at));
		MatchParams mp = ParamController.getMatchParams(type);
		if (mp == null)
			return;
		ParamController.addAlias(alias, mp);
	}


	public static Collection<ArenaType> getTypes() {
		return new HashSet<ArenaType>(types.values());
	}

	public static Collection<ArenaType> getTypes(Plugin plugin) {
		Set<ArenaType> result = new HashSet<ArenaType>();
		for (ArenaType type: types.values()){
			if (type.getPlugin().equals(plugin)){
				result.add(type);
			}
		}
		return result;
	}

	public static Class<? extends Arena> getArenaClass(ArenaType arenaType){
		return getArenaClass(arenaType.getName());
	}

	public static Class<? extends Arena> getArenaClass(String arenaType){
		return classes.get(arenaType);
	}

	public static boolean contains(String arenaType) {
		return types.containsKey(arenaType);
	}
	public static boolean isSame(String checkType, ArenaType arenaType) {
		ArenaType at = types.get(checkType);
		return at == null ? false : at.equals(arenaType);
	}
}
