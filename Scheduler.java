import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;

public class Scheduler {
	
	Model model;
	
	int num_workers;
	int hours_per_day;
	int days_per_week;
	
	static int MAX_PREFERENCES = 10000;
	
	int[] workers_min_hours_per_week = {40, 10, 10, 20};
	int[] workers_max_hours_per_week = {40, 50, 40, 60};
	int[] workers_needed_per_hour = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
									 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
									 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
									 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
									 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
									 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
									 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	
	// skills: cross reference this list with the hours, and for whoever is assigned to it, check the
	// workers_skill_level to see if they are skilled enough
	int[] skill_list = {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 
						1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 
						1, 1, 0, 0, 2, 2, 2, 0, 0, 0, 
						0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 
						0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 
						0, 0, 0, 0, 0, 0, 0, 3, 3, 3,
						3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
	
	int[] workers_skill_level = {3, 1, 1, 3};
	
	IntVar[][] timetable;
	IntVar[][] rows, cols;
	IntVar[][][] days;
	IntVar[] totals;
	
	ArrayList<ArrayList<BoolVar>> preferences;
	IntVar[] totalPreferences;
	
	public Scheduler(String modelName, int num_workers, int hours_per_day, int days_per_week) {
		Model model = new Model(modelName);
		this.model = model;
		this.num_workers = num_workers;
		this.hours_per_day = hours_per_day;
		this.days_per_week = days_per_week;
		
		IntVar[][] timetable = this.model.intVarMatrix("timetable", num_workers, hours_per_day * days_per_week, 0, 1);
		this.timetable = timetable;
		
		this.rows = new IntVar[num_workers][hours_per_day * days_per_week];
        this.cols = new IntVar[hours_per_day * days_per_week][num_workers];
        this.days = new IntVar[days_per_week][num_workers][hours_per_day];
        this.totals = new IntVar[hours_per_day * days_per_week * num_workers];
        
        for (int i=0; i<days_per_week; i++) {
        	days[i] = new IntVar[num_workers][hours_per_day];
        }
		
		 for (int i = 0; i < num_workers; i++) {
	         for (int j = 0; j < hours_per_day * days_per_week; j++) {
	        	 rows[i][j] = timetable[i][j];
	        	 cols[j][i] = rows[i][j];
	        	 totals[(i * hours_per_day * days_per_week) + j] = rows[i][j];
	         }
		 }
		 
		 int day, count;
		 for (int i=0; i<rows.length; i++) {
			 day = 0;
			 count = 0;
			 for (int j=0; j<hours_per_day*days_per_week; j++) {
				 days[day][i][count] = rows[i][j];
				 count++;
				 if (count == hours_per_day) {
					 count = 0;
					 day++;
				 }
			 }
		 }
		 this.days = days;
		 
		 this.preferences = new ArrayList<ArrayList<BoolVar>>();
		 this.totalPreferences = new IntVar[this.num_workers];
		 
		 for (int i=0; i<num_workers; i++) {
			this.preferences.add(new ArrayList<BoolVar>());
			this.totalPreferences[i] = model.intVar("total pref " + i, 0, MAX_PREFERENCES);
		}
		
		this.addHardConstraints();
	}
	
	public void addHardConstraints() {
		// -------CONSTRAINTS---------
		
		// the total of the entire matrix should add up to the sum of workers_hours_per_week
		int sum_workers_needed = 0;
	    for (int value : workers_needed_per_hour) {
	        sum_workers_needed += value;
	    }
		this.model.sum(this.totals, "=", sum_workers_needed).post();
		
		
		// the total hours in any one row in each 10 hour interval should be <= 8
		for (int i=0; i<this.num_workers; i++) {
			for (int d=0; d<this.days.length; d++)
			this.model.sum(this.days[d][i], "<=", 8).post();
		}
		
		// workers must work between their working hour limits every week
		for (int i=0; i<this.rows.length; i++) {
			this.model.sum(this.rows[i], "<=", workers_max_hours_per_week[i]).post();
			this.model.sum(this.rows[i], ">=", workers_min_hours_per_week[i]).post();
		}
		
		
		// the minimum hours in any row in each 10 hour interval, if not 0, should be >= 3
		for (int i=0; i<this.num_workers; i++) {
			for (int d=0; d<this.days.length; d++) {
				this.model.ifThen(
					this.model.sum(this.days[d][i], ">", 0), 
					this.model.sum(this.days[d][i], ">=", 3)
				);
			}
		}
		

		// every column should have a sum of exactly the sum of workers needed in that hour
		for (int i=0; i<this.cols.length; i++) {
			this.model.sum(this.cols[i], "=", workers_needed_per_hour[i]).post();
		}	
		

		// for each row, if this.rows[i][j] == 1 then skills_list[j] must be in workers_skills_matrix[i]
		for (int i=0; i<this.rows.length; i++) {
			for (int j=0; j<this.rows[i].length; j++)
				if (skill_list[j] > workers_skill_level[i]) {
					this.model.arithm(this.rows[i][j], "!=", 1).post();
			}
		}
		
		
		// no split shifts
		IAutomaton auto = new FiniteAutomaton("0*1*0*");
		for (int d=0; d < this.days.length; d++) {
			for (int i = 0; i < this.timetable.length; i++) {
				this.model.regular(this.days[d][i], auto).post();
			}
		}
	}
	
	public void addPref(int person, int day, int hour_s, int hour_f) {
		IntVar[] preference = new IntVar[hour_f - hour_s];
		int index = 0;
		for (int i=hour_s + day*this.hours_per_day; i<hour_f +day*this.hours_per_day; i++) {
			preference[index++] = this.rows[person][i];
		}
		this.preferences.get(person).add(this.model.sum(preference, "=", 0).reify());
	}
	
	public void addPrefAsHard(int person, int day, int hour_s, int hour_f) {
		IntVar[] preference = new IntVar[hour_f - hour_s];
		int index = 0;
		for (int i=hour_s + day*this.hours_per_day; i<hour_f +day*this.hours_per_day; i++) {
			preference[index++] = this.rows[person][i];
		}
		this.model.sum(preference, "=", 0).post();
	}
	
	public void clearPrefs() {
		this.preferences.clear();
	}
	
	public void optimise() {
		for (int i=0; i<num_workers; i++) {
			if (this.preferences.get(i).size() > 0) {
				BoolVar[] preferencesArr = this.preferences.get(i).toArray(new BoolVar[this.preferences.get(i).size()]); // convert to normal arrays
				this.model.sum(preferencesArr, "=", this.totalPreferences[i]).post(); // store number of preferences for each worker in totalPreferences[i]
			}
		}
	}
	
	public void solve() {
		Solver solver = this.model.getSolver();
		solver.solve();		
	}
	
	public void printSolution() {
		String row_sol;
		for (int i = 0; i < this.timetable.length; i++) {
			int count = 0;
			row_sol = "Worker " + i + ":\t";
			for (int j = 0; j < this.timetable[i].length; j++) {
				row_sol += this.timetable[i][j].getValue() + "\t";
				count += 1;
				if (count == this.hours_per_day) {
					row_sol += " | \t";
					count = 0;
				}
			}
			System.out.println(row_sol);
			
		}	
		System.out.print("\n");
	}
}
