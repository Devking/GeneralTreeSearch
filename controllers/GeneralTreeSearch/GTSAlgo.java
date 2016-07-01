// Code written by Wells Lucas Santo
package controllers.GeneralTreeSearch;

import  core.game.StateObservation;
import  ontology.Types;
import  tools.ElapsedCpuTimer;
import tools.Utils;

import  java.util.ArrayList;
import  java.io.*;
import  java.util.Random;

import controllers.Heuristics.SimpleStateHeuristic;
import controllers.Heuristics.WinScoreHeuristic;

// The actual generic tree search
public class GTSAlgo {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                           //
    // PARAMETER INITIALIZATION                                                                  //
    //                                                                                           //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // GTS Parameters
    GTSParams.EXPLORATION     exploration;
    GTSParams.EXPANSION       expansion;
    GTSParams.REMOVAL         removal;
    GTSParams.SIMULATION      simulation;
    GTSParams.EVALUATION      evaluation;
    GTSParams.BACKPROPAGATION backprop;
    GTSParams.SELECTION       selection;
    int                       depthLimit;

    public static double epsilon = 1e-6;
    public static double egreedyEpsilon = 0.05;

    // Constructor: GTS Parameter Initialization
    public GTSAlgo (String filename) {
        initParameters(filename);
        printParameters();
    }

