package blink;

import java.util.*;

abstract class Expr {
    abstract <T> T accept(Visitor<T> vis);

    interface Visitor<T> {
        T visitBinary(Binary expr);
        T visitUnary(Unary expr);
        T visitLiteral(Literal expr);
        T visitGrouping(Grouping expr);
        T visitVarExpr(Variable expr);
        T visitAssignExpr(Assign expr);
        T visitLogicalExpr(Logical expr);
        T visitConditionalExpr(Conditional expr);
        T visitCallExpr(Call expr);
        T visitGetExpr(Get expr);
        T visitSetExpr(Set expr);
        T visitThisExpr(This expr);
        T visitSuperExpr(Super expr);
        T visitArrayExpr(Array expr);
        T visitSubscriptExpr(Subscript Expr);
        T visitLambdaExpr(Lambda expr);
    }

    static class Binary extends Expr {
        final Expr left, right;
        Token op;

        Binary(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> vis) {
            return vis.visitBinary(this);
        }
    }

    static class Unary extends Expr {
        final Expr right;
        Token op;

        Unary(Token op, Expr right) {
            this.op = op;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> vis) {
            return vis.visitUnary(this);
        }
    }

    static class Literal extends Expr {
        final Object val;

        Literal(Object val) {
            this.val = val;
        }

        @Override
        <T> T accept(Visitor<T> vis) {
            return vis.visitLiteral(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> vis) {
            return vis.visitGrouping(this);
        }
    }

    static class Variable extends Expr {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> vis) {
            return vis.visitVarExpr(this);
        }
    }

    static class Assign extends Expr {
        final Token name;
        final Expr value;

        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class Logical extends Expr {
        final Token op;
        final Expr left, right;

        Logical(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    static class Conditional extends Expr {
        final Expr cond, thenBranch, elseBranch;

        Conditional(Expr cond, Expr thenBranch, Expr elseBranch) {
            this.cond = cond;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        <T> T accept(Visitor<T> visitor) {
            return visitor.visitConditionalExpr(this);
        }
    }

    static class Call extends Expr {
        final Expr callee;
        Token paren;
        final List<Expr> args;

        Call(Expr callee, Token paren, List<Expr> args) {
            this.callee = callee;
            this.paren = paren;
            this.args = args;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitCallExpr(this);
        }
    }

    static class Get extends Expr {
        Token name;
        Expr object;

        Get(Token name, Expr object) {
            this.name = name;
            this.object = object;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitGetExpr(this);
        }
    }

    static class Set extends Expr {
        Token name;
        Expr object, value;

        Set(Token name, Expr object, Expr value) {
            this.name = name;
            this.object = object;
            this.value = value;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitSetExpr(this);
        }
    }

    static class This extends Expr {
        Token keyword;

        This(Token keyword) {
            this.keyword = keyword;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitThisExpr(this);
        }
    }

    static class Super extends Expr {
        Token keyword, method;

        Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitSuperExpr(this);
        }
    }

    static class Array extends Expr {
        List<Expr> values;

        Array(List<Expr> values) {
            this.values = values;
        }

        <T> T accept(Visitor<T> vis) { return vis.visitArrayExpr(this); }
    }

    static class Subscript extends Expr {
        Expr object, index;
        Token closeBracket;

        Subscript(Expr object, Token closeBracket, Expr index) {
            this.object = object;
            this.closeBracket = closeBracket;
            this.index = index;
        }

        <T> T accept(Visitor<T> vis) { return vis.visitSubscriptExpr(this); }
    }

    static class Lambda extends Expr {
        Token start;
        List<Token> params;
        List<Stmt> body;

        Lambda(Token start, List<Token> params, List<Stmt> body) {
            this.start = start;
            this.params = params;
            this.body = body;
        }

        <T> T accept(Visitor<T> vis) { return vis.visitLambdaExpr(this); }
    }
}