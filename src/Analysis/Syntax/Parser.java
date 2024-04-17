package Analysis.Syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Analysis.Tree.*;
import Analysis.Tree.Statement.*;
import Analysis.Tree.Expression.*;
import Analysis.Tree.Expression.ExpressionNode;
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
        // Skip any leading newlines to find the start of the program
        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        // Consume the BEGIN token and the tokenType token to mark the start of the program
        consumeToken(TokenType.BEGIN);
        consumeToken(tokenType);

        // Skip any newlines after the program start to find the first statement
        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        // Parse all statements until the END token is encountered
        List<StatementNode> statements = parseStatements();

        // Skip any newlines before the program end to find the END token
        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        // Consume the END token and the tokenType token to mark the end of the program
        consumeToken(TokenType.END);
        consumeToken(tokenType);

        // Skip any newlines after the program end to find the end of the file or code block
        while (matchToken(TokenType.NEWLINE))
            consumeToken(TokenType.NEWLINE);

        // If the tokenType is CODE, consume the ENDOFFILE token to mark the end of the file
        if (tokenType == TokenType.CODE)
            consumeToken(TokenType.ENDOFFILE);

        // Return a new ProgramNode containing all parsed statements
        return new ProgramNode(statements);
    }


    // This function parses statements in the code and returns a list representing them as abstract syntax tree (AST) nodes.
    private List<StatementNode> parseStatements() throws Exception {

        // Create an empty list to store the parsed statements (represented as AST nodes)
        List<StatementNode> statementList = new ArrayList<>();

        // Loop continues as long as the current token is not of type END (indicating the end of the program)
        while (!matchToken(TokenType.END)) {

            // Check for different data type keywords (int, float, char, bool)
            if (matchToken(TokenType.INT) || matchToken(TokenType.FLOAT) ||
                    matchToken(TokenType.CHAR) || matchToken(TokenType.BOOL)) {

                // If variable declarations are allowed at this point (based on canDeclare flag)
                if (canDeclare) {
                    // Parse the variable declaration statement and add it to the list
                    statementList.add(parseVariableDeclarationStatement());
                } else {
                    // Throw an exception if variable declaration is not allowed here based on current token's line and column
                    throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Invalid syntax.");
                }
            } else if (matchToken(TokenType.IDENTIFIER)) {
                // Set canDeclare to false (since identifiers are typically used in assignments)
                canDeclare = false;
                // Parse the assignment statement and add it to the list
                statementList.add(parseAssignmentStatement());
            } else if (matchToken(TokenType.DISPLAY)) {
                // Set canDeclare to false (since display statements don't allow variable declarations before)
                canDeclare = false;
                // Parse the display statement and add it to the list
                statementList.add(parseDisplayStatement());
            } else if (matchToken(TokenType.SCAN)) {
                // Set canDeclare to false (similar to display statements)
                canDeclare = false;
                // Parse the scan statement and add it to the list
                statementList.add(parseScanStatement());
            } else if (matchToken(TokenType.IF)) {
                // Set canDeclare to false (since if statements don't allow variable declarations before)
                canDeclare = false;
                // Parse the if statement and add it to the list
                statementList.add(parseIfStatement());
            } else if (matchToken(TokenType.WHILE)) {
                // Set canDeclare to false (similar to if statements)
                canDeclare = false;
                // Parse the while statement and add it to the list
                statementList.add(parseWhileStatement());
            } else if (matchToken(TokenType.ENDOFFILE)) {
                // Throw an exception if the end of file is reached but a missing "End" statement is detected (based on current token's line and column)
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Missing End Statement.");
            } else {
                // If none of the expected tokens matched, throw an exception with the current token's information (line, column, and code)
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Invalid syntax \"" + currentToken.getCode() + "\".");
            }

            // Keep consuming newline tokens (ignoring empty lines) after a statement is parsed
            while (matchToken(TokenType.NEWLINE))
                consumeToken(TokenType.NEWLINE);
        }

        // Return the list containing the parsed statements as AST nodes
        return statementList;
    }


    private StatementNode parseVariableDeclarationStatement() {
        try {
            // Get the data type token and consume it
            Token dataTypeToken = currentToken;
            consumeToken(dataTypeToken.getTokenType());
        
            // Map to store variables with their expressions
            Map<String, ExpressionNode> variables = new HashMap<>();
        
            // Get the first variable name and expression
            Pair<String, ExpressionNode> variable = getVariable();
            variables.put(variable.getFirst(), variable.getSecond());
            variableNames.add(variable.getFirst());
        
            // Process remaining variables separated by commas
            while (matchToken(TokenType.COMMA)) {
                consumeToken(TokenType.COMMA);
                variable = getVariable();
                variables.put(variable.getFirst(), variable.getSecond());
                variableNames.add(variable.getFirst());
            }
        
            // Create and return the VariableDeclarationNode
            return new VariableDeclarationNode(dataTypeToken, variables);
        } catch (Exception e) {
            // Handle the exception
            e.printStackTrace(); // Example of printing the stack trace, replace with appropriate handling
            // You may also return a default value or throw a different exception if needed
            return null; // Or throw new RuntimeException("Error parsing variable declaration", e);
        }
    }
    // private StatementNode parseVariableDeclarationStatement() {
    //     // Get the data type token and consume it
    //     Token dataTypeToken = currentToken;
    //     consumeToken(dataTypeToken.getTokenType());
    
    //     // Map to store variables with their expressions
    //     Map<String, ExpressionNode> variables = new HashMap<>();
    
    //     // Get the first variable name and expression
    //     Map.Entry<String, ExpressionNode> variable = getVariable();
    //     variables.put(variable.getKey(), variable.getValue());  // Use getKey() and getValue()
    //     variableNames.add(variable.getKey());
    
    //     // Process remaining variables separated by commas
    //     while (matchToken(TokenType.COMMA)) {
    //         consumeToken(TokenType.COMMA);
    //         variable = getVariable();
    //         variables.put(variable.getKey(), variable.getValue());  // Use getKey() and getValue()
    //         variableNames.add(variable.getKey());
    //     }
    
    //     // Create and return the VariableDeclarationNode
    //     return new VariableDeclarationNode(dataTypeToken, variables);
    // }
    
    

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

