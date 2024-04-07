package Analysis.Syntax;

import Analysis.Table.*;
import Analysis.Tree.*;
import Analysis.Tree.Expression.ExpressionNode;
import Analysis.Tree.Expression.IdentifierNode;
import Analysis.Tree.Expression.LiteralNode;
import Analysis.Tree.Expression.ParenthesisNode;
import Analysis.Tree.Expression.UnaryNode;
import Analysis.Tree.Expression.BinaryNode;
import Analysis.Table.VariableTable;
import Analysis.Tree.Statement.*;
import Analysis.Type.*;
import java.util.List;

public class Semantic {
    private VariableTable variableTable;

    public Semantic() {
        variableTable = new VariableTable();
    }

    public void analyze(ProgramNode program) {
        List<StatementNode> statements = program.getStatements();
        for (StatementNode statement : statements) {
            if (statement instanceof VariableDeclarationNode) {
                analyzeVariableDeclaration((VariableDeclarationNode) statement);
            } else if (statement instanceof AssignmentNode) {
                analyzeAssignment((AssignmentNode) statement);
            } else if (statement instanceof DisplayNode) {
                analyzeDisplay((DisplayNode) statement);
            } else if (statement instanceof ScanNode) {
                analyzeScan((ScanNode) statement);
            } else if (statement instanceof ConditionalNode) {
                analyzeCondition((ConditionalNode) statement);
            } else if (statement instanceof LoopNode) {
                analyzeLoop((LoopNode) statement);
            }
        }
    }

    private void analyzeVariableDeclaration(VariableDeclarationNode statement) {
        DataType dataType = Grammar.getDataType(statement.getDataTypeToken().getTokenType());
        for (String identifier : statement.getVariables().keySet()) {
            if (!variableTable.exists(identifier)) {
                ExpressionNode value = statement.getVariables().get(identifier);
                if (value != null) {
                    DataType expressionType = analyzeExpression(value);
                    if (!Grammar.matchDataType(dataType, expressionType)) {
                        throw new RuntimeException(String.format("(%d,%d): Unable to assign %s on \"%s\".",
                                statement.getDataTypeToken().getLine(), statement.getDataTypeToken().getColumn(),
                                expressionType, identifier));
                    }
                }
                variableTable.addIdentifier(identifier, dataType);
            } else {
                throw new RuntimeException(String.format("(%d,%d): Variable \"%s\" already exists.",
                        statement.getDataTypeToken().getLine(), statement.getDataTypeToken().getColumn(), identifier));
            }
        }
    }

    private void analyzeAssignment(AssignmentNode statement) {
        for (int i = 0; i < statement.getIdentifiers().size(); i++) {
            String identifier = statement.getIdentifiers().get(i);
            if (variableTable.exists(identifier)) {
                DataType dataType = variableTable.getType(identifier);
                DataType expressionType = analyzeExpression(statement.getExpression());
                if (!Grammar.matchDataType(dataType, expressionType)) {
                    throw new RuntimeException(String.format("(%d,%d): Unable to assign %s on \"%s\".",
                            statement.getEqualsTokens().get(i).getLine(), statement.getEqualsTokens().get(i).getColumn(),
                            expressionType, identifier));
                }
            } else {
                throw new RuntimeException(String.format("(%d,%d): Variable \"%s\" does not exist.",
                        statement.getEqualsTokens().get(i).getLine(), statement.getEqualsTokens().get(i).getColumn(),
                        identifier));
            }
        }
    }

    private void analyzeDisplay(DisplayNode statement) {
        for (ExpressionNode expression : statement.getExpressions()) {
            if (expression instanceof IdentifierNode) {
                IdentifierNode identifierNode = (IdentifierNode) expression;
                if (!variableTable.exists(identifierNode.getName())) {
                    throw new RuntimeException(String.format("(%d,%d): Variable \"%s\" does not exist.",
                            identifierNode.getIdentifierToken().getLine(), identifierNode.getIdentifierToken().getColumn(),
                            identifierNode.getName()));
                }
            }
        }
    }

    private void analyzeScan(ScanNode statement) {
        for (String identifier : statement.getIdentifiers()) {
            if (!variableTable.exists(identifier)) {
                throw new RuntimeException(String.format("(%d,%d): Variable \"%s\" does not exist.",
                        statement.getScanToken().getLine(), statement.getScanToken().getColumn(), identifier));
            }
        }
    }

    private void analyzeCondition(ConditionalNode statement) {
    int index = 0;
    // Loop through a list of expressions
    for (ExpressionNode expression : statement.getExpressions()) {
        // If expression is not null means that the statement is either if or else if
        if (expression != null) {
            // If the data type of the expression is not bool
            if (analyzeExpression(expression) != DataType.Bool) {
                throw new RuntimeException(String.format("(%d,%d): Expression is not %s",
                        statement.getTokens().get(index).getLine(), statement.getTokens().get(index).getColumn(),
                        DataType.Bool));
            }
        }

        // Analyze the statement block of if, else if, else statement
        analyze(statement.getStatements().get(index));

        index++;
    }
}


