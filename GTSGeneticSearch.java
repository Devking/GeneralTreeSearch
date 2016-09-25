import core.competition.CompetitionParameters;
import geneticAlgorithm.GeneticAlgorithm;

public class GTSGeneticSearch {
	
	public static void main(String[] args){
		GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();
		
		String[] parts = new tools.IO().readFile(args[0])[1].split(",");
		
		geneticAlgorithm.crossoverRate = Double.parseDouble(parts[0].trim());
		geneticAlgorithm.mutationRate = Double.parseDouble(parts[1].trim());
		geneticAlgorithm.elitism = Integer.parseInt(parts[2].trim());
		geneticAlgorithm.numProcess = Integer.parseInt(parts[3].trim());
		geneticAlgorithm.numGen = Integer.parseInt(parts[4].trim());
		geneticAlgorithm.repetition = Integer.parseInt(parts[5].trim());
		geneticAlgorithm.populationSize = Integer.parseInt(parts[6].trim());
		geneticAlgorithm.gameName = parts[7].trim();
		geneticAlgorithm.paramPath = "examples/parameters/";
		geneticAlgorithm.resultPath = "examples/results/";
		CompetitionParameters.MAX_TIMESTEPS = Integer.parseInt(parts[8].trim());
		
		try {
			geneticAlgorithm.runGenetic();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
