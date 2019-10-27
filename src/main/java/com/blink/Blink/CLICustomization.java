package com.blink.Blink;

import org.json.simple.*;
import org.json.simple.parser.*;

import java.io.*;
import java.nio.file.*;

public class CLICustomization {
    public static Object cwd() {
        Path currentWorkingDir = Paths.get("");
        String cwd = currentWorkingDir.toAbsolutePath().toString();
        return cwd;
    }

    public static void createConfigFile() {
        File configFile = new File(cwd() + "/config.json");

        if (configFile.isFile()) {
        } else {
            JSONObject object = new JSONObject();
            object.put("showRamUsage", false);
            object.put("newLineStyle", "classic");
            object.put("showSplashScreens", false);

            try (FileWriter file = new FileWriter(cwd() + "/config.json")) {
                file.write(object.toJSONString());
            } catch (IOException e) {
                System.out.println("Yikes, something went wrong.");
            }
        }
    }

    public static Boolean ramFlag() {
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(cwd() + "/config.json")) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            return (Boolean) jsonObject.get("showRamUsage");
        } catch (IOException | ParseException e) {
            System.out.println("Yikes, something went wrong.");
        }

        return null;
    }

    public static String dateFlag() {
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(cwd() + "/config.json")) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            return (String) jsonObject.get("newLineStyle");
        } catch (IOException | ParseException e) {
            System.out.println("Yikes, something went wrong.");
        }

        return null;
    }

    public static Boolean splashFlag() {
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(cwd() + "/config.json")) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            return (Boolean) jsonObject.get("showSplashScreens");
        } catch (IOException | ParseException e) {
            System.out.println("Yikes, something went wrong.");
        }

        return null;
    }
}
