package blink;

enum LoopType {
    NONE,
    LOOP,
    SWITCH
}

enum JumpType {
    BREAK,
    CONTINUE
}

class Jump extends RuntimeException {
    JumpType type;
    Jump(JumpType type) {
        super(null, null, false, false);
        this.type = type;
    }
}