package Analysis.Tree.Statement;



import Analysis.Syntax.Token;
import java.util.List;

public class ScanNode extends StatementNode {
    private final Token scanToken;
    private final List<String> identifiers;

    public ScanNode(Token scanToken, List<String> identifiers) {
        this.scanToken = scanToken;
        this.identifiers = identifiers;
    }

    public Token getScanToken() {
        return scanToken;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }
}
