import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;

public class Main {
	public static void main(String[] args) {
		MinScheduler s = new MinScheduler("hello", 4, 10, 7);
	
		int[][] preferences = {
		};
		s.runThrough(preferences);
		
	}
}
