package com.kraken.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kraken.app.KrakenScraper;
import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.models.KrakenTimeSeries;
import com.kraken.util.KeyLoader;
import com.kraken.util.KrakenUtil;

public class KrakenMain {
    private static final Logger LOGGER = Logger.getLogger(KrakenMain.class.getName());

    public static void main(String[] args) {
	// KrakenUtil.buildChart(CurrencyPair.BCHUSD);
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
	
	Scanner s = new Scanner(System.in);
	while (true) {
	    printMenu();
	    try {
		int choice = Integer.valueOf(s.nextLine());
		switch (choice) {
		case 1:
		    collector.fullPriceFetch();
		    break;
		case 2:
		    System.out.println("Enter one of the following CurrencyPairs to retrieve price data for");
		    KrakenUtil.serialize(CurrencyPair.values());
		    try {
			collector.priceFetch(CurrencyPair.valueOf(s.nextLine()));
		    } catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Invalid currency pair specified (case sensitive)");
		    }
		    break;
		case 3:
		    System.out.println("Enter one of the following CurrencyPairs to retrieve existing price data for");
		    KrakenUtil.serialize(CurrencyPair.values());
		    try {
			CurrencyPair input = CurrencyPair.valueOf(s.nextLine());
			List<KrakenTimeSeries> records = KrakenDTO.getTimeSeriesData(input);
			LOGGER.log(Level.INFO, "Succesfully retrieved price history for [" + input.name() + "]");
			KrakenUtil.serialize(records);
			break;
		    } catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Invalid currency pair specified (case sensitive)");
			break;
		    }
		case 4:
		    System.out.println("Enter a currency pair to plot\ntip: enter incomplete currency pair names\nto display matching results.\n(case-insensitive) ");

		    String line = "";

		    while (KrakenUtil.validCurrencyPairSearch(line) == false) {
			line = s.nextLine();
			if (!KrakenUtil.validCurrencyPairSearch(line))
			    KrakenUtil.serialize(KrakenUtil.searchCurrencyPairs(line));

		    }
		    KrakenUtil.buildChart(CurrencyPair.valueOf(line.toUpperCase()));
		    break;
		case 5:
		    LOGGER.log(Level.INFO, "Terminating KCollector application");
		    System.exit(0);

		}
	    } catch (Exception e) {
		e.printStackTrace();
		LOGGER.log(Level.SEVERE, "Valid integer input required");
	    }
	}

    }

    public static void printMenu() {
	System.out.println("|------------------------KCollector Menu------------------------");
	System.out.println("| 1) Execute full price history collection for all currency pairs.");
	System.out.println("| 2) Execute full price history collection for a specific CurrencyPair.");
	System.out.println("| 3) Query database for existing timeseries data for a CurrencyPair.");
	System.out.println("| 4) Plot tick data and bollinger bands for a CurrencyPair.");
	System.out.println("| 5) Terminate KCollector application.");
	System.out.println("|------------------------KCollector Menu------------------------");
    }

}
