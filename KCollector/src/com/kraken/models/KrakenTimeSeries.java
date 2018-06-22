package com.kraken.models;

import java.sql.Date;

public class KrakenTimeSeries {

    private Date timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double vwap;
    private double volume;
    private int count;

    public KrakenTimeSeries(Date timestamp, double open, double high, double low, double close, double vwap,
	    double volume, int count) {
	super();
	this.timestamp = timestamp;
	this.open = open;
	this.high = high;
	this.low = low;
	this.close = close;
	this.vwap = vwap;
	this.volume = volume;
	this.count = count;
    }

    public KrakenTimeSeries() {
	// TODO Auto-generated constructor stub
    }

    public Date getTimestamp() {
	return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
	this.timestamp = timestamp;
    }

    public double getOpen() {
	return this.open;
    }

    public void setOpen(double open) {
	this.open = open;
    }

    public double getHigh() {
	return this.high;
    }

    public void setHigh(double high) {
	this.high = high;
    }

    public double getLow() {
	return this.low;
    }

    public void setLow(double low) {
	this.low = low;
    }

    public double getClose() {
	return this.close;
    }

    public void setClose(double close) {
	this.close = close;
    }

    public double getVwap() {
	return this.vwap;
    }

    public void setVwap(double vwap) {
	this.vwap = vwap;
    }

    public double getVolume() {
	return this.volume;
    }

    public void setVolume(double volume) {
	this.volume = volume;
    }

    public int getCount() {
	return this.count;
    }

    public void setCount(int count) {
	this.count = count;
    }
}
