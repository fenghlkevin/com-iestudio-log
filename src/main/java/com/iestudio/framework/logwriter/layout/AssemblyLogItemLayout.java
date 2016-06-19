package com.iestudio.framework.logwriter.layout;

import com.kevin.iesutdio.tools.clazz.ObjUtil;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.codehaus.jettison.json.JSONObject;

import com.iestudio.framework.logwriter.logitem.ILogItemInf;

public class AssemblyLogItemLayout extends Layout {

    protected StringBuffer sbuf;

    /**
     * format规则： cpid%%|%%name%%|%%cpid%%|%%an_id
     * 
     * 可以增加默认值属性，TimeMillis\Date#format\[ABCDEFG]
     */
    protected String format;
    
    /**
     * 默认是string ，可以有 string,json
     */
    protected String output="String";

    public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	private String[] formatedItems;

    public AssemblyLogItemLayout() {
        sbuf = new StringBuffer(128);
    }

    private void init() {
        if (formatedItems == null) {
            formatedItems = format.split("%%");
        }
    }
    
    private String printJson(ILogItemInf item){
        JSONObject json = new JSONObject();
        
    	//StringBuffer s = new StringBuffer();
        String itemValue;
        //s.append("{");
        try{
            for (int i = 0; i < formatedItems.length; i++) {
                itemValue = item.getItemValue(formatedItems[i]);
                
                json.put(formatedItems[i], ObjUtil.isEmpty(itemValue) ? "" : itemValue);
                
                /*if(i == formatedItems.length - 1){
                    s.append("\"").append(formatedItems[i]).append("\"").append(":").append("\"").append(ObjUtil.isEmpty(itemValue)?"":itemValue).append("\"");
                }else{
                    s.append("\"").append(formatedItems[i]).append("\"").append(":").append("\"").append(ObjUtil.isEmpty(itemValue)?"":itemValue).append("\",");
                }*/
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return json.toString();
        //s.append("}");
        //return s.toString() + "\n";
    }
    
    private String printString(ILogItemInf item) {
        StringBuffer s = new StringBuffer();
        String itemValue;
        for (int i = 0; i < formatedItems.length; i++) {
            itemValue = item.getItemValue(formatedItems[i]);
            if (ObjUtil.isEmpty(itemValue)) {
//                s.append(formatedItems[i]);
                s.append("");
            } else {
                s.append(itemValue);
            }
        }
        return s.toString() + "\n";
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String format(LoggingEvent loggingevent) {
        if (loggingevent.getMessage() != null) {
            sbuf.setLength(0);
            if (loggingevent.getMessage() instanceof ILogItemInf) {
            	ILogItemInf lii=(ILogItemInf) loggingevent.getMessage();
                sbuf.append("string".equalsIgnoreCase(this.output)?this.printString(lii):this.printJson(lii));
            }else{
                sbuf.append(loggingevent.getMessage().toString());
            }
        }
        return sbuf.toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return true;
    }

    public void activateOptions() {
        init();
    }

}
