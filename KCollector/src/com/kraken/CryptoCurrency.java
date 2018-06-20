package com.kraken;

import java.util.HashMap;
import java.util.Map;

public enum CryptoCurrency {
    XETH(0),
    XXRP(1),
    XETC(2),
    XREP(3),
    XZEC(4),
    GNO(5),
    XMLN(6),
    XXBT(7),
    DASH(8),
    XLTC(9),
    EOS(10),
    XXLM(11),
    BCH(12),
    XXMR(13),
    XICN(14),
    USDT(15),
    XXDG(16);
    public static Map<Integer, CryptoCurrency> map = new HashMap<Integer, CryptoCurrency>();
    static {
	for (CryptoCurrency lb : CryptoCurrency.values()) {
	    map.put(lb.type, lb);
	}
    }

    public int type;

    CryptoCurrency(int type) {
	this.type = type;
    }

    public static CryptoCurrency valueOf(int type) {
	return map.get(type);
    }
}
