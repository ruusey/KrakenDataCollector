package com.kraken.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.Order;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kraken.constants.BaseCurrency;
import com.kraken.constants.CryptoCurrency;
import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.models.KrakenTimeSeries;

import edu.self.kraken.api.KrakenApi.Method;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.Timestamp;

public class KrakenUtil {
    private static final Logger LOGGER = Logger.getLogger(KrakenUtil.class.getName());
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Gson gson = new GsonBuilder().create();

    public static Date fromEpoch(long time, boolean isUnix) {
	time = (isUnix) ? time * 1000L : time;
	String formattedDate = null;
	if (isUnix) {
	    formattedDate = sdf1.format(new Date(time));
	} else {
	    formattedDate = sdf1.format(new Date(time));
	}

	return java.sql.Date.valueOf(formattedDate);

    }

    public static Timestamp fromEpoch(long time) {
	return new Timestamp(time * 1000);
    }

    public static String stringFromEpoch(long time, boolean isUnix) {
	time = (isUnix) ? time * 1000L : time;
	if (isUnix) {
	    return sdf2.format(new Date(time));
	} else {
	    return sdf1.format(new Date(time));
	}

    }

    public static long timeSince(long start) {
	return System.currentTimeMillis() - start;
    }
    public static String getElapsedTime(long start) {
	return "("+timeSince(start)+"ms)";
    }

    public static int apiCallCost(Method method) {
	if(method.equals(Method.OHLC) 
		|| method.equals(Method.TRADES) 
		|| method.equals(Method.TRADE_BALANCE) 
		|| method.equals(Method.TRADES_HISTORY) 
		|| method.equals(Method.QUERY_TRADES) 
		|| method.equals(Method.LEDGERS) 
		|| method.equals(Method.QUERY_LEDGERS)) {
	    return 2;
	}else if(method.equals(Method.TIME) || method.equals(Method.ASSETS) || method.equals(Method.ASSET_PAIRS) || method.equals(Method.TICKER) || method.equals(Method.OHLC) || method.equals(Method.DEPTH) || method.equals(Method.SPREAD) || method.equals(Method.BALANCE) || method.equals(Method.OPEN_POSITIONS) || method.equals(Method.DEPOSIT_METHODS) || method.equals(Method.DEPOSIT_ADDRESSES) || method.equals(Method.DEPOSIT_STATUS) || method.equals(Method.WITHDRAW_INFO) || method.equals(Method.WITHDRAW) || method.equals(Method.WITHDRAW_STATUS) || method.equals(Method.WITHDRAW_CANCEL)) {
	    return 1;
	}else {
	    return 0;
	}
    }

