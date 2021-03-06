package com.kraken.util.strategy;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Decimal;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.util.KrakenUtil;

public class R2I2Strategy {
    private static final String STRAT_NAME = "R2I2Strategy";

    public static void main(String[] args) {
	executeStrategy(CurrencyPair.DASHUSD);
    }

    public static Strategy buildStrategy(TimeSeries series) {
	if (series == null) {
	    throw new IllegalArgumentException("Series cannot be null");
	}

	ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
	SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
	SMAIndicator longSma = new SMAIndicator(closePrice, 200);

	// We use a 2-period RSI indicator to identify buying
	// or selling opportunities within the bigger trend.
	RSIIndicator rsi = new RSIIndicator(closePrice, 2);

	// Entry rule
	// The long-term trend is up when a security is above its 200-period SMA.
	Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
		.and(new CrossedDownIndicatorRule(rsi, Decimal.valueOf(5))) // Signal 1
		.and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2

	// Exit rule
	// The long-term trend is down when a security is below its 200-period SMA.
	Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
		.and(new CrossedUpIndicatorRule(rsi, Decimal.valueOf(95))) // Signal 1
		.and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2

	// TODO: Finalize the strategy

	return new BaseStrategy(exitRule, entryRule);
    }

    public static void executeStrategy(CurrencyPair pair) {
	TimeSeries series = KrakenUtil.toBTS(KrakenDTO.getTimeSeriesData(pair), pair);
	Strategy strategy = buildStrategy(series);

	TimeSeriesManager seriesManager = new TimeSeriesManager(series);
	TradingRecord tradingRecord = seriesManager.run(strategy);

	KrakenUtil.printStratRecord(tradingRecord, series, pair);

    }

    public static void executeStrategy(TimeSeries series, CurrencyPair pair) {
	Strategy strategy = buildStrategy(series);

	TimeSeriesManager seriesManager = new TimeSeriesManager(series);
	TradingRecord tradingRecord = seriesManager.run(strategy);

	KrakenUtil.printStratRecord(tradingRecord, series, pair);
	//KrakenUtil.serializePretty(tradingRecord);

    }

}
