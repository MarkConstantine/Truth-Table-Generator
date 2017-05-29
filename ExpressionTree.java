public class ExpressionTree {

	private ExpressionTree leftChild = null;
	private ExpressionTree rightChild = null;
	private char data = '\0';
	
	public ExpressionTree(char data) {
		this.setData(data);
	}
	
	public void insert(ExpressionTree newTree) {
		if (leftChild == null) {
			leftChild = newTree;
		} else if (rightChild == null) {
			rightChild = newTree;
		} else {
			leftChild.insert(newTree);
		}
	}
	
	public boolean isEmpty() {
		if (data == '\0' && leftChild == null && rightChild == null) {
			return true;
		}
		return false;
	}
	
	public ExpressionTree getLeftChild() {
		return leftChild;
	}
	
	public ExpressionTree getRightChild() {
		return rightChild;
	}
	
	public char getData() {
		return data;
	}

	public void setData(char data) {
		this.data = data;
	}
	
}
