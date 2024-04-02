package Analysis.Syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Analysis.Tree.*;
import Analysis.Tree.Statement.*;
import Analysis.Tree.Expression.*;
import Analysis.Type.*;

public class Parser {
    private final Lexer lexer;
    private Token currentToken;
    private final List<String> variableNames;
    private boolean canDeclare;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.getToken();
        this.variableNames = new ArrayList<>();
        this.canDeclare = true;
    }

    public ProgramNode parseProgram(TokenType tokenType) throws Exception {
        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        consumeToken(TokenType.BEGIN);
        consumeToken(tokenType);

        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        List<StatementNode> statements = parseStatements();

        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        consumeToken(TokenType.END);
        consumeToken(tokenType);

        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        if (tokenType == TokenType.CODE)
            consumeToken(TokenType.ENDOFFILE);

        return new ProgramNode(statements);
    }

    private List<StatementNode> parseStatements() throws Exception {
        List<StatementNode> statementList = new ArrayList<>();

        while (!matchToken(TokenType.END)) {
            if (matchToken(TokenType.INT) || matchToken(TokenType.FLOAT) ||
                    matchToken(TokenType.CHAR) || matchToken(TokenType.BOOL)) {
                if (canDeclare)
                    statementList.add(parseVariableDeclarationStatement());
                else
                    throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Invalid syntax.");
            } else if (matchToken(TokenType.IDENTIFIER)) {
                canDeclare = false;
                statementList.add(parseAssignmentStatement());
            } else if (matchToken(TokenType.DISPLAY)) {
                canDeclare = false;
                statementList.add(parseDisplayStatement());
            } else if (matchToken(TokenType.SCAN)) {
                canDeclare = false;
                statementList.add(parseScanStatement());
            } else if (matchToken(TokenType.IF)) {
                canDeclare = false;
                statementList.add(parseIfStatement());
            } else if (matchToken(TokenType.WHILE)) {
                canDeclare = false;
                statementList.add(parseWhileStatement());
            } else if (matchToken(TokenType.ENDOFFILE))
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Missing End Statement.");
            else
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Invalid syntax \"" + currentToken.getCode() + "\".");

            while (matchToken(TokenType.NEWLINE))
                consumeToken(TokenType.NEWLINE);
        }

        return statementList;
    }

    private StatementNode parseVariableDeclarationStatement() throws Exception {
        Token dataTypeToken = currentToken;
        consumeToken(dataTypeToken.getTokenType());

        Map<String, ExpressionNode> variables = new HashMap<>();

        Pair<String, ExpressionNode> variable = getVariable();
        variables.put(variable.getKey(), variable.getValue());
        variableNames.add(variable.getKey());

        while (matchToken(TokenType.COMMA)) {
            consumeToken(TokenType.COMMA);
            variable = getVariable();
            variables.put(variable.getKey(), variable.getValue());
            variableNames.add(variable.getKey());
        }

        return new VariableDeclarationNode(dataTypeToken, variables);
    }

    private StatementNode parseAssignmentStatement() throws Exception {
        List<String> identifiers = new ArrayList<>();
        List<Token> equals = new ArrayList<>();

        Token identifierToken = currentToken;
        consumeToken(TokenType.IDENTIFIER);
        Token equalToken = currentToken;
        consumeToken(TokenType.EQUAL);

        identifiers.add(identifierToken.getCode());
        equals.add(equalToken);

        ExpressionNode expressionValue = parseExpression();

        while (matchToken(TokenType.EQUAL)) {
            IdentifierNode idenExpr = (IdentifierNode) expressionValue;
            equalToken = currentToken;
            consumeToken(TokenType.EQUAL);

            identifiers.add(idenExpr.getName());
            equals.add(equalToken);

            expressionValue = parseExpression();
        }

        return new AssignmentNode(identifiers, equals, expressionValue);
    }

    private StatementNode parseDisplayStatement() throws Exception {
        Token displayToken = currentToken;
        consumeToken(TokenType.DISPLAY);
        consumeToken(TokenType.COLON);

        List<ExpressionNode> expressions = new ArrayList<>();

        if (matchToken(TokenType.DOLLAR)) {
            expressions.add(new LiteralNode(currentToken, "\n"));
            consumeToken(TokenType.DOLLAR);

            while (matchToken(TokenType.AMPERSAND)) {
                consumeToken(TokenType.AMPERSAND);

                if (matchToken(TokenType.NEWLINE))
                    throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");

                if (matchToken(TokenType.DOLLAR)) {
                    expressions.add(new LiteralNode(currentToken, "\n"));
                    consumeToken(TokenType.DOLLAR);
                } else
                    expressions.add(parseExpression());
            }

            if (!matchToken(TokenType.NEWLINE))
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected " + TokenType.NEWLINE + " token");

            return new DisplayNode(displayToken, expressions);
        } else if (matchToken(TokenType.ESCAPE) || matchToken(TokenType.IDENTIFIER) || matchToken(TokenType.INTLITERAL) || matchToken(TokenType.FLOATLITERAL)
                || matchToken(TokenType.CHARLITERAL) || matchToken(TokenType.BOOLLITERAL) || matchToken(TokenType.STRINGLITERAL)
                || matchToken(TokenType.MINUS) || matchToken(TokenType.PLUS) || matchToken(TokenType.NOT)) {
            expressions.add(parseExpression());

            while (matchToken(TokenType.AMPERSAND)) {
                consumeToken(TokenType.AMPERSAND);

                if (matchToken(TokenType.NEWLINE))
                    throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");

                if (matchToken(TokenType.DOLLAR)) {
                    expressions.add(new LiteralNode(currentToken, "\n"));
                    consumeToken(TokenType.DOLLAR);
                } else
                    expressions.add(parseExpression());
            }

            if (!matchToken(TokenType.NEWLINE))
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected " + TokenType.NEWLINE + " token");

            return new DisplayNode(displayToken, expressions);
        } else
            throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");
    }

    private StatementNode parseScanStatement() throws Exception {
        Token scanToken = currentToken;
        consumeToken(TokenType.SCAN);
        consumeToken(TokenType.COLON);

        List<String> identifiers = new ArrayList<>();
        identifiers.add(currentToken.getCode());
        consumeToken(TokenType.IDENTIFIER);

        while (matchToken(TokenType.COMMA)) {
            consumeToken(TokenType.COMMA);
            identifiers.add(currentToken.getCode());
            consumeToken(TokenType.IDENTIFIER);
        }

        return new ScanNode(scanToken, identifiers);
    }

    private StatementNode parseIfStatement() throws Exception {
        boolean isElse = false;
        List<ExpressionNode> conditions = new ArrayList<>();
        List<ProgramNode> statementBlocks = new ArrayList<>();
        List<Token> tokens = new ArrayList<>();

        tokens.add(currentToken);
        consumeToken(TokenType.IF);
        conditions.add(parseConditionExpression());
        statementBlocks.add(parseProgram(TokenType.IF));

        while (matchToken(TokenType.ELSE)) {
            if (isElse)
                throw new Exception("(" + currentToken.getLine() + ", " + currentToken.getColumn() + "): Invalid syntax " + currentToken.getTokenType());

            tokens.add(currentToken);
            consumeToken(TokenType.ELSE);

            if (matchToken(TokenType.IF)) {
                consumeToken(TokenType.IF);
                conditions.add(parseConditionExpression());
            } else {
                conditions.add(null);
                isElse = true;
            }

            statementBlocks.add(parseProgram(TokenType.IF));
        }

        return new ConditionalNode(tokens, conditions, statementBlocks);
    }

    private StatementNode parseWhileStatement() throws Exception {
        Token whileToken = currentToken;
        consumeToken(TokenType.WHILE);

        ExpressionNode condition = parseConditionExpression();
        ProgramNode statementBlock = parseProgram(TokenType.WHILE);

        return new LoopNode(whileToken, condition, statementBlock);
    }

    private ExpressionNode parseExpression() throws Exception {
        ExpressionNode expression;

        if (matchToken(TokenType.ESCAPE)) {
            Token escapeToken = currentToken;
            consumeToken(TokenType.ESCAPE);
            return new LiteralNode(escapeToken, escapeToken.getValue());
        } else if (matchToken(TokenType.OPENPARENTHESIS)) {
            expression = parseParenthesisExpression();
            return expression;
        } else if (matchToken(TokenType.PLUS) || matchToken(TokenType.MINUS) || matchToken(TokenType.NOT)) {
            expression = parseUnaryExpression();
            return expression;
        } else if (matchToken(TokenType.IDENTIFIER) || matchToken(TokenType.INTLITERAL) || matchToken(TokenType.FLOATLITERAL)
                || matchToken(TokenType.CHARLITERAL) || matchToken(TokenType.BOOLLITERAL) || matchToken(TokenType.STRINGLITERAL)) {
            expression = parseBinaryExpression();
            return expression;
        } else
            throw new Exception("(" + currentToken.getLine() + ", " + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token.");
    }

    private ExpressionNode parseParenthesisExpression() throws Exception {
        Token openParenthesis = currentToken;
        consumeToken(TokenType.OPENPARENTHESIS);

        ExpressionNode expression = parseExpression();

        Token closeParenthesis = currentToken;
        consumeToken(TokenType.CLOSEPARENTHESIS);

        int precedence = Grammar.getBinaryPrecedence(currentToken.getTokenType());

        if (precedence > 0) {
            ParenthesisNode parenExpr = new ParenthesisNode(openParenthesis, expression, closeParenthesis);
            return parseBinaryExpression(parenExpr);
        }

        return new ParenthesisNode(openParenthesis, expression, closeParenthesis);
    }

    private ExpressionNode parseConditionExpression() throws Exception {
        Token openParenthesis = currentToken;
        consumeToken(TokenType.OPENPARENTHESIS);

        ExpressionNode expression = parseExpression();

        Token closeParenthesis = currentToken;
        consumeToken(TokenType.CLOSEPARENTHESIS);

        return new ParenthesisNode(openParenthesis, expression, closeParenthesis);
    }

    private ExpressionNode parseUnaryExpression() throws Exception {
        Token unaryToken = currentToken;
        consumeToken(unaryToken.getTokenType());

        ExpressionNode expression;
        if (matchToken(TokenType.OPENPARENTHESIS))
            expression = parseExpression();
        else
            expression = parseTerm();

        UnaryNode unaryExpr = new UnaryNode(unaryToken, expression);

        if (Grammar.getBinaryPrecedence(currentToken.getTokenType()) > 0)
            return parseBinaryExpression(unaryExpr);

        return unaryExpr;
    }

    private ExpressionNode parseBinaryExpression(ExpressionNode prevLeft) throws Exception {
        ExpressionNode left = prevLeft != null ? prevLeft : parseTerm();

        int precedence = Grammar.getBinaryPrecedence(currentToken.getTokenType());

        while (precedence > 0) {
            Token binaryToken = currentToken;
            consumeToken(binaryToken.getTokenType());

            ExpressionNode right = parseTerm();
            int nextPrecedence = Grammar.getBinaryPrecedence(currentToken.getTokenType());

            if (nextPrecedence > precedence)
                right = parseBinaryExpression(right);

            left = new BinaryNode(left, binaryToken, right);
            precedence = Grammar.getBinaryPrecedence(currentToken.getTokenType());
        }

        return left;
    }

    private ExpressionNode parseTerm() throws Exception {
        if (matchToken(TokenType.IDENTIFIER)) {
            Token identifierToken = currentToken;
            consumeToken(TokenType.IDENTIFIER);
            return new IdentifierNode(identifierToken, identifierToken.getCode());
        } else if (matchToken(TokenType.INTLITERAL) || matchToken(TokenType.FLOATLITERAL) || matchToken(TokenType.CHARLITERAL)
                || matchToken(TokenType.BOOLLITERAL) || matchToken(TokenType.STRINGLITERAL)) {
            Token literalToken = currentToken;
            consumeToken(literalToken.getTokenType());
            return new LiteralNode(literalToken, literalToken.getValue());
        } else
            return parseExpression();
    }

    private void consumeToken(TokenType tokenType) throws Exception {
        if (matchToken(tokenType)) {
            Token prevToken = currentToken;
            currentToken = lexer.getToken();
            if (matchToken(TokenType.ERROR)) {
                if (prevToken.getTokenType() == TokenType.INT || prevToken.getTokenType() == TokenType.FLOAT || prevToken.getTokenType() == TokenType.CHAR || prevToken.getTokenType() == TokenType.BOOL) {
                    if (currentToken.getValue().toString().contains("Invalid keyword") || currentToken.getValue().toString().contains("Invalid data type")) {
                        currentToken.setTokenType(TokenType.IDENTIFIER);
                        currentToken.setValue(null);
                    }
                } else if (variableNames.contains(currentToken.getCode())) {
                    currentToken.setTokenType(TokenType.IDENTIFIER);
                    currentToken.setValue(null);
                } else
                    throw new Exception("(" + currentToken.getLine() + ", " + currentToken.getColumn() + "): " + currentToken.getValue());
            }
        } else
            throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected token " + currentToken.getTokenType() + " token expected " + tokenType + " token");
    }

    private boolean matchToken(TokenType tokenType) {
        return currentToken.getTokenType() == tokenType;
    }

    private Pair<String, ExpressionNode> getVariable() throws Exception {
        Token identifier = currentToken;
        consumeToken(TokenType.IDENTIFIER);

        if (matchToken(TokenType.EQUAL)) {
            consumeToken(TokenType.EQUAL);
            return new Pair<>(identifier.getCode(), parseExpression());
        }
        return new Pair<>(identifier.getCode(), null);
    }
}
