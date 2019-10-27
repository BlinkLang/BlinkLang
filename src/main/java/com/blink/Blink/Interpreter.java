package com.blink.Blink;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedWriter;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    private InputStreamReader getcStream = new InputStreamReader(System.in, StandardCharsets.UTF_8);

    Interpreter() {
        // Returns a formatted date and time string
        globals.define("dateAndTime", new BlinkCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Date dateAndTime = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                return formatter.format(dateAndTime);
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("date", new BlinkCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                return formatter.format(date);
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("time", new BlinkCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Date time = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                return formatter.format(time);
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("readFile", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                String contents;

                try {
                    // File path is 1st argument
                    BufferedReader br = new BufferedReader(new FileReader(stringify(arguments.get(0))));
                    String currentLine;
                    contents = "";
                    while ((currentLine = br.readLine()) != null) {
                        contents += currentLine + "\n";
                    }
                } catch (IOException exception) {
                    return null;
                }

                return contents;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("writeFile", new BlinkCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                try {
                    // File path is 1st argument
                    BufferedWriter bw = new BufferedWriter(new FileWriter(stringify(arguments.get(0))));
                    // Data is 2nd argument
                    bw.write(stringify(arguments.get(1)));

                    bw.close();
                    return true;
                } catch (IOException exception) {
                    return false;
                }
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("appendFile", new BlinkCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                try {
                    // File path is 1st argument
                    BufferedWriter bw = new BufferedWriter(new FileWriter(stringify(arguments.get(0)), true));
                    // Data is 2nd argument
                    bw.append(stringify(arguments.get(1)));

                    bw.close();
                    return true;
                } catch (IOException exception) {
                    return false;
                }
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        // RNG number calculation
        globals.define("random", new BlinkCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                double firstArg = (Double) arguments.get(0);
                int floor = (int) firstArg;
                double secondArg = (Double) arguments.get(1);
                int ceiling = (int) secondArg;

                int rnd = (int) (Math.random()*ceiling+floor);
                return rnd;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        // Return the size of an array
        globals.define("sizeof", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object object = arguments.get(0);

                if (object instanceof String) {
                    return (double) ((String) object).length();
                }

                if (object instanceof  List) {
                    return (double) ((List) object).size();
                }

                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        // Get input from console!
        globals.define("input", new BlinkCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    return br.readLine();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("getc", new BlinkCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    int c = getcStream.read();
                    if (c < 0) {
                        return (double)-1;
                    }
                    return (double)c;
                } catch (IOException error) {
                    return (double)-1;
                }
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("chr", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return Character.toString((char)(double)arguments.get(0));
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("exit", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.exit((int)(double)arguments.get(0));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("print_error", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.err.println((String)arguments.get(0));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });

        globals.define("put", new BlinkCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(stringify(arguments.get(0)));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Blink.runtimeError(error);
        }
    }

    @Override
    public Object visitAllotExpr(Expr.Allot expr) {
        Expr.Subscript subscript = null;
        if (expr.object instanceof Expr.Subscript) {
            subscript = (Expr.Subscript)expr.object;
        }

        Object listObject = evaluate(subscript.object);
        if (!(listObject instanceof List)) {
            throw new RuntimeError(expr.name,
                    "Only arrays can be subscripted.");
        }

        List<Object> list = (List)listObject;

        Object indexObject = evaluate(subscript.index);
        if (!(indexObject instanceof Double)) {
            throw new RuntimeError(expr.name,
                    "Only numbers can be used as an array index.");
        }

        int index = ((Double) indexObject).intValue();
        if (index >= list.size()) {
            throw new RuntimeError(expr.name,
                    "Array index out of range.");
        }

        Object value = evaluate(expr.value);

        list.set(index, value);
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
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof BlinkInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((BlinkInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        BlinkClass superclass = (BlinkClass)environment.getAt(distance, "super");

        BlinkInstance object = (BlinkInstance)environment.getAt(distance - 1, "this");
        BlinkFunction method = superclass.findMethod(expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be a number.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "null";

        if (object instanceof List) {
            String text = "[";
            List<Object> list = (List<Object>)object;
            for (int i = 0; i < list.size(); i++) {
                text += stringify(list.get(i));
                if (i != list.size() - 1) {
                    text += ", ";
                }
            }
            text += "]";
            return text;
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
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

        BlinkClass klass = new BlinkClass(stmt.name.lexeme, (BlinkClass)superclass, methods);

        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        BlinkFunction function = new BlinkFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        Object module = evaluate(stmt.module);
        if (!(module instanceof String)) {
            throw new RuntimeError(stmt.keyword, "Module name must be a string.");
        }

        String source = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader((String)module));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                source += currentLine + "\n";
            }
        } catch (IOException exception) {
            throw new RuntimeError(stmt.keyword, "Could not import module '" + module + "'.");
        }

        Blink.run(source);

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }

        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case MINUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left - (double)right;
                }

                if (left instanceof List && right instanceof Double) {
                    List list = (List)left;
                    List<Object> newList = new ArrayList<>();
                    int newSize = list.size() - ((Double) right).intValue();

                    if (newSize < 0) {
                        throw new RuntimeError(expr.operator,
                                "Cannot remove " + ((Double) right).intValue() + " elements from an array of size " +
                                        list.size() + ".");
                    }

                    for (int index = 0; index < newSize; ++index) {
                        newList.add(list.get(index));
                    }

                    return newList;
                }

                throw new RuntimeError(expr.operator, "Invalid operands to binary operator '-'.");
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                if (left instanceof List) {
                    List list = (List)left;
                    list.add(right);
                    return left;
                }

                throw new RuntimeError(expr.operator, "Invalid operands to binary operator '+'.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof BlinkCallable)) {
            throw new RuntimeError(expr.paren, "Only functions and classes are callable.");
        }

        BlinkCallable function = (BlinkCallable)callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() +
                    " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof BlinkInstance) {
            return ((BlinkInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object left = evaluate(expr.left);
        Object middle = evaluate(expr.middle);
        Object right = evaluate(expr.right);

        if (expr.leftOper.type == TokenType.QUESTION &&
                expr.rightOper.type == TokenType.COLON) {
            if (isTruthy(left)) {
                return middle;
            }

            return right;
        }

        return null;
    }

    @Override
    public Object visitSubscriptExpr(Expr.Subscript expr) throws RuntimeError {
        Object listObject = evaluate(expr.object);
        if (!(listObject instanceof List)) {
            throw new RuntimeError(expr.name, "Only arrays can be subscripted.");
        }

        List list = (List)listObject;

        Object indexObject = evaluate(expr.index);
        if (!(indexObject instanceof Double)) {
            throw new RuntimeError(expr.name, "Only numbers can be used as an index.");
        }

        int index = ((Double) indexObject).intValue();
        if (index >= list.size()) {
            throw new RuntimeError(expr.name, "Array index out of range.");
        }
        return list.get(index);
    }
}
