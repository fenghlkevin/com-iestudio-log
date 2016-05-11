package com.iestudio.framework.logwriter.realwriter;

import java.io.Writer;

import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.spi.ErrorHandler;

public class LogWriter extends CountingQuietWriter {
	private int row;

	public LogWriter(Writer writer, ErrorHandler eh) {
		super(writer, eh);
	}

	public long getRowCount() {
		return row;
	}

	public void setRowCount(int row) {
		this.row = row;
	}
	
	 public void write(String string){
		 super.write(string);
		 row++;
	 }
	 
	 public void clearRow(){
		row=0; 
	 }
}
