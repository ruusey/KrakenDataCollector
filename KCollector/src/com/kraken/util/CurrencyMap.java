package com.kraken.util;

import java.util.HashMap;

import com.kraken.constants.BaseCurrency;
import com.kraken.constants.CryptoCurrency;
import com.kraken.constants.CurrencyPair;

public class CurrencyMap {
    private static HashMap<CurrencyPair,BaseCurrency> pairToBase = new HashMap<CurrencyPair, BaseCurrency>();
    private static HashMap<CurrencyPair, CryptoCurrency> pairToCrypto = new HashMap<CurrencyPair, CryptoCurrency>();
    static {
	for(CurrencyPair pair : CurrencyPair.values()) {
	    for(BaseCurrency base : BaseCurrency.values()) {
		if(pair.name().endsWith(base.name()) || pair.name().endsWith(base.name().substring(1, base.name().length()))) {
		    pairToBase.put(pair, base);
		}
	    }
	    for(CryptoCurrency base : CryptoCurrency.values()) {
		if(pair.name().startsWith(base.name()) || pair.name().startsWith(base.name().substring(1, base.name().length()))) {
		    pairToCrypto.put(pair, base);
		}
	    }
	}
    }
    public static BaseCurrency getPairBase(CurrencyPair pair) {
	return pairToBase.get(pair);
    }
    public static CryptoCurrency getPairCrypto(CurrencyPair pair) {
	return pairToCrypto.get(pair);
    }

}
