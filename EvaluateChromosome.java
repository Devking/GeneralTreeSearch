import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import core.ArcadeMachine;
import tools.GameResult;

public class EvaluateChromosome {
	
	public static void main(String[] args){
		if(args.length < 4){
			return;
		}
		
		String gamesPath = "examples/gridphysics/";
		String parameterPath = "examples/parameters/";
		String resultPath = "examples/results/";
		
		boolean visuals = false;
        String recordActionsFile = null;
        int seed = new Random().nextInt();
        int levelIdx = 0;
        String agentName = "controllers.GeneralTreeSearch.Agent";
        
		
		int numberOfRepetition = Integer.parseInt(args[0]); 
		controllers.GeneralTreeSearch.Agent.filename = parameterPath + "generation_" + args[1] + 
				"/chromosome_" + args[2] + ".txt";
		String gameFile = gamesPath + args[3] + ".txt";
		String levelFile = gamesPath + args[3] + "_lvl" + levelIdx + ".txt";
		
		ArrayList<GameResult> results = new ArrayList<GameResult>();
		for(int i=0; i< numberOfRepetition; i++){
			GameResult r = ArcadeMachine.runOneGame(gameFile, levelFile, visuals, agentName, recordActionsFile, seed);
			if(r.invalid){
				numberOfRepetition += 1;
			}
			else{
				results.add(r);
			}
		}
		
		try {
			PrintWriter file = new PrintWriter(resultPath + "generation_" + args[1] + "/results_" + args[2] + ".csv", "UTF-8");
			file.println("Run,\tWin,\tScore,\tTime");
			double totalWins = 0;
			double totalScore = 0;
			double totalTime = 0;
			for(int i=0; i< results.size(); i++){
				double win = 0;
				if(results.get(i).win){
					win = 1;
				}
				totalWins += win;
				totalScore += results.get(i).score;
				totalTime += results.get(i).time;
				file.println((i + 1) + ",\t" + win + ",\t" + results.get(i).score + ",\t" + results.get(i).time);
			}
			file.println("#,\t" + (totalWins / results.size()) + ",\t" + 
					(totalScore / results.size()) + ",\t" + (totalTime / results.size()));
			file.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
