package mc.alk.arena.util;

import mc.alk.arena.objects.ArenaSize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MinMax {
    public int min;

    public int max;

    public MinMax(int size){
        this.min = size; this.max = size;
    }

    public MinMax(int min, int max){
        this.min = min; this.max = max;
    }

    public MinMax(MinMax mm){
        this.min = mm.min; this.max = mm.max;
    }

    @Override
    public String toString(){return ArenaSize.rangeString(min, max);}

    public boolean contains(int i) {
        return min <= i && max >= i;
    }

    public static MinMax valueOf(String s) throws NumberFormatException{
        if (s == null) throw new NumberFormatException("Number can not be null");
        if (s.contains("+")){
            s = s.replaceAll("\\+", "");
            Integer i = Integer.valueOf(s);
            return new MinMax(i,ArenaSize.MAX);
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


    public boolean intersect(MinMax mm) {
        return Math.min(mm.max, max) <= Math.max(mm.min, min);
    }

    public boolean valid() {
        return min <= max;
    }

}
