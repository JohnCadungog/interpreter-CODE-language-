package Analysis.Tree.Expression;



import Analysis.Syntax.*;
import Analysis.Type.*;




public class ParenthesisNode extends ExpressionNode {
    private final Token open;
    private final ExpressionNode expression;
    private final Token close;

    public ParenthesisNode(Token open, ExpressionNode expression, Token close) {
        this.open = open;
        this.expression = expression;
        this.close = close;
    }

    public Token getOpen() {
        return open;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    public Token getClose() {
        return close;
    }
}
