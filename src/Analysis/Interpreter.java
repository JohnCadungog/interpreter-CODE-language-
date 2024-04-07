package Analysis;



import Analysis.Tree.*;
import Analysis.Table.*;
import Analysis.Tree.Expression.ExpressionNode;
import Analysis.Tree.Expression.BinaryNode;
import Analysis.Tree.Expression.UnaryNode;
import Analysis.Tree.Expression.ParenthesisNode;
import Analysis.Tree.Expression.IdentifierNode;
import Analysis.Tree.Expression.LiteralNode;
import Analysis.Tree.Statement.*;
import Analysis.Type.TokenType;
import Analysis.Table.VariableTable;
import java.util.List;
import Analysis.Syntax.*;
import java.util.Map;

import Analysis.Syntax.*;

public class Interpreter {
    private VariableTable variableTable;
    private ProgramNode program;

    public Interpreter(String code) throws Exception{
        Lexer lex = new Lexer(code);
        Parser parser = new Parser(lex);
        Semantic semantic = new Semantic();


        TokenType tokenType = TokenType.CODE; // Assuming your program starts with CODE token
        program = parser.parseProgram(tokenType);
     
        semantic.analyze(program);
        variableTable = new VariableTable();
    }

    public void execute(ProgramNode statementBlock) {
        ProgramNode prog = statementBlock == null ? program : statementBlock;

        for (StatementNode statement : prog.getStatements()) {
            if (statement instanceof VariableDeclarationNode)
                executeVariableDeclaration((VariableDeclarationNode) statement);
            else if (statement instanceof AssignmentNode)
                executeAssignment((AssignmentNode) statement);
            else if (statement instanceof DisplayNode)
                executeDisplay((DisplayNode) statement);
            else if (statement instanceof ScanNode)
                executeScan((ScanNode) statement);
            else if (statement instanceof ConditionalNode)
                executeCondition((ConditionalNode) statement);
            else if (statement instanceof LoopNode)
                executeLoop((LoopNode) statement);
        }
    }

    // private void executeVariableDeclaration(VariableDeclarationNode statement) {
    //     for (String identifier : statement.getVariables()) {
    //         Object value = null;
    //         if (statement.getVariables().get(identifier) != null)
    //             value = evaluateExpression(statement.getVariables().get(identifier));

    //         variableTable.addVariable(identifier, Grammar.getDataType(statement.getDataTypeToken().getTokenType()), value);
    //     }
    // }
//     private void executeVariableDeclaration(VariableDeclarationNode statement) {
//     // Loop through the dictionary of variables
//     for (Map.Entry<String, ExpressionNode> entry : statement.getVariables().entrySet()) {
//         // Get the variable name
//         String identifier = entry.getKey();

//         // Set default value to null
//         Object value = null;

//         // If the variable value is not null ex. INT a = 5
//         if (entry.getValue() != null) {
//             value = evaluateExpression(entry.getValue());
//         }

//         // Add variable to table of variables
//         variableTable.addVariable(identifier, Grammar.getDataType(statement.getDataTypeToken().getTokenType()), value);
//     }
// }

private void executeVariableDeclaration(VariableDeclarationNode statement) {
    // Loop through the map of variables
    for (Map.Entry<String, ExpressionNode> entry : statement.getVariables().entrySet()) {
        // Get the variable name
        String identifier = entry.getKey();

        // Set default value to null
        Object value = null;

        // If the variable value is not null (e.g., INT a = 5)
        if (entry.getValue() != null) {
            value = evaluateExpression(entry.getValue());
        }

        // Add variable to table of variables
        variableTable.addVariable(identifier, Grammar.getDataType(statement.getDataTypeToken().getTokenType()), value);
    }
}
    private void executeAssignment(AssignmentNode statement) {
        Object value = null;
        for (String identifier : statement.getIdentifiers()) {
            value = evaluateExpression(statement.getExpression());
            variableTable.addValue(identifier, value);
        }
    }

    private void executeDisplay(DisplayNode statement) {
        StringBuilder result = new StringBuilder();
        for (ExpressionNode expression : statement.getExpressions())
            result.append(evaluateExpression(expression));

        System.out.print(result.toString());
    }

