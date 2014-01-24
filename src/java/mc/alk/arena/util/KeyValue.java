package mc.alk.arena.util;

public class KeyValue<KEY,VALUE> {
	public KEY key;
	public VALUE value;

	public KeyValue(){}
	public KeyValue(KEY k, VALUE v){
		this.key = k; 
		this.value = v;
	}

	public KEY getKey() {
		return key;
	}
	public void setKey(KEY key) {
		this.key = key;
	}
	public VALUE getValue() {
		return value;
	}
	public void setValue(VALUE value) {
		this.value = value;
	}
	public static KeyValue<String, String> split(String string, String splitOn) {
		KeyValue<String,String> kv = new KeyValue<String,String>();
		String[] split = string.split(splitOn);
		switch(split.length){
		case 2: kv.value = split[1]; /// notice there is no break on this, it only stops after doing case 1 
		case 1: kv.key = split[0];
			return kv;
		default:
			return null;
		}

	}
}
