package Analysis.Tree.Statement;





import Analysis.Syntax.Token;
import Analysis.Syntax.*;
import Analysis.Type.*;
import Analysis.Tree.Expression.ExpressionNode;
import java.util.List;

public class DisplayNode extends StatementNode {
    private final Token displayToken;
    private final List<ExpressionNode> expressions;

    public DisplayNode(Token displayToken, List<ExpressionNode> expressions) {
        this.displayToken = displayToken;
        this.expressions = expressions;
    }

    public Token getDisplayToken() {
        return displayToken;
    }

    public List<ExpressionNode> getExpressions() {
        return expressions;
    }
}
