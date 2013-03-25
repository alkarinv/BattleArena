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
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.objects.YamlSerializable;
import mc.alk.arena.serializers.Persist;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

public class Persistable {

	public static class NotPersistableException extends Exception{
		private static final long serialVersionUID = 1L;
		public NotPersistableException(String msg){
			super(msg);
		}
	}

	public static void yamlToObjects(Object object, ConfigurationSection cs){
		if (cs == null)
			return;
		yamlToObjects(object, object.getClass(),cs, null);
	}

	public static void yamlToObjects(Object object, ConfigurationSection cs, Class<?> onlyCheckClass){
		if (cs == null)
			return;
		yamlToObjects(object, object.getClass(),cs,onlyCheckClass);
	}

	@SuppressWarnings("unchecked")
	private static void yamlToObjects(Object object, Class<?> objectClass, ConfigurationSection cs, Class<?> onlyCheckClass){
		for(Field field : objectClass.getDeclaredFields()){
			Class<?> type = field.getType();
			String name = field.getName();
			Annotation[] annotations = field.getDeclaredAnnotations();
			for (Annotation a : annotations){
				if (!(a instanceof Persist || !cs.contains(name))){
					continue;
				}

//				System.out.println("Type = " + type +"  " + name +"   " + annotations + "   " + cs.getString(name));
				field.setAccessible(true);
				try {
					Object obj = null;
					if (type == int.class){
						field.setInt(object, cs.getInt(name));
					} else if (type == float.class){
						field.setFloat(object, (float)cs.getDouble(name));
					} else if (type == double.class){
						field.setDouble(object, cs.getDouble(name));
					} else if (type == long.class){
						field.setLong(object, cs.getLong(name));
					} else if (type == boolean.class){
						field.setBoolean(object, cs.getBoolean(name));
					} else if (type == short.class){
						field.setShort(object, (short)cs.getInt(name));
					} else if (type == byte.class){
						field.setByte(object, (byte)cs.getInt(name));
					} else if (type == char.class){
						String str = cs.getString(name);
						if (str != null && !str.isEmpty())
							field.setChar(object, str.charAt(0));
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
						obj = cs.getString(name);
					} else if (type == Location.class){
						String locstr = cs.getString(name);
						obj = SerializerUtil.getLocation(locstr);
					} else if (type == ItemStack.class){
						String str = cs.getString(name);
						if (str != null)
							obj = InventoryUtil.parseItem(str);
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
					} else if (ConfigurationSerializable.class.isAssignableFrom(type)){
						obj = ConfigurationSerialization.deserializeObject((Map<String,Object>)cs.get(name));
					} else if (YamlSerializable.class.isAssignableFrom(type)){
						Object o = cs.get(name);
						if (o != null && Map.class.isAssignableFrom(o.getClass())){
							obj = createYamlSerializable(type,(Map<String,Object>)o, cs.getString(name));
						} else {
							obj = createYamlSerializable(type,null, cs.getString(name));
						}
					} else {
						obj = yamlToObj(name,type,cs);
					}
					if (obj != null)
						field.set(object, obj);
				} catch (NotPersistableException e) {
					System.err.println(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Class<?> superClass = objectClass.getSuperclass();
		if (superClass != null && (onlyCheckClass == null || onlyCheckClass.isAssignableFrom(superClass))){
			yamlToObjects(object,superClass,cs, onlyCheckClass);
		}
	}


	@SuppressWarnings("unchecked")
	private static Object yamlToObj(Object name, Type type,  ConfigurationSection cs) throws Exception {
		if (type == Integer.class){
			return new Integer(name.toString());
		} else if (type == Float.class){
			return new Float(name.toString());
		} else if (type == Double.class){
			return new Double(name.toString());
		} else if (type == Character.class){
			return name.toString().charAt(0);
		} else if (type == Byte.class){
			return new Byte(name.toString());
		} else if (type == Short.class){
			return new Short(name.toString());
		} else if (type == Long.class){
			return new Long(name.toString());
		} else if (type == Boolean.class){
			return new Boolean(name.toString());
		} else if (type == String.class){
			return name;
		} else if (type == Location.class){
			return SerializerUtil.getLocation(name.toString());
		} else if (type == ItemStack.class){
			return InventoryUtil.parseItem(name.toString());
		} else if (YamlSerializable.class.isAssignableFrom((Class<?>) type)){
			if (Map.class.isAssignableFrom(name.getClass())){
				return createYamlSerializable((Class<?>)type, (Map<String,Object>)name, null);
			} else {
				return createYamlSerializable((Class<?>)type, null, (String)name);
			}
		}
		throw new NotPersistableException("Type " + type +" is not persistable. Not loading values for "+name);
	}

	private static Object createYamlSerializable(Class<?> clazz, Map<String,Object> map, String value) {
		if (clazz == null)
			return null;
		Class<?>[] args = {};
		try {
			Constructor<?> constructor = clazz.getConstructor(args);
			YamlSerializable ys = (YamlSerializable) constructor.newInstance((Object[])args);
			if (ys == null)
				return null;
			ys = (YamlSerializable) ys.yamlToObject(map, value);
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

	public static Map<String, Object> objectsToYamlMap(Object object) {
		return objectsToYamlMap(object,null);
	}

	public static Map<String, Object> objectsToYamlMap(Object object, Class<?> onlyCheckClass) {
		Map<String,Object> map = new HashMap<String,Object>();

		Class<?> objectClass = object.getClass();
		objectsToYamlMap(object,objectClass,map,onlyCheckClass);

		return map;
	}

	private static void objectsToYamlMap(Object object, Class<?> objectClass, Map<String,Object> map, Class<?> onlyCheckClass){
		for(Field field : objectClass.getDeclaredFields()){
			Class<?> type = field.getType();
			String name = field.getName();
			Annotation[] annotations = field.getDeclaredAnnotations();
			for (Annotation a : annotations){
				if (!(a instanceof Persist)){
					continue;
				}
//						System.out.println("Type = " + type +"  " + name +"   " + annotations);
				field.setAccessible(true);

				try {
					Object obj = null;
					if (type == Integer.class || type == Float.class || type == Double.class ||
							type == Byte.class || type == Boolean.class || type == Character.class ||
							type == Short.class || type == Long.class || type==String.class){
						obj = field.get(object);
					} else if (type == int.class){
						map.put(name, field.getInt(object));
					} else if (type == float.class){
						map.put(name, field.getFloat(object));
					} else if (type == double.class){
						map.put(name, field.getDouble(object));
					} else if (type == byte.class){
						map.put(name, field.getByte(object));
					} else if (type == boolean.class){
						map.put(name, field.getBoolean(object));
					} else if (type == char.class){
						map.put(name, field.getChar(object));
					} else if (type == short.class){
						map.put(name, field.getShort(object));
					} else if (type == long.class){
						map.put(name, field.getLong(object));
					} else if (type == Location.class || type == ItemStack.class){
						obj = objToYaml(field.get(object));
					} else if (List.class.isAssignableFrom(type)){
						@SuppressWarnings("unchecked")
						List<Object> list = (List<Object>) field.get(object);
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
						Set<Object> set = (Set<Object>) field.get(object);
						if (set == null)
							continue;
						List<Object> oset = new ArrayList<Object>();
						for (Object o : set){
							oset.add(objToYaml(o));
						}
						obj = oset;
					} else if (ConcurrentHashMap.class.isAssignableFrom(type)){
						@SuppressWarnings("unchecked")
						Map<Object,Object> mymap = (ConcurrentHashMap<Object,Object>) field.get(object);
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
					} else if (Map.class.isAssignableFrom(type)){
						@SuppressWarnings("unchecked")
						Map<Object,Object> mymap = (Map<Object,Object>) field.get(object);
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
						YamlSerializable ys = (YamlSerializable) field.get(object);
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
		Class<?> superClass = objectClass.getSuperclass();

		if (superClass != null && (onlyCheckClass == null || onlyCheckClass.isAssignableFrom(superClass))){
			objectsToYamlMap(object,superClass,map,onlyCheckClass);
		}


	}


	private static Object objToYaml(Object obj) {
		if (obj == null)
			return null;
		if (obj instanceof Location){
			return SerializerUtil.getLocString((Location)obj);
		} else if (obj instanceof ItemStack){
			return InventoryUtil.getItemString((ItemStack)obj);
		} else if (obj instanceof YamlSerializable){
			return ((YamlSerializable)obj).objectToYaml();
		}
		return obj;
	}

}
