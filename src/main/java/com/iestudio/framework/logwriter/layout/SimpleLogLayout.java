package com.iestudio.framework.logwriter.layout;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class SimpleLogLayout extends Layout {

    @Override
    public String format(LoggingEvent arg0) {
        Object obj=arg0.getMessage();
        if(obj instanceof String){
            return (String)obj;
        }
        return "wrong Object type for format log";
    }

    @Override
    public boolean ignoresThrowable() {
        return true;
    }

    public void activateOptions() {
    }

}