//    private StatementNode parseDisplayStatement() throws Exception {
//        Token displayToken = currentToken;
//        consumeToken(TokenType.DISPLAY);
//        consumeToken(TokenType.COLON);
//
//        List<ExpressionNode> expressions = new ArrayList<>();
//
//        if (matchToken(TokenType.DOLLAR)) {
//            expressions.add(new LiteralNode(currentToken, "\n"));
//            consumeToken(TokenType.DOLLAR);
//
//            while (matchToken(TokenType.AMPERSAND)) {
//                consumeToken(TokenType.AMPERSAND);
//
//                if (matchToken(TokenType.NEWLINE))
//                    throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");
//
//                if (matchToken(TokenType.DOLLAR)) {
//                    expressions.add(new LiteralNode(currentToken, "\n"));
//                    consumeToken(TokenType.DOLLAR);
//                } else
//                    expressions.add(parseExpression());
//            }
//
//            if (!matchToken(TokenType.NEWLINE))
//                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected " + TokenType.NEWLINE + " token");
//
//            return new DisplayNode(displayToken, expressions);
//        } else if (matchToken(TokenType.ESCAPE) || matchToken(TokenType.IDENTIFIER) || matchToken(TokenType.INTLITERAL) || matchToken(TokenType.FLOATLITERAL)
//                || matchToken(TokenType.CHARLITERAL) || matchToken(TokenType.BOOLLITERAL) || matchToken(TokenType.STRINGLITERAL)
//                || matchToken(TokenType.MINUS) || matchToken(TokenType.PLUS) || matchToken(TokenType.NOT)) {
//            expressions.add(parseExpression());
//
//            while (matchToken(TokenType.AMPERSAND)) {
//                consumeToken(TokenType.AMPERSAND);
//
//                if (matchToken(TokenType.NEWLINE))
//                    throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");
//
//                if (matchToken(TokenType.DOLLAR)) {
//                    expressions.add(new LiteralNode(currentToken, "\n"));
//                    consumeToken(TokenType.DOLLAR);
//                } else
//                    expressions.add(parseExpression());
//            }
//
//            if (!matchToken(TokenType.NEWLINE))
//                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected " + TokenType.NEWLINE + " token");
//
//            return new DisplayNode(displayToken, expressions);
//        } else
//            throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");
//    }
private StatementNode parseDisplayStatement() throws Exception {
    // Read and remove the current token
    Token displayToken = currentToken;
    consumeToken(TokenType.DISPLAY);
    consumeToken(TokenType.COLON);

    // List of expressions
    List<ExpressionNode> expressions = new ArrayList<>();

    // If display starts with '$'
    // ex. DISPLAY: $ ....
    if (matchToken(TokenType.DOLLAR)) {
        expressions.add(new LiteralNode(currentToken, "\n"));
        consumeToken(TokenType.DOLLAR);

        // While token is & iterate until no & token.
        while (matchToken(TokenType.AMPERSAND)) {
            consumeToken(TokenType.AMPERSAND);

            // If newline is next to & throw an error
            if (matchToken(TokenType.NEWLINE))
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");

            // If $ is next to &, create a new Literal Expression with
            // the value \n
            if (matchToken(TokenType.DOLLAR)) {
                expressions.add(new LiteralNode(currentToken, "\n"));
                consumeToken(TokenType.DOLLAR);
            }
            // Else get the expression
            else
                expressions.add(parseExpression());
        }

        // If the token is not newline then throw an error
        if (!matchToken(TokenType.NEWLINE))
            throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected " + TokenType.NEWLINE + " token");

        // Create the Display Statement
        return new DisplayNode(displayToken, expressions);
    }
    // If display starts with expression
    // ex. DISPLAY: 6 ....
    else if (matchToken(TokenType.ESCAPE) || matchToken(TokenType.IDENTIFIER) || matchToken(TokenType.INTLITERAL) || matchToken(TokenType.FLOATLITERAL)
            || matchToken(TokenType.CHARLITERAL) || matchToken(TokenType.BOOLLITERAL) || matchToken(TokenType.STRINGLITERAL)
            || matchToken(TokenType.MINUS) || matchToken(TokenType.PLUS) || matchToken(TokenType.NOT)) {
        expressions.add(parseExpression());

        // While token is & iterate until no & token.
        while (matchToken(TokenType.AMPERSAND)) {
            consumeToken(TokenType.AMPERSAND);

            // If newline is next to & throw an error
            if (matchToken(TokenType.NEWLINE))
                throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected expression token");

            // If $ is next to &, create a new Literal Expression with
            // the value \n
            if (matchToken(TokenType.DOLLAR)) {
                expressions.add(new LiteralNode(currentToken, "\n"));
                consumeToken(TokenType.DOLLAR);
            }
            // Else get the expression
            else
                expressions.add(parseExpression());
        }

        // If the token is not newline then throw an error
        if (!matchToken(TokenType.NEWLINE))
            throw new Exception("(" + currentToken.getLine() + "," + currentToken.getColumn() + "): Unexpected " + currentToken.getTokenType() + " token expected " + TokenType.NEWLINE + " token");

        // Create the Display Statement
        return new DisplayNode(displayToken, expressions);
    }
    // If display starts with '&'
    // ex. DISPLAY: & ....
    else
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
            expression = parseBinaryExpression(null);
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

    public class Pair<F, S> {
        private final F first;
        private final S second;
    
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    
        public F getFirst() {
            return first;
        }
    
        public S getSecond() {
            return second;
        }
    }
    


    // private Pair<String, ExpressionNode> getVariable() {
    //     // Read and consume the identifier token
    //     Token identifier = _currentToken;
    //     consumeToken(TokenType.IDENTIFIER);
    
    //     // Check for optional assignment
    //     if (matchToken(TokenType.EQUAL)) {
    //         consumeToken(TokenType.EQUAL);
    //         // Parse the expression after the equal sign
    //         ExpressionNode expression = parseExpression();
    //         return new Pair<>(identifier.getCode(), expression);
    //     }
    
    //     // No assignment, return identifier and null for expression
    //     return new Pair<>(identifier.getCode(), null);
    // }
    
}
