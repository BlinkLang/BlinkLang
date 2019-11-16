package blink;

import java.util.*;

class BlinkClass implements BlinkCallable {
    final String name;
    private final Map<String, BlinkFunction> methods;
    final BlinkClass superclass;

    BlinkClass(String name, BlinkClass superclass, Map<String, BlinkFunction> methods) {
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }

    @Override
    public int arity() {
        BlinkFunction initializer = methods.get("init");
        if (initializer == null) {
            return 0;
        }

        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        BlinkInstance instance = new BlinkInstance(this);
        BlinkFunction initializer = methods.get("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }

    boolean inherits(Token _class) {
        if (_class.lexeme.equals(name)) {
            return true;
        }
        if (superclass != null) {
            return superclass.inherits(_class);
        }
        return false;
    }

    public BlinkFunction findMethod(BlinkInstance instance, String name) {
        if (methods.containsKey(name)) {
            return methods.get(name).bind(instance);
        }

        if (superclass != null) {
            return superclass.findMethod(instance, name);
        }

        return null;
    }
}
