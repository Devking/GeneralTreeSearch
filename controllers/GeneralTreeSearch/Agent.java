// Code written by Wells Lucas Santo

package controllers.GeneralTreeSearch;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {
	public static String filename;
	
    GTSAlgo algo;

    // Initialize the Generic Tree Search, manually set which file to read from
    public Agent (StateObservation so, ElapsedCpuTimer elapsedTimer) {
        // filename = "TSDLAlgorithms/BFS.txt";
        // filename = "TSDLAlgorithms/DFS.txt";
        // filename = "TSDLAlgorithms/BestFirstSearch.txt";
    	// filename = "TSDLAlgorithms/MCTS.txt";
        algo = new GTSAlgo(filename);
    }

    // Initialize the Generic Tree Search based on the file that's passed in
    public Agent (StateObservation so, ElapsedCpuTimer elapsedTimer, String filename) {
        algo = new GTSAlgo(filename);
    }

    // Called at each step, will return the action to perform
    public Types.ACTIONS act (StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        return algo.eval(stateObs, elapsedTimer);
    }
}
