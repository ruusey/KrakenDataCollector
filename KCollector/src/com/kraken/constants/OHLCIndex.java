package com.kraken.constants;

import java.util.HashMap;
import java.util.Map;

public enum OHLCIndex {
	TIMESTAMP(0),
	OPEN(1),
	HIGH(2),
	LOW(3),
	CLOSE(4),
	VWAP(5),
	VOLUME(6),
	COUNT(7),
	ID(8);
	
	public static Map<Integer, OHLCIndex> map = new HashMap<Integer, OHLCIndex>();
	static {
        for (OHLCIndex idx : OHLCIndex.values()) {
            map.put(idx.id, idx);
        }
    }
	
	public int id;
	OHLCIndex(int type){
		this.id=type;
	}
	public static OHLCIndex valueOf(int type) {
        return map.get(type);
    }
 
}
