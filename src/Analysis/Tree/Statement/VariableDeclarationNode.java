package Analysis.Tree.Statement;



import Analysis.Syntax.Token;
import Analysis.Tree.Expression.ExpressionNode;
import java.util.Dictionary;
import java.util.Map;
import java.util.HashMap;

public class VariableDeclarationNode extends StatementNode {
    private Token dataTypeToken;
    private Map<String, ExpressionNode> variables;

    public VariableDeclarationNode(Token dataTypeToken, Map<String, ExpressionNode> variables) {
        this.dataTypeToken = dataTypeToken;
        this.variables = variables;
    }

    public Token getDataTypeToken() {
        return dataTypeToken;
    }

    public Map<String, ExpressionNode> getVariables() {
        return variables;
    }


    
}