package com.kraken.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kraken.constants.CurrencyPair;
import com.kraken.dto.KrakenDTO;
import com.kraken.models.TradeTimeSeries;

import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Tick;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;

public class KrakenUtil {
    private static final Logger LOGGER = Logger.getLogger(KrakenUtil.class.getName());
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
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

    public static String stringFromEpoch(long time, boolean isUnix) {
	time = (isUnix) ? time * 1000L : time;
	if (isUnix) {
	    return sdf2.format(new Date(time));
	} else {
	    return sdf1.format(new Date(time));
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

    public static double round(double value, int precision) {
	int scale = (int) Math.pow(10, precision);
	return (double) Math.round(value * scale) / scale;
    }
    public static Tick toTick(TradeTimeSeries tts) {
	DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	 ZonedDateTime date = LocalDate.parse(tts.getTimestamp().toString(), DATE_FORMAT).atStartOfDay(ZoneId.systemDefault());
	 return new BaseTick(date,tts.getOpen(),tts.getHigh(),tts.getLow(),tts.getClose(),tts.getVolume());
    }
    public static BaseTimeSeries toBTS(List<TradeTimeSeries> data, CurrencyPair pair) {
	List<Tick> ticks = new ArrayList<Tick>();
	for(TradeTimeSeries tts: data) {
	    ticks.add(toTick(tts));
	}
	return new BaseTimeSeries(pair.name(),ticks);
    }
}
