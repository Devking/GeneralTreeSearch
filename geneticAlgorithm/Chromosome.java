package geneticAlgorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import controllers.GeneralTreeSearch.GTSParams;

public class Chromosome implements Comparable<Chromosome>{
	
	public GTSParams.EXPLORATION exploration;
	public GTSParams.BACKPROPAGATION backprop;
	public GTSParams.EXPANSION expansion;
	public GTSParams.EVALUATION evaluation;
	public GTSParams.REMOVAL removal;
	public GTSParams.SELECTION selection;
	public GTSParams.SIMULATION simulation;
	public int depthLimit;
	public int simulationLimit;
	
	public double fitness;
	public double averageWins;
	public double averageScore;
	public double averageTime;
	public int index;
	
	public Chromosome(){
		exploration = GTSParams.EXPLORATION.HIGHUCT;
		backprop = GTSParams.BACKPROPAGATION.INCREMENT;
		expansion = GTSParams.EXPANSION.RANDOM;
		evaluation = GTSParams.EVALUATION.STATEHEURISTIC;
		removal = GTSParams.REMOVAL.NO;
		selection = GTSParams.SELECTION.MOSTVISITS;
		simulation = GTSParams.SIMULATION.RANDOM;
		depthLimit = -1;
		simulationLimit = 10;
		
		fitness = -1;
	}
	
	public Chromosome(String path) throws NumberFormatException, IOException{
		File myFile = new File(path);
        FileReader fileReader = new FileReader(myFile);
        BufferedReader reader = new BufferedReader(fileReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
        	if (!line.isEmpty()) {
        		// Assumption: There's a colon in each line
                String info [] = line.split(":");
                switch (info[0]) {
                	case "Exploration":      exploration 	 = GTSParams.EXPLORATION.valueOf(info[1].trim());     break;
                    case "Expansion":        expansion   	 = GTSParams.EXPANSION.valueOf(info[1].trim());       break;
                    case "Removal":          removal     	 = GTSParams.REMOVAL.valueOf(info[1].trim());         break;
                    case "Simulation":       simulation  	 = GTSParams.SIMULATION.valueOf(info[1].trim());      break;
                    case "Evaluation":       evaluation  	 = GTSParams.EVALUATION.valueOf(info[1].trim());      break;
                    case "Backpropagation":  backprop    	 = GTSParams.BACKPROPAGATION.valueOf(info[1].trim()); break;
                    case "Selection":        selection   	 = GTSParams.SELECTION.valueOf(info[1].trim());       break;
                    case "Depth Limit":      depthLimit  	 = Integer.parseInt(info[1].trim());                  break;
                    case "Simulation Limit": simulationLimit = Integer.parseInt(info[1].trim());                  break;
                    default: break;
                }
            }
        }
        reader.close();
		
		fitness = -1;
	}
	
	public Chromosome clone(){
		Chromosome c = new Chromosome();
		
		c.exploration = this.exploration;
		c.backprop = this.backprop;
		c.expansion = this.expansion;
		c.evaluation = this.evaluation;
		c.removal = this.removal;
		c.selection = this.selection;
		c.simulation = this.simulation;
		c.depthLimit = this.depthLimit;
		c.simulationLimit = this.simulationLimit;
		
		return c;
	}
	
	public void writeFile(String path, int gNum, int cNum) 
			throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(path + "generation_" + gNum + "/chromosome_" + cNum + ".txt", "UTF-8");
		
		writer.println("Exploration: " + exploration.toString());
		writer.println("Expansion: " + expansion.toString());
		writer.println("Removal: " + removal.toString());
		writer.println("Simulation: " + simulation.toString());
		writer.println("Evaluation: " + evaluation.toString());
		writer.println("Backpropagation: " + backprop.toString());
		writer.println("Selection: " + selection.toString());
		writer.println("Depth Limit: " + depthLimit);
		writer.println("Simulation Limit: " + simulationLimit);
		
		writer.close();
	}
	
	public Chromosome mutate(Random r, double mutationRate){
		Chromosome c = this.clone();
		
		if(r.nextDouble() < mutationRate){
			int value = r.nextInt(GTSParams.EXPLORATION.values().length);
			exploration = GTSParams.EXPLORATION.values()[value];
		}
		if(r.nextDouble() < mutationRate){
			int value = r.nextInt(GTSParams.EXPANSION.values().length);
			expansion = GTSParams.EXPANSION.values()[value];
		}
		if(r.nextDouble() < mutationRate){
			int value = r.nextInt(GTSParams.REMOVAL.values().length);
			removal = GTSParams.REMOVAL.values()[value];
		}
		if(r.nextDouble() < mutationRate){
			int value = r.nextInt(GTSParams.SIMULATION.values().length);
			simulation = GTSParams.SIMULATION.values()[value];
		}
		if(r.nextDouble() < mutationRate){
			int value = r.nextInt(GTSParams.EVALUATION.values().length);
			evaluation = GTSParams.EVALUATION.values()[value];
		}
		if(r.nextDouble() < mutationRate){
			int value = r.nextInt(GTSParams.BACKPROPAGATION.values().length);
			backprop = GTSParams.BACKPROPAGATION.values()[value];
		}
		if(r.nextDouble() < mutationRate){
			int value = r.nextInt(GTSParams.SELECTION.values().length);
			selection = GTSParams.SELECTION.values()[value];
		}
		if(r.nextDouble() < mutationRate){
			depthLimit = r.nextInt(11) - 1;
		}
		if(r.nextDouble() < mutationRate){
			simulationLimit = r.nextInt(10);
		}
		
		return c;
	}
	
	public Chromosome[] crossOver(Random r, double crossoverRate, Chromosome c){
		Chromosome[] children = {this.clone(), c.clone()};
		
		if(r.nextDouble() < crossoverRate){
			children[0].exploration = c.exploration;
			children[1].exploration  = this.exploration;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].expansion = c.expansion;
			children[1].expansion = this.expansion;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].removal = c.removal;
			children[1].removal = this.removal;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].simulation = c.simulation;
			children[1].simulation = this.simulation;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].evaluation = c.evaluation;
			children[1].evaluation = this.evaluation;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].backprop = c.backprop;
			children[1].backprop = this.backprop;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].selection = c.selection;
			children[1].selection = this.selection;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].depthLimit = c.depthLimit;
			children[1].depthLimit = this.depthLimit;
		}
		if(r.nextDouble() < crossoverRate){
			children[0].simulationLimit = c.simulationLimit;
			children[1].simulationLimit = this.simulationLimit;
		}
		
		return children;
	}
	
	public double getFitness(String path, int gNum, int cNum){
		String[] lines = new tools.IO().readFile(path + "generation_" + gNum + "/chromosome_" + cNum + ".csv");
		
		String[] parts = lines[lines.length - 2].split(",");
		averageWins = Double.parseDouble(parts[1]);
		averageScore = Double.parseDouble(parts[2]);
		averageTime = Double.parseDouble(parts[3]);
		fitness = averageWins * 1000 + averageScore;
		
		return fitness;
	}

	@Override
	public int compareTo(Chromosome c) {
		if(this.fitness < c.fitness){
			return 1;
		}
		
		if(this.fitness > c.fitness){
			return -1;
		}
		
		return 0;
	}
}
