package blink;

import java.util.*;

class BlinkInstance {
    private BlinkClass _class;
    private final Map<String, Object> fields = new HashMap<>();

    BlinkInstance(BlinkClass _class) {
        this._class = _class;
    }

    @Override
    public String toString() {
        return "<" + _class.name + " instance>";
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        BlinkFunction method = _class.findMethod(this, name.lexeme);
        if (method != null) {
            return method;
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    BlinkClass _class() {
        return _class;
    }
}
