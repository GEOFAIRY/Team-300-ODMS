package seng302.Config;

import jdk.nashorn.internal.runtime.ParserException;
import org.slf4j.LoggerFactory;
import seng302.Server;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Creates or loads a config file into a Properties object
 */
public class ConfigParser {

    /**
     * The filename of the configuration file
     */
    private final String FILENAME = "server_config.txt";

    /**
     * The required parameters for the config file.
     */
    private final String[] PARAMETERS = {"database_address", "database_name", "username", "password", "vs_token"};

    /**
     * A map containg the properties
     */
    private Properties properties = new Properties();

    public ConfigParser() {
        try {
            File file = new File(FILENAME);
            // If the config file does not exist, create one with the default config.
            if (!file.exists()) {
                Files.copy(getClass().getResourceAsStream("/" + FILENAME), file.toPath());
            }
            properties.load(new FileInputStream(file));
            // Recreate the config file if the current one is corrupt
            for (String parameter : PARAMETERS) {
                if (!properties.containsKey(parameter)) {
                    LoggerFactory.getLogger(Server.class).warn("Invalid config file, backing up and reverting to default");
                    File backup = new File(FILENAME + ".bak");
                    backup.delete();
                    Files.copy(file.toPath(), backup.toPath());
                    file.delete();
                    Files.copy(getClass().getResourceAsStream("/" + FILENAME), file.toPath());
                    break;
                }
            }
            properties.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            Server.getInstance().log.warn("Could not load or create config file");
            Server.getInstance().log.error(e.getMessage());
        } catch (IOException e) {
            Server.getInstance().log.warn("An unexpected error occurred while reading config files.");
            Server.getInstance().log.error(e.getMessage());
        }
    }

    /**
     * Get an unmodifiable map of the properties provided
     * @return An unmodifiable Map of properties
     */
    public Map<Object, Object> getConfig(){
        return Collections.unmodifiableMap(properties);
    }

}
