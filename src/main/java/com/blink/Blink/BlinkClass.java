package com.blink.Blink;

import java.util.List;
import java.util.Map;

class BlinkClass implements BlinkCallable {
    final String name;
    final BlinkClass superclass;
    private final Map<String, BlinkFunction> methods;

    BlinkClass(String name, BlinkClass superclass,
               Map<String, BlinkFunction> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    BlinkFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        BlinkInstance instance = new BlinkInstance(this);
        BlinkFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        BlinkFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }
}
