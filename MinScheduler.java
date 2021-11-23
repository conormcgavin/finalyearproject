import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public class MinScheduler extends Scheduler {
	IntVar minPreferences;
	IntVar[] scores;
	IntVar minOverallScore;
	int[] penalty;
	
	public MinScheduler(String modelName, int num_workers, int hours_per_day, int days_per_week) {
		super(modelName, num_workers, hours_per_day, days_per_week);
		
		this.minPreferences = model.intVar("min", 0, 1000);
		this.penalty = new int[this.num_workers];
		this.scores = new IntVar[this.num_workers];
		
		for (int i=0; i<num_workers; i++) {
			this.penalty[i] = 0;
			this.scores[i] = model.intVar("score" + i, 0, MAX_PREFERENCES);
		}
		this.minOverallScore = this.model.intVar("overall score", 0, MAX_PREFERENCES);
	} 
	
	public void optimise() {
		super.optimise();
		for (int i=0; i<this.num_workers; i++) {
			if (this.penalty[i] > 0) {
				this.model.arithm(this.totalPreferences[i], "=", this.minOverallScore, "-", this.penalty[i]).post();
			}
			this.model.arithm(this.scores[i], "=", this.totalPreferences[i], "-", this.penalty[i]).post();
		}
		
		this.model.min(this.minOverallScore, this.scores).post();
		this.model.setObjective(Model.MAXIMIZE, this.minOverallScore);
	}
	
	public void printScores() {
		System.out.println("Minimum Amount of Preferences Given Out: " + this.minPreferences.getValue());
		System.out.println("Preferences Per Person: ");
		for (int i=0; i<num_workers; i++) {
			System.out.println("Worker " + i + ": " +  + this.totalPreferences[i].getValue());
		}
		System.out.println();
		System.out.println("Penalty Per Person: ");
		for (int i=0; i<num_workers; i++) {
			System.out.println("Worker " + i + ": " +  + this.penalty[i]);
		}
	}
	
	public int[] analyseScoresAndApplyPenalty() {
		int min = this.minPreferences.getValue();
		for (int i=0; i<num_workers; i++) {
			if (this.totalPreferences[i].getValue() > min) {
				this.penalty[i] += (this.totalPreferences[i].getValue() - min);
			} else if (this.totalPreferences[i].getValue() < min) {
				this.penalty[i] -= (min - this.totalPreferences[i].getValue());
			}
		}
		return this.penalty;
	}
	
	public int[] runThrough(int[][] preferences) {
		this.clearPrefs();
		for (int i=0; i<preferences.length; i++) {
			this.addPref(preferences[i][0], preferences[i][1], preferences[i][2], preferences[i][3]);
		}
		this.optimise();
		this.solve();
		this.printSolution();
		this.penalty = this.analyseScoresAndApplyPenalty();
		this.printScores();
		return penalty;
	}
	
	public int[] runThrough(int[][] preferences, int[][]hardPreferences) {
		this.clearPrefs();
		for (int i=0; i<preferences.length; i++) {
			this.addPref(preferences[i][0], preferences[i][1], preferences[i][2], preferences[i][3]);
		}
		for (int i=0; i<hardPreferences.length; i++) {
			this.addPrefAsHard(hardPreferences[i][0], hardPreferences[i][1], hardPreferences[i][2], hardPreferences[i][3]);
		}
		this.optimise();
		this.solve();
		this.printSolution();
		this.penalty = this.analyseScoresAndApplyPenalty();
		this.printScores();
		return penalty;
	}
 }
