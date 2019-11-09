package blink;

import java.util.*;

abstract class Stmt {
    abstract <T> T accept(Visitor<T> vis);

    interface Visitor<T> {
        T visitExprStmt(Expression stmt);
        T visitLetStmt(Let stmt);
        T visitBlockStmt(Block stmt);
        T visitIfStmt(If stmt);
        T visitWhileStmt(While stmt);
        T visitDoWhileStmt(DoWhile stmt);
        T visitForStmt(For stmt);
        T visitFunctionStmt(Function stmt);
        T visitReturnStmt(Return stmt);
        T visitBreakStmt(Break stmt);
        T visitContinueStmt(Continue stmt);
        T visitSwitchStmt(Switch stmt);
        T visitClassStmt(Class stmt);
        T visitUseStmt(Use stmt);
    }

    static class Expression extends Stmt {
        final Expr expr;

        Expression(Expr expr) {
            this.expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> vis) {
            return vis.visitExprStmt(this);
        }
    }

    static class Let extends Stmt {
        final Token name;
        final Expr initializer;

        Let(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <T> T accept(Visitor<T> vis) {
            return vis.visitLetStmt(this);
        }
    }

    static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitBlockStmt(this);
        }
    }

    static class If extends Stmt {
        final Expr cond;
        final Stmt thenBranch;
        final Stmt elseBranch;

        If(Expr cond, Stmt thenBranch, Stmt elseBranch) {
            this.cond = cond;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitIfStmt(this);
        }
    }

    static class While extends Stmt {
        final Expr cond;
        final Stmt body;

        While(Expr cond, Stmt body) {
            this.cond = cond;
            this.body = body;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitWhileStmt(this);
        }
    }

    static class DoWhile extends Stmt {
        final Expr cond;
        final Stmt body;

        DoWhile(Expr cond, Stmt body) {
            this.cond = cond;
            this.body = body;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitDoWhileStmt(this);
        }
    }

    static class For extends Stmt {
        final Expr init, cond, incr;
        final Stmt body;

        For(Expr init, Expr cond, Expr incr, Stmt body) {
            this.init = init;
            this.cond = cond;
            this.incr = incr;
            this.body = body;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitForStmt(this);
        }
    }

    static class Function extends Stmt {
        Token name;
        List<Token> params;
        List<Stmt> body;

        Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitFunctionStmt(this);
        }
    }

    static class Return extends Stmt {
        Expr expr;
        Token keyword;

        Return(Token keyword, Expr expr) {
            this.keyword = keyword;
            this.expr = expr;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitReturnStmt(this);
        }
    }

    static class Break extends Stmt {
        Token keyword;

        Break(Token keyword) {
            this.keyword = keyword;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitBreakStmt(this);
        }
    }

    static class Continue extends Stmt {
        Token keyword;

        Continue(Token keyword) {
            this.keyword = keyword;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitContinueStmt(this);
        }
    }

    static class Switch extends Stmt {
        Expr cond;
        ArrayList<Stmt> branches;
        ArrayList<Object> exprs;

        Switch(Expr cond, ArrayList<Object> exprs, ArrayList<Stmt> branches) {
            this.cond = cond;
            this.exprs = exprs;
            this.branches = branches;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitSwitchStmt(this);
        }
    }

    static class Class extends Stmt {
        Token name;
        List<Stmt.Function> methods;
        Expr.Variable superclass;

        Class(Token name, List<Stmt.Function> methods, Expr.Variable superclass) {
            this.name = name;
            this.methods = methods;
            this.superclass = superclass;
        }

        <T> T accept(Visitor<T> vis) {
            return vis.visitClassStmt(this);
        }
    }

    static class Use extends Stmt {
        Token keyword;
        Expr module;

        Use(Token keyword, Expr module) {
            this.keyword = keyword;
            this.module = module;
        }

        <T> T accept(Visitor<T> vis) { return vis.visitUseStmt(this); }
    }
}