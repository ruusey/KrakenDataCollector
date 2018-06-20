package com.kraken;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IO {
    public static void insertTimeseries(CurrencyPair pair, BaseCurrency base, CryptoCurrency crypto,
	    List<TradeTimeSeries> data) {

	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException ex) {

	}
	String url = "jdbc:mysql://localhost:3306/kraken";
	String USER = "root";
	String PASS = "";
	Connection conn = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    conn = DriverManager.getConnection(url, USER, PASS);
	    for (TradeTimeSeries entry : data) {
		String sql = "INSERT INTO crypto_timeseries (currency_pair, base_currency, quote_currency,timestamp,open,high,low,close,vwap,count)"
			+ "values (?, ? ,? ,? ,? ,?,?,?,?,?)";
		int idx = 1;
		PreparedStatement preparedStmt = conn.prepareStatement(sql);
		preparedStmt.setInt(idx++, pair.type);
		preparedStmt.setInt(idx++, base.type);
		preparedStmt.setInt(idx++, crypto.type);
		preparedStmt.setDate(idx++, entry.getTimestamp());
		preparedStmt.setDouble(idx++, entry.getOpen());
		preparedStmt.setDouble(idx++, entry.getHigh());
		preparedStmt.setDouble(idx++, entry.getLow());
		preparedStmt.setDouble(idx++, entry.getClose());
		preparedStmt.setDouble(idx++, entry.getVwap());
		preparedStmt.setInt(idx++, entry.getCount());

		preparedStmt.execute();
	    }

	} catch (Exception e2) {

	    e2.printStackTrace();

	} finally {
	    try {
		conn.close();
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public static List<TradeTimeSeries> getTimeSeriesData(CurrencyPair pair) {
	ArrayList<TradeTimeSeries> series = new ArrayList<TradeTimeSeries>();
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException ex) {

	}
	String url = "jdbc:mysql://localhost:3306/kraken";
	String USER = "root";
	String PASS = "";
	Connection conn = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    conn = DriverManager.getConnection(url, USER, PASS);
	    Statement st = conn.createStatement();
	    String SQL = "SELECT * FROM crypto_timeseries WHERE currency_pair=" + pair.type + "";
	    ResultSet rs = st.executeQuery(SQL);
	    while (rs.next()) {
		int columnId = 5;
		TradeTimeSeries tts = new TradeTimeSeries();
		tts.setTimestamp(rs.getDate(columnId++));
		tts.setOpen(rs.getDouble(columnId++));
		tts.setHigh(rs.getDouble(columnId++));
		tts.setLow(rs.getDouble(columnId++));
		tts.setClose(rs.getDouble(columnId++));
		tts.setVwap(rs.getDouble(columnId++));
		tts.setVolume(rs.getDouble(columnId++));
		tts.setCount(rs.getInt(columnId++));
		series.add(tts);
	    }

	} catch (Exception e2) {

	    e2.printStackTrace();

	} finally {
	    try {
		conn.close();
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return series;
    }

}
