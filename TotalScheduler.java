import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public class TotalScheduler extends Scheduler {
	
	IntVar overallTotalPreferences; // total number of preferences being satisfied over all employees
	IntVar[] scores; // individual scores for workers (preference count * credit multiplier
	IntVar overallScore; // overall score of the whole solution
	int[] credit; // current credit multiplier of the 
	
	public TotalScheduler(String modelName, int num_workers, int hours_per_day, int days_per_week) {
		super(modelName, num_workers, hours_per_day, days_per_week);
		
		this.credit = new int[this.num_workers];
		this.scores = new IntVar[this.num_workers];
		for (int i=0; i<num_workers; i++) {
			this.credit[i] = 10;
			this.scores[i] = model.intVar("scores" + i, 0, MAX_PREFERENCES);
		}
		
		this.overallScore = this.model.intVar("overall score", 0, MAX_PREFERENCES);
		this.overallTotalPreferences = this.model.intVar("overall total score", 0, MAX_PREFERENCES);
	}
	
	public void optimise() {
		super.optimise();
		for(int i=0; i<this.num_workers; i++) {
			this.model.times(this.totalPreferences[i], this.credit[i], this.scores[i]).post(); // apply credit 
		}
		this.model.sum(this.totalPreferences, "=", this.overallTotalPreferences).post(); // store total preferences over all employees
		this.model.sum(this.scores, "=", this.overallScore).post(); // store total score (what we are trying to maximize)
		this.model.setObjective(Model.MAXIMIZE, this.overallScore);
		
	}
	
	public void printScores() {
		System.out.print("\n");
		System.out.println("Overall Total Preferences:" + this.overallTotalPreferences.getValue()); // PRINTING TOTAL PREFERENCES
		System.out.println("Preferences Per Person: ");
		for (int i=0; i<num_workers; i++) {
			System.out.println("Worker " + i + ": " +  + this.totalPreferences[i].getValue());
		}
		System.out.println();
		System.out.println("Credit Per Person: ");
		for (int i=0; i<num_workers; i++) {
			System.out.println("Worker " + i + ": " +  + this.credit[i]);
		}
		System.out.print("\n");
	}
	
	public int[] analyseScoresAndApplyCredit() {
		int total = this.overallTotalPreferences.getValue();
		int avg = total/this.num_workers;
		for (int i=0; i<num_workers; i++) {
			// for each worker, add a .1 credit for each under and -.1 for each over
			if (this.totalPreferences[i].getValue() > avg) {
				this.credit[i] -= (1 * (this.totalPreferences[i].getValue() - avg));
			} else if (this.totalPreferences[i].getValue() < total/this.num_workers) {
				this.credit[i] += (1 * (avg - this.totalPreferences[i].getValue()));
			}
		}
		return this.credit;
	}
	
	public int[] runThrough(int[][] preferences) {
		this.clearPrefs();
		for (int i=0; i<preferences.length; i++) {
			this.addPref(preferences[i][0], preferences[i][1], preferences[i][2], preferences[i][3]);
		}
		this.optimise();
		this.solve();
		this.printSolution();
		this.credit = this.analyseScoresAndApplyCredit();
		this.printScores();
		return credit;
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
		this.credit = this.analyseScoresAndApplyCredit();
		this.printScores();
		return credit;
	}
}
