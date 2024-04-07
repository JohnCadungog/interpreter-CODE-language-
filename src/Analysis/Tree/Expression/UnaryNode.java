package Analysis.Tree.Expression;



import Analysis.Syntax.Token;

public class UnaryNode extends ExpressionNode {
    private final Token tokenOperator;
    private final ExpressionNode expression;

    public UnaryNode(Token tokenOperator, ExpressionNode expression) {
        this.tokenOperator = tokenOperator;
        this.expression = expression;
    }

    public Token getTokenOperator() {
        return tokenOperator;
    }

    public ExpressionNode getExpression() {
        return expression;
    }
}
