package Analysis.Tree.Statement;



import Analysis.Syntax.Token;
import Analysis.Tree.Expression.ExpressionNode;
import Analysis.Tree.ProgramNode;

public class LoopNode extends StatementNode {
    private final Token whileToken;
    private final ExpressionNode expression;
    private final ProgramNode statement;

    public LoopNode(Token whileToken, ExpressionNode expression, ProgramNode statement) {
        this.whileToken = whileToken;
        this.expression = expression;
        this.statement = statement;
    }

    public Token getWhileToken() {
        return whileToken;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    public ProgramNode getStatement() {
        return statement;
    }
}
