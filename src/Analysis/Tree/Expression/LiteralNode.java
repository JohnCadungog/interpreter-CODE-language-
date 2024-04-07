package Analysis.Tree.Expression;



import Analysis.Syntax.*;

public class LiteralNode extends ExpressionNode {
    private final Token literalToken;
    private final Object literal;

    public LiteralNode(Token literalToken, Object literal) {
        this.literalToken = literalToken;
        this.literal = literal;
    }

    public Token getLiteralToken() {
        return literalToken;
    }

    public Object getLiteral() {
        return literal;
    }
}
