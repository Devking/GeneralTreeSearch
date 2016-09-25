package tools;

public class GameResult {
	public boolean invalid;
	public boolean win;
	public double score;
	public double time;
	
	public GameResult(boolean win, double score, double time, boolean invalid){
		this.win = win;
		this.score = score;
		this.time = time;
		this.invalid = invalid;
	}
}