    // Read TSDL file to initialize parameters
    //**// Will necessarily crash if invalid parameter is given
    void initParameters (String filename) {
        try {
            File myFile = new File(filename);
            FileReader fileReader = new FileReader(myFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    // Assumption: There's a colon in each line
                    String info [] = line.split(": ");
                    switch (info[0]) {
                        case "Exploration":     exploration = GTSParams.EXPLORATION.valueOf(info[1]);     break;
                        case "Expansion":       expansion   = GTSParams.EXPANSION.valueOf(info[1]);       break;
                        case "Removal":         removal     = GTSParams.REMOVAL.valueOf(info[1]);         break;
                        case "Simulation":      simulation  = GTSParams.SIMULATION.valueOf(info[1]);      break;
                        case "Evaluation":      evaluation  = GTSParams.EVALUATION.valueOf(info[1]);      break;
                        case "Backpropagation": backprop    = GTSParams.BACKPROPAGATION.valueOf(info[1]); break;
                        case "Selection":       selection   = GTSParams.SELECTION.valueOf(info[1]);       break;
                        case "Depth Limit":     depthLimit  = Integer.parseInt(info[1]);                  break;
                        default: break;
                    }
                }
            }
        // Quit the program if the file is invalid or nonexistant
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(666);
        }
    }

    //

    // Debugging, to make sure the right attributes were set
    void printParameters () {
        System.out.println("--------------------------------");
        System.out.println("(1) Exploration     Type: " + exploration);
        System.out.println("(2) Expansion       Type: " + expansion);
        System.out.println("(3) Removal         Type: " + removal);
        System.out.println("(4) Simulation      Type: " + simulation);
        System.out.println("(5) Evaluation      Type: " + evaluation);
        System.out.println("(6) Backpropagation Type: " + backprop);
        System.out.println("(7) Selection       Type: " + selection);
        System.out.println("(8) Depth Limit         : " + depthLimit);
        System.out.println("--------------------------------");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                           //
    // GENERIC TREE SEARCH                                                                       //
    //                                                                                           //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // (1) Initialize the root, run the algorithm, and return the decision
    public Types.ACTIONS eval (StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        // Initialize the frontier collection
        ArrayList<GTSNode> nodes = new ArrayList<GTSNode>();

        // Keep track of the root of the state tree
        GTSNode root = new GTSNode (stateObs, null);

        // Add the root to the frontier
        nodes.add(root);

        // While there's still time, explore the tree using GTS
        while (elapsedTimer.remainingTimeMillis() > 3.0) {
            // Check if there are still states to explore
            if (nodes.size() == 0) {
                System.out.println("No more states (we've evaluated all"
                                   + "possibilities in our decision space).");
                break;
            } else {
                generalSearch(nodes, elapsedTimer);
            }
        }

        // Once we're out of time, make a decision
        return decide(root);
    }
    
	///////////////////////////////////////////////////////////////////////////////////////////
	// 1. Exploration: Get the node that we will expand from (which node's children do we
	//                 add to the frontier?)
	///////////////////////////////////////////////////////////////////////////////////////////
    int explore(ArrayList<GTSNode> nodes, GTSNode outNode){
        int nodeIndex = -1;
        GTSNode currentNode = null;
        
    	switch (exploration) {
	        // First: Select the node at the front of the ArrayList
	        // This mimics the behavior of a FIFO queue (for BFS)
	        case FIRST:
	            nodeIndex   = 0;
	            currentNode = nodes.get(nodeIndex);
	            break;
	
	        // Last: Select the node at the end of the ArrayList
	        // This mimics the behavior of a LIFO stack (for DFS)
	        case LAST:
	            nodeIndex   = nodes.size() - 1;
	            currentNode = nodes.get(nodeIndex);
	            break;
	
	        // High: Select the node that has the highest reward in the ArrayList
	        // This takes O(n) time to traverse the ArrayList to make a decision!
	        //*/ Will this actually work? Only if there's independent meaning to nodes
	        //*/ with high reward -- if the branch shares the reward of a leaf, then
	        //*/ this will always just pick the leaf
	        //**// Need to initialize leaves with values!
	        case HIGHREWARD:
	            int highIndex = 0;
	            double highReward = nodes.get(highIndex).getReward();
	            for (int i = 0; i < nodes.size(); i++) {
	                if (nodes.get(i).getReward() > highReward) {
	                    highIndex = i;
	                    highReward = nodes.get(i).getReward();
	                }
	            }
	            nodeIndex   = highIndex;
	            currentNode = nodes.get(nodeIndex);
	            break;
	
	        // Low: Select the node that has the lowest reward in the ArrayList
	        // This takes O(n) time to traverse the ArrayList to make a decision
	        // Think about this one: won't low reward just pick the most recently added
	        // node in the tree, since it's initialized to a reward of 0?
	        //**// Need to initialize leaves with values!
	        case LOWREWARD:
	            int lowIndex = 0;
	            double lowReward = nodes.get(lowIndex).getReward();
	            for (int i = 0; i < nodes.size(); i++) {
	                if (nodes.get(i).getReward() < lowReward) {
	                    lowIndex = i;
	                    lowReward = nodes.get(i).getReward();
	                }
	            }
	            nodeIndex   = lowIndex;
	            currentNode = nodes.get(nodeIndex);
	            break;
	
	        // High UCT: Will pick the deepest unexpanded node in the tree based on UCT
	        case HIGHUCT:
	            // Start traversal at the root node
	            currentNode = nodes.get(0);
	            boolean hasAllChildren = true;
	            // In the situation where this node still has unexplored children,
	            // we will select this node to expand
	            for (int i = 0; i < currentNode.children.length; i++) {
	                if (currentNode.children[i] == null) {
	                    hasAllChildren = false;
	                    break;
	                }
	            }
	            // In the situation where all of the children have been explored
	            // Use UCT to go down the tree until we get to a node without all children explored
	            while (hasAllChildren && currentNode.children.length != 0) {
	                // Find the best child to select from
	                // this is the "uct" function in the SampleMCTS SingleTreeNode
	                int bestIndex = 0;
	                double bestValue = -Double.MAX_VALUE;
	                int totalVisits = currentNode.visitCount;
	
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    GTSNode child = currentNode.children[i];
	
	                    double reward = child.getReward();
	                    int childVisits = child.visitCount;
	                    // The below is based on the UCT equation!
	                    double weight = Math.sqrt(2);
	                    double thisValue = (reward) / (childVisits + this.epsilon) +
	                                       weight * Math.sqrt(Math.log(totalVisits + 1)/(childVisits + this.epsilon));
	                    if (thisValue > bestValue) {
	                        bestValue = thisValue;
	                        bestIndex = i;
	                    }
	                }
	
	                // Update currentNode to best child node
	                currentNode = currentNode.children[bestIndex];
	                // System.out.println("Updated currentNode to depth " + currentNode.getDepth());
	
	                // If the node we've selected by UCT has unexplored children, we break out of this
	                // and move on, to the expansion phase
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    if (currentNode.children[i] == null) {
	                        hasAllChildren = false;
	                        break;
	                    }
	                }
	            }
	            break;
	            
	         // Low UCT: Will pick the deepest unexpanded node in the tree based on UCT
	        case LOWUCT:
	            // Start traversal at the root node
	            currentNode = nodes.get(0);
	            hasAllChildren = true;
	            // In the situation where this node still has unexplored children,
	            // we will select this node to expand
	            for (int i = 0; i < currentNode.children.length; i++) {
	                if (currentNode.children[i] == null) {
	                    hasAllChildren = false;
	                    break;
	                }
	            }
	            // In the situation where all of the children have been explored
	            // Use UCT to go down the tree until we get to a node without all children explored
	            while (hasAllChildren && currentNode.children.length != 0) {
	                // Find the best child to select from
	                // this is the "uct" function in the SampleMCTS SingleTreeNode
	                int bestIndex = 0;
	                double bestValue = Double.MAX_VALUE;
	                int totalVisits = currentNode.visitCount;
	
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    GTSNode child = currentNode.children[i];
	
	                    double reward = child.getReward();
	                    int childVisits = child.visitCount;
	                    // The below is based on the UCT equation!
	                    double weight = Math.sqrt(2);
	                    double thisValue = (reward) / (childVisits + this.epsilon) +
	                                       weight * Math.sqrt(Math.log(totalVisits + 1)/(childVisits + this.epsilon));
	                    if (thisValue < bestValue) {
	                        bestValue = thisValue;
	                        bestIndex = i;
	                    }
	                }
	
	                // Update currentNode to best child node
	                currentNode = currentNode.children[bestIndex];
	                // System.out.println("Updated currentNode to depth " + currentNode.getDepth());
	
	                // If the node we've selected by UCT has unexplored children, we break out of this
	                // and move on, to the expansion phase
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    if (currentNode.children[i] == null) {
	                        hasAllChildren = false;
	                        break;
	                    }
	                }
	            }
	            break;
	        
	        // Use E-Greedy on Reward
	        case HIGHEGREEDYREWARD:
	        	// Start traversal at the root node
	            currentNode = nodes.get(0);
	            hasAllChildren = true;
	            Random random = new Random();
	            // In the situation where this node still has unexplored children,
	            // we will select this node to expand
	            for (int i = 0; i < currentNode.children.length; i++) {
	                if (currentNode.children[i] == null) {
	                    hasAllChildren = false;
	                    break;
	                }
	            }
	            // In the situation where all of the children have been explored
	            // Use UCT to go down the tree until we get to a node without all children explored
	            while (hasAllChildren && currentNode.children.length != 0) {
	                // Find the best child to select from
	                // this is the "uct" function in the SampleMCTS SingleTreeNode
	                int bestIndex = 0;
	                if(random.nextDouble() < egreedyEpsilon){
	                    //Choose randomly
	                    bestIndex = random.nextInt(currentNode.children.length);
	
	                }else{
	                    //pick the best Q.
	                    double bestValue = -Double.MAX_VALUE;
	                    for (int i = 0; i < currentNode.children.length; i++) {
	                        double hvVal = currentNode.children[i].getReward();
	                        hvVal = Utils.noise(hvVal, this.epsilon, random.nextDouble());     //break ties randomly
	                        // small sampleRandom numbers: break ties in unexpanded nodes
	                        if (hvVal > bestValue) {
	                            bestIndex = i;
	                            bestValue = hvVal;
	                        }
	                    }
	                }
	
	                // Update currentNode to best child node
	                currentNode = currentNode.children[bestIndex];
	                // System.out.println("Updated currentNode to depth " + currentNode.getDepth());
	
	                // If the node we've selected by UCT has unexplored children, we break out of this
	                // and move on, to the expansion phase
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    if (currentNode.children[i] == null) {
	                        hasAllChildren = false;
	                        break;
	                    }
	                }
	            }
	            break;
	            
	            
	         // Use E-Greedy on Reward
	        case LOWEGREEDYREWARD:
	        	// Start traversal at the root node
	            currentNode = nodes.get(0);
	            hasAllChildren = true;
	            random = new Random();
	            // In the situation where this node still has unexplored children,
	            // we will select this node to expand
	            for (int i = 0; i < currentNode.children.length; i++) {
	                if (currentNode.children[i] == null) {
	                    hasAllChildren = false;
	                    break;
	                }
	            }
	            // In the situation where all of the children have been explored
	            // Use UCT to go down the tree until we get to a node without all children explored
	            while (hasAllChildren && currentNode.children.length != 0) {
	                // Find the best child to select from
	                // this is the "uct" function in the SampleMCTS SingleTreeNode
	                int bestIndex = 0;
	                if(random.nextDouble() < egreedyEpsilon){
	                    //Choose randomly
	                    bestIndex = random.nextInt(currentNode.children.length);
	
	                }else{
	                    //pick the best Q.
	                    double bestValue = Double.MAX_VALUE;
	                    for (int i = 0; i < currentNode.children.length; i++) {
	                        double hvVal = currentNode.children[i].getReward();
	                        hvVal = Utils.noise(hvVal, this.epsilon, random.nextDouble());     //break ties randomly
	                        // small sampleRandom numbers: break ties in unexpanded nodes
	                        if (hvVal < bestValue) {
	                            bestIndex = i;
	                            bestValue = hvVal;
	                        }
	                    }
	                }
	
	                // Update currentNode to best child node
	                currentNode = currentNode.children[bestIndex];
	                // System.out.println("Updated currentNode to depth " + currentNode.getDepth());
	
	                // If the node we've selected by UCT has unexplored children, we break out of this
	                // and move on, to the expansion phase
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    if (currentNode.children[i] == null) {
	                        hasAllChildren = false;
	                        break;
	                    }
	                }
	            }
	            break;
	            
	         // Use E-Greedy on Reward
	        case HIGHEGREEDYVISITS:
	        	// Start traversal at the root node
	            currentNode = nodes.get(0);
	            hasAllChildren = true;
	            random = new Random();
	            // In the situation where this node still has unexplored children,
	            // we will select this node to expand
	            for (int i = 0; i < currentNode.children.length; i++) {
	                if (currentNode.children[i] == null) {
	                    hasAllChildren = false;
	                    break;
	                }
	            }
	            // In the situation where all of the children have been explored
	            // Use UCT to go down the tree until we get to a node without all children explored
	            while (hasAllChildren && currentNode.children.length != 0) {
	                // Find the best child to select from
	                // this is the "uct" function in the SampleMCTS SingleTreeNode
	                int bestIndex = 0;
	                if(random.nextDouble() < egreedyEpsilon){
	                    //Choose randomly
	                    bestIndex = random.nextInt(currentNode.children.length);
	
	                }else{
	                    //pick the best Q.
	                    double bestValue = -Double.MAX_VALUE;
	                    for (int i = 0; i < currentNode.children.length; i++) {
	                        double hvVal = currentNode.children[i].getVisitCount();
	                        hvVal = Utils.noise(hvVal, this.epsilon, random.nextDouble());     //break ties randomly
	                        // small sampleRandom numbers: break ties in unexpanded nodes
	                        if (hvVal > bestValue) {
	                            bestIndex = i;
	                            bestValue = hvVal;
	                        }
	                    }
	                }
	
	                // Update currentNode to best child node
	                currentNode = currentNode.children[bestIndex];
	                // System.out.println("Updated currentNode to depth " + currentNode.getDepth());
	
	                // If the node we've selected by UCT has unexplored children, we break out of this
	                // and move on, to the expansion phase
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    if (currentNode.children[i] == null) {
	                        hasAllChildren = false;
	                        break;
	                    }
	                }
	            }
	            break;
	            
	         // Use E-Greedy on Reward
	        case LOWEGREEDYVISITS:
	        	// Start traversal at the root node
	            currentNode = nodes.get(0);
	            hasAllChildren = true;
	            random = new Random();
	            // In the situation where this node still has unexplored children,
	            // we will select this node to expand
	            for (int i = 0; i < currentNode.children.length; i++) {
	                if (currentNode.children[i] == null) {
	                    hasAllChildren = false;
	                    break;
	                }
	            }
	            // In the situation where all of the children have been explored
	            // Use UCT to go down the tree until we get to a node without all children explored
	            while (hasAllChildren && currentNode.children.length != 0) {
	                // Find the best child to select from
	                // this is the "uct" function in the SampleMCTS SingleTreeNode
	                int bestIndex = 0;
	                if(random.nextDouble() < egreedyEpsilon){
	                    //Choose randomly
	                    bestIndex = random.nextInt(currentNode.children.length);
	
	                }else{
	                    //pick the best Q.
	                    double bestValue = Double.MAX_VALUE;
	                    for (int i = 0; i < currentNode.children.length; i++) {
	                        double hvVal = currentNode.children[i].getVisitCount();
	                        hvVal = Utils.noise(hvVal, this.epsilon, random.nextDouble());     //break ties randomly
	                        // small sampleRandom numbers: break ties in unexpanded nodes
	                        if (hvVal < bestValue) {
	                            bestIndex = i;
	                            bestValue = hvVal;
	                        }
	                    }
	                }
	
	                // Update currentNode to best child node
	                currentNode = currentNode.children[bestIndex];
	                // System.out.println("Updated currentNode to depth " + currentNode.getDepth());
	
	                // If the node we've selected by UCT has unexplored children, we break out of this
	                // and move on, to the expansion phase
	                for (int i = 0; i < currentNode.children.length; i++) {
	                    if (currentNode.children[i] == null) {
	                        hasAllChildren = false;
	                        break;
	                    }
	                }
	            }
	            break;
	
	        // Default: Select first node from frontier
	        default:
	            nodeIndex   = 0;
	            currentNode = nodes.get(nodeIndex);
	            break;
    	}
    	
    	outNode.parent = currentNode;
    	return nodeIndex;
    }
    
	///////////////////////////////////////////////////////////////////////////////////////////
	//2. Removal: Do we remove the explored node from the frontier?
	///////////////////////////////////////////////////////////////////////////////////////////
    void remove(ArrayList<GTSNode> nodes, int nodeIndex){
    	// Next, check removal parameter to see whether we remove or not
    	if (nodeIndex != -1) {
    		switch (removal) {
				case YES: nodes.remove(nodeIndex); break;
				case NO: default: break;
			}
		}
    }
    
	///////////////////////////////////////////////////////////////////////////////////////////
	//3. Node Expansion: All, First
	///////////////////////////////////////////////////////////////////////////////////////////
    ArrayList<GTSNode> expand(ArrayList<GTSNode> nodes, GTSNode currentNode){
    	ArrayList<GTSNode> results = new ArrayList<GTSNode>();
    	
		// If the node is a valid game state within the depth limit, do expansion
		if (currentNode.getDepth() < depthLimit && !currentNode.thisState.isGameOver()) {
			switch (expansion) {
				// All: Add all of the children of this node to the frontier
				// This is the behavior you want in DFS/BFS/A*
				default: case ALL:
					for (int i = 0; i < currentNode.children.length; i++) {
						StateObservation nextState = currentNode.thisState.copy();
						nextState.advance(currentNode.thisState.getAvailableActions().get(i));
						currentNode.children[i] = new GTSNode(nextState, currentNode);
						nodes.add(currentNode.children[i]);
						results.add(currentNode.children[i]);
					}
					break;
				
				// First: Add only the first unexplored child to the frontier
				case FIRST:
					int expandIndex = -1;
					for (int i = 0; i < currentNode.children.length; i++) {
						if (currentNode.children[i] == null) {
							expandIndex = i;
							break;
						}
					}
					// If there's actually an explored node, explore it
					if (expandIndex != -1) {
						StateObservation nextState = currentNode.thisState.copy();
						nextState.advance(currentNode.thisState.getAvailableActions().get(expandIndex));
						currentNode.children[expandIndex] = new GTSNode(nextState, currentNode);
						nodes.add(currentNode.children[expandIndex]);
						results.add(currentNode.children[expandIndex]);
					}
					break;
					
				// Last: Add only the last unexplored child to the frontier
				case LAST:
					expandIndex = -1;
					for (int i = currentNode.children.length - 1; i >= 0; i--) {
						if (currentNode.children[i] == null) {
							expandIndex = i;
							break;
						}
					}
					// If there's actually an explored node, explore it
					if (expandIndex != -1) {
						StateObservation nextState = currentNode.thisState.copy();
						nextState.advance(currentNode.thisState.getAvailableActions().get(expandIndex));
						currentNode.children[expandIndex] = new GTSNode(nextState, currentNode);
						nodes.add(currentNode.children[expandIndex]);
						results.add(currentNode.children[expandIndex]);
					}
					break;
				
				// Random: Add a random child to the frontier
				case RANDOM:
					// Do the random selection based on how SampleMCTS expand does it
					Random rnd = new Random();
					int bestAction = 0;
					double bestValue = -1;
					// Go through the unexplored children and randomly assign values to each
					// The one who receives the highest value (randomly) is the one we expand
					for (int i = 0; i < currentNode.children.length; i++) {
						double randomValue = rnd.nextDouble();
						if (randomValue > bestValue && currentNode.children[i] == null) {
							bestAction = i;
							bestValue = randomValue;
						}
					}
					// Here, we've selected a random child node to create/add to the frontier
					StateObservation nextState = currentNode.thisState.copy();
					nextState.advance(currentNode.thisState.getAvailableActions().get(bestAction));
					currentNode.children[bestAction] = new GTSNode(nextState, currentNode);
					nodes.add(currentNode.children[bestAction]);
					results.add(currentNode.children[bestAction]);
					break;
			}
		}
		
		return results;
    }
    
	///////////////////////////////////////////////////////////////////////////////////
	// 4. Simulation: Random Playout, None
	///////////////////////////////////////////////////////////////////////////////////
    ArrayList<StateObservation> simulation(ArrayList<GTSNode> expandedNodes, ElapsedCpuTimer elapsedTimer){
    	ArrayList<StateObservation> results = new ArrayList<StateObservation>();
		// Also known as the "rollOut" phase for MCTS
		
    	for(int i=0; i<expandedNodes.size(); i++){
    		GTSNode currentNode = expandedNodes.get(i);
			// Keep track of the final state to be evaluated
			StateObservation finalState = currentNode.thisState.copy();
			
			// Random Playout: Advance randomly from 'currentNode' to an end state
			// Only used by MCTS to bring currentNode to a terminal state
			// These simulated nodes are *not* added to the ArrayList
			switch (simulation) {
				case RANDOM: default:
					int finalDepth = currentNode.getDepth();
					Random random = new Random();
					while (finalDepth < depthLimit &&
						elapsedTimer.remainingTimeMillis() > 5.0 && !finalState.isGameOver()) {
						int actionNo = random.nextInt(finalState.getAvailableActions().size());
						finalState.advance(finalState.getAvailableActions().get(actionNo));
						finalDepth++;
					}
					// System.out.println("Simulation to depth: " + finalDepth);
					break;
				case SAME:
					finalDepth = currentNode.getDepth();
					random = new Random();
					int actionNo = -1;
					if(currentNode.parent != null){
						for(int j=0; j<currentNode.parent.children.length; j++){
							if(currentNode == currentNode.parent.children[j]){
								actionNo = j;
								break;
							}
						}
					}
					else{
						actionNo = random.nextInt(finalState.getAvailableActions().size());
					}
					while (finalDepth < depthLimit &&
						elapsedTimer.remainingTimeMillis() > 5.0 && !finalState.isGameOver()) {
						finalState.advance(finalState.getAvailableActions().get(actionNo));
						finalDepth++;
					}
					break;
				case NIL:
					finalDepth = currentNode.getDepth();
					while (finalDepth < depthLimit &&
						elapsedTimer.remainingTimeMillis() > 5.0 && !finalState.isGameOver()) {
						finalState.advance(Types.ACTIONS.ACTION_NIL);
						finalDepth++;
					}
					break;
				case NONE:
					break;
			}
			results.add(finalState);
    	}
		
		return results;
    }
    
	///////////////////////////////////////////////////////////////////////////////////////
	// 5. Evaluation: Points, Win (Heuristics)
	///////////////////////////////////////////////////////////////////////////////////////
    ArrayList<Double> evaluate(ArrayList<GTSNode> expandedNodes, ArrayList<StateObservation> finalStates){
    	ArrayList<Double> results = new ArrayList<Double>();
		// Do evaluation *only* if this node is a game over state, or past the depth limit
		//*/ Is this really what I want?
		for(int i=0; i<expandedNodes.size(); i++){
			GTSNode tempNode = expandedNodes.get(i);
			StateObservation tempState = finalStates.get(i);
			if (tempNode.getDepth() >= depthLimit || tempState.isGameOver()) {
				double reward = 0;
				switch (evaluation) {
					// Points: Reward is just the score
					case POINTS:
						reward = tempState.getGameScore();
						break;
					
					// Win: Add 1 to the reward if this node is a winning state
					case WIN:
						if (tempState.isGameOver() &&
								tempState.getGameWinner() == Types.WINNER.PLAYER_WINS)
						reward = 1;
						break;
					
					// WinLossPoints: Winning state is large reward, losing state is negative, and points
					case WINLOSSPOINTS:
						WinScoreHeuristic winScoreHeuristic = new WinScoreHeuristic(tempState);
						reward = winScoreHeuristic.evaluateState(tempState);
						break;
					
					// StateHeuristic:
					case STATEHEURISTIC:
						SimpleStateHeuristic stateHeuristic = new SimpleStateHeuristic(tempState);
						reward = stateHeuristic.evaluateState(tempState);
						break;
					
					// Default: Points
					default:
						reward = tempState.getGameScore();
						break;
			
				}
				results.add(reward);
			}
		}
		
		return results;
    }
    
	///////////////////////////////////////////////////////////////////////////////////
	// 6. Backprop: High, Low, Increment
	///////////////////////////////////////////////////////////////////////////////////
    void backprop(ArrayList<GTSNode> expandedNodes, ArrayList<Double> rewards){
    	for(int i=0; i<expandedNodes.size(); i++){
    		GTSNode currentNode = expandedNodes.get(i);
    		double reward = rewards.get(i);
    		// Backprop all the way to the root
			while (currentNode.parent != null) {
				switch (backprop) {
					// High: Propagate the max reward seen so far
					case MAX:
						currentNode.setRewardMax(reward);
						break;
					
					// Increment: Add reward to each node as you go up the tree
					case INCREMENT:
						currentNode.incrementReward(reward);
						break;
					
					// Low: Propagate the lowest reward seen so far
					case MIN:
						currentNode.setRewardMin(reward);
						break;
					
					// Default: Propagate Max
					default:
						currentNode.setRewardMax(reward);
						break;
				}
					currentNode.incrementVisitCount();
					currentNode = currentNode.parent;
			}
    	}
    }
    
    
    // (2) Explore/expand/simulate/evaluate/backprop the tree
    
    // What to do if the current node is a isGameOver() state? (checks on expand, not explore)
    void generalSearch (ArrayList<GTSNode> nodes, ElapsedCpuTimer elapsedTimer) {

        // 1. Explore Step
        GTSNode currentNode = new GTSNode(null, null);
        int nodeIndex = explore(nodes, currentNode);
        currentNode = currentNode.parent;

        // 2. Removal Step
        remove(nodes, nodeIndex);

        // 3. Expansion Step
        ArrayList<GTSNode> expandedNodes = expand(nodes, currentNode);

        // 4. Simulation Step
        ArrayList<StateObservation> finalStates = simulation(expandedNodes, elapsedTimer);

        // 5. Evaluation Step
        ArrayList<Double> rewards = evaluate(expandedNodes, finalStates);

        // 6. Backprop Step
        backprop(expandedNodes, rewards);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // 7. Selection: High, Low
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // (3) Make the final decision of what action to actually take next
    // Biases actions with lower index (in case of tie)
    Types.ACTIONS decide (GTSNode root) {
        int action = 0;

        switch (selection) {
            // High: Select the node with the highest reward
            case HIGHREWARD:
                double bestReward = -Double.MAX_VALUE;
                for (int i = 0; i < root.children.length; i++) {
                    if (root.children[i] != null && bestReward < root.children[i].getReward()) {
                        action = i;
                        bestReward = root.children[i].getReward();
                    }
                }
                break;

            // Low: Select the node with the lowest reward
            case LOWREWARD:
                double lowReward = Double.MAX_VALUE;
                for (int i = 0; i < root.children.length; i++) {
                    if (root.children[i] != null && lowReward > root.children[i].getReward()) {
                        action = i;
                        lowReward = root.children[i].getReward();
                    }
                }
                break;

            // Most Visits: Select the node that's been visited the most
            case MOSTVISITS:
                double mostVisits = -Double.MAX_VALUE;
                for (int i = 0; i < root.children.length; i++) {
                    if (root.children[i] != null && mostVisits < root.children[i].getVisitCount()) {
                        action = i;
                        mostVisits = root.children[i].getVisitCount();
                    }
                }
                break;
                
            // Least Visits: Select the node that's been visited the most
            case LEASTVISITS:
                double leastVisits = Double.MAX_VALUE;
                for (int i = 0; i < root.children.length; i++) {
                    if (root.children[i] != null && leastVisits > root.children[i].getVisitCount()) {
                        action = i;
                        leastVisits = root.children[i].getVisitCount();
                    }
                }
                break;
            
            // Highest UCT: Select the node that's has the highest UCT value
            case HIGHUCT:
            	double highUCT = -Double.MAX_VALUE;
                for (int i = 0; i < root.children.length; i++) {
                    if (root.children[i] != null) {
                    	double reward = root.children[i].getReward();
                        int childVisits = root.children[i].visitCount;
                        int totalVisits = root.visitCount;
                        // The below is based on the UCT equation!
                        double weight = Math.sqrt(2);
                        double uct = (reward) / (childVisits + this.epsilon) +
                                           weight * Math.sqrt(Math.log(totalVisits + 1)/(childVisits + this.epsilon));
                        if(highUCT < uct){
                        	action = i;
                        	highUCT = uct;
                        }
                    }
                }
                break;
            
            // Highest UCT: Select the node that's has the highest UCT value
            case LOWUCT:
            	double lowUCT = -Double.MAX_VALUE;
                for (int i = 0; i < root.children.length; i++) {
                    if (root.children[i] != null) {
                    	double reward = root.children[i].getReward();
                        int childVisits = root.children[i].visitCount;
                        int totalVisits = root.visitCount;
                        // The below is based on the UCT equation!
                        double weight = Math.sqrt(2);
                        double uct = (reward) / (childVisits + this.epsilon) +
                                           weight * Math.sqrt(Math.log(totalVisits + 1)/(childVisits + this.epsilon));
                        if(lowUCT < uct){
                        	action = i;
                        	lowUCT = uct;
                        }
                    }
                }
                break;
            
            // Random Action: Just select any random action
            case RANDOM:
            	action = new Random().nextInt(root.thisState.getAvailableActions().size());
            
            // Default: Just select first action
            default:
                break;
        }

        return root.thisState.getAvailableActions().get(action);
    }

}
