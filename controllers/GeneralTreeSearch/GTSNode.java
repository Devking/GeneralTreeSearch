// Code written by Wells Lucas Santo
package controllers.GeneralTreeSearch;

import core.game.StateObservation;
import ontology.Types;

// Nodes in the decision tree
public class GTSNode {

    // Based on the parameters needed for the SampleMCTS SingleTreeNode
    public GTSNode (StateObservation s, GTSNode par) {
        thisState = s;
        totalValue = 0;
        visitCount = 0;
        parent = par;
        if(s != null){
        	children = new GTSNode [thisState.getAvailableActions().size()];
        }
        if (par != null)
            depth = par.depth+1;
        else
            depth = 0;
    }

    public Types.ACTIONS getAction () {
        return thisState.getAvailableActions().get(0);
    }

    public int getVisitCount () {
        return visitCount;
    }

    public void incrementVisitCount () {
        visitCount++;
    }

    public int getDepth () {
        return depth;
    }

    public void setRewardMax (double reward) {
        if (reward > totalValue) totalValue = reward;
    }

    public void setRewardMin (double reward) {
        if (reward < totalValue) totalValue = reward;
    }

    public double getReward () {
        return totalValue;
    }

    public void incrementReward (double reward) {
        totalValue += reward;
    }
    
    public void printNode(){
    	int numOfChildren = 0;
    	for(int i=0; i<this.children.length; i++){
    		if(this.children[i] != null){
    			numOfChildren += 1;
    		}
    	}
    	
    	int actionNumber = -1;
    	if(this.parent != null){
    		for(int i=0; i<this.parent.children.length; i++){
    			if(this.parent.children[i] == this){
    				actionNumber = i;
    				break;
    			}
    		}
    	}
    	
    	System.out.println("[Action: " + actionNumber + ", Visit Count: " + visitCount + ", Depth: " + 
    			depth + ", Total Value: " + totalValue + ", Num of Children: " + 
    			numOfChildren + ", isGameOver: " + thisState.isGameOver() + ", Score: " + 
    			thisState.getGameScore() + "]");
    }

    public StateObservation thisState;
    double totalValue;
    int visitCount;
    GTSNode parent;
    public GTSNode[] children;
    int depth;
}
