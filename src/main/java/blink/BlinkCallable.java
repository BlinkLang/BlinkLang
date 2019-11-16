package blink;

import java.util.*;

interface BlinkCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> args);
}
