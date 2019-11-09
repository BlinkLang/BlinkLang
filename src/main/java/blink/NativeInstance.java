package blink;

import java.util.*;

public class NativeInstance implements BlinkCallable{
    final String name;
    private final Map<String, BlinkCallable> methods;

    NativeInstance(String name, Map<String, BlinkCallable> methods) {
        this.name = name;
        this.methods = methods;
    }

    BlinkCallable findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return null;
    }

    @Override
    public String toString() {
        return "<native instance " + name + ">";
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        BlinkCallable initializer = methods.get("init");
        if (initializer != null) {
            initializer.call(interpreter, args);
        }
        return this;
    }

    @Override
    public int arity() {
        BlinkCallable initializer = methods.get("init");
        if (initializer == null) {
            return 0;
        }

        return initializer.arity();
    }
}
