import java.util.*;
import java.util.function.*;

public class FStream<T>{
    private Function<ArrayList, Step> stepper; // the stepper function: (s -> Step a s)
    private ArrayList state; // the stream's state (normally a list)

    public FStream(Function<ArrayList, Step> stepper, ArrayList<T> state){
        this.stepper = stepper;
        this.state = state;
    }

    public static <T> FStream<T> fstream(ArrayList<T> l){
        Function<ArrayList, Step> stepper = x -> x.isEmpty() ? new Done() : new Yield<T, ArrayList<T>>((T)x.get(0), new ArrayList<T>(x.subList(1, x.size())));
        return new FStream<T>(stepper, l);
    }

    public ArrayList<T> unfstream(){
        ArrayList<T> res = new ArrayList<>();
        Step step = this.stepper.apply(this.state);
        int slicer = 1;
        while(!(step instanceof Done)){
            if(step instanceof Yield){
                res.add((T)((Yield) step).elem);
            }
            step = this.stepper.apply(new ArrayList<T>(this.state.subList(slicer, this.state.size())));
            slicer++;
        }
        return res;
    }

    public <S> FStream<S> mapfs(Function<T,S> funcTtoS){
        Function<ArrayList, Step> stepper = x -> { 
                                              Step aux = this.stepper.apply(x);
                                              if(aux instanceof Done){
                                                return new Done();
                                              }
                                              else if(aux instanceof Skip){
                                                return new Skip<ArrayList>((ArrayList)((Skip)aux).state); //Need to change this later
                                              }
                                              else if(aux instanceof Yield){
                                                return new Yield<S,ArrayList>(funcTtoS.apply((T)((Yield)aux).elem), (ArrayList)((Yield)aux).state);
                                              }
                                              return null;
                                            };
        return new FStream<S>(stepper, this.state);
    }

    public FStream<T> filterfs(Predicate p){
        Function<ArrayList, Step> stepper = x -> { 
                                            Step aux = this.stepper.apply(x);
                                            if(aux instanceof Done){
                                                return new Done();
                                            }
                                            else if(aux instanceof Skip){
                                                return new Skip<ArrayList>((ArrayList)((Skip)aux).state); //Need to change this later
                                            }
                                            else if(aux instanceof Yield){
                                                if(p.test(((Yield)aux).elem)){
                                                    return new Yield<T,ArrayList>((T)((Yield)aux).elem, (ArrayList)((Yield)aux).state);
                                                }
                                                else{
                                                    return new Skip<ArrayList>((ArrayList)((Yield)aux).state);
                                                }
                                            }
                                            return null;
                                        };
        return new FStream<T>(stepper, this.state);
    }

    public FStream<T> appendfs(FStream<T> streamB){
      Function<Either, Step> stepper = x -> {
                                          if(x instanceof Left){
                                            ArrayList leftState = (ArrayList) ((Left) x).fromLeft();
                                            Step aux = this.stepper.apply(leftState);

                                            if(aux instanceof Done){
                                              return new Skip<Either>(new Right(streamB.state));
                                            }
                                            else if(aux instanceof Skip){
                                              return new Skip<Either>(new Left(((Skip) aux).state));
                                            }
                                            else if(aux instanceof Yield){
                                              return new Yield<T, Either>((T)((Yield) aux).elem, new Left(((Yield) aux).state));
                                            }
                                          }
                                          else if(x instanceof Right){
                                            ArrayList rightState = (ArrayList) ((Right) x).fromRight();
                                            Step aux = streamB.stepper.apply(rightState);

                                            if(aux instanceof Done){
                                              return new Done();
                                            }
                                            else if(aux instanceof Skip){
                                              return new Skip<Either>(new Right(((Skip) aux).state));
                                            }
                                            else if(aux instanceof Yield){
                                              return new Yield<T, Either>((T)((Yield) aux).elem, new Right(((Yield) aux).state));
                                            }
                                          }
                                          return null;
                                        };
      return new FStream<T>(stepper, this.state);
    }

    public static <T> ArrayList<T> map(Function f, ArrayList<T> l){
        return fstream(l).mapfs(f).unfstream();
    }



    public static void main(String[] args){
        final int SIZE = 6;
        ArrayList<Integer> l = new ArrayList<>();
        for(int i = 1; i < SIZE; i++){
            l.add(i);
        }
        l.add(1);

        FStream<Integer> fsOrig = fstream(l);
        System.out.println(fsOrig.state);
        ArrayList<Integer> lOrig = fsOrig.unfstream();
        System.out.println(lOrig);

        System.out.println("Mapping...");
        Function<Integer, Integer> f = n -> n + 30;
        FStream<Integer> fsMap = fsOrig.mapfs(f);
        ArrayList<Integer> lMapped = fsMap.unfstream();
        System.out.println(lMapped);
        System.out.println(lOrig);

        System.out.println("Filtering...");
        Predicate<Integer> p = n -> n < 3;
        FStream<Integer> fsFilter = fsOrig.filterfs(p);
        ArrayList<Integer> lFiltered = fsFilter.unfstream();
        System.out.println(lFiltered);
        System.out.println(lOrig);

        Function<Integer,Integer> inc = x -> x + 1;
        System.out.println("Mapping chained...");
        FStream<Integer> fsInc = fsOrig.mapfs(inc).mapfs(inc);
        ArrayList<Integer> lInc = fsInc.unfstream();
        System.out.println(lInc);
        System.out.println("Mapping merged...");
        FStream<Integer> fsInc2 = fsOrig.mapfs(inc.andThen(inc));
        ArrayList<Integer> lInc2 = fsInc2.unfstream();
        System.out.println(lInc2);

        System.out.println(map(inc,l));
    }
}