package com.iestudio.framework.logwriter.logitem;

import java.io.Serializable;

public interface ILogItemInf extends Serializable{
	
	public abstract String getItemValue(String itemName);

	public abstract void setItemValue(String itemName, String itemValue);
}
