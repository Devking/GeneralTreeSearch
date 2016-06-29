// Code written by Wells Lucas Santo
package controllers.GenericTreeSearch;

public class GTSParams {
    public static enum EXPLORATION     { FIRST, HIGH, LOW, LAST, UCT };
    public static enum EXPANSION       { ALL, FIRST, RANDOM };
    public static enum REMOVAL         { YES, NO };
    public static enum SIMULATION      { RANDOM, NONE };
    public static enum EVALUATION      { POINTS, WIN, WINLOSSPOINTS, DISTANCE };
    public static enum BACKPROPAGATION { HIGH, LOW, INCREMENT };
    public static enum SELECTION       { HIGH, LOW, MOSTVISITS };
}
