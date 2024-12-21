package com.spacecomplexity.longboilife.game.globals;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class to represent the user settings
 */
public final class Settings {
    /**
     * The current game volume (where 0 is muted and 1 is the max volume).
     */
    public static float volume;
    
    
    private static HashMap<SETTING, String> DEFAULTS = new HashMap<>() {{
        put(SETTING.FULLSCREEN, "false");
        put(SETTING.RESOLUTION, "1920x1080");
        put(SETTING.VOLUME, "0.5");
    }};
    
    private static enum SETTING {
        FULLSCREEN("fullscreen"),
        RESOLUTION("resolution"),
        VOLUME("volume");

        String id;
        SETTING(String identifier) {
            this.id = identifier;
        }
        
        /**
         * Gets the setting corresponding to the identifier.
         * @param identifier the string representation of a setting.
         * @return the corresponding setting if it can be found.
         * <li><code>null</code> if it cannot be found.
         */
        private static SETTING getSetting(String identifier) {
            identifier = identifier.toLowerCase();
            
            for (SETTING setting : SETTING.values()) {
                // Iterate through all settings
                if (setting.id.equals(identifier)){
                    // Return the setting that matches the ID
                    return setting;
                }
            }
            // No matching setting found
            return null;
        }
    }
    
    /**
     * Saves the current settings to file.
     */
    public static void save() {
        try {
            FileWriter writer = new FileWriter("Settings.txt");
            
            // Clear the file
            writer.write("");
            
            // Write all settings
            writer.append(SETTING.FULLSCREEN.id + ":" +
                Window.isFullscreen + "\n");
            writer.append(SETTING.RESOLUTION.id + ":" + 
                Window.width + "x" + Window.height + "\n");
            writer.append(SETTING.VOLUME.id + ":" +
                volume + "\n");
            
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Attempts to read the user settings from file, 
     * creates a new file with default values if none exists.
     */
    public static void initialise() {
        File settings = new File("Settings.txt");
        try {
            if (settings.createNewFile()) {
                // Settings file has been successfully made
                // Initialise settings file with default values
                initialiseFile(new FileWriter(settings));
            }
            // Read all the settings into memory
            readFile(new Scanner(settings));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Writes the default settings to the file.
     * @throws IOException 
     */
    private static void initialiseFile(FileWriter writer) throws IOException {
        // Iterates through every setting and appends its identifier and default value to the file
        for (SETTING setting : DEFAULTS.keySet()) {
            String defaultValue = DEFAULTS.get(setting);
            writer.append(setting.id + ":" + defaultValue + "\n");
        }
        writer.close();
        
        
    }
    
    /**
     * Reads all the settings into memory.
     * <p>TODO: If a parameter cannot be found, it will be added with the default value.
     */
    private static void readFile(Scanner reader) {
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            readLine(line);
        }
        reader.close();
    }
    
    private static void readLine(String line) {
        String[] setting = line.split(":");
        if (setting.length != 2) {
            // Incorrectly formatted string within the settings file
            // Ignore the line and continue reading
            return;
        }
        
        // Retrieves the name of the setting, case insensitive
        String identifier = setting[0].strip().toLowerCase();
        // Retrieves the value of the setting
        String value = setting[1].strip();
        
        // Sets the local value of the corresponding setting to the value read from file
        switch (SETTING.getSetting(identifier)) {
            case FULLSCREEN:
                // Set fullscreen
                Window.isFullscreen = value.equals("true");
                break;
            case RESOLUTION:
                // Set window width and height
                String[] resolution = value.split("x");
                Window.width = Integer.parseInt(resolution[0]);
                Window.height = Integer.parseInt(resolution[1]);
                break;
            case VOLUME:
                // Set volume (currently does nothing)
                volume = Float.parseFloat(value);
                break;
            default:
                // Unknown setting identifier
                break;
        }
    }
}
