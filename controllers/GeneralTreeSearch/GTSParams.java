// Code written by Wells Lucas Santo
package controllers.GeneralTreeSearch;

public class GTSParams {
    public static enum EXPLORATION     { FIRST, LAST, HIGHREWARD, LOWREWARD, MOSTVISITS, LEASTVISITS, HIGHUCT, LOWUCT, 
    	HIGHEGREEDYREWARD, LOWEGREEDYREWARD, HIGHEGREEDYVISITS, LOWEGREEDYVISITS };
    public static enum REMOVAL         { YES, NO };
    public static enum EXPANSION       { ALL, FIRST, LAST, RANDOM };
    public static enum SIMULATION      { RANDOM, SAME, NIL, NONE };
    public static enum EVALUATION      { POINTS, WIN, WINLOSSPOINTS, STATEHEURISTIC };
    public static enum BACKPROPAGATION { MAX, MIN, INCREMENT };
    public static enum SELECTION       { HIGHREWARD, LOWREWARD, MOSTVISITS, LEASTVISITS, HIGHUCT, LOWUCT, RANDOM };
}
