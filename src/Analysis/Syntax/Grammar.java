package Analysis.Syntax;


import Analysis.Type.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {

    public static Token getWordToken(String input, int line, int column) {
        Map<String, TokenType> keywords = new HashMap<>();
        keywords.put("BEGIN", TokenType.BEGIN);
        keywords.put("END", TokenType.END);
        keywords.put("CODE", TokenType.CODE);
        keywords.put("IF", TokenType.IF);
        keywords.put("ELSE", TokenType.ELSE);
        keywords.put("WHILE", TokenType.WHILE);
        keywords.put("DISPLAY", TokenType.DISPLAY);
        keywords.put("SCAN", TokenType.SCAN);
        keywords.put("AND", TokenType.AND);
        keywords.put("OR", TokenType.OR);
        keywords.put("NOT", TokenType.NOT);

        Map<String, TokenType> dataTypes = new HashMap<>();
        dataTypes.put("INT", TokenType.INT);
        dataTypes.put("FLOAT", TokenType.FLOAT);
        dataTypes.put("CHAR", TokenType.CHAR);
        dataTypes.put("BOOL", TokenType.BOOL);

        String inputUpperCase = input.toUpperCase();
        if (keywords.containsKey(inputUpperCase)) {
            if (keywords.containsKey(input))
                return new Token(keywords.get(input), input, null, line, column);
            else
                return new Token(TokenType.ERROR, input, "Invalid keyword '" + input + "' should be " + inputUpperCase, line, column);
        } else if (dataTypes.containsKey(inputUpperCase)) {
            if (dataTypes.containsKey(input))
                return new Token(dataTypes.get(input), input, null, line, column);
            else
                return new Token(TokenType.ERROR, input, "Invalid data type '" + input + "' should be " + inputUpperCase, line, column);
        } else
            return new Token(TokenType.IDENTIFIER, input, null, line, column);
    }

    public static int getBinaryPrecedence(TokenType tokenType) {
        switch (tokenType) {
            case OR:
                return 1;
            case AND:
                return 2;
            case LESSTHAN:
            case LESSEQUAL:
            case GREATERTHAN:
            case GREATEREQUAL:
            case EQUALTO:
            case NOTEQUAL:
                return 4;
            case PLUS:
            case MINUS:
                return 5;
            case PERCENT:
                return 6;
            case STAR:
            case SLASH:
                return 7;
            default:
                return 0;
        }
    }

    public static DataType getDataType(Token token) {
        TokenType tokenType = token.getTokenType();
        switch (tokenType) {
            case INT:
            case INTLITERAL:
                return DataType.Int;
            case FLOAT:
            case FLOATLITERAL:
                return DataType.Float;
            case CHAR:
            case CHARLITERAL:
                return DataType.Char;
            case BOOL:
            case BOOLLITERAL:
                return DataType.Bool;
            default:
                throw new IllegalArgumentException("Unknown data type");
        }
    }

    public static DataType getDataType(TokenType tokenType) {
        switch (tokenType) {
            case INT:
            case INTLITERAL:
                return DataType.Int;
            case FLOAT:
            case FLOATLITERAL:
                return DataType.Float;
            case CHAR:
            case CHARLITERAL:
                return DataType.Char;
            case BOOL:
            case BOOLLITERAL:
                return DataType.Bool;
            default:
                throw new IllegalArgumentException("Unknown data type");
        }
    }

    public static DataType getDataType(Object val) {
        if (val instanceof Integer)
            return DataType.Int;
        else if (val instanceof Double)
            return DataType.Float;
        else if (val instanceof Character)
            return DataType.Char;
        else if (val instanceof Boolean)
            return DataType.Bool;
        else
            return DataType.String;
    }

    public static boolean isArithmeticOperator(TokenType tokenType) {
        return tokenType == TokenType.PLUS || tokenType == TokenType.MINUS ||
               tokenType == TokenType.STAR || tokenType == TokenType.SLASH ||
               tokenType == TokenType.PERCENT;
    }

    public static boolean isComparisonOperator(TokenType tokenType) {
        return tokenType == TokenType.LESSTHAN || tokenType == TokenType.GREATERTHAN ||
               tokenType == TokenType.LESSEQUAL || tokenType == TokenType.GREATEREQUAL ||
               tokenType == TokenType.EQUALTO || tokenType == TokenType.NOTEQUAL;
    }

    public static Object convertValue(String val) {
        String floatPattern = "^(?:\\+|\\-)?\\d*\\.\\d+$";
        String intPattern = "^(?:\\+|\\-)?\\d+$";
        String charPattern = "^'(?:\\[[\\[\\]\\&\\$\\#']\\])'|'[\\[\\]\\&\\$\\#']'$";
        String boolPattern = "^\"TRUE\"$|^\"FALSE\"$";

        Pattern floatPatternRegex = Pattern.compile(floatPattern);
        Pattern intPatternRegex = Pattern.compile(intPattern);
        Pattern charPatternRegex = Pattern.compile(charPattern);
        Pattern boolPatternRegex = Pattern.compile(boolPattern);

        Matcher floatMatcher = floatPatternRegex.matcher(val);
        Matcher intMatcher = intPatternRegex.matcher(val);
        Matcher charMatcher = charPatternRegex.matcher(val);
        Matcher boolMatcher = boolPatternRegex.matcher(val);

        if (intMatcher.matches())
            return Integer.parseInt(val);
        else if (floatMatcher.matches())
            return Double.parseDouble(val);
        else if (charMatcher.matches())
            return val;
        else if (boolMatcher.matches())
            return val.equals("\"TRUE\"") ? true : false;
        else
            throw new IllegalArgumentException("Runtime Error: Invalid input " + val);
    }

    public static boolean matchDataType(DataType ldt, DataType rdt) {
        return (ldt == DataType.Float && rdt == DataType.Int) || ldt == rdt;
    }
}
