package com.kraken.constants;

import java.util.HashMap;
import java.util.Map;

public enum CurrencyPair {

    XETHZUSD(0),
    XXRPZUSD(1),
    XETCZEUR(2),
    XREPXXBT(3),
    XZECXXBT(4),
    GNOUSD(5),
    XREPZUSD(6),
    XMLNXETH(7),
    DASHEUR(8),
    XETCXETH(9),
    XLTCZEUR(10),
    XXRPXXBT(11),
    XZECZUSD(12),
    DASHXBT(13),
    EOSXBT(14),
    XETHXXBT(15),
    EOSEUR(16),
    GNOETH(17),
    XZECZJPY(18),
    XXLMZEUR(19),
    XXBTZCAD(20),
    BCHUSD(21),
    XXBTZEUR(22),
    XETHZGBP(23),
    XREPXETH(24),
    XXMRZEUR(25),
    XETHZJPY(26),
    XICNXXBT(27),
    XETHZCAD(28),
    XXRPZCAD(29),
    GNOEUR(30),
    XETHZEUR(31),
    XLTCXXBT(32),
    XMLNXXBT(33),
    USDTZUSD(34),
    XETCXXBT(35),
    XXRPZEUR(36),
    EOSETH(37),
    XETCZUSD(38),
    BCHXBT(39),
    DASHUSD(40),
    XZECZEUR(41),
    XREPZEUR(42),
    XLTCZUSD(43),
    EOSUSD(44),
    BCHEUR(45),
    XICNXETH(46),
    XXMRXXBT(47),
    XXBTZUSD(48),
    XXLMZUSD(49),
    XXMRZUSD(50),
    XXLMXXBT(51),
    XXDGXXBT(52),
    XXBTZGBP(53),
    GNOXBT(54),
    XXRPZJPY(55),
    XXBTZJPY(56);
    public static Map<Integer, CurrencyPair> map = new HashMap<Integer, CurrencyPair>();
    static {
	for (CurrencyPair lb : CurrencyPair.values()) {
	    map.put(lb.type, lb);
	}
    }

    public int type;

    CurrencyPair(int type) {
	this.type = type;
    }

    public static CurrencyPair valueOf(int type) {
	return map.get(type);
    }

}
