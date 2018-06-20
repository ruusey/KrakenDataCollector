package com.kraken.constants;

import java.util.HashMap;
import java.util.Map;

public enum BaseCurrency {
    ZUSD(0), ZEUR(1), XXBT(2), XETH(3), ZCAD(4), ZGBP(5), ZJPY(6);
    public static Map<Integer, BaseCurrency> map = new HashMap<Integer, BaseCurrency>();
    static {
	for (BaseCurrency lb : BaseCurrency.values()) {
	    map.put(lb.type, lb);
	}
    }

    public int type;

    BaseCurrency(int type) {
	this.type = type;
    }

    public static BaseCurrency valueOf(int type) {
	return map.get(type);
    }
}
