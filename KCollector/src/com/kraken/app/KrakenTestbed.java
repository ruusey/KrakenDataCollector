package com.kraken.app;

import java.util.HashMap;

import org.ta4j.core.TimeSeries;

import com.kraken.constants.CurrencyPair;
import com.kraken.constants.OHLCTimePeriod;
import com.kraken.util.KeyLoader;
import com.kraken.util.KrakenUtil;
import com.kraken.util.strategy.GlobalExtremaStrategy;
import com.kraken.util.strategy.R2I2Strategy;

public class KrakenTestbed {

    public static void main(String[] args) {

	HashMap<String, String> apiKeys = KeyLoader.loadApiKeys("C:/temp/kraken_keys.txt");
	KrakenScraper collector = new KrakenScraper(apiKeys.get("api_key"), apiKeys.get("api_secret"));

	CurrencyPair toFetch = CurrencyPair.DASHUSD;
	TimeSeries series = KrakenUtil.toBTS(collector.priceFetch(toFetch, OHLCTimePeriod.FIFTEEN_MINUTES), toFetch);
	R2I2Strategy.executeStrategy(series, toFetch);
    }

}
