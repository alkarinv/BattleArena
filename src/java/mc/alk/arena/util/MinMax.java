package mc.alk.arena.util;

import mc.alk.arena.objects.ArenaSize;

public class MinMax {
    public int min;

    public int max;

    public MinMax(){
        this.min = -1; this.max = -1;
    }

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
        if (s.indexOf('+')!=-1){
            Integer i = Integer.valueOf(s.substring(0,s.indexOf('+')));
            return new MinMax(i,ArenaSize.MAX);
        }
        if (s.contains("-")){
            String[] vals = s.split("-");
            int i = Integer.valueOf(vals[0]);
            int j = Integer.valueOf(vals[1]);
            return new MinMax(i,j);
        }

        Integer i;
        if (s.contains("v")){
            i = Integer.valueOf(s.split("v")[0]);
        } else {
            i = Integer.valueOf(s);
        }
        return new MinMax(i,i);
    }


    public boolean intersect(MinMax mm) {
        return  Math.max(mm.min, min) <= Math.min(mm.max, max);
    }

    public boolean valid() {
        return min <= max;
    }

}
