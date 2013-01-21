package mc.alk.arena.util;

import mc.alk.arena.objects.ArenaParams;

public class MinMax {
	public int min;

	public int max;

	public MinMax(int size){
		this.min = size; this.max = size;
	}

	public MinMax(int min, int max){
		this.min = min; this.max = max;
	}

	@Override
	public String toString(){return getStr(min,max);}
	public boolean contains(int i) {
		return min <= i && max >= i;
	}

	public static MinMax valueOf(String s) throws NumberFormatException{
		if (s == null) throw new NumberFormatException("Number can not be null");
		if (s.contains("+")){
			s = s.replaceAll("\\+", "");
			Integer i = Integer.valueOf(s);
			return new MinMax(i,ArenaParams.MAX);
		}
		if (s.contains("-")){
			String[] vals = s.split("-");
			int i = Integer.valueOf(vals[0]);
			int j = Integer.valueOf(vals[1]);
			return new MinMax(i,j);
		}

		Integer i = null;
		if (s.contains("v")){
			i = Integer.valueOf(s.split("v")[0]);
		} else {
			i = Integer.valueOf(s);
		}
		return new MinMax(i,i);
	}


	public static String getStr(int min, int max){
		if (min==max)
			return min+"";
		if (max == ArenaParams.MAX){
			return min+"+";}
		return min+"-"+max;
	}


}
