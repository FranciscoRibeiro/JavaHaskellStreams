package optimizations;

import datatypes.Done;
import datatypes.Skip;
import datatypes.Step;
import datatypes.Yield;
import util.Either;
import util.Left;
import util.RecursiveLambda;
import util.Right;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class CaseOfCaseInGo {
    private static <S,T> S foldlAppend(BiFunction<S,T,S> f, S value, List<T> xs, List<T> ys){
        RecursiveLambda<BiFunction<S, Object, S>> go = new RecursiveLambda<>();

        go.function = (z, x) -> {
            Step aux = null;

            if(x instanceof Left){
                List lAux = (List) ((Left) x).fromLeft();

                if(lAux.isEmpty()){
                    aux = new Skip<Either>(new Right(ys));

                    if(aux instanceof Done){
                        return z;
                    }
                    else if(aux instanceof Skip){
                        return go.function.apply(z, aux.state);
                    }
                    else if(aux instanceof Yield){
                        return go.function.apply(f.apply(z, (T) aux.elem), aux.state);
                    }
                }
                else{
                    List<T> sub = lAux.subList(1, lAux.size());
                    aux = new Yield<T, Either>((T) lAux.get(0), new Left(sub));

                    if(aux instanceof Done){
                        return z;
                    }
                    else if(aux instanceof Skip){
                        return go.function.apply(z, aux.state);
                    }
                    else if(aux instanceof Yield){
                        return go.function.apply(f.apply(z, (T) aux.elem), aux.state);
                    }
                }
            }

            else if(x instanceof Right){
                List lAux = (List) ((Right) x).fromRight();

                if(lAux.isEmpty()){
                    aux = new Done();

                    if(aux instanceof Done){
                        return z;
                    }
                    else if(aux instanceof Skip){
                        return go.function.apply(z, aux.state);
                    }
                    else if(aux instanceof Yield){
                        return go.function.apply(f.apply(z, (T) aux.elem), aux.state);
                    }
                }
                else{
                    List<T> sub = lAux.subList(1, lAux.size());
                    aux = new Yield<T, Either>((T) lAux.get(0), new Right(sub));

                    if(aux instanceof Done){
                        return z;
                    }
                    else if(aux instanceof Skip){
                        return go.function.apply(z, aux.state);
                    }
                    else if(aux instanceof Yield){
                        return go.function.apply(f.apply(z, (T) aux.elem), aux.state);
                    }
                }
            }

            return null;
        };

        return go.function.apply(value, new Left(xs));
    }

    public static void main(String[] args) {
        System.out.println("GHC optimizations...");
        ArrayList<Integer> xs = new ArrayList<>(Arrays.asList(new Integer[]{1, 2, 3, 4, 5}));
        ArrayList<Integer> ys = new ArrayList<>(Arrays.asList(new Integer[]{6, 7, 8, 9, 10}));
        BiFunction<Integer, Integer, Integer> f = (a, b) -> a+b;
        System.out.println(foldlAppend(f, 0, xs, ys));
    }
}
