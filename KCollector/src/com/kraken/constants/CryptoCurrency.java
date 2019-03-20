package com.kraken.constants;

import java.util.HashMap;
import java.util.Map;

public enum CryptoCurrency {
	XETH(0),
	XXRP(1),
	XETC(2),
	XREP(3),
	XZEC(4),
	BSV(5),
	GNO(6),
	QTUM(7),
	XMLN(8),
	XXBT(9),
	DASH(10),
	XLTC(11),
	EOS(12),
	ADA(13),
	XXLM(14),
	BCH(15),
	XTZ(16),
	XXMR(17),
	USDT(18),
	XXDG(19);
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
