package mc.alk.arena.objects.arenas;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.objects.YamlSerializable;
import mc.alk.arena.serializers.Persist;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class Persistable {
	
	public static class NotPersistableException extends Exception{
		private static final long serialVersionUID = 1L;
		public NotPersistableException(String msg){
			super(msg);
		}
	}

	public static void yamlToObjects(Arena arena, ConfigurationSection cs){
		if (cs == null)
			return;
		yamlToObjects(arena, arena.getClass(),cs);
//		System.out.println("aname = " + arena.getName() +"   " + arena.getRegion());
	}
	
	private static void yamlToObjects(Arena arena, Class<?> arenaClass, ConfigurationSection cs){		
		for(Field field : arenaClass.getDeclaredFields()){
			Class<?> type = field.getType();
			String name = field.getName();
			Annotation[] annotations = field.getDeclaredAnnotations();
			//			AccessibleObject[] accessibleObjects = new AccessibleObject[1]; 
			for (Annotation a : annotations){
				if (!(a instanceof Persist || !cs.contains(name))){
					continue;
				}
//				if (type == String.class)
//					System.out.println("Type = " + type +"  " + name +"   " + annotations + "   " + cs.getString(name));
				
				//				Persist p = (Persist) a;
				//				accessibleObjects[0] = field;
				field.setAccessible(true);
				//				java.lang.reflect.AccessibleObject.setAccessible(accessibleObjects, true);
				//				System.out.println("Persist object " + p );
				try {
					Object obj = null;
					if (type == int.class){
						field.setInt(arena, cs.getInt(name));
					} else if (type == float.class){
						field.setFloat(arena, (float)cs.getDouble(name));
					} else if (type == double.class){
						field.setDouble(arena, cs.getDouble(name));
					} else if (type == long.class){
						field.setLong(arena, cs.getLong(name));
					} else if (type == boolean.class){
						field.setBoolean(arena, cs.getBoolean(name));
					} else if (type == short.class){
						field.setShort(arena, (short)cs.getInt(name));
					} else if (type == byte.class){
						field.setByte(arena, (byte)cs.getInt(name));
					} else if (type == char.class){
						String str = cs.getString(name);
						if (str != null && !str.isEmpty())
							field.setChar(arena, str.charAt(0));
					} else if (type == Integer.class){
						obj = cs.getInt(name);
					} else if (type == Float.class){
						Double d= cs.getDouble(name);
						if (d != null)
							obj = new Float(d);
					} else if (type == Double.class){
						obj = cs.getDouble(name);
					} else if (type == Character.class){
						String str = cs.getString(name);
						if (str != null && !str.isEmpty())
							obj = str.charAt(0);
					} else if (type == Byte.class){
						Integer i= cs.getInt(name);
						if (i != null)
							obj = new Byte(i.byteValue());
					} else if (type == Short.class){
						Integer i= cs.getInt(name);
						if (i != null)
							obj = new Short(i.shortValue());
					} else if (type == Long.class){
						obj = cs.getLong(name);
					} else if (type == Boolean.class){
						obj = new Boolean( cs.getBoolean(name));
					} else if (type == String.class){
						field.set(arena, cs.getString(name));
					} else if (type == Location.class){
						String locstr = cs.getString(name);
						Location loc = SerializerUtil.getLocation(locstr);
						field.set(arena, loc);
					} else if (type == ItemStack.class){
						String str = cs.getString(name);
						ItemStack is = null;
						if (str != null)
							is = InventoryUtil.parseItem(str);
						if (is != null)
							field.set(arena, is);
					} else if (List.class.isAssignableFrom(type)){
						ParameterizedType pt = (ParameterizedType) field.getGenericType();  
						List<?> list = cs.getList(name);
						if (list == null)
							continue;
						Type genType = pt.getActualTypeArguments()[0];
						List<Object> newList = new ArrayList<Object>();
						for (Object o : list){
							newList.add(yamlToObj(o,genType, cs));
						}
//						for (Object o: newList){
//							System.out.println("!!!!!!!!! new list " + o);
//						}
						obj = newList;
					} else if (Set.class.isAssignableFrom(type)){
						ParameterizedType pt = (ParameterizedType) field.getGenericType();  
						List<?> list = cs.getList(name);
						if (list == null)
							continue;
						Type genType = pt.getActualTypeArguments()[0];
						Set<Object> newSet = new HashSet<Object>();
						for (Object o : list){
							newSet.add(yamlToObj(o,genType,cs));
						}
//						for (Object o: newSet){
//							System.out.println("!!!!!!!!! new set " + o);
//						}
						obj = newSet;
					} else if (Map.class.isAssignableFrom(type)){
						ParameterizedType pt = (ParameterizedType) field.getGenericType();  
						ConfigurationSection mapcs = cs.getConfigurationSection(name);
						if (mapcs == null)
							continue;
						Set<String> keyset = mapcs.getKeys(false);
						Type keyType = pt.getActualTypeArguments()[0];
						Type mapType = pt.getActualTypeArguments()[1];
						Map<Object,Object> newMap = new HashMap<Object,Object>();
						for (String key : keyset){
							Object k = yamlToObj(key,keyType,cs);
							Object v = yamlToObj(mapcs.get(key), mapType,cs);
							if (k != null && v != null)
								newMap.put(k,v);
						}
						obj = newMap;
					} else if (YamlSerializable.class.isAssignableFrom(type)){
						obj = createYamlSerializable(type,cs.getConfigurationSection(name));
					} else {
						throw new NotPersistableException("Type " + type +" is not persistable. Not loading values for "+name);
					}
					if (obj != null)
						field.set(arena, obj);
				} catch (NotPersistableException e) {
					System.err.println(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Class<?> superClass = arenaClass.getSuperclass();
		if (superClass != null && Arena.class.isAssignableFrom(superClass) ){
			yamlToObjects(arena,superClass,cs);
		}

	}

	private static Object createYamlSerializable(Class<?> clazz, ConfigurationSection cs) {
		if (clazz == null)
			return null;
		Class<?>[] args = {};
		try {
			Constructor<?> constructor = clazz.getConstructor(args);
			YamlSerializable ys = (YamlSerializable) constructor.newInstance((Object[])args);
			if (ys == null)
				return null;
			ys = (YamlSerializable) ys.yamlToObject(cs);
			return ys;
		} catch (NoSuchMethodException e){
			System.err.println("If you have custom constructors for your YamlSerializable class you must also have a public default constructor");
			System.err.println("Add the following line to your YamlSerializable Class '" + clazz.getSimpleName()+".java'");
			System.err.println("public " + clazz.getSimpleName()+"(){}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, Object> objectsToYamlMap(Arena arena) {
		Map<String,Object> map = new HashMap<String,Object>();

		Class<?> arenaClass = arena.getClass();
		objectsToYamlMap(arena,arenaClass,map);

		return map;
	}
	
	private static void objectsToYamlMap(Arena arena, Class<?> arenaClass, Map<String,Object> map){
		for(Field field : arenaClass.getDeclaredFields()){
			Class<?> type = field.getType();
			String name = field.getName();
//			System.out.println("Field " + name +"    " + type);
			Annotation[] annotations = field.getDeclaredAnnotations();
			for (Annotation a : annotations){
				if (!(a instanceof Persist)){
					continue;
				}
//				if (type == String.class)
//					System.out.println("Type = " + type +"  " + name +"   " + annotations);
				field.setAccessible(true);

//				System.out.println("Persist object " + p );
				try {
					Object obj = null;
					if (type == Integer.class || type == Float.class || type == Double.class ||
							type == Byte.class || type == Boolean.class || type == Character.class ||
							type == Short.class || type == Long.class || type==String.class){
						obj = field.get(arena);
					} else if (type == int.class){
						map.put(name, field.getInt(arena));
					} else if (type == float.class){
						map.put(name, field.getFloat(arena));
					} else if (type == double.class){
						map.put(name, field.getDouble(arena));
					} else if (type == byte.class){
						map.put(name, field.getByte(arena));
					} else if (type == boolean.class){
						map.put(name, field.getBoolean(arena));
					} else if (type == char.class){
						map.put(name, field.getChar(arena));
					} else if (type == short.class){
						map.put(name, field.getShort(arena));
					} else if (type == long.class){
						map.put(name, field.getLong(arena));
					} else if (type == Location.class || type == ItemStack.class){
						obj = objToYaml(field.get(arena));
					} else if (List.class.isAssignableFrom(type)){
						@SuppressWarnings("unchecked")
						List<Object> list = (List<Object>) field.get(arena);
						if (list == null)
							continue;
						List<Object> olist = new ArrayList<Object>();
						for (Object o : list){
							olist.add(objToYaml(o));
						}
						obj = olist;
					} else if (Set.class.isAssignableFrom(type)){
						/// Just convert to a list, then we can put it back into set form later when we deserialize
						@SuppressWarnings("unchecked")
						Set<Object> set = (Set<Object>) field.get(arena);
						if (set == null)
							continue;
						List<Object> oset = new ArrayList<Object>();
						for (Object o : set){
							oset.add(objToYaml(o));
						}
						obj = oset;
					} else if (Map.class.isAssignableFrom(type)){
						@SuppressWarnings("unchecked")
						Map<Object,Object> mymap = (HashMap<Object,Object>) field.get(arena);
						if (mymap == null)
							continue;
						Map<Object,Object> oset = new HashMap<Object,Object>();
						for (Object o : mymap.keySet()){
							Object key = objToYaml(o);
							if (key == null)
								continue;
							Object value = mymap.get(o);
							if (value == null)
								continue;
							oset.put(key.toString(),objToYaml(value));
						}
						obj = oset;
					} else if (YamlSerializable.class.isAssignableFrom(type)){
						YamlSerializable ys = (YamlSerializable) field.get(arena);
						if (ys != null)
							obj = ys.objectToYaml();
					} else {
						throw new NotPersistableException("Type " + type +" is not persistable. Not saving value for " + name);
					}

					if (obj == null)
						continue;
					map.put(name, obj);						
				} catch (NotPersistableException e) {
					System.err.println(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Class<?> superClass = arenaClass.getSuperclass();
		if (superClass != null && Arena.class.isAssignableFrom(superClass) ){
			objectsToYamlMap(arena,superClass,map);
		}


	}

	private static Object yamlToObj(Object obj, Type genType, ConfigurationSection cs) {
//		System.out.println("Generic type = " + genType);
		if (genType == Location.class){
//			System.out.println("Generic type location = " + genType);
			return SerializerUtil.getLocation((String) obj);
		} else if (genType == ItemStack.class){
			try {
				return InventoryUtil.parseItem((String)obj);
			} catch (Exception e) {
				return null;
			}
		} else if (obj instanceof YamlSerializable){
			createYamlSerializable(genType.getClass(),cs);
		}
		//		else if (genType == Block.class){
		//			System.out.println("Generic type block = " + genType);
		//			return SerializerUtil.getBlock((String) obj);
		//		}
		return obj;
	}
	
	private static Object objToYaml(Object obj) {
		if (obj == null)
			return null;
//		System.out.println("Object class = " + obj.getClass());
		if (obj instanceof Location){
			//			System.out.println("!!!!!!!!!!!!!!!!!!! Object class = " + obj.getClass());
			return SerializerUtil.getLocString((Location)obj);
		} else if (obj instanceof ItemStack){
			return InventoryUtil.getItemString((ItemStack)obj);
		} else if (obj instanceof YamlSerializable){
			return ((YamlSerializable)obj).objectToYaml();
		}
		return obj;
	}

}
