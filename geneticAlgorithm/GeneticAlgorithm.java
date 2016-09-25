package geneticAlgorithm;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class GeneticAlgorithm {
	private Random r;
	
	public int numGen;
	public String paramPath;
	public String resultPath;
	public String startingPath;
	public int elitism;
	public double crossoverRate;
	public double mutationRate;
	public int numProcess;
	public int repetition;
	public String gameName;
	public int populationSize;
	
	public GeneticAlgorithm(){
		r = new Random();
		numGen = 100;
		paramPath = "examples/parameters/";
		resultPath = "examples/results/";
		startingPath = "examples/starting/";
		elitism = 0;
		crossoverRate = 0.4;
		mutationRate = 0.01;
		numProcess = 20;
		repetition = 30;
		populationSize = 40;
		gameName = "zelda";
	}
	
	public void runGenetic() throws IOException, InterruptedException{
		PrintWriter writer = new PrintWriter(resultPath + "overall.csv", "UTF-8");
		writer.println("GenNum,\tBestWins,\tBestScore,\tBestTime,\tBestFitness,"
				+ "\tAverageWins,\tAverageScore,\tAverageTime,\tAverageFitness,"
				+ "\tStdWins,\tStdScore,\tStdTime,\tStdFitness");
		
		Generation g = new Generation();
		g.randomInitiazation(r, populationSize, startingPath);
		
		for(int i=0; i<numGen; i++){
			Generation newGen = g.getNextGeneration(r, paramPath, resultPath, elitism, crossoverRate, 
					mutationRate, numProcess, repetition, gameName);
			writer.println(g.gNum + ",\t" + g.chromosomes.get(0).averageWins + ",\t" 
					+ g.chromosomes.get(0).averageScore + ",\t" 
					+ g.chromosomes.get(0).averageTime + ",\t" + g.chromosomes.get(0).fitness + ",\t" 
					+ g.averageWins + ",\t" + g.averageScores + ",\t" + g.averageTimes 
					+ ",\t" + g.averageFitness + ",\t" 
					+ g.stdWins + ",\t" + g.stdScores + ",\t" + g.stdTimes + ",\t" + g.stdFitness);
			g = newGen;
		}
		
		writer.println(g.gNum + ",\t" + g.chromosomes.get(0).averageWins + ",\t" 
				+ g.chromosomes.get(0).averageScore + ",\t" 
				+ g.chromosomes.get(0).averageTime + ",\t" + g.chromosomes.get(0).fitness + ",\t" 
				+ g.averageWins + ",\t" + g.averageScores + ",\t" + g.averageTimes 
				+ ",\t" + g.averageFitness + ",\t" 
				+ g.stdWins + ",\t" + g.stdScores + ",\t" + g.stdTimes + ",\t" + g.stdFitness);
		
		writer.close();
	}
}