    private void analyzeLoop(LoopNode statement) {
        DataType expressionType = analyzeExpression(statement.getExpression());
        if (expressionType != DataType.Bool) {
            throw new RuntimeException(String.format("(%d,%d): Expression is not %s",
                    statement.getWhileToken().getLine(), statement.getWhileToken().getColumn(), DataType.Bool));
        }
        analyze(statement.getStatement());
    }

    private DataType analyzeExpression(ExpressionNode expression) {
        if (expression instanceof BinaryNode) {
            return analyzeBinaryExpression((BinaryNode) expression);
        } else if (expression instanceof UnaryNode) {
            return analyzeUnaryExpression((UnaryNode) expression);
        } else if (expression instanceof ParenthesisNode) {
            return analyzeExpression(((ParenthesisNode) expression).getExpression());
        } else if (expression instanceof IdentifierNode) {
            return analyzeIdentifierExpression((IdentifierNode) expression);
        } else if (expression instanceof LiteralNode) {
            return analyzeLiteralExpression((LiteralNode) expression);
        } else {
            throw new RuntimeException("Unknown expression.");
        }
    }

    private DataType analyzeBinaryExpression(BinaryNode expression) {
        Token operatorToken = expression.getTokenOperator();
        DataType leftDataType = analyzeExpression(expression.getLeft());
        DataType rightDataType = analyzeExpression(expression.getRight());
        if (!matchExpressionDataType(leftDataType, rightDataType)) {
            throw new RuntimeException(String.format("(%d,%d): Operator '%s' cannot be applied to operands of type %s and %s",
                    operatorToken.getLine(), operatorToken.getColumn(), operatorToken.getCode(), leftDataType, rightDataType));
        }
        if (Grammar.isArithmeticOperator(operatorToken.getTokenType()) &&
                ((leftDataType == DataType.Char || leftDataType == DataType.String || leftDataType == DataType.Bool) &&
                        (rightDataType == DataType.Char || rightDataType == DataType.String || rightDataType == DataType.Bool))) {
            throw new RuntimeException(String.format("(%d,%d): Operator '%s' cannot be applied to operands of type %s and %s",
                    operatorToken.getLine(), operatorToken.getColumn(), operatorToken.getCode(), leftDataType, rightDataType));
        } else if (Grammar.isComparisonOperator(operatorToken.getTokenType()) &&
                !matchExpressionDataType(leftDataType, rightDataType)) {
            throw new RuntimeException(String.format("(%d,%d): Operator '%s' cannot be applied to operands of type %s and %s",
                    operatorToken.getLine(), operatorToken.getColumn(), operatorToken.getCode(), leftDataType, rightDataType));
        }
        return Grammar.isComparisonOperator(operatorToken.getTokenType()) ? DataType.Bool : leftDataType;
    }

    private DataType analyzeUnaryExpression(UnaryNode expression) {
        Token operatorToken = expression.getTokenOperator();
        DataType expressionDataType = analyzeExpression(expression.getExpression());
        if (operatorToken.getTokenType() == TokenType.NOT) {
            if (expressionDataType != DataType.Bool) {
                throw new RuntimeException(String.format("(%d,%d): Operator '%s' cannot be applied to %s",
                        operatorToken.getLine(), operatorToken.getColumn(), operatorToken.getCode(), expressionDataType));
            }
            return DataType.Bool;
        }
        return expressionDataType;
    }

    private DataType analyzeIdentifierExpression(IdentifierNode expression) {
        if (!variableTable.exists(expression.getName())) {
            throw new RuntimeException(String.format("(%d,%d): Variable \"%s\" does not exist.",
                    expression.getIdentifierToken().getLine(), expression.getIdentifierToken().getColumn(), expression.getName()));
        }
        return variableTable.getType(expression.getName());
    }

    private DataType analyzeLiteralExpression(LiteralNode expression) {
        Object value = expression.getLiteral();
        if (value instanceof Integer) {
            return DataType.Int;
        } else if (value instanceof Float || value instanceof Double) {
            return DataType.Float;
        } else if (value instanceof Character) {
            return DataType.Char;
        } else if (value instanceof Boolean) {
            return DataType.Bool;
        } else if (value instanceof String) {
            return DataType.String;
        } else {
            throw new RuntimeException(String.format("(%d,%d): Unknown data type %s",
                    expression.getLiteralToken().getLine(), expression.getLiteralToken().getColumn(), expression.getLiteral()));
        }
    }

    private boolean matchExpressionDataType(DataType ldt, DataType rdt) {
        if ((ldt == DataType.Int && rdt == DataType.Float) || (ldt == DataType.Float && rdt == DataType.Int))
            return true;
        return ldt == rdt;
    }
}
