package Analysis.Tree.Expression;


import Analysis.Syntax.Token;

public class BinaryNode extends ExpressionNode {
    private final ExpressionNode left;
    private final Token tokenOperator;
    private final ExpressionNode right;

    public BinaryNode(ExpressionNode left, Token tokenOperator, ExpressionNode right) {
        this.left = left;
        this.tokenOperator = tokenOperator;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public Token getTokenOperator() {
        return tokenOperator;
    }

    public ExpressionNode getRight() {
        return right;
    }
}
