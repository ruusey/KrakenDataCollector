package com.kraken.dto;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.kraken.constants.BaseCurrency;
import com.kraken.constants.CryptoCurrency;
import com.kraken.constants.CurrencyPair;
import com.kraken.models.KrakenTimeSeries;
import com.kraken.util.KeyLoader;
import com.kraken.util.KrakenUtil;

public class KrakenDTO {
    public static final String SCHEMA_SQL = "CREATE DATABASE `kraken` /*!40100 DEFAULT CHARACTER SET latin1 */;";
    private static final Logger LOGGER = Logger.getLogger(KrakenDTO.class.getName());

    public static void insertTimeseries(CurrencyPair pair, BaseCurrency base, CryptoCurrency crypto,
	    List<KrakenTimeSeries> data) {

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
	    for (KrakenTimeSeries entry : data) {
		String sql = "INSERT INTO crypto_timeseries (currency_pair, base_currency, quote_currency,timestamp,open,high,low,close,vwap,volume,count)"
			+ "values (?, ? ,? ,? ,? ,?,?,?,?,?,?)";
		int idx = 1;
		PreparedStatement preparedStmt = conn.prepareStatement(sql);
		preparedStmt.setInt(idx++, pair.type);
		preparedStmt.setInt(idx++, base.type);
		preparedStmt.setInt(idx++, crypto.type);
		preparedStmt.setTimestamp(idx++, entry.getTimestamp());
		preparedStmt.setDouble(idx++, entry.getOpen());
		preparedStmt.setDouble(idx++, entry.getHigh());
		preparedStmt.setDouble(idx++, entry.getLow());
		preparedStmt.setDouble(idx++, entry.getClose());
		preparedStmt.setDouble(idx++, entry.getVwap());
		preparedStmt.setDouble(idx++, KrakenUtil.round(entry.getVolume(),2));
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

    public static List<KrakenTimeSeries> getTimeSeriesData(CurrencyPair pair) {
	ArrayList<KrakenTimeSeries> series = new ArrayList<KrakenTimeSeries>();
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
		KrakenTimeSeries tts = new KrakenTimeSeries();
		tts.setTimestamp(rs.getTimestamp(columnId++));
		tts.setOpen(rs.getDouble(columnId++));
		tts.setHigh(rs.getDouble(columnId++));
		tts.setLow(rs.getDouble(columnId++));
		tts.setClose(rs.getDouble(columnId++));
		tts.setVwap(rs.getDouble(columnId++));
		tts.setVolume(rs.getDouble(columnId++));
		tts.setCount(rs.getInt(columnId++));
		series.add(tts);
	    }
	    conn.close();

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

    public static boolean checkKrakenSchema() {
	String exists = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'kraken'";
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException ex) {

	}
	String url = "jdbc:mysql://localhost:3306";
	String USER = "root";
	String PASS = "";
	Connection conn = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    conn = DriverManager.getConnection(url, USER, PASS);
	    Statement st = conn.createStatement();

	    ResultSet rs = st.executeQuery(exists);
	    if (rs.first()) {
		conn.close();
		return true;
	    } else {
		conn.close();
		return false;

	    }

	} catch (Exception e2) {

	    e2.printStackTrace();

	} finally {
	    try {
		conn.close();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	return false;
    }

    public static boolean checkKrakenTable() {
	String exists = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'crypto_timeseries'";
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException ex) {

	}
	String url = "jdbc:mysql://localhost:3306";
	String USER = "root";
	String PASS = "";
	Connection conn = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    conn = DriverManager.getConnection(url, USER, PASS);
	    Statement st = conn.createStatement();
	    ResultSet rs = st.executeQuery(exists);
	    if (rs.first()) {
		conn.close();
		return true;
	    } else {
		conn.close();
		return false;
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
	return false;
    }

    public static void createKrakenSchema() {
	String SQL = null;

	try {
	    SQL = FileUtils.readFileToString(new File("resources/crypto_timeseries"), Charset.defaultCharset());

	    try {
		Class.forName("com.mysql.jdbc.Driver");
	    } catch (ClassNotFoundException ex) {

	    }
	    String url = "jdbc:mysql://localhost:3306";
	    String USER = "root";
	    String PASS = "";
	    Connection conn = null;
	    try {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn = DriverManager.getConnection(url, USER, PASS);
		Statement st = conn.createStatement();
		st.executeUpdate(SQL);

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
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static String getTableCreateStatement() {
	String SQL = "SHOW CREATE TABLE kraken.crypto_timeseries";

	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (Exception ex) {

	}
	String url = "jdbc:mysql://localhost:3306";
	String USER = "root";
	String PASS = "";
	Connection conn = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    conn = DriverManager.getConnection(url, USER, PASS);
	    Statement st = conn.createStatement();
	    ResultSet rs = st.executeQuery(SQL);
	    if (rs.next()) {
		String create = rs.getString(2);
		String search = "AUTO_INCREMENT=";
		int idx = create.indexOf(search);
		idx += search.length();
		int end = create.indexOf(" ", idx);
		String autoIncr = create.substring(idx, end);
		return create.replace(autoIncr, "0");

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

	return null;
    }

    public static String getSchemaCreateStatement() {
	String SQL = "SHOW CREATE SCHEMA kraken";

	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (Exception ex) {

	}
	String url = "jdbc:mysql://localhost:3306";
	String USER = "root";
	String PASS = "";
	Connection conn = null;
	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    conn = DriverManager.getConnection(url, USER, PASS);
	    Statement st = conn.createStatement();
	    ResultSet rs = st.executeQuery(SQL);
	    if (rs.next()) {
		return rs.getString(2);
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

	return null;
    }

}
