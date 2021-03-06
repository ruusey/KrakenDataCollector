package com.kraken.util.strategy;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Decimal;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import com.kraken.app.KrakenScraper;
import com.kraken.constants.CurrencyPair;
import com.kraken.constants.OHLCTimePeriod;
import com.kraken.util.KeyLoader;
import com.kraken.util.KrakenUtil;
public class WalkForward {
    
    /**
     * Builds a list of split indexes from splitDuration.
     * @param series the time series to get split begin indexes of
     * @param splitDuration the duration between 2 splits
     * @return a list of begin indexes after split
     */
    public static List<Integer> getSplitBeginIndexes(TimeSeries series, Duration splitDuration) {
        ArrayList<Integer> beginIndexes = new ArrayList<Integer>();

        int beginIndex = series.getBeginIndex();
        int endIndex = series.getEndIndex();
        
        // Adding the first begin index
        beginIndexes.add(beginIndex);

        // Building the first interval before next split
        ZonedDateTime beginInterval = series.getFirstBar().getEndTime();
        ZonedDateTime endInterval = beginInterval.plus(splitDuration);

        for (int i = beginIndex; i <= endIndex; i++) {
            // For each tick...
            ZonedDateTime tickTime = series.getBar(i).getEndTime();
            if (tickTime.isBefore(beginInterval) || !tickTime.isBefore(endInterval)) {
                // Tick out of the interval
                if (!endInterval.isAfter(tickTime)) {
                    // Tick after the interval
                    // --> Adding a new begin index
                    beginIndexes.add(i);
                }

                // Building the new interval before next split
                beginInterval = endInterval.isBefore(tickTime) ? tickTime : endInterval;
                endInterval = beginInterval.plus(splitDuration);
            }
        }
        return beginIndexes;
    }
    
    /**
     * Returns a new time series which is a view of a subset of the current series.
     * <p>
     * The new series has begin and end indexes which correspond to the bounds of the sub-set into the full series.<br>
     * The tick of the series are shared between the original time series and the returned one (i.e. no copy).
     * @param series the time series to get a sub-series of
     * @param beginIndex the begin index (inclusive) of the time series
     * @param duration the duration of the time series
     * @return a constrained {@link TimeSeries time series} which is a sub-set of the current series
     */
    public static TimeSeries subseries(TimeSeries series, int beginIndex, Duration duration) {

        // Calculating the sub-series interval
        ZonedDateTime beginInterval = series.getBar(beginIndex).getEndTime();
        ZonedDateTime endInterval = beginInterval.plus(duration);

        // Checking ticks belonging to the sub-series (starting at the provided index)
        int subseriesNbTicks = 0;
        int endIndex = series.getEndIndex();
        for (int i = beginIndex; i <= endIndex; i++) {
            // For each tick...
            ZonedDateTime tickTime = series.getBar(i).getEndTime();
            if (tickTime.isBefore(beginInterval) || !tickTime.isBefore(endInterval)) {
                // Tick out of the interval
                break;
            }
            // Tick in the interval
            // --> Incrementing the number of ticks in the subseries
            subseriesNbTicks++;
        }

        return new BaseTimeSeries(series, beginIndex, beginIndex + subseriesNbTicks - 1);
    }

    /**
     * Splits the time series into sub-series lasting sliceDuration.<br>
     * The current time series is splitted every splitDuration.<br>
     * The last sub-series may last less than sliceDuration.
     * @param series the time series to split
     * @param splitDuration the duration between 2 splits
     * @param sliceDuration the duration of each sub-series
     * @return a list of sub-series
     */
    public static List<TimeSeries> splitSeries(TimeSeries series, Duration splitDuration, Duration sliceDuration) {
        ArrayList<TimeSeries> subseries = new ArrayList<TimeSeries>();
        if (splitDuration != null && !splitDuration.isZero()
                && sliceDuration != null && !sliceDuration.isZero()) {

            List<Integer> beginIndexes = getSplitBeginIndexes(series, splitDuration);
            for (Integer subseriesBegin : beginIndexes) {
                subseries.add(subseries(series, subseriesBegin, sliceDuration));
            }
        }
        return subseries;
    }

    /**
     * @param series the time series
     * @return a map (key: strategy, value: name) of trading strategies
     */
    public static Map<Strategy, String> buildStrategiesMap(TimeSeries series) {
        HashMap<Strategy, String> strategies = new HashMap<Strategy, String>();
        strategies.put(CCICorrectionStrategy.buildStrategy(series), "CCI Correction");
        strategies.put(GlobalExtremaStrategy.buildStrategy(series), "Global Extrema");
        strategies.put(MovingMomentumStrategy.buildStrategy(series), "Moving Momentum");
        strategies.put(R2I2Strategy.buildStrategy(series), "RSI-2");
        return strategies;
    }

    public static void main(String[] args) {
	HashMap<String, String> apiKeys = KeyLoader.loadApiKeys("C:/temp/kraken_keys.txt");
	KrakenScraper collector = new KrakenScraper(apiKeys.get("api_key"), apiKeys.get("api_secret"));

	CurrencyPair toFetch = CurrencyPair.XLTCZUSD;
	TimeSeries series = KrakenUtil.toBTS(collector.priceFetch(toFetch, OHLCTimePeriod.FIFTEEN_MINUTES), toFetch);
        List<TimeSeries> subseries = splitSeries(series, Duration.ofMinutes(60), Duration.ofHours(24));

        // Building the map of strategies
        Map<Strategy, String> strategies = buildStrategiesMap(series);

        // The analysis criterion
        AnalysisCriterion profitCriterion = new TotalProfitCriterion();

        for (TimeSeries slice : subseries) {
            // For each sub-series...
            System.out.println("Sub-series: " + slice.getSeriesPeriodDescription());
            TimeSeriesManager sliceManager = new TimeSeriesManager(slice);
            for (Map.Entry<Strategy, String> entry : strategies.entrySet()) {
                Strategy strategy = entry.getKey();
                String name = entry.getValue();
                // For each strategy...
                TradingRecord tradingRecord = sliceManager.run(strategy);
                KrakenUtil.printStratRecord(tradingRecord, series, toFetch);
                double profit = profitCriterion.calculate(slice, tradingRecord);
                //System.out.println("\tProfit for " + name + ": " + profit);
            }
            Strategy bestStrategy = profitCriterion.chooseBest(sliceManager, new ArrayList<Strategy>(strategies.keySet()));
            System.out.println("\t\t--> Best strategy: " + strategies.get(bestStrategy) + "\n");
        }
    }
    
}
