package com.spacecomplexity.longboilife.game.globals;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.IOException;

/**
 * Class to represent the user settings
 */
public final class Settings {
    /**
     * The current game volume (where 0 is muted and 1 is the max volume).
     */
    public static float volume;
    
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
            else {
                // Settings file already exists
                // Read all the settings into local memory
                readFile(new Scanner(settings));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Writes the default settings to the file.
     * @throws IOException 
     */
    private static void initialiseFile(FileWriter writer) throws IOException {
        // Manually writes the contents of the file
        // TODO: Change this to iterate through a dictionary or something
        writer.append("resolution:1920x1080\n");
        writer.append("volume:0.5\n");
        
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
        
        // Retrieves the name of the setting
        String identifier = setting[0].strip();
        // Retrieves the value of the setting
        String value = setting[1].strip();
        
        // Sets the local value of the corresponding setting to the value read from file
        switch (identifier) {
            case "resolution":
                // TODO: Let this actually set the resolution somehow
                break;
            case "volume":
                volume = Float.parseFloat(value);
                break;
            default:
                // Unknown setting identifier
                break;
        }
    }
}
