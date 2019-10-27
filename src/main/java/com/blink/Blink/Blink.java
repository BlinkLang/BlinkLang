package com.blink.Blink;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.sun.security.auth.module.*;

import static com.blink.Blink.CLICustomization.*;

public class Blink {

    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: blink [script]");
            System.exit(1074);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        clearScreen();

        createConfigFile();

        System.out.println("Blink [Version 1.3.00.0]");
        System.out.println();
        splashScreens();
        System.out.println();

        showJVMRamUsage();

        System.out.println("(c) 2019 BlinkLang Organization. All rights reserved.");
        System.out.println();

        run(new String(bytes, Charset.defaultCharset()));

        System.out.println();

        if (hadError) System.exit(1070);
        if (hadRuntimeError) System.exit(1075);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        clearScreen();

        createConfigFile();

        System.out.println("Blink [Version 1.3.00.0]");
        System.out.println();
        splashScreens();
        System.out.println();

        showJVMRamUsage();

        System.out.println("(c) 2019 BlinkLang Organization. All rights reserved.");
        System.out.println();

        while (true) {
            consoleNewLineStyle();
            run(reader.readLine() + "\n");
            hadError = false;
        }
    }

    public static void showJVMRamUsage() {
        long memoryUsage = Runtime.getRuntime().totalMemory() / 1000000;
        long totalMemory = (((com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1000000000);

        if (ramFlag().equals(true)) {
            System.out.println("Currently using " + memoryUsage + "MB out of " + totalMemory + "GB total");
        } else if (ramFlag().equals(false)) {
        }
    }

    public static void consoleNewLineStyle() {
        NTSystem Info = new NTSystem();
        Date time = new Date();

        SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String DateAndTime = dateAndTimeFormat.format(time);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String Date = dateFormat.format(time);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String Time = timeFormat.format(time);

        if (dateFlag().equals("dateAndTime")) {
            System.out.print("[" + DateAndTime +"] " + Info.getName() + "@" + Info.getDomain() + " Blink > ");

        } else if (dateFlag().equals("date")) {
            System.out.print("[" + Date +"] " + Info.getName() + "@" + Info.getDomain() + " Blink > ");

        } else if (dateFlag().equals("time")) {
            System.out.print("[" + Time +"] " + Info.getName() + "@" + Info.getDomain() + " Blink > ");

        } else if (dateFlag().equals("userAndPcName")) {
            System.out.print(Info.getName() + "@" + Info.getDomain() + " Blink > ");

        } else if (dateFlag().equals("classic")) {
            System.out.print("Blink > ");

        } else {
            System.out.println("'config.json' contains an invalid key in the 'newLineStyle' object. Allowed values: \"dateAndTime\" | \"date\" | \"time\" | \"userAndPcName\" | \"classic\"");
            System.exit(1076);
        }
    }

    public static void splashScreens() {
        String[] splashScreenMessages = {"Try harder.", "0x3A282I3A", "I cannot wait to be able to use '-ffascist -Wanal' in GCC", "01000001 01110111 01100101 01110011 01101111 01101101 01100101", "How about using INTERCAL for this instead?", "Not gonna lie, Minecraft command blocks are lit", "Can we get a bruh moment to whoever is using this right now"};
        Random random = new Random();
        int randomNumber = random.nextInt(splashScreenMessages.length);

        if (splashFlag().equals(true)) {
            System.out.println(splashScreenMessages[randomNumber]);
        } else if (splashFlag().equals(false)) {
        } else {
            System.out.println("'config.json' contains an invalid key in the 'showSplashScreens' object. Allowed values: true | false");
            System.exit(1076);
        }
    }

    public static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[Line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, "at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.out.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public static void clearScreen(){
        try {
            if (System.getProperty("os.name").contains("Windows"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else
                Runtime.getRuntime().exec("clear");
        } catch (IOException | InterruptedException ex) {}
    }
}
