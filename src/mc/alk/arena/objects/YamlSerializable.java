package mc.alk.arena.objects;

import java.util.Map;

import mc.alk.arena.objects.exceptions.SerializationException;


public interface YamlSerializable {
	Object yamlToObject(Map<String,Object> map, String value) throws SerializationException;
	Object objectToYaml() throws SerializationException;
}
