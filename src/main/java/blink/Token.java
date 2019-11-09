package blink;

class Token {
    final TokenType type;
    final int line, col;
    final String lexeme;
    final Object literal;

    Token(TokenType type, String lexeme, Object literal, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.col = col;
    }

    public String toString() {
        return "<" + type + "," + lexeme + "," + literal + "," + line + "," + col + ">";
    }
}
