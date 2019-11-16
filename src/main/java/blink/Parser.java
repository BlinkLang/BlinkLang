package blink;

import java.util.*;

class Parser {
    private final List<Token> tokens;
    private int curr = 0;

    private static class ParseError extends RuntimeException {
    }

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        try {
            List<Stmt> statements = new ArrayList<>();
            while (!atEnd()) {
                statements.add(declaration());
            }
            return statements;
        } catch (ParseError error) {
            return null;
        }
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.LET)) return varDeclaration();
            if (check(TokenType.FUNCTION) && checkNext(TokenType.ID)) {
                consume(TokenType.FUNCTION, null);
                return funDeclaration("function");
            }
            if (match(TokenType.CLASS)) return classDeclaration();
            return statement();
        } catch (ParseError error) {
            sync();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.ID, "Expect class name.");
        Expr.Variable superclass = null;
        if (match(TokenType.INHERITS)) {
            consume(TokenType.ID, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }
        consume(TokenType.LBRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !atEnd()) {
            methods.add((Stmt.Function) funDeclaration("method"));
        }
        consume(TokenType.RBRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, methods, superclass);
    }

    private Stmt funDeclaration(String kind) {
        Token name = consume(TokenType.ID, "Expected " + kind + " name.");
        consume(TokenType.LPAREN, "Expected '(' after" + kind + " name.");

        List<Token> params = new ArrayList<>();

        if (!check(TokenType.RPAREN)) {
            do {
                if (params.size() > 32) Blink.error(peek(), "Cannot have more than 32 parameters");
                params.add(consume(TokenType.ID, "Expected parameter name."));
            } while(match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expected ')' after parameters.");
        consume(TokenType.LBRACE, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, params, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.ID, "Expect variable name");
        Expr initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Let(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.LBRACE)) return new Stmt.Block(block());
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.DO)) return doWhileStmt();
        if (match(TokenType.FOR)) return forStmt();
        if (match(TokenType.RETURN)) return returnStmt();
        if (match(TokenType.BREAK)) {
            Token keyword = previous();
            consume(TokenType.SEMICOLON, "Expect ';' after break statement.");
            return new Stmt.Break(keyword);
        }
        if (match(TokenType.CONTINUE)) {
            Token keyword = previous();
            consume(TokenType.SEMICOLON, "Expect ';' after continue statement.");
            return new Stmt.Continue(keyword);
        }
        if (match(TokenType.SWITCH)) return switchStmt();
        if (match(TokenType.USE)) return useStmt();

        return expressionStmt();
    }

    private Stmt switchStmt() {
        consume(TokenType.LPAREN, "Expect '(' after switch.");
        Expr cond = expression();
        consume(TokenType.RPAREN, "Expect ')' after switch expression.");
        consume(TokenType.LBRACE, "Expect '{' at the start of switch block.");

        ArrayList<Stmt> branches = new ArrayList<>();
        ArrayList<Object> exprs = new ArrayList<>();

        while (!match(TokenType.RBRACE)) {
            if (atEnd()) {
                Blink.error(peek(), "Unexpected end of file.");
            } else if (match(TokenType.CASE)) {
                if (!match(TokenType.STRING, TokenType.NUMBER, TokenType.TRUE, TokenType.FALSE, TokenType.NULL)) {
                    Blink.error(peek(), "Case expressions must be constants.");
                }
                Object val = previous().literal;
                if (exprs.indexOf(val) != -1) {
                    Blink.error(peek(), "Case expressions must be unique.");
                }
                consume(TokenType.COLON, "Expect ':' after case.");
                Stmt toDo = null;
                if (!check(TokenType.CASE) && !check(TokenType.DEFAULT) && !check(TokenType.RBRACE)) {
                    toDo = statement();
                }
                exprs.add(val);
                branches.add(toDo);
            } else if (match(TokenType.DEFAULT)) {
                if (exprs.indexOf("default") != -1) {
                    Blink.error(peek(), "Duplicate default stmt.");
                }
                consume(TokenType.COLON, "Expect ':' after case.");
                Stmt toDo = null;
                if (!check(TokenType.CASE) && !check(TokenType.DEFAULT) && !check(TokenType.RBRACE)) {
                    toDo = statement();
                }
                exprs.add("default");
                branches.add(toDo);
            } else {
                Blink.error(peek(), "Unexpected token in middle of switch block.");
                break;
            }
        }
        return new Stmt.Switch(cond, exprs, branches);
    }

    private Stmt returnStmt() {
        Token keyword = previous();
        Expr expr = null;
        if (!check(TokenType.SEMICOLON)) {
            expr = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return statement.");
        return new Stmt.Return(keyword, expr);
    }

    private Stmt forStmt() {
        consume(TokenType.LPAREN, "Expect '(' after for.");
        Expr init = null;
        if (!match(TokenType.SEMICOLON)) {
            init = expression();
            consume(TokenType.SEMICOLON, "Expect ';' after initializer.");
        }
        Expr cond = null;
        if (!match(TokenType.SEMICOLON)) {
            cond = expression();
            consume(TokenType.SEMICOLON, "Expect ';' after condition.");
        }
        Expr incr = null;
        if (!match(TokenType.RPAREN)) {
            incr = expression();
            consume(TokenType.RPAREN, "Expect ')' after for expression.");
        }
        Stmt body = statement();
        return new Stmt.For(init, cond, incr, body);
    }

    private Stmt doWhileStmt() {
        Stmt body = statement();
        consume(TokenType.WHILE, "Expect 'while' after do block.");
        consume(TokenType.LPAREN, "Expect '(' after while.");
        Expr cond = expression();
        consume(TokenType.RPAREN, "Expect ')' after while condition.");
        consume(TokenType.SEMICOLON, "Expect ';' after do-while.");
        return new Stmt.DoWhile(cond, body);
    }

    private Stmt whileStatement() {
        consume(TokenType.LPAREN, "Expect '(' after while.");
        Expr cond = expression();
        consume(TokenType.RPAREN, "Expect ')' after while condition.");
        Stmt body = statement();
        return new Stmt.While(cond, body);
    }

    private Stmt ifStatement() {
        consume(TokenType.LPAREN, "Expect '(' after if.");
        Expr cond = expression();
        consume(TokenType.RPAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(cond, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !atEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RBRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStmt() {
        Expr val = expression();
        consume(TokenType.SEMICOLON, "Expect ';' at end of expression.");
        return new Stmt.Expression(val);
    }

    private Stmt useStmt() {
        Token keyword = previous();
        Expr module = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after module name");
        return new Stmt.Use(keyword, module);
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr left = assignment();
        while (match(TokenType.COMMA)) {
            Token op = previous();
            Expr right = assignment();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr assignment() {

        Expr expr = lambda();
        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);

            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.name, get.object, value);
            }
            Blink.error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr lambda() {
        if (match(TokenType.LESS)) {
            List<Token> params = new ArrayList<>();
            if (!check(TokenType.LESS)) {
                do {
                    if (params.size() > 32) {
                        Blink.error(peek(), "Cannot have more than 32 parameters.");
                    }
                    params.add(consume(TokenType.ID, "Expected parameter name."));
                } while(match(TokenType.COMMA));
            }
            consume(TokenType.GREATER, "Expected '>' after lambda parameters");
            consume(TokenType.EQUALS_GREATER, "Expected '=>' before '{'.");
            Token start = consume(TokenType.LBRACE, "Expected '{' before lambda body.");
            List<Stmt> body = block();
            return new Expr.Lambda(start, params, body);
        }
        return logicalOr();
    }

    private Expr conditional() {
        Expr expr = logicalOr();
        if (match(TokenType.QUESTION)) {
            Expr thenBranch = expression();
            consume(TokenType.COLON, "Expect ':' after conditional expression.");
            Expr elseBranch = conditional();
            expr = new Expr.Conditional(expr, thenBranch, elseBranch);
        }
        return expr;
    }

    private Expr logicalOr() {
        Expr left = logicalAnd();
        while (match(TokenType.OR)) {
            Token op = previous();
            Expr right = logicalAnd();
            left = new Expr.Logical(left, op, right);
        }
        return left;
    }

    private Expr logicalAnd() {
        Expr left = bitwiseOr();
        while (match(TokenType.AND)) {
            Token op = previous();
            Expr right = bitwiseOr();
            left = new Expr.Logical(left, op, right);
        }
        return left;
    }

    private Expr bitwiseOr() {
        Expr left = bitwiseXor();
        while (match(TokenType.BIT_OR)) {
            Token op = previous();
            Expr right = bitwiseOr();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr bitwiseXor() {
        Expr left = bitwiseAnd();
        while (match(TokenType.BIT_XOR)) {
            Token op = previous();
            Expr right = bitwiseAnd();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr bitwiseAnd() {
        Expr left = equality();
        while (match(TokenType.BIT_AND)) {
            Token op = previous();
            Expr right = equality();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr equality() {
        Expr left = comparison();
        while (match(TokenType.NOT_EQUALS, TokenType.EQUALS)) {
            Token op = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr comparison() {
        Expr left = addition();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUALS, TokenType.LESS, TokenType.LESS_EQUALS)) {
            Token op = previous();
            Expr right = addition();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr addition() {
        Expr left = multiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            Expr right = multiplication();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr multiplication() {
        Expr left = modulo();
        while (match(TokenType.MUL, TokenType.DIV)) {
            Token op = previous();
            Expr right = modulo();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr modulo() {
        Expr left = exponentiation();
        while (match(TokenType.MOD)) {
            Token op = previous();
            Expr right = exponentiation();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr exponentiation() {
        Expr left = unary();
        while (match(TokenType.EXP)) {
            Token op = previous();
            Expr right = unary();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr unary() {
        if (match(TokenType.BIT_NOT, TokenType.NOT, TokenType.MINUS)) {
            Token op = previous();
            Expr right = unary();
            return new Expr.Unary(op, right);
        }
        return call();
    }

    private Expr call() {
        Expr callee = primary();
        while (true) {
            if (match(TokenType.LPAREN)) {

                callee = finishCall(callee);
            } else if (match(TokenType.DOT)) {
                    Token name = consume(TokenType.ID, "Expect property name after '.'");
                    callee = new Expr.Get(name, callee);
            } else if (match(TokenType.LSQUARE)) {
                Expr index = primary();
                Token closeBracket = consume(TokenType.RSQUARE, "Expected ']' after subscript index");
                callee = new Expr.Subscript(callee, closeBracket, index);
            } else {
                break;
            }
        }
        return callee;
}

    private Expr finishCall(Expr callee) {
        List<Expr> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                if (args.size() >= 32) {
                    Blink.error(peek(), "Cannot have more than 32 arguments.");
                }
                args.add(assignment());
            } while (match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RPAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, args);
    }

    private Expr primary() {
        Expr expr = null;
        if (match(TokenType.FALSE)) {
            expr = new Expr.Literal(false);

        } else if (match(TokenType.TRUE)) {
            expr = new Expr.Literal(true);
        } else if (match(TokenType.NULL)) {
            expr = new Expr.Literal(null);

        } else if (match(TokenType.NUMBER, TokenType.STRING)) {
            expr = new Expr.Literal(previous().literal);
        } else if (match(TokenType.LPAREN)) {
            Expr grp = expression();
            consume(TokenType.RPAREN, "Unmatched ')'");
            expr = new Expr.Grouping(grp);

        } else if (match(TokenType.LSQUARE)) {
            List<Expr> values = new ArrayList<>();
            if (match(TokenType.RSQUARE)) {
                return new Expr.Array(null);
            }
            while (!match(TokenType.RSQUARE)) {
                Expr value = assignment();
                values.add(value);
                if (peek().type != TokenType.RSQUARE) {
                    consume(TokenType.COMMA, "Expected a comma before the next expression");
                }
            }
            return new Expr.Array(values);

        } else if (match(TokenType.ID)) {
            expr = new Expr.Variable(previous());

        } else if (match(TokenType.THIS)) {
            return new Expr.This(previous());

        } else if (match(TokenType.SUPER)) {
            Token keyword = previous();
            consume(TokenType.DOT, "Expect '.' after 'super'.");
            Token method = consume(TokenType.ID, "Expect superclass method name.");
            expr = new Expr.Super(keyword, method);
        }
        if (expr != null) {
            return expr;
        }
        throw error(peek(), "Expect expression");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Blink.error(token, message);
        return new ParseError();
    }

    private void sync() {
        advance();
        while (!atEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }
            switch (peek().type) {
                // Enums don't need to be qualified in switch cases apparently
                case CLASS:
                case FUNCTION:
                case LET:
                case FOR:
                case WHILE:
                case DO:
                case IF:
                case SWITCH:
                case BREAK:
                case CONTINUE:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType expected) {
        if (atEnd()) {
            return false;
        }
        return peek().type == expected;
    }

    private boolean checkNext(TokenType expected) {
        if (atEnd()) return false;
        if (tokens.get(curr + 1).type == TokenType.EOF) return false;
        return tokens.get(curr + 1).type == expected;
    }

    private Token advance() {
        if (!atEnd()) {
            curr++;
        }
        return previous();
    }

    private boolean atEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(curr);
    }

    private Token previous() {
        return tokens.get(curr - 1);
    }
}