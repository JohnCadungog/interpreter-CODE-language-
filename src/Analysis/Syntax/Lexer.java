package Analysis.Syntax;



import Analysis.Type.*;
import Analysis.Type.TokenType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Lexer {
    private final String code;
    private int position;
    private int line, column;

    public Lexer(String code) {
        this.code = code;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    private char current() {
        return peek(0);
    }

    private char lookAhead() {
        return peek(1);
    }

    private char peek(int offset) {
        int index = position + offset;
        if (index >= code.length())
            return '\0';
        return code.charAt(index);
    }

    private void next(int offset) {
        position += offset;
        column += offset;
    }

    private void newLine() {
        line++;
        column = 1;
        next(1);
    }

    public Token getToken() {
        while (position < code.length()) {
            if (Character.isLetter(current()))
                return getKeywordOrDataTypeOrIdentifierToken();

            if (Character.isDigit(current()))
                return getNumberLiteralToken();

            switch (current()) {
                case ' ':
                case '\t':
                    next(1);
                    continue;
                case '\n':
                    Token newLineToken = new Token(TokenType.NEWLINE, "\n", null, line, column);
                    newLine();
                    return newLineToken;
                case '_':
                    return getKeywordOrDataTypeOrIdentifierToken();
                case '\'':
                    return getCharacterLiteralToken();
                case '\"':
                    return getBooleanOrStringLiteralToken();
                case '.':
                    return getNumberLiteralToken();
                case '#':
                    while (current() != '\n' && current() != '\0')
                        next(1);
                    continue;
                case '*':
                    next(1);
                    return new Token(TokenType.STAR, "*", null, line, column - 1);
                case '/':
                    next(1);
                    return new Token(TokenType.SLASH, "/", null, line, column - 1);
                case '%':
                    next(1);
                    return new Token(TokenType.PERCENT, "%", null, line, column - 1);
                case '+':
                    next(1);
                    return new Token(TokenType.PLUS, "+", null, line, column - 1);
                case '-':
                    next(1);
                    return new Token(TokenType.MINUS, "-", null, line, column - 1);
                case '>':
                    if (lookAhead() == '=') {
                        next(2);
                        return new Token(TokenType.GREATEREQUAL, ">=", null, line, column - 2);
                    }
                    next(1);
                    return new Token(TokenType.GREATERTHAN, ">", null, line, column - 1);
                case '<':
                    if (lookAhead() == '=') {
                        next(2);
                        return new Token(TokenType.LESSEQUAL, "<=", null, line, column - 2);
                    } else if (lookAhead() == '>') {
                        next(2);
                        return new Token(TokenType.NOTEQUAL, "<>", null, line, column - 2);
                    }
                    next(1);
                    return new Token(TokenType.LESSTHAN, "<", null, line, column - 1);
                case '=':
                    if (lookAhead() == '=') {
                        next(2);
                        return new Token(TokenType.EQUALTO, "==", null, line, column - 2);
                    }
                    next(1);
                    return new Token(TokenType.EQUAL, "=", null, line, column - 1);
                case '$':
                    next(1);
                    return new Token(TokenType.DOLLAR, "$", null, line, column - 1);
                case '&':
                    next(1);
                    return new Token(TokenType.AMPERSAND, "&", null, line, column - 1);
                case '[':
                    return getEscapeCodeToken();
                case '(':
                    next(1);
                    return new Token(TokenType.OPENPARENTHESIS, "(", null, line, column - 1);
                case ')':
                    next(1);
                    return new Token(TokenType.CLOSEPARENTHESIS, ")", null, line, column - 1);
                case ',':
                    next(1);
                    return new Token(TokenType.COMMA, ",", null, line, column - 1);
                case ':':
                    next(1);
                    return new Token(TokenType.COLON, ":", null, line, column - 1);
                default:
                    next(1);
                    return new Token(TokenType.ERROR, Character.toString(current()), "Unknown symbol", line, column - 1);
            }
        }
        return new Token(TokenType.ENDOFFILE, "\0", null, line, column);
    }

    
    private Token getKeywordOrDataTypeOrIdentifierToken() {
        int start = position;
        int lineCol = column;
    
        while (Character.isLetter(current()) || current() == '_' || Character.isDigit(current()))
            next(1);
    
        int length = position - start;
        String text = code.substring(start, start + length);
    
        return Grammar.getWordToken(text, line, lineCol);
    }
    
    private Token getCharacterLiteralToken() {
        int start = position;
        int lineCol = column;
    
        next(1);
        while (current() != '\'' && !Character.isWhitespace(lookAhead()))
            next(1);
        next(1);
    
        String charPattern = "^'(?:\\[\\[\\]\\&\\$#']\\])'|'[^\\[\\]\\&\\$#']'$";
        Pattern charRegex = Pattern.compile(charPattern);
    
        int length = position - start;
        String text = code.substring(start, start + length);
        Object value = null;
    
        Matcher matcher = charRegex.matcher(text);
        if (matcher.matches()) {
            value = text.charAt(text.length() / 2);
            return new Token(TokenType.CHARLITERAL, text, value, line, lineCol);
        }
        return new Token(TokenType.ERROR, text, "Invalid CHAR literal.", line, lineCol);
    }
    private Token getBooleanOrStringLiteralToken() {
        int start = position;
        int lineCol = column;
    
        next(1);
        while (current() != '\"' && !Character.isWhitespace(lookAhead()))
            next(1);
        next(1);
    
        String boolPattern = "^\"TRUE\"$|^\"FALSE\"$";
        String stringPattern = "^\"[^\"]*\"$";
        Pattern boolRegex = Pattern.compile(boolPattern);
        Pattern stringRegex = Pattern.compile(stringPattern);
    
        int length = position - start;
        String text = code.substring(start, start + length);
    
        Matcher boolMatcher = boolRegex.matcher(text);
        Matcher stringMatcher = stringRegex.matcher(text);
    
        if (boolMatcher.matches())
            return new Token(TokenType.BOOLLITERAL, text, text.equals("\"TRUE\""), line, lineCol);
        else if (stringMatcher.matches())
            return new Token(TokenType.STRINGLITERAL, text, text.substring(1, text.length() - 1), line, lineCol);
        else {
            String errorMessage = text.contains("TRUE") || text.contains("FALSE") ? "Invalid BOOL literal" : "Invalid STRING literal";
            return new Token(TokenType.ERROR, text, errorMessage, line, lineCol);
        }
    }
      
    private Token getNumberLiteralToken() {
        boolean isFloat = current() == '.';
    
        int start = position;
        int lineCol = column;
    
        while (Character.isDigit(current()) || current() == '.')
            next(1);
    
        int length = position - start;
        String text = code.substring(start, start + length);
    
        String floatPattern = "\\d*\\.\\d+";
        String intPattern = "\\d+";
        Pattern floatRegex = Pattern.compile(floatPattern);
        Pattern intRegex = Pattern.compile(intPattern);
    
        Object val = null;
    
        Matcher floatMatcher = floatRegex.matcher(text);
        Matcher intMatcher = intRegex.matcher(text);
    
        if (intMatcher.matches()) {
            val = Integer.parseInt(text);
            return new Token(TokenType.INTLITERAL, text, val, line, lineCol);
        } else if (floatMatcher.matches()) {
            val = Float.parseFloat(text);
            return new Token(TokenType.FLOATLITERAL, text, val, line, lineCol);
        }
        return new Token(TokenType.ERROR, text, "Invalid Number.", line, lineCol);
    }
    private Token getEscapeCodeToken() {
        int start = position;
        int lineCol = column;

        while (!Character.isWhitespace(current()))
            next(1);

        int length = position - start;
        String text = code.substring(start, start + length);
        Object val = null;

        String escapeSequencePattern = "^\\[[\\]\\[\\&\\$\\#]\\]$";
//        String escapeSequencePattern = "\\[\\[\\]\\&\\$#\\]\\]";
        Pattern escapeRegex = Pattern.compile(escapeSequencePattern);

        Matcher matcher = escapeRegex.matcher(text);
        if (matcher.matches()) {
            val = text.charAt(1);
            return new Token(TokenType.ESCAPE, text, val, line, lineCol);
        }
        return new Token(TokenType.ERROR, text, "Invalid '" + text + "' as escape sequence.", line, lineCol);
    }
    
    
}
