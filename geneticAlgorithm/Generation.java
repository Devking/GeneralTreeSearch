package geneticAlgorithm;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import controllers.GeneralTreeSearch.GTSParams;

public class Generation {
	public int gNum;
	public ArrayList<Chromosome> chromosomes;
	
	public double averageWins;
	public double averageScores;
	public double averageTimes;
	public double averageFitness;
	
	public double stdWins;
	public double stdScores;
	public double stdTimes;
	public double stdFitness;
	
	public Generation(){
		chromosomes = new ArrayList<Chromosome>();
	}
	
	public void randomInitiazation(Random r, int populationSize, String path) throws NumberFormatException, IOException{
		File[] files = new File(path).listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().contains("chromosome");
			}
			
		});
		
		for(int i=0; i<populationSize - files.length; i++){
			Chromosome c = new Chromosome();
			
			int value = r.nextInt(GTSParams.EXPLORATION.values().length);
			c.exploration = GTSParams.EXPLORATION.values()[value];
			value = r.nextInt(GTSParams.EXPANSION.values().length);
			c.expansion = GTSParams.EXPANSION.values()[value];
			value = r.nextInt(GTSParams.REMOVAL.values().length);
			c.removal = GTSParams.REMOVAL.values()[value];
			value = r.nextInt(GTSParams.SIMULATION.values().length);
			c.simulation = GTSParams.SIMULATION.values()[value];
			value = r.nextInt(GTSParams.EVALUATION.values().length);
			c.evaluation = GTSParams.EVALUATION.values()[value];
			value = r.nextInt(GTSParams.BACKPROPAGATION.values().length);
			c.backprop = GTSParams.BACKPROPAGATION.values()[value];
			value = r.nextInt(GTSParams.SELECTION.values().length);
			c.selection = GTSParams.SELECTION.values()[value];
			c.depthLimit = r.nextInt(11) - 1;
			c.simulationLimit = r.nextInt(10);
			
			chromosomes.add(c);
		}
		
		for(int i=0; i<files.length; i++){
			chromosomes.add(new Chromosome(files[i].getPath()));
		}
	}
	
	private void writeChromosomes(String path) throws FileNotFoundException, UnsupportedEncodingException{
		File gFolder = new File(path + "generation_" + gNum);
		gFolder.mkdir();
		
		for(int i=0; i<chromosomes.size(); i++){
			chromosomes.get(i).index = i;
			chromosomes.get(i).writeFile(path, gNum, i);
		}
	}
	
	private void runFitnessProcess(int repetition, int gNum, int cNum, String gameName) throws IOException{
		ProcessBuilder process = new ProcessBuilder("java", "-jar", "evaluateChromosome.jar", 
				String.valueOf(repetition), String.valueOf(gNum), String.valueOf(cNum), gameName);
		process.redirectOutput(new File("/dev/null"));
		process.redirectError(new File("errors/g_" + gNum + "_c_" + cNum + ".txt"));
		process.start();
	}
	
	private void calculateFitness(String path, int numProcess, int repetition, String gameName) throws IOException, InterruptedException{
		File gFolder = new File(path + "generation_" + gNum);
		gFolder.mkdir();
		
		File[] resultFiles = {};
		HashMap<String, Boolean> doneChromosomes = new HashMap<String, Boolean>();
		int numberOfFinished = numProcess;
		
		for(int i=0; i<numProcess; i++){
			runFitnessProcess(repetition, gNum, i, gameName);
		}
		
		while(resultFiles.length < chromosomes.size()){
			Thread.sleep(1*1000);
			resultFiles = new File(path + "generation_" + gNum + "/").listFiles(new FileFilter(){
				@Override
				public boolean accept(File f) {
					return f.getName().contains("chromosome");
				}
			});
			
			if(resultFiles != null){
				for(int i=0; i<resultFiles.length; i++){
					if(!doneChromosomes.containsKey(resultFiles[i].getName())){
						doneChromosomes.put(resultFiles[i].getName(), true);
						if(numberOfFinished < chromosomes.size()){
							runFitnessProcess(repetition, gNum, numberOfFinished, gameName);
							numberOfFinished += 1;
						}
					}
				}
			}
		}
	}
	
	private void loadFitness(String path){
		for(int i=0; i<chromosomes.size(); i++){
			chromosomes.get(i).getFitness(path, gNum, i);
		}
	}
	
	private Chromosome tournmentSelection(Random r){
		double totalSum = 0;
		ArrayList<Double> ranks = new ArrayList<Double>();
		for(int i=0; i<chromosomes.size(); i++){
			totalSum += chromosomes.size() - i;
			ranks.add(totalSum);
		}
		
		double prob = r.nextDouble();
		for(int i=0; i<chromosomes.size(); i++){
			ranks.set(i, ranks.get(i) / totalSum);
			if(prob < ranks.get(i)){
				return chromosomes.get(i);
			}
		}
		
		return null;
	}
	
	private void writeGeneration(String resultPath) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(resultPath + "generation_" + gNum + "/total.csv", "UTF-8");
		
		writer.println("Num,\tWins,\tScores,\tTimes,\tFitness");
		for(int i=0; i<this.chromosomes.size(); i++){
			writer.println(this.chromosomes.get(i).index + ",\t" + this.chromosomes.get(i).averageWins + 
					",\t" + this.chromosomes.get(i).averageScore + ",\t" + this.chromosomes.get(i).fitness);
			averageWins += this.chromosomes.get(i).averageWins;
			averageScores += this.chromosomes.get(i).averageScore;
			averageTimes += this.chromosomes.get(i).averageTime;
			averageFitness += this.chromosomes.get(i).fitness;
			stdWins += Math.pow(this.chromosomes.get(i).averageWins, 2);
			stdScores += Math.pow(this.chromosomes.get(i).averageScore, 2);
			stdTimes += Math.pow(this.chromosomes.get(i).averageTime, 2);
			stdFitness += Math.pow(this.chromosomes.get(i).fitness, 2);
		}
		averageWins /= this.chromosomes.size();
		averageScores /= this.chromosomes.size();
		averageTimes /= this.chromosomes.size();
		averageFitness /= this.chromosomes.size();
		stdWins = Math.sqrt(stdWins / this.chromosomes.size() - Math.pow(averageWins, 2));
		stdScores = Math.sqrt(stdScores / this.chromosomes.size() - Math.pow(averageScores, 2));
		stdTimes = Math.sqrt(stdTimes / this.chromosomes.size() - Math.pow(averageTimes, 2));
		stdFitness = Math.sqrt(stdFitness / this.chromosomes.size() - Math.pow(averageFitness, 2));
		
		writer.println("");
		writer.println("average,\t" + averageWins + ",\t" + averageScores + ",\t" + averageTimes + ",\t" + averageFitness);
		writer.println("std,\t" + stdWins + ",\t" + stdScores + ",\t" + stdTimes + ",\t" + stdFitness);
		
		writer.close();
	}
	
	public Generation getNextGeneration(Random r, String paramPath, String resultPath, int elitism,
			double crossoverRate, double mutationRate, int numProcess, int repetition, String gameName) throws IOException, InterruptedException{
		Generation g = new Generation();
		g.gNum = this.gNum + 1;
		
		this.writeChromosomes(paramPath);
		this.calculateFitness(resultPath, numProcess, repetition, gameName);
		this.loadFitness(resultPath);
		this.chromosomes.sort(null);
		while(g.chromosomes.size() < this.chromosomes.size() - elitism){
			Chromosome c1 = this.tournmentSelection(r);
			Chromosome c2 = this.tournmentSelection(r);
			
			Chromosome[] cs = c1.crossOver(r, crossoverRate, c2);
			cs[0].mutate(r, mutationRate);
			cs[1].mutate(r, mutationRate);
			
			g.chromosomes.add(cs[0]);
			g.chromosomes.add(cs[1]);
		}
		for(int i=0; i<elitism; i++){
			g.chromosomes.add(this.chromosomes.get(i).clone());
		}
		this.writeGeneration(resultPath);
		
		return g;
	}
}
