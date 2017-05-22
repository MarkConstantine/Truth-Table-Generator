import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class TruthTable {
	
	// Input Chars
	private static final char NOT = '~';
	private static final char AND = '*';
	private static final char OR = '+';
	private static final char COND = '>';
	private static final char BICOND = '=';
	private static final char LEFT_PAREN = '(';
	private static final char RIGHT_PAREN = ')';
	private static final char TRUE = 'T';
	private static final char FALSE = 'F';
	
	private HashMap<Character, Integer> precedence = new HashMap<Character, Integer>();
	private ArrayList<Character> variables = new ArrayList<Character>();
	private ArrayList<String> inValues = new ArrayList<String>();
	private ArrayList<Character> outValues = new ArrayList<Character>();
	private String infix = "";
	private String postfix = "";
	private boolean isValid = false;
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		try {
			while (true) {
			
				System.out.print("Enter a boolean expression: ");
				String input = scanner.nextLine();
				
				if (input.isEmpty()) {
					break;
				}
				
				String formatted = "";
				
				// Format input string by ignoring spaces.
				for (int i = 0; i < input.length(); i++) {
					char c = input.charAt(i);
					if (c != ' ') {
						formatted += c;
					}
				}
				
				TruthTable t = new TruthTable(formatted);
				t.print();
			
			}
		} finally {
			System.out.println("Have a good day!");
			scanner.close();
		}
		
	}
	
	
	
	TruthTable(String infix) {
		initOpPrecedence();
		if (errorCheck(infix)) {
			toPostfix(infix);
			getVariables();
			generateAllInputValues();
			evaluate();
		}
	}

	
	
	public String getInfix() {
		return infix;
	}
	
	public String getPostfix() {
		return postfix;
	}
	
	public void print() {
		if (isValid) {
			// Header Information
			for (int i = 0; i < variables.size(); i++) {
				System.out.print(variables.get(i));
			}
			System.out.println(" | " + infix);
			
			// NOTE: inValues.size() == outValues.size() 
			for (int i = 0; i < inValues.size(); i++) {
				System.out.println(inValues.get(i) + " | " + outValues.get(i));
			}
		} else {
			System.out.println("Error. Invalid expression.");
		}
	}
	
	
	
	private void initOpPrecedence() {
		// Larger Number == Higher Precedence
		precedence.put(NOT, 5);
		precedence.put(AND, 4);
		precedence.put(OR, 3);
		precedence.put(COND, 2);
		precedence.put(BICOND, 1);
		// "No" Precedence Operators (Dealt with separately on the stack)
		precedence.put(LEFT_PAREN, 0);
		precedence.put(RIGHT_PAREN, 0);
	}
	
	private boolean isOperator(char c) {
		if (c == NOT || c == AND || c == OR || c == COND || c == BICOND || c == LEFT_PAREN || c == RIGHT_PAREN) {
			return true;
		}
		return false;
	}
	
	private boolean isVariable(char c) {
		if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
			return true;
		}
		return false;
	}
	
	private boolean errorCheck(String expression) {
		for (int i = 0; i < expression.length(); i++) {
			char inputChar = expression.charAt(i);
			// Check if valid input characters.
			if (!isOperator(inputChar)) {
				if (!isVariable(inputChar)) {
					return false;
				}
			}
		}
		infix = expression;
		return true;
	}
	
	private void toPostfix(String infix) {
		Stack<Character> operatorStack = new Stack<Character>();
		
		for (int i = 0; i < infix.length(); i++) {
			char c = infix.charAt(i);
			
			if (isVariable(c)) {
				postfix += c;
			} else if (c == LEFT_PAREN) {
				operatorStack.push(c);
			} else if (c == RIGHT_PAREN) {
				while (operatorStack.peek() != LEFT_PAREN) {
					postfix += operatorStack.pop();
				}
				operatorStack.pop(); // Remove the left parenthesis from stack.
			} else if (isOperator(c)) {
				while (!operatorStack.isEmpty() && precedence.get(operatorStack.peek()) > precedence.get(c)) {
					postfix += operatorStack.pop();
				}
				operatorStack.push(c);
			}
		}
		
		// Add remaining operators on the stack.
		while (!operatorStack.isEmpty()) {
			postfix += operatorStack.pop();
		}
	}
	
	private void getVariables() {
		for (int i = 0; i < postfix.length(); i++) {
			char c = postfix.charAt(i);
			if (isVariable(c) && !variables.contains(c)) {
				variables.add(c);
			}
		}
	}
	
	private void generateAllInputValues() {
		// NOTE: Each string.size = N
		// NOTE: Size of ArrayList = 2^N
		int n = variables.size();
		for (int i = 0; i < (int)Math.pow(2, n); i++) {
			String binary = Integer.toBinaryString(i);
			
			// Add leading zeros to converted binary string.
			if (binary.length() < n) {
				int leadingZerosAmount = n - binary.length();
				String leadingZeros = "";
				for (int z = 0; z < leadingZerosAmount; z++) {
					leadingZeros += "0";
				}
				leadingZeros += binary;
				binary = leadingZeros;
			}
			
			// Convert to 0 to F or 1 to T.
			binary = binary.replace('0', FALSE);
			binary = binary.replace('1', TRUE);
			
			// Store into ArrayList
			inValues.add(i, binary);
		}
	}
	
	private boolean evaluate() {
		Stack<Boolean> operandStack = new Stack<Boolean>();
		boolean LHS = false;
		boolean RHS = false;
		
		// For every truth value.
		for (int inputPos = 0; inputPos < inValues.size(); inputPos++) {
			String inputs = inValues.get(inputPos); // example: "FFF"
			
			// Start evaluating
			for (int i = 0; i < postfix.length(); i++) {
				char c = postfix.charAt(i);
				
				if (variables.contains(c)) {
					// Char index in variables = Char index in inputs string.
					c = inputs.charAt(variables.indexOf(c));
				}
				
				switch (c) {
					case TRUE:
						operandStack.push(true);
						break;
					case FALSE:
						operandStack.push(false);
						break;
					case NOT:
						operandStack.push(!operandStack.pop());
						break;
					case AND:
						RHS = operandStack.pop();
						LHS = operandStack.pop();
						operandStack.push(LHS && RHS);
						break;
					case OR:
						RHS = operandStack.pop();
						LHS = operandStack.pop();
						operandStack.push(LHS || RHS);
						break;
					case COND:
						RHS = operandStack.pop();
						LHS = operandStack.pop();
						operandStack.push(!LHS || RHS);
						break;
					case BICOND:
						RHS = operandStack.pop();
						LHS = operandStack.pop();
						operandStack.push((LHS && RHS) || (!LHS && !RHS));
						break;
					default:
						return false;
				}		
			}
			
			// Invalid boolean expression.
			// By definition, it must have one remaining value on stack.
			if (operandStack.size() != 1) {
				isValid = false;
				return false;
			}
			
			if (operandStack.pop()) {
				// NOTE: outValues is ArrayList<Character>.
				outValues.add(inputPos, TRUE);
			} else {
				outValues.add(inputPos, FALSE);
			}
		}
		isValid = true;
		return true;
	}
}
