import org.chocosolver.solver.Model;

public class SameScheduler extends Scheduler {	
	public SameScheduler(String modelName, int num_workers, int hours_per_day, int days_per_week) {
		super(modelName, num_workers, hours_per_day, days_per_week);
	}
	
	public void optimise() {
		super.optimise();
		this.model.allEqual(this.totalPreferences).post();
		this.model.setObjective(Model.MAXIMIZE, this.totalPreferences[0]);
	}

	
	public void printScores() {
		System.out.println("Amount of Preferences Each:" + this.totalPreferences[0].getValue());
	}
	
	public void runThrough(int[][] preferences) {
		this.clearPrefs();
		for (int i=0; i<preferences.length; i++) {
			this.addPref(preferences[i][0], preferences[i][1], preferences[i][2], preferences[i][3]);
		}
		this.optimise();
		this.solve();
		this.printSolution();
		this.printScores();
	}
}
