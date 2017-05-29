
public class Operator {

	public char inputChar;
	public char outputChar;
	public int precedence = 0;
	public int connective = 0;
	
	public Operator(char inChar, char outChar, int precedence, int connective) {
		this.inputChar = inChar;
		this.outputChar = outChar;
		this.precedence = precedence;
		this.connective = connective;
	}
	
}
