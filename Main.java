public class Main {
	public static void main(String[] args) {
		MinScheduler m = new MinScheduler("hello", 4, 10, 7);
		SameScheduler s = new SameScheduler("hello", 4, 10, 7);
		TotalScheduler t = new TotalScheduler("hello", 4, 10, 7);
		
		int[][] preferences = {{0, 5, 0, 1},
							   {1, 6, 0, 1},
							   {2, 2, 0, 1},
							   {3, 3, 0, 1},
							   {0, 5, 0, 1},
							   {1, 6, 0, 1},
							   {2, 2, 0, 1},
							   {3, 3, 0, 1}};
		

		//s.runThrough(preferences);
		//t.credit = t.runThrough(preferences);
		//t.credit = t.runThrough(preferences);
		//t.credit = t.runThrough(preferences);
		
		int[][] hardPreferences = {preferences[1],
				   preferences[2]};
		
		t.credit = t.runThrough(preferences, hardPreferences );
		
	}
}
