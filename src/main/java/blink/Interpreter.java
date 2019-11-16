package blink;

import java.io.*;
import java.util.*;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public Environment globals = new Environment();
    private Environment environment = globals;
    private static Object unitialized = new Object();
    private static Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("print", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.print(stringify(args.get(0)));
                return null;
            }
        });

        globals.define("println", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.println(stringify(args.get(0)));
                return null;
            }
        });
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    public void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (blink.RuntimeError error) {
            Blink.runtimeError(error);
        }
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof BlinkClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }
        environment.define(stmt.name.lexeme, null);
        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }
        Map<String, BlinkFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            BlinkFunction function = new BlinkFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }
        BlinkClass _class = new BlinkClass(stmt.name.lexeme, (BlinkClass) superclass, methods);
        if (superclass != null) {
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, _class);
        return null;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int dist = locals.get(expr);
        BlinkClass superclass = (BlinkClass) environment.getAt(dist, "super");
        BlinkInstance object = (BlinkInstance) environment.getAt(dist - 1, "this");
        BlinkFunction method = superclass.findMethod(object, expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariables(expr.keyword, expr);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof BlinkInstance) {
            return ((BlinkInstance) object).get(expr.name);
        }

        if (object instanceof NativeInstance) {
            Object result = ((NativeInstance) object).findMethod(expr.name.lexeme);
            return result;
        }

        throw new RuntimeError(expr.name, "Not an instance of a class.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof BlinkInstance)) {
            throw new RuntimeError(expr.name, "Not an instance of a class.");
        }
        Object value = evaluate(expr.value);
        ((BlinkInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitArrayExpr(Expr.Array expr) {
        List<Object> values = new ArrayList<>();
        if (expr.values != null) {
            for (Expr value : expr.values) {
                values.add(evaluate(value));
            }
        }
        return values;
    }

    @Override
    public Object visitSubscriptExpr(Expr.Subscript expr) {
        List<Object> list = null;
        try {
            list = (List<Object>) evaluate(expr.object);
        } catch (Exception e) {
            throw new RuntimeError(expr.closeBracket, "Only arrays can be subscripted");
        }

        Object indexObject = evaluate(expr.index);
        if (!(indexObject instanceof Double)) {
            throw new RuntimeError(expr.closeBracket, "Only numbers can be used to index an array.");
        }

        int index = ((Double) indexObject).intValue();
        if (index >= list.size()) {
            throw new RuntimeError(expr.closeBracket, "Array index out of range.");
        }
        return list.get(index);
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new Jump(JumpType.BREAK);
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        throw new Jump(JumpType.CONTINUE);
    }

    @Override
    public Void visitExprStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitLetStmt(Stmt.Let stmt) {
        Object value = unitialized;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name, stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    public void executeBlock(List<Stmt> stmts, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.cond))) {
            try {
                execute(stmt.body);
            } catch (Jump jump) {
                if (jump.type == JumpType.BREAK) {
                    break;
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    @Override
    public Void visitDoWhileStmt(Stmt.DoWhile stmt) {
        do {
            try {
                execute(stmt.body);
            } catch (Jump jump) {
                if (jump.type == JumpType.BREAK) {
                    break;
                } else {
                    continue;
                }
            }
        } while (isTruthy(evaluate(stmt.cond)));
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        if (stmt.init != null) {
            evaluate(stmt.init);
        }
        while (true) {
            if (stmt.cond != null) {
                if (!isTruthy(evaluate(stmt.cond))) {
                    break;
                }
            }
            try {
                execute(stmt.body);
            } catch (Jump jump) {
                if (jump.type == JumpType.BREAK) {
                    break;
                } else {
                    if (stmt.incr != null) {
                        evaluate(stmt.incr);
                    }
                    continue;
                }
            }
            if (stmt.incr != null) {
                evaluate(stmt.incr);
            }
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        BlinkFunction func = new BlinkFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, func);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.expr != null) {
            value = evaluate(stmt.expr);
        }
        throw new Return(value);
    }

    @Override
    public Void visitSwitchStmt(Stmt.Switch stmt) {
        Object cond = evaluate(stmt.cond);
        int index = stmt.exprs.indexOf(cond);
        if (index == -1) {
            index = stmt.exprs.indexOf("default");
        }
        if (index != -1) {
            try {
                for (int i = index; i < stmt.branches.size(); i++) {
                    execute(stmt.branches.get(i));
                }
            } catch (Jump jump) {
                if (jump.type == JumpType.BREAK) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public Void visitUseStmt(Stmt.Use stmt) {
        Object module = evaluate(stmt.module);
        if (!(module instanceof String)) {
            throw new RuntimeError(stmt.keyword, "Module name must be a string.");
        }

        String moduleName = (String) module;

        if (moduleName.startsWith("std")) {
            String library = moduleName.split("::")[1];
            // Encryption stdlib
            switch (library) {
                case "Crypto":
                    globals.define("Crypto", StandardLibrary.Crypto);
                    return null;
                // Time stdlib
                case "Time":
                    globals.define("Time", StandardLibrary.Time);
                    return null;
                // File stdlib
                case "File":
                    globals.define("File", StandardLibrary.File);
                    return null;
                // Math stdlib
                case "Math":
                    globals.define("Math", StandardLibrary.Math);
                    return null;
                // Utils stdlib
                case "Utils":
                    globals.define("Utils", StandardLibrary.Utils);
                    return null;
                // Import all existing stdlibs
                case "*":
                    StandardLibrary.importAll(globals);
                    return null;

                default:
                    throw new RuntimeError(stmt.keyword, "'" + moduleName + "' is not a standard library");
            }
        }

        String source = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader((String) module));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                source += currentLine + "\n";
            }
        } catch (IOException e) {
            throw new RuntimeError(stmt.keyword, "Couldn't import module '" + module + "'.");
        }

        Blink.run(source);

        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.op.type == TokenType.OR) {
            if (isTruthy(left)) {
                return true;
            }
            return isTruthy(evaluate(expr.right));
        } else {
            if (!isTruthy(left)) {
                return false;
            }
            return isTruthy(evaluate(expr.right));
        }
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda expr) {
        return new BlinkFunction(expr, environment, false);
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.op.type) {
            case PLUS:
                if (left instanceof String && right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof  String && right instanceof Double) {
                    return (stringify(left)) + (stringify(right));
                }
                if (left instanceof Double && right instanceof String) {
                    return (stringify(left)) + (stringify(right));
                }
                if (left instanceof List) {
                    ((List)(left)).add(right);
                    return left;
                }
                throw new RuntimeError(expr.op, "Addition operation not supported for operands.");
            case MINUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left - (double) right;
                }

                if (left instanceof List && right instanceof Double) {
                    List<Object> list = (List) left;
                    List<Object> newList = new ArrayList<>();
                    int newSize = list.size() - ((Double) right).intValue();

                    if (newSize < 0) {
                        throw new RuntimeError(expr.op, "Cannot remove " + ((Double) right).intValue() +
                                " elements from an array with " + list.size() + " elements.");
                    }

                    for (int i = 0; i < newSize; i++) {
                        newList.add(list.get(i));
                    }

                    return newList;
                }

                throw new RuntimeError(expr.op, "Subtraction operation not supported for operands.");
            case MUL:
                checkNumbers(expr.op, left, right);
                return (double) left * (double) right;
            case DIV:
                checkNumbers(expr.op, left, right);
                if ((double) right == 0) {
                    throw new RuntimeError(expr.op, "Cannot divide by zero.");
                }
                return (double) left / (double) right;
            case MOD:
                checkNumbers(expr.op, left, right);
                if ((double) right == 0) {
                    throw new RuntimeError(expr.op, "Cannot divide by zero.");
                }
                return (double) left % (double) right;
            case EXP:
                checkNumbers(expr.op, left, right);
                return Math.pow((double) left, (double) right);
            case GREATER:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return left.toString().compareTo((String) right) > 0;
                }
                throw new RuntimeError(expr.op, "Comparison not supported for operands.");
            case GREATER_EQUALS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return left.toString().compareTo((String) right) >= 0;
                }
                throw new RuntimeError(expr.op, "Comparison not supported for operands.");
            case LESS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return left.toString().compareTo((String) right) < 0;
                }
                throw new RuntimeError(expr.op, "Comparison not supported for operands.");
            case LESS_EQUALS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return left.toString().compareTo((String) right) <= 0;
                }
                throw new RuntimeError(expr.op, "Comparison not supported for operands.");
            case EQUALS:
                return isEqual(left, right);
            case NOT_EQUALS:
                return !isEqual(left, right);
            case BIT_AND:
                if (isInteger(left) && isInteger(right)) {
                    Double l = (Double) left;
                    Double r = (Double) right;
                    int result = l.intValue() & r.intValue();
                    return (double) result;
                }
                throw new RuntimeError(expr.op, "Operand must be integers");
            case BIT_XOR:
                if (isInteger(left) && isInteger(right)) {
                    Double l = (Double) left;
                    Double r = (Double) right;
                    int result = l.intValue() ^ r.intValue();
                    return (double) result;
                }
                throw new RuntimeError(expr.op, "Operand must be integers");
            case BIT_OR:
                if (isInteger(left) && isInteger(right)) {
                    Double l = (Double) left;
                    Double r = (Double) right;
                    int result = l.intValue() | r.intValue();
                    return (double) result;
                }
                throw new RuntimeError(expr.op, "Operand must be integers");
            case COMMA:
                return right;
        }
        return null;
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.op.type) {
            case MINUS:
                checkNumber(expr.op, right);
                return -(double) right;
            case NOT:
                return !isTruthy(right);
            case BIT_NOT:
                if (isInteger(right)) {
                    Double val = (Double) right;
                    return (double) (~val.intValue());
                }
                throw new RuntimeError(expr.op, "Operand must be an integer");
        }
        return null;
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.val;
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitVarExpr(Expr.Variable expr) {
        Object value = lookUpVariables(expr.name, expr);
        if (value == unitialized) {
            throw new RuntimeError(expr.name, "Variable must be initialized before use");
        }
        return value;
    }

    private Object lookUpVariables(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        if (!(callee instanceof BlinkCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }
        for (Expr arg : expr.args) {
            arguments.add(evaluate(arg));
        }
        BlinkCallable function = (BlinkCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got "
                    + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        if (isTruthy(evaluate(expr.cond))) {
            return evaluate(expr.thenBranch);
        } else {
            return evaluate(expr.elseBranch);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    private void checkNumber(Token op, Object object) {
        if (object instanceof Double) {
            return;
        }
        throw new RuntimeError(op, "Operand must be a number");
    }

    private boolean isInteger(Object object) {
        if (object instanceof Double) {
            double val = (double) object;
            return !Double.isInfinite(val) && (Math.floor(val) == val);
        }
        return false;
    }

    private void checkNumbers(Token op, Object a, Object b) {
        if (a instanceof Double && b instanceof Double) {
            return;
        }
        throw new RuntimeError(op, "Operand must be numbers");
    }

    private String stringify(Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof List) {
            StringBuilder text = new StringBuilder("[");
            List<Object> list = (List<Object>) object;
            for (int i = 0; i < list.size(); ++i) {
                text.append(stringify(list.get(i)));
                if (i != list.size() - 1) {
                    text.append(", ");
                }
            }
            text.append("]");
            return text.toString();
        }

        if (object instanceof Double) {
            String num = object.toString();
            if (num.endsWith(".0")) {
                num = num.substring(0, num.length() - 2);
            }
            return num;
        }
        return object.toString();
    }
}