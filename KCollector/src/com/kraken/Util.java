package com.kraken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;

public class Util {
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static Date fromEpoch(long date) {
	String formattedDate = sdf.format(new Date(date));

	return java.sql.Date.valueOf(formattedDate);
	
    }
}
