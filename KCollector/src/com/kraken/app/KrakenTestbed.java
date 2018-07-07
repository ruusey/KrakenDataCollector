package com.kraken.app;

import java.util.HashMap;

import org.ta4j.core.TimeSeries;

import com.kraken.api.impl.APICallManager;
import com.kraken.constants.CurrencyPair;
import com.kraken.constants.OHLCTimePeriod;
import com.kraken.constants.VerificationTier;
import com.kraken.util.KeyLoader;
import com.kraken.util.KrakenUtil;
import com.kraken.util.strategy.BasicStrategy;
import com.kraken.util.strategy.CCICorrectionStrategy;
import com.kraken.util.strategy.GlobalExtremaStrategy;
import com.kraken.util.strategy.MovingMomentumStrategy;
import com.kraken.util.strategy.R2I2Strategy;
import com.kraken.util.strategy.StrategyAnalyser;

import edu.self.kraken.api.KrakenApi.Method;

public class KrakenTestbed {

    public static void main(String[] args) {
	
	
	HashMap<String, String> apiKeys = KeyLoader.loadApiKeys("C:/temp/kraken_keys.txt");
	KrakenScraper collector = new KrakenScraper(apiKeys.get("api_key"), apiKeys.get("api_secret"));
	//collector.getLastTickIdentifier(CurrencyPair.XLTCZUSD, OHLCTimePeriod.FIFTEEN_MINUTES);
	CurrencyPair toFetch = CurrencyPair.BCHUSD;
	TimeSeries series = KrakenUtil.toBTS(collector.priceFetch(toFetch, OHLCTimePeriod.FIVE_MINUTES), toFetch);
	
	
	BasicStrategy simpleRangeScalper = new BasicStrategy();
        simpleRangeScalper.initStrategy(series);

        // run strategy on time series and analyse results
        StrategyAnalyser analyser = new StrategyAnalyser();
        analyser.printAllResults(simpleRangeScalper);

	
	
	//CCICorrectionStrategy.executeStrategy(series, toFetch);
    }

}
