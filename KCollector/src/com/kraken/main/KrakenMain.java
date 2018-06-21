package com.kraken.main;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kraken.app.KrakenScraper;
import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.models.TradeTimeSeries;
import com.kraken.util.KeyLoader;
import com.kraken.util.KrakenUtil;

public class KrakenMain {
    private static final Logger LOGGER = Logger.getLogger(KrakenMain.class.getName());

    public static void main(String[] args) {
	//KrakenUtil.saveSchemaSql();
	if (args.length < 1) {
	    LOGGER.log(Level.SEVERE, "Please provide the path to your kraken_keys.txt via argument");
	    System.exit(0);
	}
	HashMap<String, String> apiKeys = KeyLoader.loadApiKeys(args[0]);
	KrakenScraper collector = new KrakenScraper(apiKeys.get("api_key"), apiKeys.get("api_secret"));
	
	LOGGER.log(Level.INFO, "Succesfully initialized Kraken API");
	LOGGER.log(Level.INFO, "Checking for kraken.crypto_timeseries database at localhost:3306...");

	if (KrakenDTO.checkKrakenSchema()) {
	    LOGGER.log(Level.INFO, "Successfully located crypto_timeseries DB.");
	} else {
	    LOGGER.log(Level.WARNING, "Unable to locate crypto_timeseries DB... Auto generating schema and table...");
	    KrakenDTO.createKrakenSchema();
	}
	collector.fullPriceFetch();
	Scanner s = new Scanner(System.in);
	while (true) {
	    printMenu();
	    try {
		int choice = Integer.valueOf(s.nextLine());
		switch (choice) {
		case 1:
		    collector.fullPriceFetch();
		    
		case 2:
		    System.out.println("Enter one of the following CurrencyPairs to retrieve price data for");
		    KrakenUtil.serialize(CurrencyPair.values());
		    try {
			collector.priceFetch(CurrencyPair.valueOf(s.nextLine()));
		    } catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Invalid currency pair specified (case sensitive)");
		    }
		    
		case 3:
		    System.out.println("Enter one of the following CurrencyPairs to retrieve existing price data for");
		    KrakenUtil.serialize(CurrencyPair.values());
		    try {
			CurrencyPair input = CurrencyPair.valueOf(s.nextLine());
			List<TradeTimeSeries> records = KrakenDTO.getTimeSeriesData(input);
			LOGGER.log(Level.INFO, "Succesfully retrieved price history for ["+input.name()+"]");
			KrakenUtil.serialize(records);
			break;
		    } catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Invalid currency pair specified (case sensitive)");
			break;
		    }
		    
		case 4:
		    LOGGER.log(Level.INFO, "Terminating KCollector application");
		    System.exit(0);
		    

		}
	    } catch (Exception e) {
		LOGGER.log(Level.SEVERE, "Valid integer input required");
	    }
	}

	

    }

    public static void printMenu() {
	System.out.println("|------------------------KCollector Menu------------------------");
	System.out.println("| 1) Execute full price history collection for all currency pairs");
	System.out.println("| 2) Execute full price history collection for a specific CurrencyPair");
	System.out.println("| 3) Query database for existing timeseries data for a CurrencyPair");
	System.out.println("|------------------------KCollector Menu------------------------");
    }

}
