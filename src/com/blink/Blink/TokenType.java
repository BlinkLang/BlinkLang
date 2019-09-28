package com.blink.Blink;

enum TokenType {
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_SQUARE, RIGHT_SQUARE, // punctuation
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, // punctuation

    BANG, BANG_EQUAL, // operators
    EQUAL, EQUAL_EQUAL, // operators
    GREATER, GREATER_EQUAL, // operators
    LESS, LESS_EQUAL, // operators

    IDENTIFIER, STRING, NUMBER, // Types (these are used in the code and not in the actual .blink files)

    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, // Keywords
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE, // Keywords
    INHERITS, IMPORT, // Keywords

    EOF // Just identifies the End of File (this isn't used in blink files)
}