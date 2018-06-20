package com.kraken;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class Analysis {

    private static final Logger LOGGER = Logger.getLogger(Analysis.class.getName());

    public static void main(String[] args) {
	for(CurrencyPair pair: CurrencyPair.values()) {
	    LOGGER.log(Level.INFO, "Performing analysis on [" + pair.name() + "]");
	    List<TradeTimeSeries> data = IO.getTimeSeriesData(pair);
	    double[] avgs = new double[data.size()];
	    int idx =0;
	    for(TradeTimeSeries tts : data) {
		double vwap = tts.getVwap();
		avgs[idx++]=vwap;
	    }
	    StandardDeviation sd = new StandardDeviation(false);
	    double sdev = sd.evaluate(avgs);
	    LOGGER.log(Level.INFO, "Standard Deviation for [" + pair.name() + "] is "+sdev);
	}
    }

}
