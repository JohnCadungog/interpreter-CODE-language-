package Analysis.Syntax;

import Analysis.Type.*;

public class Token {
    private TokenType tokenType;
    private String code;
    private Object value;
    private int line;
    private int column;

    public Token(TokenType tokenType, String code, Object value, int line, int column) {
        this.tokenType = tokenType;
        this.code = code;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public String getCode() {
        return code;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "Token(" + tokenType + ", " + code + ", " + value + ")";
    }
}