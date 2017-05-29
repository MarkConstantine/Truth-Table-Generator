import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Scanner;
import java.util.Stack;

public class TruthTable {
	
	// Main Operators
	private Operator not = new Operator('~', '~', 5, 1);
	private Operator and = new Operator('&', '&', 4, 2);
	private Operator or = new Operator('+', '+', 3, 2);
	private Operator cond = new Operator('>', '>', 2, 2);
	private Operator bicond = new Operator('=', '=', 1, 2);
	
	// "Other" Operators
	private Operator leftParen = new Operator('(', '(', 0, 0);
	private Operator rightParen = new Operator(')', ')', 0, 0);
	
	private char TRUE = 'T';
	private char FALSE = 'F';
	
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
				
				TruthTable t = new TruthTable(formatted, true);
				if (t.getIsValid()) {
					t.print();
				} else {
					System.out.println("Error. Invalid expression.");
				}
			}
		} finally {
			System.out.println("Have a good day!");
			scanner.close();
		}
		
	}
	
	
	
	public TruthTable(String infix, boolean isTrueFirst) {
		if (hasValidCharacters(infix)) {
			toPostfix(infix);
			if (errorCheck()) {
				evaluate(isTrueFirst);
			}
		}
	}

	
	
	public String getInfix() {
		return infix;
	}
	
	public String getPostfix() {
		return postfix;
	}
	
	public boolean getIsValid() {
		return isValid;
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
	
	
	
	// From char returns the defined operator.
	private Operator getOperator(char given) {
		if (given == not.inputChar) {
			return not;
		} else if (given == and.inputChar) {
			return and;
		} else if (given == or.inputChar) {
			return or;
		} else if (given == cond.inputChar) {
			return cond;
		} else {
			return bicond;
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
	
	private boolean isOperator(char c) {
		if (c == not.inputChar || c == and.inputChar || c == or.inputChar ||
			c == cond.inputChar || c == bicond.inputChar ||
			c == leftParen.inputChar || c == rightParen.inputChar) {
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
	
	private boolean hasValidCharacters(String expression) {
		// Check if expression has all valid characters.
		for (int i = 0; i < expression.length(); i++) {
			char inputChar = expression.charAt(i);
			if (!isOperator(inputChar)) {
				if (!isVariable(inputChar)) {
					return false;
				}
			}
		}		
		return true;
	}
	
	private void toPostfix(String infix) {
		Stack<Character> operatorStack = new Stack<Character>();
		
		for (int i = 0; i < infix.length(); i++) {
			char c = infix.charAt(i);
			
			if (isVariable(c)) {
				postfix += c;
			} else if (c == leftParen.inputChar) {
				operatorStack.push(c);
			} else if (c == rightParen.inputChar) {
				while (operatorStack.peek() != leftParen.inputChar) {
					postfix += operatorStack.pop();
				}
				operatorStack.pop(); // Remove the left parenthesis from stack.
			} else if (isOperator(c)) {
				while (!operatorStack.isEmpty() && getOperator(operatorStack.peek()).precedence > getOperator(c).precedence) {
					postfix += operatorStack.pop();
				}
				operatorStack.push(getOperator(c).outputChar);
			}
		}
		
		// Add remaining operators on the stack.
		while (!operatorStack.isEmpty()) {
			postfix += operatorStack.pop();
		}
	}
	
	// Constructs an infix expression from the postfix expression.
	// NOTE: unary operators lead to missing parenthesis.
	private void constructInfix(ExpressionTree startNode) {
		if (startNode != null) {
			if (isOperator(startNode.getData())) {
				infix += '(';
			}
			
			constructInfix(startNode.getLeftChild());
			infix += startNode.getData();
			constructInfix(startNode.getRightChild());
			
			if (isOperator(startNode.getData())) {
				infix += ')';
			}
		}
	}
	
	// Generates an infix expression from an expression tree.
	private void toInfix(ExpressionTree mainTree) {
		constructInfix(mainTree);
		
		// Format the infix expression (Place negation in front of expression).
		for (int i = 0; i < infix.length(); i++) {
			if (infix.charAt(i) == not.outputChar) {
				
				// Find the not's left parenthesis.
				for (int j = i; j >= 0; j--) {
					if (infix.charAt(j) == leftParen.outputChar) {
						String firstPart = infix.substring(0, j);
						String negatedExpression = infix.substring(j, i);
						String endPart = infix.substring(i + 1, infix.length());
						
						infix = firstPart + not.outputChar + negatedExpression + endPart;
						break;
					}
				}
			}
		}
		
		// Remove outer left parenthesis for easier viewing.
		if (infix.charAt(0) == '(') {
			infix = infix.substring(1);
		}
		
		// Remove outer right parenthesis for easier viewing.
		if (infix.charAt(infix.length() - 1) == ')') {
			infix = infix.substring(0, infix.length() - 1);
		}
		
	}
	
	// Inputs the postfix expression into an expression tree.
	// If error then invalid boolean expression.
	private boolean errorCheck() {
		Stack<ExpressionTree> expressionStack = new Stack<ExpressionTree>();
		
		try {
			for (int i = 0; i < postfix.length(); i++) {
				char c = postfix.charAt(i);
				if (isVariable(c)) {
					ExpressionTree newNode = new ExpressionTree(c);
					
					expressionStack.push(newNode);
				} else if (c == not.outputChar){
					ExpressionTree newNode = new ExpressionTree(c);
					ExpressionTree leftChild = expressionStack.pop();
					
					newNode.insert(leftChild);
					
					expressionStack.push(newNode);
				} else if (isOperator(c)) { 

					ExpressionTree newNode = new ExpressionTree(c);
					ExpressionTree rightChild = expressionStack.pop();
					ExpressionTree leftChild = expressionStack.pop();
					
					newNode.insert(leftChild);
					newNode.insert(rightChild);
					
					expressionStack.push(newNode);
				}
			}
		} catch (EmptyStackException e) {
			return false;
		}
		
		// Set infix.
		toInfix(expressionStack.pop());
		
		return true;
	}
	
	private void generateAllInputValues(boolean isTrueFirst) {
		getVariables();
		// NOTE: Each string.size = N
		// NOTE: Size of ArrayList inValues = 2^N
		int n = variables.size();
		if (isTrueFirst) {
			int startPoint = (int)Math.pow(2, n) - 1;
			for (int i = startPoint; i >= 0; i--) {
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
				
				inValues.add(startPoint - i, binary);
			}
		} else {
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
				
				inValues.add(i, binary);
			}
		}
	}
	
	// Evaluates postfix expression.
	private boolean evaluate(boolean isTrueFirst) {
		generateAllInputValues(isTrueFirst);
		
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
				
				if (c == TRUE) {
					operandStack.push(true);
				} else if (c == FALSE) {
					operandStack.push(false);
				} else if (c == not.outputChar) {
					operandStack.push(!operandStack.pop());
				} else if (c == and.outputChar) {
					RHS = operandStack.pop();
					LHS = operandStack.pop();
					operandStack.push(LHS && RHS);
				} else if (c == or.outputChar) {
					RHS = operandStack.pop();
					LHS = operandStack.pop();
					operandStack.push(LHS || RHS);
				} else if (c == cond.outputChar) {
					RHS = operandStack.pop();
					LHS = operandStack.pop();
					operandStack.push(!LHS || RHS);
				} else if (c == bicond.outputChar) {
					RHS = operandStack.pop();
					LHS = operandStack.pop();
					operandStack.push((LHS && RHS) || (!LHS && !RHS));
				} else {
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
				outValues.add(inputPos, TRUE);
			} else {
				outValues.add(inputPos, FALSE);
			}
		}
		isValid = true;
		return true;
	}
}
