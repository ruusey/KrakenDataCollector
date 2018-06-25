package com.kraken.constants;

import java.util.HashMap;
import java.util.Map;

public enum OHLCTimePeriod {

    ONE_MINUTE("1"), FIVE_MINUTES("5"), FIFTEEN_MINUTES("15"), HALF_HOUR("30"), ONE_HOUR("60"), FOUR_HOURS("240"), ONE_DAY("1440"), ONE_WEEK("10080"), FIFTEEN_DAYS("21600");

    public static Map<String, OHLCTimePeriod> map = new HashMap<String, OHLCTimePeriod>();
    static {
	for (OHLCTimePeriod idx : OHLCTimePeriod.values()) {
	    map.put(idx.id, idx);
	}
    }

    public String id;

    OHLCTimePeriod(String type) {
	this.id = type;
    }

    public static OHLCTimePeriod valueOfString(String type) {
	return map.get(type);
    }

}
