package blink;

import java.util.*;

class BlinkFunction implements BlinkCallable {
    private final String name;
    private final FunctionType type;
    private final List<Token> params;
    private final List<Stmt> body;
    private final Environment closure;
    private final boolean isInitializer;

    BlinkFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this(declaration.name.lexeme, FunctionType.FUNCTION, declaration.params, declaration.body, closure, isInitializer);
    }

    BlinkFunction(Expr.Lambda declaration, Environment closure, boolean isInitializer) {
        this("", FunctionType.LAMBDA, declaration.params, declaration.body, closure, isInitializer);
    }

    BlinkFunction(String name, FunctionType type, List<Token> params, List<Stmt> body, Environment closure, boolean isInitializer) {
        this.name = name;
        this.type = type;
        this.params = params;
        this.body = body;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    BlinkFunction bind(BlinkInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new BlinkFunction(name, type, params, body, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < params.size(); i++) {
            environment.define(params.get(i).lexeme, args.get(i));
        }
        try {
            interpreter.executeBlock(body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }
        if (isInitializer) return closure.getAt(0, "this");
        return null;
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
