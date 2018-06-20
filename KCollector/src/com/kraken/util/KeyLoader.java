package com.kraken.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyLoader {
    private static final Logger LOGGER = Logger.getLogger(KeyLoader.class.getName());

    public static HashMap<String, String> loadApiKeys(String path) {
	HashMap<String, String> keys = new HashMap<String, String>();
	Scanner credScan = null;
	try {
	    credScan = new Scanner(new File(path));
	    keys.put("api_key", credScan.nextLine());
	    keys.put("api_secret", credScan.nextLine());

	    LOGGER.log(Level.INFO, "Successfully loaded kraken API keys");
	    credScan.close();
	    return keys;
	} catch (FileNotFoundException e1) {
	    LOGGER.log(Level.SEVERE, "Error locating kraken API keys. Please check for the correct filepath.");
	    System.exit(0);
	}
	return keys;

    }

}
