package com.blink.Blink;

import java.util.List;

interface BlinkCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
