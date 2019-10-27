package com.blink.Blink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.blink.Blink.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private int openParen = 0;
    private int openSquareBrackets = 0;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("function", FUN);
        keywords.put("if", IF);
        keywords.put("null", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        keywords.put("inherits", INHERITS);
        keywords.put("import", IMPORT);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' : addToken(LEFT_PAREN); ++openParen; break;
            case ')' : addToken(RIGHT_PAREN); --openParen; break;
            case '{' : addToken(LEFT_BRACE); break;
            case '}' : addToken(RIGHT_BRACE); break;
            case '[' : addToken(LEFT_SQUARE); ++openSquareBrackets; break;
            case ']' : addToken(RIGHT_SQUARE); --openSquareBrackets; break;
            case ',' : addToken(COMMA); break;
            case '.' : addToken(DOT); break;
            case '-' : addToken(MINUS); break;
            case '+' : addToken(PLUS); break;
            case ';' : addToken(SEMICOLON); break;
            case '*' : addToken(STAR); break;
            case ':' : addToken(COLON); break;
            case '?' : addToken(QUESTION); break;

            case '!' : addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=' : addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<' : addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>' : addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if(match('*')) {
                    blockComment();
                } else {
                    addToken(SLASH);
                }
                break;

            case '"' : string(); break;

            case '\n':
                ++line;
                if (tokens.size() == 0) {
                    ++line;
                } else {
                   Token lastToken  = tokens.get(tokens.size() - 1);
                    if (openParen == 0 && openSquareBrackets == 0 &&
                            lastToken.type != SEMICOLON &&
                            lastToken.type != LEFT_BRACE &&
                            lastToken.type != RIGHT_BRACE)
                        addToken(SEMICOLON);
                }

                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlphaOrUnderscore(c)) {
                    identifier();
                } else {
                    Blink.error(line, "Unexpected character '" + c + "'.");
                }
                break;
        }
    }

    private void blockComment() {
        while (peek() != '*' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            if (peek() == '/') {
                if (peekNext() == '*') {
                    advance();
                    advance();
                    blockComment();
                }
            }
            advance();
        }
        if (isAtEnd()) {
            Blink.error(line, "Unterminated block comment");
        }

        advance();

        if (!match('/')) {
            advance();
            blockComment();
        }
    }

    private void identifier() {
        while (isAlphaNumericOrUnderscore(peek())) advance();

        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            if (peek() == '\\' && peekNext() == '"') advance();
            advance();
        }

        if (isAtEnd()) {
            Blink.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        ++current;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char beforePrevious() {
        return source.charAt(current - 2);
    }

    private boolean isAlphaOrUnderscore(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumericOrUnderscore(char c) {
         return isAlphaOrUnderscore(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

}