    private void executeScan(ScanNode statement) {
        List<String> values = null;
        List<String> identifiers = statement.getIdentifiers();
        String inputted = "";

        System.out.print("");
        inputted = new java.util.Scanner(System.in).nextLine();
        values = java.util.Arrays.asList(inputted.replace(" ", "").split(","));

        if (values.size() != identifiers.size())
            throw new RuntimeException("Runtime Error: Missing input/s.");

        Object value;
        int index = 0;
        for (String val : values) {
            value = Grammar.convertValue(val);

            if (!Grammar.matchDataType(variableTable.getType(identifiers.get(index)), Grammar.getDataType(value)))
                throw new RuntimeException("Runtime Error: Unable to assign " + Grammar.getDataType(value) + " on \"" + identifiers.get(index) + "\".");

            variableTable.addValue(identifiers.get(index), value);
            index++;
        }
    }

    private void executeCondition(ConditionalNode statement) {
        boolean displayed = false;
        int index = 0;

        for (ExpressionNode expression : statement.getExpressions()) {
            if (expression != null) {
                if ((boolean) evaluateExpression(expression)) {
                    displayed = true;
                    execute(statement.getStatements().get(index));
                    break;
                }
            } else
                break;

            index++;
        }

        if (statement.getExpressions().get(index) == null)
            if (!displayed)
                execute(statement.getStatements().get(index));
    }

    private void executeLoop(LoopNode statement) {
        while ((boolean) evaluateExpression(statement.getExpression()))
            execute(statement.getStatement());
    }

    private Object evaluateExpression(ExpressionNode expression) {
        if (expression instanceof BinaryNode)
            return evaluateBinaryExpression((BinaryNode) expression);
        else if (expression instanceof UnaryNode)
            return evaluateUnaryExpression((UnaryNode) expression);
        else if (expression instanceof ParenthesisNode)
            return evaluateExpression(((ParenthesisNode) expression).getExpression());
        else if (expression instanceof IdentifierNode)
            return evaluateIdentifierExpression((IdentifierNode) expression);
        else if (expression instanceof LiteralNode)
            return ((LiteralNode) expression).getLiteral();
        else
            throw new RuntimeException("Unknown expression.");
    }

    private Object evaluateBinaryExpression(BinaryNode expression) {
        Object left = evaluateExpression(expression.getLeft());
        Object right = evaluateExpression(expression.getRight());
        Object binResult;

        switch (expression.getTokenOperator().getTokenType()) {
            case PLUS:
                binResult = (int) left + (int) right;
                return binResult;
            case MINUS:
                binResult = (int) left - (int) right;
                return binResult;
            case STAR:
                binResult = (int) left * (int) right;
                return binResult;
            case SLASH:
                binResult = (int) left / (int) right;
                return binResult;
            case PERCENT:
                binResult = (int) left % (int) right;
                return binResult;
            case LESSTHAN:
                binResult = (int) left < (int) right;
                return binResult;
            case GREATERTHAN:
                binResult = (int) left > (int) right;
                return binResult;
            case LESSEQUAL:
                binResult = (int) left <= (int) right;
                return binResult;
            case GREATEREQUAL:
                binResult = (int) left >= (int) right;
                return binResult;
            case EQUALTO:
                binResult = (int) left == (int) right;
                return binResult;
            case NOTEQUAL:
                binResult = (int) left != (int) right;
                return binResult;
            case AND:
                binResult = (boolean) left && (boolean) right;
                return binResult;
            case OR:
                binResult = (boolean) left || (boolean) right;
                return binResult;
            default:
                throw new RuntimeException("Unknown operator.");
        }
    }

    private Object evaluateUnaryExpression(UnaryNode expression) {
        Object unaryValue = evaluateExpression(expression.getExpression());
        if (expression.getTokenOperator().getTokenType() == TokenType.MINUS)
            return -(int) unaryValue;
        else if (expression.getTokenOperator().getTokenType() == TokenType.NOT)
            return !((String) unaryValue).contains("TRUE") ? true : false;
        else
            return unaryValue;
    }

    private Object evaluateIdentifierExpression(IdentifierNode expression) {
        if (variableTable.getValue(expression.getName()) == null)
            throw new RuntimeException("(" + expression.getIdentifierToken().getLine() + "," + expression.getIdentifierToken().getColumn() + "): Variable '" + expression.getName() + "' is null.");

        Object result = variableTable.getValue(expression.getName());

        if (result instanceof Boolean)
            return ((boolean) result) ? "TRUE" : "FALSE";
        return result;
    }
}
