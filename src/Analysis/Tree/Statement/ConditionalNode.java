package Analysis.Tree.Statement;



import Analysis.Syntax.Token;
import Analysis.Tree.Expression.ExpressionNode;
import Analysis.Tree.*;
import java.util.List;

public class ConditionalNode extends StatementNode {
    private final List<Token> tokens;
    private final List<ExpressionNode> expressions;
    private final List<ProgramNode> statements;

    public ConditionalNode(List<Token> tokens, List<ExpressionNode> expressions, List<ProgramNode> statements) {
        this.tokens = tokens;
        this.expressions = expressions;
        this.statements = statements;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<ExpressionNode> getExpressions() {
        return expressions;
    }

    public List<ProgramNode> getStatements() {
        return statements;
    }
}
