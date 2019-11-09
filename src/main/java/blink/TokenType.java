package blink;

enum TokenType {
    // Grouping
    LPAREN, RPAREN, SEMICOLON, LBRACE, RBRACE, LSQUARE, RSQUARE, COMMA, DOT,

    // Arithmetic
    PLUS, MINUS, MUL, DIV, ASSIGN, MOD, EXP,
    PLUS_PLUS, MINUS_MINUS,

    // Logical
    EQUALS, NOT_EQUALS, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS,

    // Point symbol
    EQUALS_GREATER,

    // Boolean
    NOT, AND, OR,

    // Bitwise
    BIT_NOT, BIT_AND, BIT_OR, BIT_XOR,

    // Literals
    ID, STRING, NUMBER,

    // Reserved words
    IF, ELSE, LET, FUNCTION, FOR, WHILE, DO, RETURN, TRUE, FALSE, NULL, BREAK, CONTINUE, CLASS,
    SUPER, THIS, QUESTION, COLON, SWITCH, CASE, DEFAULT, INHERITS, USE, CONST, ENUM,

    // EOF
    EOF
}
