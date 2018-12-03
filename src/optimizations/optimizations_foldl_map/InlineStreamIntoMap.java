package optimizations.optimizations_foldl_map;

import datatypes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InlineStreamIntoMap {
    public static void main(String[] args) {
        ArrayList<Integer> ints = new ArrayList<>(Arrays.asList(3,4,5,6,5,7,8,9,0));

        Function<Integer, Boolean> p = x -> x > 2;

        Function<Object, Step> nextMap = x -> {
            Step aux = ((Function<Object, Step>) x1 -> {
                List aux1 = (List) x1;

                if (aux1.isEmpty()) {
                    return new Done();
                } else {
                    List<Integer> sub = aux1.subList(1, aux1.size());
                    return new Yield<Integer, List<Integer>>((Integer) aux1.get(0), sub);
                }
            }).apply(x);

            if(aux instanceof Done){
                return new Done();
            }
            else if(aux instanceof Skip){
                return new Skip<>(aux.state);
            }
            else if(aux instanceof Yield){
                return new Yield<>(p.apply((Integer) aux.elem), aux.state);
            }

            return null;
        };

        Boolean value = true;
        Object auxState = ints;
        boolean over = false;

        while (!over) {
            Step step = nextMap.apply(auxState);

            if (step instanceof Done) {
                over = true;
            } else if (step instanceof Skip) {
                auxState = step.state;
            } else if (step instanceof Yield) {
                value = ((BiFunction<Boolean, Boolean, Boolean>) (x, y) -> x && y).apply(value, (Boolean) step.elem);
                auxState = step.state;
            }
        }

        boolean res = value;

        System.out.println(res);
    }
}
