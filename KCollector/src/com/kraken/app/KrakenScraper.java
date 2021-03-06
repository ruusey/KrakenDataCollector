package com.kraken.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.kraken.constants.BaseCurrency;
import com.kraken.constants.CryptoCurrency;
import com.kraken.constants.CurrencyPair;
import com.kraken.constants.OHLCIndex;
import com.kraken.constants.OHLCTimePeriod;
import com.kraken.dto.KrakenDTO;
import com.kraken.models.KrakenTimeSeries;
import com.kraken.util.KrakenUtil;

import edu.self.kraken.api.KrakenApi;
import edu.self.kraken.api.KrakenApi.Method;

public class KrakenScraper {
    private static final int TIMEOUT_RETRY_COUNT = 3;
    private static final long API_SLEEP_MS = 10000;
    public static Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(KrakenScraper.class.getName());
    public KrakenApi api;
    public KrakenScraper(String apiKey, String apiSecret) {
	api = new KrakenApi();
	api.setKey(apiKey);
	api.setSecret(apiSecret);
	pingServer();
    }
    public boolean pingServer() {
	String response=null;
	try {
	    response = api.queryPublic(Method.TIME);
	    JSONObject obj = new JSONObject(response);
	    JSONObject res = obj.getJSONObject("result");
	    long timestamp = res.getLong("unixtime");
	    LOGGER.log(Level.INFO, "Succesfully contacted the Kraken API. Server time ["+KrakenUtil.stringFromEpoch(timestamp,true)+"]");
		return true;
	    
	}catch(Exception e) {
	    LOGGER.log(Level.SEVERE, "Error pinging Kraken API. Please check your API keys. Msg["+response+"]");
	    return false;
	}
    }
    public void fullPriceFetch() {
	for (CurrencyPair pair : CurrencyPair.values()) {
	    ArrayList<KrakenTimeSeries> timeSeriesData = new ArrayList<KrakenTimeSeries>();
	    LOGGER.log(Level.INFO, "Fetching daily price data for currency pair [" + pair.name() + "]");
	    Map<String, String> input = new HashMap<String, String>();
	    String response = null;
	    input.put("pair", pair.name());
	    input.put("interval", "1440");
	    long startTime = System.currentTimeMillis();
	    int attempts = 0;
	    while (attempts<TIMEOUT_RETRY_COUNT) {
		try {
		    LOGGER.log(Level.INFO, "Sending request to the Kraken API for currency pair [" + pair.name() + "]");
		    response = api.queryPublic(Method.OHLC, input);
		    JSONObject obj = new JSONObject(response);
		    
		    JSONObject res = obj.getJSONObject("result");
		    if(res.has("result"))
		    break;
		} catch (Exception e) {
		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
		    attempts++;
		    continue;

		}
	    }

	    LOGGER.log(Level.INFO, "Waiting 10 seconds to retrieve asset info for [" + pair.name() + "]");
	    try {
		Thread.sleep(10000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    String[] pairData = getAssetInfo(api, pair);
	    BaseCurrency base = null;
	    CryptoCurrency crypto = null;

	    try {
		base = BaseCurrency.valueOf(pairData[0]);
		crypto = CryptoCurrency.valueOf(pairData[1]);
	    } catch (Exception e) {
		base = BaseCurrency.valueOf(pairData[1]);
		crypto = CryptoCurrency.valueOf(pairData[0]);
	    }
	    JSONObject obj = new JSONObject(response);
	    JSONObject res = obj.getJSONObject("result");
	    JSONArray data = res.getJSONArray(pair.name());
	    for (int i = 0; i < data.length(); i++) {
		JSONArray test = data.getJSONArray(i);

		KrakenTimeSeries tts = parseTimeSeries(test);
		timeSeriesData.add(tts);

	    }
	    LOGGER.log(Level.INFO, "Succesfully retrieved all price data for currency pair [" + pair.name() + "]("
		    + timeSeriesData.size() + " records) in " + (System.currentTimeMillis() - startTime));
	    KrakenDTO.insertTimeseries(pair, base, crypto, timeSeriesData);
	    try {
		Thread.sleep(10000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
    public void priceFetch(CurrencyPair pair) {
	ArrayList<KrakenTimeSeries> timeSeriesData = new ArrayList<KrakenTimeSeries>();
	    LOGGER.log(Level.INFO, "Fetching daily price data for currency pair [" + pair.name() + "]");
	    Map<String, String> input = new HashMap<String, String>();
	    String response = null;
	    input.put("pair", pair.name());
	    input.put("interval", "1440");
	    long startTime = System.currentTimeMillis();
	    while (true) {
		try {
		    LOGGER.log(Level.INFO, "Sending request to the Kraken API for currency pair [" + pair.name() + "]");
		    response = api.queryPublic(Method.OHLC, input);
		    JSONObject obj = new JSONObject(response);
		    JSONObject res = obj.getJSONObject("result");
		    break;
		} catch (Exception e) {
		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
		    continue;

		}
	    }

	    LOGGER.log(Level.INFO, "Waiting 10 seconds to retrieve asset info for [" + pair.name() + "]");
	    try {
		Thread.sleep(10000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    String[] pairData = getAssetInfo(api, pair);
	    BaseCurrency base = null;
	    CryptoCurrency crypto = null;

	    try {
		base = BaseCurrency.valueOf(pairData[0]);
		crypto = CryptoCurrency.valueOf(pairData[1]);
	    } catch (Exception e) {
		base = BaseCurrency.valueOf(pairData[1]);
		crypto = CryptoCurrency.valueOf(pairData[0]);
	    }
	    JSONObject obj = new JSONObject(response);
	    JSONObject res = obj.getJSONObject("result");
	    JSONArray data = res.getJSONArray(pair.name());
	    for (int i = 0; i < data.length(); i++) {
		JSONArray test = data.getJSONArray(i);

		KrakenTimeSeries tts = parseTimeSeries(test);
		timeSeriesData.add(tts);

	    }
	    LOGGER.log(Level.INFO, "Succesfully retrieved all price data for currency pair [" + pair.name() + "]("
		    + timeSeriesData.size() + " records) in " + (System.currentTimeMillis() - startTime));
	    KrakenDTO.insertTimeseries(pair, base, crypto, timeSeriesData);
	    try {
		Thread.sleep(10000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
    }
    public ArrayList<KrakenTimeSeries> priceFetch(CurrencyPair pair,OHLCTimePeriod interval) {
   	ArrayList<KrakenTimeSeries> timeSeriesData = new ArrayList<KrakenTimeSeries>();
   	    LOGGER.log(Level.INFO, "Fetching daily price data for currency pair [" + pair.name() + "]");
   	    Map<String, String> input = new HashMap<String, String>();
   	    String response = null;
   	    input.put("pair", pair.name());
   	    input.put("interval", interval.id);
   	    long startTime = System.currentTimeMillis();
   	    while (true) {
   		try {
   		    LOGGER.log(Level.INFO, "Sending request to the Kraken API for currency pair [" + pair.name() + "]");
   		    response = api.queryPublic(Method.OHLC, input);
   		    JSONObject obj = new JSONObject(response);
   		    JSONObject res = obj.getJSONObject("result");
   		    break;
   		} catch (Exception e) {
   		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
   		    continue;

   		}
   	    }
   	    JSONObject obj = new JSONObject(response);
   	    JSONObject res = obj.getJSONObject("result");
   	    JSONArray data = res.getJSONArray(pair.name());
   	    for (int i = 0; i < data.length(); i++) {
   		JSONArray test = data.getJSONArray(i);

   		KrakenTimeSeries tts = parseTimeSeries(test);
   		timeSeriesData.add(tts);

   	    }
   	    LOGGER.log(Level.INFO, "Succesfully retrieved all price data for currency pair [" + pair.name() + "]("
   		    + timeSeriesData.size() + " records) in " + (System.currentTimeMillis() - startTime));
   	   return timeSeriesData;
       }
    public ArrayList<KrakenTimeSeries> tradesFetch(CurrencyPair pair) {
   	ArrayList<KrakenTimeSeries> timeSeriesData = new ArrayList<KrakenTimeSeries>();
   	    LOGGER.log(Level.INFO, "Fetching trade data for currency pair [" + pair.name() + "]");
   	    Map<String, String> input = new HashMap<String, String>();
   	    String response = null;
   	    long last = 0;
   	    input.put("pair", pair.name());
   	   
   	    long startTime = System.currentTimeMillis();
   	    while (true) {
   	
   		//input.put("since", last+"");
   		try {
   		    LOGGER.log(Level.INFO, "Sending request to the Kraken API for currency pair [" + pair.name() + "]");
   		    response = api.queryPublic(Method.TRADES, input);
   		    JSONObject obj = new JSONObject(response);
   		    JSONObject res = obj.getJSONObject("result");
   		    //last = obj.getLong("last");
   		    Thread.sleep(4000);
   		} catch (Exception e) {
   		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
   		    try {
   			
   		    }catch(Exception e1) {
   			//Thread.sleep(4000);
   		    }
   		    continue;

   		}
   	    }
       }
    public ArrayList<KrakenTimeSeries> priceFetch(CurrencyPair pair,OHLCTimePeriod interval, long since) {
   	ArrayList<KrakenTimeSeries> timeSeriesData = new ArrayList<KrakenTimeSeries>();
   	    LOGGER.log(Level.INFO, "Fetching daily price data for currency pair [" + pair.name() + "]");
   	    Map<String, String> input = new HashMap<String, String>();
   	    String response = null;
   	    input.put("pair", pair.name());
   	    input.put("interval", interval.id);
   	    input.put("since", String.valueOf(since));
   	    long startTime = System.currentTimeMillis();
   	    while (true) {
   		try {
   		    LOGGER.log(Level.INFO, "Sending request to the Kraken API for currency pair [" + pair.name() + "]");
   		    response = api.queryPublic(Method.OHLC, input);
   		    JSONObject obj = new JSONObject(response);
   		    JSONObject res = obj.getJSONObject("result");
   		    break;
   		} catch (Exception e) {
   		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
   		    try {
   			Thread.sleep(10000);
   		    }catch(Exception e1) {};
   		    continue;

   		}
   	    }
   	    //System.out.println(this.gson.toJson(response));
   	    JSONObject obj = new JSONObject(response);
   	    JSONObject res = obj.getJSONObject("result");
   	    JSONArray data = res.getJSONArray(pair.name());
   	    for (int i = 0; i < data.length(); i++) {
   		JSONArray test = data.getJSONArray(i);

   		KrakenTimeSeries tts = parseTimeSeries(test);
   		timeSeriesData.add(tts);

   	    }
   	    LOGGER.log(Level.INFO, "Succesfully retrieved all price data for currency pair [" + pair.name() + "]("
   		    + timeSeriesData.size() + " records) in " + (System.currentTimeMillis() - startTime));
   	   return timeSeriesData;
       }
    public long getLastTickIdentifier(CurrencyPair pair,OHLCTimePeriod interval) {
	LOGGER.log(Level.INFO, "Fetching last tick ID for currency pair [" + pair.name() + "]");
	    Map<String, String> input = new HashMap<String, String>();
	    String response = null;
	    input.put("pair", pair.name());
	    input.put("interval", interval.id);
	   
	    while (true) {
		try {
		    LOGGER.log(Level.INFO, "Sending request to the Kraken API for currency pair [" + pair.name() + "]");
		    response = api.queryPublic(Method.OHLC, input);
		    JSONObject obj = new JSONObject(response);
		    //KrakenUtil.serializePretty(obj);
		    JSONObject res = obj.getJSONObject("result");
		    long last = (Long)res.get("last");
		    return last;
		} catch (Exception e) {
		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
		    continue;

		}
	    }
	
    }
    public static void main(String[] args) {
	Scanner credScan = null;
	try {
	    credScan = new Scanner(new File("D:/Temp/kraken_keys.txt"));
	    LOGGER.log(Level.INFO, "Successfully loaded kraken API keys");
	} catch (FileNotFoundException e1) {
	    LOGGER.log(Level.SEVERE, "Error locating kraken API keys. Please check for the correct filepath.");
	    System.exit(0);
	}
	KrakenApi api = new KrakenApi();
	api.setKey(credScan.nextLine());
	api.setSecret(credScan.nextLine());
	getAssetPairs(api);
	for (CurrencyPair pair : CurrencyPair.values()) {
	    ArrayList<KrakenTimeSeries> timeSeriesData = new ArrayList<KrakenTimeSeries>();
	    LOGGER.log(Level.INFO, "Fetching daily price data for currency pair [" + pair.name() + "]");
	    Map<String, String> input = new HashMap<String, String>();
	    String response = null;
	    input.put("pair", pair.name());
	    input.put("interval", "1440");
	    long startTime = System.currentTimeMillis();
	    while (true) {
		try {
		    LOGGER.log(Level.INFO, "Sending request to the Kraken API for currency pair [" + pair.name() + "]");
		    response = api.queryPublic(Method.OHLC, input);
		    JSONObject obj = new JSONObject(response);
		    JSONObject res = obj.getJSONObject("result");
		    break;
		} catch (Exception e) {
		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
		    continue;

		}
	    }

	    LOGGER.log(Level.INFO, "Waiting 10 seconds to retrieve asset info for [" + pair.name() + "]");
	    try {
		Thread.sleep(10000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    String[] pairData = getAssetInfo(api, pair);
	    BaseCurrency base = null;
	    CryptoCurrency crypto = null;

	    try {
		base = BaseCurrency.valueOf(pairData[0]);
		crypto = CryptoCurrency.valueOf(pairData[1]);
	    } catch (Exception e) {
		base = BaseCurrency.valueOf(pairData[1]);
		crypto = CryptoCurrency.valueOf(pairData[0]);
	    }
	    JSONObject obj = new JSONObject(response);
	    JSONObject res = obj.getJSONObject("result");
	    JSONArray data = res.getJSONArray(pair.name());
	    for (int i = 0; i < data.length(); i++) {
		JSONArray test = data.getJSONArray(i);
		
		KrakenTimeSeries tts = parseTimeSeries(test);
		timeSeriesData.add(tts);

	    }
	    LOGGER.log(Level.INFO, "Succesfully retrieved all price data for currency pair [" + pair.name() + "]("
		    + timeSeriesData.size() + " records) in " + (System.currentTimeMillis() - startTime));
	    KrakenDTO.insertTimeseries(pair, base, crypto, timeSeriesData);
	    try {
		Thread.sleep(10000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	// FIXME
    }

    public static KrakenTimeSeries parseTimeSeries(JSONArray tradeData) {
	return new KrakenTimeSeries(KrakenUtil.fromEpoch(tradeData.getLong(OHLCIndex.TIMESTAMP.id)),
		tradeData.getDouble(OHLCIndex.OPEN.id), tradeData.getDouble(OHLCIndex.HIGH.id),
		tradeData.getDouble(OHLCIndex.LOW.id), tradeData.getDouble(OHLCIndex.CLOSE.id),
		tradeData.getDouble(OHLCIndex.VWAP.id), tradeData.getDouble(OHLCIndex.VOLUME.id),
		tradeData.getInt(OHLCIndex.COUNT.id));

    }

    public static String[] getAssetInfo(KrakenApi api, CurrencyPair pair) {
	String response = null;
	Map<String, String> input = new HashMap<String, String>();

	input.put("pair", pair.name());
	try {
	    response = api.queryPublic(Method.ASSET_PAIRS, input);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;

	}

	JSONObject obj = new JSONObject(response);
	JSONObject res = obj.getJSONObject("result");
	JSONObject fin = res.getJSONObject(pair.name());
	String base = fin.getString("base");
	String quote = fin.getString("quote");
	return new String[] { base, quote };
    }

    public static void getAssetPairs(KrakenApi api) {
	String response = null;

	try {
	    response = api.queryPublic(Method.ASSET_PAIRS);
	} catch (Exception e) {
	    e.printStackTrace();
	    return;

	}

	JSONObject obj = new JSONObject(response);

	JSONObject res = obj.getJSONObject("result");
	Map<String, Object> map = res.toMap();
	System.out.println(map.keySet().size());
	ArrayList<String> currencies = new ArrayList<String>();
	int count = 0;
	for (Entry<String, Object> entry : map.entrySet()) {
	    String pairName = entry.getKey();
	    JSONObject job = new JSONObject(gson.toJson(entry.getValue()));
	    String quote = job.getString("quote");
	    String baseCurrency = job.getString("base");
	    if (!currencies.contains(baseCurrency)) {
		currencies.add(baseCurrency);
	    }
	    if (pairName.contains("."))
		continue;
	    // System.out.println(pairName+"("+count+"),");

	}

	for (String s : currencies) {
	    if (s.contains("."))
		continue;
	    System.out.println(s + "(" + count + "),");
	    count++;

	}
	System.out.println(response);

    }

}
