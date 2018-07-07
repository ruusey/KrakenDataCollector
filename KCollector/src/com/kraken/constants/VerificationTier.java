package com.kraken.constants;

import java.util.HashMap;
import java.util.Map;

public enum VerificationTier {

    TIER_2(2), TIER_3(3), TIER_4(4);

    public static Map<Integer, VerificationTier> map = new HashMap<Integer, VerificationTier>();
    static {
	for (VerificationTier idx : VerificationTier.values()) {
	    map.put(idx.id, idx);
	}
    }

    public int id;

    VerificationTier(int type) {
	this.id = type;
    }

    public static VerificationTier valueOf(int type) {
	return map.get(type);
    }

}