    public static void saveSchemaSql() {
	String schema = KrakenDTO.getSchemaCreateStatement();
	String table = KrakenDTO.getTableCreateStatement();
	String string = schema + "\n" + table;
	File file = new File("resources/crypto_timeseries");
	try {
	    FileUtils.writeStringToFile(file, string, Charset.defaultCharset());
	    LOGGER.log(Level.INFO, "Succesfully saved DB create statements.");
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "Failed to save DB create statements. Cause: " + e.getMessage());
	}
    }

    public static void serialize(Object o) {
	LOGGER.log(Level.INFO, gson.toJson(o));
    }

    public static void serializePretty(Object o) {
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	String json = gson.toJson(o);
	System.out.println(json);
    }

    public static double round(double value, int precision) {
	int scale = (int) Math.pow(10, precision);
	return (double) Math.round(value * scale) / scale;
    }

    public static Bar toTick(KrakenTimeSeries tts) {
	DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
	ZonedDateTime date = LocalDateTime.parse(tts.getTimestamp().toString(), DATE_FORMAT)
		.atZone(ZoneId.systemDefault());
	return new BaseBar(date, tts.getOpen(), tts.getHigh(), tts.getLow(), tts.getClose(), tts.getVolume());
    }

    public static BaseTimeSeries toBTS(List<KrakenTimeSeries> data, CurrencyPair pair) {
	List<Bar> ticks = new ArrayList<Bar>();
	for (KrakenTimeSeries tts : data) {
	    ticks.add(toTick(tts));
	}
	return new BaseTimeSeries(pair.name(), ticks);
    }

    public static void buildChart(CurrencyPair pair) {
	TimeSeries series = toBTS(KrakenDTO.getTimeSeriesData(pair), pair);
	ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
	EMAIndicator avg14 = new EMAIndicator(closePrice, 14);
	StandardDeviationIndicator sd14 = new StandardDeviationIndicator(closePrice, 14);

	// Bollinger bands
	BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(avg14);
	BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd14);
	BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, sd14);

	TimeSeriesCollection dataset = new TimeSeriesCollection();
	dataset.addSeries(buildChartTimeSeries(series, closePrice, pair.name()));
	dataset.addSeries(buildChartTimeSeries(series, lowBBand, "Low Bollinger Band"));
	dataset.addSeries(buildChartTimeSeries(series, upBBand, "High Bollinger Band"));

	JFreeChart chart = ChartFactory.createTimeSeriesChart(pair.name(), // title
		"Date", // x-axis label
		"Price Per Unit", // y-axis label
		dataset, // data
		true, // create legend?
		true, // generate tooltips?
		false // generate URLs?
	);
	XYPlot plot = (XYPlot) chart.getPlot();
	DateAxis axis = (DateAxis) plot.getDomainAxis();
	axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

	/*
	 * Displaying the chart
	 */
	displayChart(chart, pair.name());
    }

    public static org.jfree.data.time.TimeSeries buildChartTimeSeries(TimeSeries barseries,
	    Indicator<Decimal> indicator, String name) {
	org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
	for (int i = 0; i < barseries.getBarCount(); i++) {
	    Bar bar = barseries.getBar(i);
	    chartTimeSeries.addOrUpdate(new Day(Date.from(bar.getEndTime().toInstant())),
		    indicator.getValue(i).doubleValue());
	}
	return chartTimeSeries;
    }

    public static void displayChart(JFreeChart chart, String appTitle) {
	// Chart panel
	ChartPanel panel = new ChartPanel(chart);
	panel.setFillZoomRectangle(true);
	panel.setMouseWheelEnabled(true);
	panel.setPreferredSize(new java.awt.Dimension(600, 350));
	// Application frame
	ApplicationFrame frame = new ApplicationFrame("Sample Analysis of [" + appTitle + "]");
	frame.setContentPane(panel);
	frame.pack();
	RefineryUtilities.centerFrameOnScreen(frame);
	frame.setVisible(true);
    }

    public static CurrencyPair[] searchCurrencyPairs(String query) {
	if (query.length() == 0)
	    return CurrencyPair.values();
	int count = 0;
	List<CurrencyPair> results = new ArrayList<CurrencyPair>();
	for (CurrencyPair pair : CurrencyPair.values()) {
	    if (pair.name().toLowerCase().startsWith(query) || pair.name().startsWith(query.toUpperCase())) {
		count++;
		results.add(pair);
	    }
	}
	CurrencyPair[] ret = new CurrencyPair[count];
	results.toArray(ret);
	return ret;
    }

    public static boolean validCurrencyPairSearch(String query) {
	if (searchCurrencyPairs(query).length == 1) {
	    return true;
	} else {
	    return false;
	}
    }

    public static void printStratRecord(TradingRecord record, TimeSeries original, CurrencyPair pair) {
	System.out.println("|----Strategy Indicator Data----");

	BaseCurrency base = CurrencyMap.getPairBase(pair);
	CryptoCurrency crypto = CurrencyMap.getPairCrypto(pair);
	if (original == null)
	    return;
	if (record.getTrades().size() == 0) {
	    Order order = record.getLastEntry();
	    if (order == null) {
		return;
	    }
	    Bar bar = original.getBar(order.getIndex());

	    String entryDate = original.getBar(order.getIndex()).getSimpleDateName();
	    System.out.println("|\t|Recommended Entry: " + order.getType() + "{" + crypto + "}" + "@" + order.getPrice()
		    + "{" + base + "} [" + entryDate + "]");
	    return;
	}

	for (Trade trade : record.getTrades()) {
	    Order entry = trade.getEntry();
	    Order exit = trade.getExit();
	    String entryDate = original.getBar(entry.getIndex()).getSimpleDateName();
	    String exitDate = original.getBar(exit.getIndex()).getSimpleDateName();
	    System.out.println("|\t|----Trade Entry----");
	    System.out.println("|\t|Recommended Entry: " + entry.getType() + "{" + crypto + "}" + "@" + entry.getPrice()
		    + "{" + base + "} [" + entryDate + "]");
	    System.out.println("|\t|Recommended Exit: " + exit.getType() + "{" + crypto + "}" + "@" + exit.getPrice()
		    + "{" + base + "} [" + exitDate + "]");
	    // System.out.println("|\t|----Trade Entry----");
	}
	Trade current = record.getCurrentTrade();
	if (current != null) {
	    Order entry = current.getEntry();
	    if (entry != null) {
		String entryDate = original.getBar(entry.getIndex()).getSimpleDateName();
		System.out.println("|\t|Recommended Entry: " + entry.getType() + "{" + crypto + "}" + "@"
			+ entry.getPrice() + "{" + base + "} [" + entryDate + "]");
	    }

	}
	System.out.println("|\t|Total Expected Profit: " + new TotalProfitCriterion().calculate(original, record) + "{"
		+ crypto + "}");
	System.out.println("|----Strategy Indicator Data----");
    }
}
