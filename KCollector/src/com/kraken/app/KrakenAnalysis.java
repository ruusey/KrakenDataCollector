package com.kraken.app;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.models.TradeTimeSeries;

public class KrakenAnalysis {

    private static final Logger LOGGER = Logger.getLogger(KrakenAnalysis.class.getName());

    public KrakenAnalysis() {
	fullTimeSeriesAnalysis();
    }

    public static void main(String[] args) {
	for (CurrencyPair pair : CurrencyPair.values()) {
	    LOGGER.log(Level.INFO, "Performing analysis on [" + pair.name() + "]");
	    List<TradeTimeSeries> data = KrakenDTO.getTimeSeriesData(pair);
	    double[] avgs = new double[data.size()];
	    int idx = 0;
	    for (TradeTimeSeries tts : data) {
		double vwap = tts.getVwap();
		avgs[idx++] = vwap;
	    }
	    StandardDeviation sd = new StandardDeviation(false);
	    double sdev = sd.evaluate(avgs);
	    LOGGER.log(Level.INFO, "Standard Deviation for [" + pair.name() + "] is " + sdev);
	}
    }

    public void fullTimeSeriesAnalysis() {
	for (CurrencyPair pair : CurrencyPair.values()) {
	    LOGGER.log(Level.INFO, "Performing analysis on [" + pair.name() + "]");
	    List<TradeTimeSeries> data = KrakenDTO.getTimeSeriesData(pair);
	    double[] avgs = new double[data.size()];
	    int idx = 0;
	    for (TradeTimeSeries tts : data) {
		double vwap = tts.getVwap();
		avgs[idx++] = vwap;
	    }
	    StandardDeviation sd = new StandardDeviation(false);
	    double sdev = sd.evaluate(avgs);
	    LOGGER.log(Level.INFO, "Standard Deviation for [" + pair.name() + "] is " + sdev);
	}
    }

    public void timeSeriesAnalysis(CurrencyPair pair) {

	LOGGER.log(Level.INFO, "Performing analysis on [" + pair.name() + "]");
	List<TradeTimeSeries> data = KrakenDTO.getTimeSeriesData(pair);
	double[] avgs = new double[data.size()];
	int idx = 0;
	for (TradeTimeSeries tts : data) {
	    double vwap = tts.getVwap();
	    avgs[idx++] = vwap;
	}
	StandardDeviation sd = new StandardDeviation(false);
	double sdev = sd.evaluate(avgs);
	LOGGER.log(Level.INFO, "Standard Deviation for [" + pair.name() + "] is " + sdev);
    }

}
