package Analysis.Tree.Statement;



import Analysis.Syntax.Token;
import Analysis.Tree.Expression.ExpressionNode;
import java.util.List;

public class AssignmentNode extends StatementNode {
    private final List<String> identifiers;
    private final List<Token> equalsTokens;
    private final ExpressionNode expression;

    public AssignmentNode(List<String> identifiers, List<Token> equalsToken, ExpressionNode expression) {
        this.identifiers = identifiers;
        this.equalsTokens = equalsToken;
        this.expression = expression;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public List<Token> getEqualsTokens() {
        return equalsTokens;
    }

    public ExpressionNode getExpression() {
        return expression;
    }
}
