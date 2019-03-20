package com.kraken.app;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.github.signaflo.timeseries.TimePeriod;
import com.github.signaflo.timeseries.TimeSeries;
import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.models.KrakenTimeSeries;
import com.github.signaflo.*;
import com.github.signaflo.data.DoubleDataSet;
import com.github.signaflo.data.visualization.Plots;
public class KrakenAnalysis {

    private static final Logger LOGGER = Logger.getLogger(KrakenAnalysis.class.getName());

    public KrakenAnalysis() {
	fullTimeSeriesAnalysis();
    }

    public static void main(String[] args) {
	plotTimeSeries(CurrencyPair.XZECZJPY);
	for (CurrencyPair pair : CurrencyPair.values()) {
	    LOGGER.log(Level.INFO, "Performing analysis on [" + pair.name() + "]");
	    List<KrakenTimeSeries> data = KrakenDTO.getTimeSeriesData(pair);
	    double[] avgs = new double[data.size()];
	    int idx = 0;
	    for (KrakenTimeSeries tts : data) {
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
	    List<KrakenTimeSeries> data = KrakenDTO.getTimeSeriesData(pair);
	    double[] avgs = new double[data.size()];
	    int idx = 0;
	    for (KrakenTimeSeries tts : data) {
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
	List<KrakenTimeSeries> data = KrakenDTO.getTimeSeriesData(pair);
	double[] avgs = new double[data.size()];
	int idx = 0;
	for (KrakenTimeSeries tts : data) {
	    double vwap = tts.getVwap();
	    avgs[idx++] = vwap;
	}
	StandardDeviation sd = new StandardDeviation(false);
	double sdev = sd.evaluate(avgs);
	LOGGER.log(Level.INFO, "Standard Deviation for [" + pair.name() + "] is " + sdev);
    }
    public static void plotTimeSeries(CurrencyPair pair) {
	List<KrakenTimeSeries> data = KrakenDTO.getTimeSeriesData(pair);
	double[] avgs = new double[data.size()];
	int idx = 0;
	for (KrakenTimeSeries tts : data) {
	    double vwap = tts.getVwap();
	    avgs[idx++] = vwap;
	}
	
	TimeSeries ts = TimeSeries.from(TimePeriod.oneDay(),avgs);
	Plots.plot(ts,"["+pair.name()+"] Daily VWAP Plot");
    }

}
