import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import core.ArcadeMachine;
import tools.GameResult;

public class ValidationRunner {
	
	public static void main(String[] args) throws IOException{
		if(args.length < 5){
			return;
		}
		
		String gamesPath = "examples/gridphysics/";
		String resultPath = args[4];
		
		boolean visuals = false;
        String recordActionsFile = null;
        int seed = new Random().nextInt();
        int levelIdx = 0;
        String agentName = "controllers.GeneralTreeSearch.Agent";
		
		int numberOfRepetition = Integer.parseInt(args[1]); 
		core.competition.CompetitionParameters.MAX_TIMESTEPS = Integer.parseInt(args[2]);
		controllers.GeneralTreeSearch.Agent.filename = args[3] + ".txt";
		File[] games = new File(gamesPath).listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return !pathname.getName().contains("_");
			}
			
		});
		
		PrintWriter file = new PrintWriter(resultPath + args[0] + ".csv", "UTF-8");
		file.println("Game,\tWin,\tScore,\tTime");
		file.close();
		ArrayList<GameResult> results = new ArrayList<GameResult>();
		ArrayList<String> gameName = new ArrayList<String>();
		for(int j=0; j<games.length; j++){
			String gameFile = games[j].getPath();
			String levelFile = gamesPath + games[j].getName().substring(0, games[j].getName().length() - 4) + "_lvl" + levelIdx + ".txt";
			
			for(int i=0; i< numberOfRepetition; i++){
				GameResult r = ArcadeMachine.runOneGame(gameFile, levelFile, visuals, agentName, recordActionsFile, seed);
				if(r.invalid){
					i -= 1;
				}
				else{
					results.add(r);
					gameName.add(games[j].getName().substring(0, games[j].getName().length() - 4));
				    file = new PrintWriter(new BufferedWriter(new FileWriter(resultPath + args[0] + ".csv", true)));
					file.println(games[j].getName().substring(0, games[j].getName().length() - 4) + ",\t" + (r.win?"1":"0") + ",\t" + r.score + ",\t" + r.time);
					file.close();
				}
			}
		}
		double totalWins = 0;
		double totalScore = 0;
		double totalTime = 0;
		double sqrWins = 0;
		double sqrScore = 0;
		double sqrTime = 0;
		for(int i=0; i< results.size(); i++){
			double win = 0;
			if(results.get(i).win){
				win = 1;
			}
			totalWins += win;
			totalScore += results.get(i).score;
			totalTime += results.get(i).time;
			sqrWins += Math.pow(win, 2);
			sqrScore += Math.pow(results.get(i).score, 2);
			sqrTime += Math.pow(results.get(i).time, 2);
		}
		totalWins /= results.size();
		totalScore /= results.size();
		totalTime /= results.size();
		sqrWins = Math.sqrt(sqrWins / results.size() - Math.pow(totalWins, 2));
		sqrScore = Math.sqrt(sqrScore / results.size() - Math.pow(totalScore, 2));
		sqrTime = Math.sqrt(sqrTime / results.size() - Math.pow(totalTime, 2));
		
		file = new PrintWriter(new BufferedWriter(new FileWriter(resultPath + args[0] + ".csv", true)));
		file.println("");
		file.println("average,\t" + totalWins + ",\t" + totalScore + ",\t" + totalTime);
		file.println("std,\t" + sqrWins + ",\t" + sqrScore + ",\t" + sqrTime);
		file.close();
	}
}
