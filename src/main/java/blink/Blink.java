package blink;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class Blink {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();

    public static final List<Object> argv = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            for (int i = 1; i < args.length; i++) {
                argv.add(args[i]);
            }

            try {
                runFile(args[0]);
            } catch (IOException exception) {
                ErrorHandler.FILE_ERROR(args[0]);
            }
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        if (path.endsWith(".blink")) {
            run(new String(bytes, Charset.defaultCharset()));
            if (hadError) ErrorHandler.STATIC_ERROR();
            if (hadRuntimeError) ErrorHandler.RUNTIME_ERROR();
        } else {
            ErrorHandler.UNKNOWN_FILE_TYPE();
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(input);

        while (true) {
            System.out.print("Blink > ");
            run(br.readLine() + "\n");
            hadError = false;
        }
    }

    public static void run(String source) {
        Tokenizer tokenizer = new Tokenizer(source);
        tokenizer.scanTokens();
        List<Token> tokens = tokenizer.getTokens();

        if (hadError) {
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) {
            return;
        }

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }

    static void error(int line, int col, String message) {
        report(line, col, "", message);
    }

    private static void report(int line, int col, String where, String message) {
        System.err.println("[Line " + line + ", Col " + col + "] Error" + where + " : " + message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, token.col, " at end", message);
        } else {
            report(token.line, token.col, " at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println("[Line " + error.token.line + ", Col " + error.token.col + "] : " + error.getMessage());
        hadRuntimeError = true;
    }
}
