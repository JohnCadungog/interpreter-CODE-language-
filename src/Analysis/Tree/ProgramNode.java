package Analysis.Tree;


import Analysis.Tree.Statement.StatementNode;
import java.util.List;

public class ProgramNode extends ASTNode {
    private final List<StatementNode> statements;

    public ProgramNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    public List<StatementNode> getStatements() {
        return statements;
    }
}
