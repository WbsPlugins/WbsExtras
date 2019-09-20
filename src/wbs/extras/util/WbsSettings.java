package wbs.extras.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class WbsSettings {
	
	protected WbsPlugin plugin;
	protected Logger logger;
	protected WbsSettings(WbsPlugin plugin) {
		this.plugin = plugin;
		logger = plugin.getLogger();
	}

	public ArrayList<String> errors = new ArrayList<>();
	protected void logError(String error, String directory) {
		errors.add("&c" + error + " &7(" + directory + ")");
		logger.warning(error + "(" + directory + ")");
	}

	// Taken from spigot code; needed to add to how it swallows config exceptions
	protected YamlConfiguration loadConfigSafely(File file) {
		Validate.notNull(file, "File cannot be null");

        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file , ex);
            
			errors.add("&cYAML parsing error in " + file.getName() + ". Check console for logs.");
        }

        return config;
	}
	
	protected File genConfig(String path) {
		File configFile = new File(plugin.getDataFolder(), path);
        if (!configFile.exists()) { 
        	configFile.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }
        
        return configFile;
	}
}
