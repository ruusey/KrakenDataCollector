package com.kraken.api.impl;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.kraken.app.KrakenScraper;
import com.kraken.constants.VerificationTier;
import com.kraken.util.KrakenUtil;

import edu.self.kraken.api.KrakenApi;
import edu.self.kraken.api.KrakenApi.Method;

public class KrakenAPI {
    private static final int TIMEOUT_RETRY_COUNT = 3;
    private static final long API_SLEEP_MS = 10000;
    public static Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(KrakenAPI.class.getName());
    public KrakenApi api;
    public APICallManager manager;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    public KrakenAPI(String apiKey, String apiSecret, VerificationTier tier) {
	api = new KrakenApi();
	api.setKey(apiKey);
	api.setSecret(apiSecret);
	manager = new APICallManager(tier);
    }


    private JSONObject getResult(String json) {
	JSONObject base = new JSONObject(json);
	return (base.has("result")) ? base.getJSONObject("result") : null;
    }
    private boolean hasResult(String json) {
	JSONObject base = new JSONObject(json);
	return (base.has("result")) ? true : false;
    }

   
    public String executeQuery(Method apiMethod) {
	long start = System.currentTimeMillis();
	String response = null;
	    int attempts = 0;
	    while (attempts<TIMEOUT_RETRY_COUNT) {
		try {
		    LOGGER.log(Level.INFO, "Sending [" +apiMethod.name+"] request to Kraken API [attempt "+(attempts+1)+"/"+TIMEOUT_RETRY_COUNT+"]");
		    response = api.queryPublic(apiMethod);
		    if(hasResult(response)) {
			LOGGER.log(Level.INFO, "Recieved response for [" +apiMethod.name+"] request to Kraken API "+KrakenUtil.getElapsedTime(start));
			return response;
		    }
		    
		} catch (Exception e) {
		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
		    attempts++;
		    continue;

		}
	    }
	
	return response;
    }
    public String executeQuery(Method apiMethod,  Map<String, String> input) {
	long start = System.currentTimeMillis();
	String response = null;
	    int attempts = 0;
	    while (attempts<TIMEOUT_RETRY_COUNT) {
		try {
		    //if(!manager.canRequest(apiMethod)) continue;
		    LOGGER.log(Level.INFO, "Sending [" +apiMethod.name+"] request to Kraken API [attempt "+(attempts+1)+"/"+TIMEOUT_RETRY_COUNT+"]");
		    response = api.queryPublic(apiMethod,input);
		    if(hasResult(response)) {
			LOGGER.log(Level.INFO, "Recieved response for [" +apiMethod.name+"] request to Kraken API "+KrakenUtil.getElapsedTime(start));
			return response;
		    }
		    
		} catch (Exception e) {
		    LOGGER.log(Level.SEVERE, "Error contacting the Kraken API\nCause: " + e.getMessage());
		    attempts++;
		    continue;

		}
	    }
	
	return response;
    }
    public boolean pingServer() {
   	String response = null;
   	try {
   	    
   	    JSONObject obj = getResult(executeQuery(Method.TIME));
   	    
   	    long timestamp = obj.getLong("unixtime");
   	    LOGGER.log(Level.INFO, "Succesfully contacted the Kraken API. Server time ["
   		    + KrakenUtil.stringFromEpoch(timestamp, true) + "]");
   	    return true;

   	} catch (Exception e) {
   	    LOGGER.log(Level.SEVERE, "Error pinging Kraken API. Please check your API keys. Msg[" + response + "]");
   	    return false;
   	}
       }

}
