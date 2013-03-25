package mc.alk.arena.objects;

import java.util.Map;


public interface YamlSerializable {

	Object yamlToObject(Map<String,Object> map, String value);
	Object objectToYaml();
}
