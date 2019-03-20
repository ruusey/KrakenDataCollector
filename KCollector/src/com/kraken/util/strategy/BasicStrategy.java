package com.kraken.util.strategy;

import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.Order;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.indicators.helpers.MultiplierIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.StopGainRule;
import org.ta4j.core.trading.rules.StopLossRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.util.KrakenUtil;

public class BasicStrategy implements StrategyBuilder {
    private TimeSeries series;

    private ClosePriceIndicator closePrice;
    private Indicator<Decimal> maxPrice;
    private Indicator<Decimal> minPrice;

    private BollingerBandsUpperIndicator upperBollingerBand;
    private BollingerBandsMiddleIndicator middleBollingerBand;
    private BollingerBandsLowerIndicator lowerBollingeBand;

    // parameters
    private Decimal takeProfitValue;
    private int emaForBollingerBandValue;
    /**
     * Constructor
     */
    public BasicStrategy(){}

    public BasicStrategy(TimeSeries series){
        initStrategy(series);
    }

    
    public void initStrategy(TimeSeries series) {
        this.series = series;
        this.minPrice = new MinPriceIndicator(this.series);
        this.closePrice = new ClosePriceIndicator(this.series);
        this.maxPrice = new MaxPriceIndicator(this.series);
        setParams(16, Decimal.valueOf(0.015));
    }

    
    public Strategy buildStrategy(Order.OrderType type){
        if (type.equals(Order.OrderType.SELL))
            return getShortStrategy();
        return getLongStrategy();
    }

    
    public TradingRecord getTradingRecord(Order.OrderType type) {
	TimeSeriesManager seriesManager = new TimeSeriesManager(series);
	TradingRecord tradingRecord = seriesManager.run(buildStrategy(type));
        return tradingRecord;
    }

    
    public TimeSeries getTimeSeries(){
        return this.series;
    }

    
    public String getName(){
        return "Simple Range Scalper";
    }

    
    public List<String> getParamters(){
        ArrayList<String> parameters = new ArrayList<String>();
        String takeProfit = "Take Profit: "+ this.takeProfitValue;
        String ema = "EMA :"+ this.emaForBollingerBandValue;
        parameters.add(takeProfit);
        parameters.add(ema);
        return  parameters;
    }

    /**
     * call this function to change the parameter of the strategy
     * @param emaForBollingerBandValue exponential moving average the bollinger bands are based on
     * @param takeProfitValue close a trade if this percentage profit is reached
     */
    public void setParams(int emaForBollingerBandValue, Decimal takeProfitValue){
        this.takeProfitValue = takeProfitValue;
        this.emaForBollingerBandValue = emaForBollingerBandValue;

        EMAIndicator ema = new EMAIndicator(this.closePrice, emaForBollingerBandValue);
        StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(this.closePrice, emaForBollingerBandValue);
        this.middleBollingerBand = new BollingerBandsMiddleIndicator(ema);
        this.lowerBollingeBand = new BollingerBandsLowerIndicator(this.middleBollingerBand, standardDeviation);
        this.upperBollingerBand = new BollingerBandsUpperIndicator(this.middleBollingerBand, standardDeviation);
    }

    private Strategy getLongStrategy() {

        Indicator<Decimal> d_upper_middle = new DifferenceIndicator(this.upperBollingerBand, this.middleBollingerBand);
        // exit if half way up to middle reached
        Indicator<Decimal> threshold = new MultiplierIndicator(d_upper_middle, Decimal.valueOf(0.5));

        Rule entrySignal = new CrossedUpIndicatorRule(this.maxPrice, this.upperBollingerBand);
        Rule entrySignal2 = new UnderIndicatorRule(this.minPrice, this.upperBollingerBand);

        Rule exitSignal = new CrossedDownIndicatorRule(this.closePrice, threshold);
        Rule exitSignal2 = new StopGainRule(closePrice, this.takeProfitValue);

        return new BaseStrategy(entrySignal.and(entrySignal2), exitSignal.or(exitSignal2));
    }

    private Strategy getShortStrategy(){

        Indicator<Decimal> d_middle_lower = new DifferenceIndicator(this.middleBollingerBand, this.lowerBollingeBand);
        // exit if half way down to middle reached
        Indicator<Decimal> threshold = new MultiplierIndicator(d_middle_lower, Decimal.valueOf(0.5));

        Rule entrySignal = new CrossedDownIndicatorRule(this.minPrice, this.lowerBollingeBand);
        Rule entrySignal2 = new OverIndicatorRule(this.maxPrice, this.lowerBollingeBand);

        Rule exitSignal = new CrossedUpIndicatorRule(this.closePrice, threshold);
        Rule exitSignal2 = new StopLossRule(closePrice, this.takeProfitValue); // stop loss long = stop gain short?

        return new BaseStrategy(entrySignal.and(entrySignal2), exitSignal.or(exitSignal2));
    }
}
