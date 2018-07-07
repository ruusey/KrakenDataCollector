package com.kraken.api.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.kraken.constants.VerificationTier;
import com.kraken.util.KrakenUtil;

import edu.self.kraken.api.KrakenApi.Method;

public class APICallManager extends Thread{
    private static final double LIMIT_THRESHOLD=0.8;
    private static final Logger LOGGER = Logger.getLogger(APICallManager.class.getName());
    private int callCount;
    private int callLimit;
    private double callResetRate;
    private double reductionProgress;
    public APICallManager(VerificationTier tier) {
	switch(tier.id) {
	case 2:
	    this.reductionProgress=0.0;
	    this.callCount=0;
	    this.callLimit=15;
	    this.callResetRate=0.33;
	    break;
	case 3:
	    this.reductionProgress=0.0;
	    this.callCount=0;
	    this.callLimit=20;
	    this.callResetRate=0.5;
	    break;
	case 4:
	    this.reductionProgress=0.0;
	    this.callCount=0;
	    this.callLimit=20;
	    this.callResetRate=1.0;
	    break;
	}
    }
    public void run() {
	while(true) {
	    try {
		LOGGER.log(Level.INFO, "Current API call count ("+callCount+"/"+callLimit+")");
		Thread.sleep(1000);
		reductionProgress+=callResetRate;
		if(reductionProgress>0.9) {
		    decrementCallCount();
		}
		
	    }catch(Exception e) {
		e.printStackTrace();
	    }
	    
	}
	
    }
    public boolean canRequest(Method m) {
	int cost = KrakenUtil.apiCallCost(m);
	if(exceedsThreshold(cost)) {
	    return false;
	}else {
	    return true;
	}
	
    }
    public boolean handleRequest(Method m) {
	if(canRequest(m)) {
	    LOGGER.log(Level.INFO, "Able to handle [" +m.name+"] request to Kraken API");
	    makeRequest(KrakenUtil.apiCallCost(m));
	    return true;
	}else {
	    LOGGER.log(Level.SEVERE, "Unable to handle [" +m.name+"] request to Kraken API");
	    return false;
	}
	
    }
    private boolean exceedsThreshold(int cost) {
	return ((double) (callCount+cost))/((double)callLimit) >= LIMIT_THRESHOLD;
    }
    public double getThreshold() {
	return ((double) callCount)/((double)callLimit);
    }
    public void makeRequest(int cost) {
	callCount=(callCount+cost);
    }
    private void decrementCallCount() {
	callCount=(callCount-1);
	reductionProgress=0.0;
    }
    

}
