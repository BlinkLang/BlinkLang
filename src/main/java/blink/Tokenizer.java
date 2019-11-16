package blink;

import java.util.*;

class Tokenizer {
    private final String source;
    private final List<Token> tokens;
    private int line, col, begin, curr;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("not", TokenType.NOT);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("let", TokenType.LET);
        keywords.put("for", TokenType.FOR);
        keywords.put("do", TokenType.DO);
        keywords.put("while", TokenType.WHILE);
        keywords.put("null", TokenType.NULL);
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("class", TokenType.CLASS);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("switch", TokenType.SWITCH);
        keywords.put("case", TokenType.CASE);
        keywords.put("default", TokenType.DEFAULT);
        keywords.put("inherits", TokenType.INHERITS);
        keywords.put("use", TokenType.USE);
        keywords.put("const", TokenType.CONST);
        keywords.put("enum", TokenType.ENUM);
    }

    Tokenizer(String source) {
        this.source = source;
        this.tokens = new ArrayList<Token>();
        this.begin = this.curr = this.col = 0;
        this.line = 1;
    }

    List<Token> getTokens() {
        return tokens;
    }

    public void scanTokens() {
        while(!atEnd()) {
            begin = curr;
            nextToken();
        }

        tokens.add(new Token(TokenType.EOF, null, "EOF", line, col + 1));
    }

    private void nextToken() {
        char c = consume();
        switch (c) {
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case '[': addToken(TokenType.LSQUARE); break;
            case ']': addToken(TokenType.RSQUARE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '+': addToken(match('+') ? TokenType.PLUS_PLUS : TokenType.PLUS); break;
            case '-': addToken(match('-') ? TokenType.MINUS_MINUS : TokenType.MINUS); break;
            case '/':
                if (match('*')) {
                    handleBlockComments();
                } else {
                    addToken(TokenType.DIV);
                }
                break;
            case '%': addToken(TokenType.MOD); break;
            case '=': addToken(match('>') ? TokenType.EQUALS_GREATER : TokenType.EQUALS); break;
            case '~': addToken(TokenType.BIT_NOT); break;
            case '&': addToken(TokenType.BIT_AND); break;
            case '|': addToken(TokenType.BIT_OR); break;
            case '^': addToken(TokenType.BIT_XOR); break;
            case '*':
                if (match('*')) {
                    addToken(TokenType.EXP);
                } else {
                    addToken(TokenType.MUL);
                }
                break;
            case '?': addToken(TokenType.QUESTION); break;
            case ':':
                if (match('=')) {
                    addToken(TokenType.ASSIGN);
                } else {
                    addToken(TokenType.COLON);
                }
                break;
            case '!':
                if (match('=')) {
                    addToken(TokenType.NOT_EQUALS);
                } else {
                    Blink.error(line, col, "Unexpected character");
                }
                break;
            case '>':
                if (match('=')) {
                    addToken(TokenType.GREATER_EQUALS);
                } else {
                    addToken(TokenType.GREATER);
                }
                break;
            case '<':
                if (match('=')) {
                    addToken(TokenType.LESS_EQUALS);
                } else {
                    addToken(TokenType.LESS);
                }
                break;
            case '#': handleComments(); break;
            case '"': handleStrings(); break;
            case '.': addToken(TokenType.DOT); break;
            case ' ':
            case '\t':
            case '\r':
                break;
            case '\n':
                ++line;
                col = 0;
                break;
            default:
                if (Character.isDigit(c)) {
                    handleNumber();
                } else if (Character.isLetter(c) || c == '_') {
                    handleIdentifier();
                } else {
                    Blink.error(line, col, "Unknown symbol '" + c + "'.");
                }
        }
    }

    private void handleBlockComments() {
        while (true) {
            if (atEnd()) {
                Blink.error(line, col, "Unterminated comment.");
                return;
            }

            if (peek() == '/') {
                consume();
                if (match('*')) {
                    handleBlockComments();
                }
            } else if (peek() == '*') {
                consume();
                if (match('/')) {
                    return;
                }
            } else if (peek() == '\n') {
                line++;
                col = 0;
            }

            if (!atEnd()) {
                consume();
            }
        }
    }

    private void handleComments() {
        while (!atEnd()) {
            if (peek() == '\n') {
                break;
            }

            consume();
        }
    }

    private void handleIdentifier() {
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            consume();
        }

        String lexeme = source.substring(begin, curr);
        TokenType type = keywords.get(lexeme);
        if (type == null) {
            addToken(TokenType.ID, lexeme);
        } else {
            addToken(type);
        }
    }

    private void handleStrings() {
        while (peek() != '"') {
            if (atEnd() || peek() == '\n') {
                Blink.error(line, col, "Unterminated string.");
                break;
            }

            consume();
        }

        consume();
        String lexeme = source.substring(begin + 1, curr - 1);
        addToken(TokenType.STRING, lexeme);
    }

    private void handleNumber() {
        while (Character.isDigit(peek())) {
            consume();
        }

        if (peek() == '.' && Character.isDigit(peekNext())) {
            consume();

            while (Character.isDigit(peek())) consume();
        }

        double val = Double.parseDouble(source.substring(begin, curr));
        addToken(TokenType.NUMBER, val);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(begin, curr);
        tokens.add(new Token(type, lexeme, literal, line, col));
    }

    private char peekNext() {
        if (curr + 1 >= source.length()) return '\0';
        return source.charAt(curr + 1);
    }

    private char peek() {
        if (atEnd()) return '\0';
        return source.charAt(curr);
    }

    private boolean match(char expected) {
        if (atEnd() || source.charAt(curr) != expected) {
            return false;
        }

        curr++;
        col++;
        return true;
    }

    private char consume() {
        curr++;
        col++;
        return source.charAt(curr - 1);
    }

    private boolean atEnd() {
        return curr >= source.length();
    }
}
