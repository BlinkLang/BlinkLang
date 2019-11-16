package blink;

import java.io.*;
import java.nio.charset.*;
import java.security.*;
import java.text.*;
import java.util.*;

class StandardLibrary {
    // HexToString for SHA3-256 string generation
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append("0");
            hexString.append(hex);
        }

        return hexString.toString();
    }

    // Stringify method for various operations
    private static String stringify(Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof List) {
            StringBuilder text = new StringBuilder("[");
            List<Object> list = (List<Object>) object;
            for (int i = 0; i < list.size(); ++i) {
                text.append(stringify(list.get(i)));
                if (i != list.size() - 1) {
                    text.append(", ");
                }
            }
            text.append("]");
            return text.toString();
        }

        if (object instanceof Double) {
            String num = object.toString();
            if (num.endsWith(".0")) {
                num = num.substring(0, num.length() - 2);
            }
            return num;
        }
        return object.toString();
    }

    public static final NativeInstance Crypto =
            new NativeInstance("Crypto", new HashMap<>() {{
                put("sha", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        String originalString = (String) args.get(0);
                        MessageDigest digest;
                        try {
                            digest = MessageDigest.getInstance("SHA3-256");
                            byte[] hash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
                            return bytesToHex(hash);
                        } catch (NoSuchAlgorithmException e) {
                            System.err.println("Oops, couldn't hash your string.");
                        }

                        return null;
                    }
                });
            }});

    public static final NativeInstance Time =
            new NativeInstance("Time", new HashMap<>() {{
                put("time", new BlinkCallable() {
                    @Override
                    public int arity() { return 0; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        return formatter.format(date);
                    }
                });

                put("date", new BlinkCallable() {
                    @Override
                    public int arity() { return 0; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        return formatter.format(date);
                    }
                });

                put("dateAndTime", new BlinkCallable() {
                    @Override
                    public int arity() { return 0; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        return formatter.format(date);
                    }
                });
            }});

    public static final NativeInstance File =
            new NativeInstance("File", new HashMap<>() {{
                put("read", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        StringBuilder contents = new StringBuilder();

                        try {
                            BufferedReader br = new BufferedReader(new FileReader(stringify(args.get(0))));
                            String currLine;
                            contents = new StringBuilder();
                            while((currLine = br.readLine()) != null) {
                                contents.append(currLine).append("\n");
                            }
                        } catch (IOException e) {
                            System.err.println("There was an error reading from the file.");
                        }

                        return contents.toString();
                    }
                });

                put("write", new BlinkCallable() {
                    @Override
                    public int arity() { return 2; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(stringify(args.get(0))));
                            bw.write(stringify(args.get(1)));
                            bw.close();
                            return true;
                        } catch (IOException e) {
                            System.err.println("There was an error while writing to the file.");
                            return false;
                        }
                    }
                });

                put("append", new BlinkCallable() {
                    @Override
                    public int arity() { return 2; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(stringify(args.get(0)), true));
                            bw.append(stringify(args.get(1)));
                            bw.close();
                            return true;
                        } catch (IOException e) {
                            System.err.println("There was an error while writing to the file.");
                            return false;
                        }
                    }
                });
            }});

    public static final NativeInstance Math =
            new NativeInstance("Math", new HashMap<>() {{
                put("round", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        Double value = (Double) args.get(0);
                        return java.lang.Math.round(value);
                    }
                });

                put("random", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        double arg = (Double) args.get(0);
                        int value = (int) arg;
                        Random rand = new Random();
                        return rand.nextInt(value);
                    }
                });

                put("leftShift", new BlinkCallable() {
                    @Override
                    public int arity() { return 2; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        return ((Integer)(((Double) args.get(0)).intValue()
                                << ((Double) args.get(1)).intValue())).doubleValue();
                    }
                });

                put("rightShift", new BlinkCallable() {
                    @Override
                    public int arity() { return 2; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        return ((Integer)(((Double) args.get(0)).intValue()
                                >> ((Double) args.get(1)).intValue())).doubleValue();
                    }
                });
            }});

    public static final NativeInstance Utils =
            new NativeInstance("Utils", new HashMap<>() {{
                put("input", new BlinkCallable() {
                    @Override
                    public int arity() { return 0; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                            return br.readLine();
                        } catch (IOException e) {
                            System.err.println("There was an error while reading from the console.");
                            return null;
                        }
                    }
                });

                put("error", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        System.err.println((String) args.get(0));
                        return null;
                    }
                });

                put("exit", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        System.exit((int)(double) args.get(0));
                        return null;
                    }
                });

                put("sizeof", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        Object object = args.get(0);
                        if (object instanceof String) return ((String) object).length();
                        if (object instanceof List) return ((List) object).size();
                        return null;
                    }
                });

                put("typeof", new BlinkCallable() {
                    @Override
                    public int arity() { return 1; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> args) {
                        Object value = args.get(0);

                        if (value instanceof String) {
                            return "String";

                        } else if (value instanceof Double) {
                            return "Double";

                        } else if (value instanceof List) {
                            return "Array";

                        } else {
                            return "Invalid type.";
                        }
                    }
                });

                put("clock", new BlinkCallable() {
                    @Override
                    public int arity() { return 0; }

                    @Override
                    public Object call(Interpreter interpreter, List<Object> arg) {
                        return (double) System.currentTimeMillis() / 1000;
                    }
                });
            }});

    static void importAll(Environment environment) {
        environment.define("Crypto", Crypto);
        environment.define("Time", Time);
        environment.define("File", File);
        environment.define("Math", Math);
        environment.define("Utils", Utils);
    }
}
