package blink;

class ErrorHandler {
    public static void INVALID_ARGS() {
        System.err.println("Invalid arguments passed. Error code '1001'");
        System.exit(1001);
    }

    public static void FILE_ERROR(String file) {
        System.err.println("There was an error reading the file '" + file + "'. Error code '1002'");
        System.exit(1002);
    }

    public static void UNKNOWN_FILE_TYPE() {
        System.err.println("The file extension is not known to Blink. Error code '1003'");
        System.exit(1003);
    }

    public static void STATIC_ERROR() {
        System.err.println("There was an error while parsing code. Error code '1004'");
        System.exit(1004);
    }

    public static void RUNTIME_ERROR() {
        System.err.println("The Blink parser encountered an error. Error code '1005'");
        System.exit(1005);
    }
}
