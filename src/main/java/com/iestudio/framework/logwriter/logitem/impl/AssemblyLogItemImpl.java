package com.iestudio.framework.logwriter.logitem.impl;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.iestudio.object.ClassUtil;
import com.iestudio.object.ObjUtil;
import com.iestudio.framework.logwriter.constants.LogContants;
import com.iestudio.framework.logwriter.logitem.ILogEncoder;
import com.iestudio.framework.logwriter.logitem.ILogItemInf;
import com.iestudio.framework.logwriter.util.Util;

public abstract class AssemblyLogItemImpl implements ILogItemInf {

	/**
     * 
     */
	private static final long serialVersionUID = 6300654586541097988L;

	public String getItemValue(String itemName) {
		return getTypeValue(itemName);
//		Method method = Util.getGetMethodByName(this, itemName);
//		if (method == null) {
//			return getTypeValue(itemName);
//		}
//		String rValue = "";
//		Object obj = getMethodValue(method);
//		if (ObjUtil.isEmpty(obj)) {
//			rValue = "";
//		} else {
//			rValue = String.valueOf(obj);
//		}
//		return rValue;
	}

	private Object getMethodValue(Method method) {
		try {
			Object obj = method.invoke(this);
			return obj;
		} catch (Exception e) {
			throw new RuntimeException("反射 " + method.getName() + " 时候异常", e);
		}
	}

	public void setItemValue(String itemName, String itemValue) {
		Method method = Util.getSetMethodByName(this, itemName);
		if (method == null) {
			return;
		}
		try {
			method.invoke(this, new Object[] { itemValue });
		} catch (Exception e) {
			throw new RuntimeException("反射 " + itemName + " 时候异常", e);
		}
	}

	private String getTypeValue(String itemName) {
		if (itemName == null) {
			return "";
		}
		String reValue = itemName;
		Method method = Util.getGetMethodByName(this, itemName);
		if (method != null) {
			Object obj = getMethodValue(method);
			if (ObjUtil.isEmpty(obj)) {
				reValue = "";
			} else {
				reValue = String.valueOf(obj);
			}
		} else {
			if (itemName.contains(LogContants.TYPE_DATE)) {
				int index = itemName.indexOf("#");
				String type = null;
				if (index <= 0) {
					type = "yyyyMMddHHmmss";
				} else {
					type = itemName.substring(index + 1);
				}
				SimpleDateFormat s = new SimpleDateFormat(type);
				reValue = s.format(new Date());
			} else if (LogContants.TYPE_TIMEMILLIS.equalsIgnoreCase(itemName)) {
				reValue = String.valueOf(System.currentTimeMillis());
			} else if (itemName.startsWith(LogContants.TYPE_ENCODE_CLASS)) {
				String clazzName = itemName.substring(itemName.indexOf(LogContants.TYPE_ENCODE_CLASS)+LogContants.TYPE_ENCODE_CLASS.length(), itemName.indexOf("#")).trim();
				String methodName = itemName.substring(itemName.indexOf("#")+1);
				ILogEncoder encoder;
				try {
					encoder = (ILogEncoder) ClassUtil.createObject(clazzName);
				} catch (Exception e) {
					throw new RuntimeException("instance ILogEncoder error. class is [" + clazzName + "]");
				}
				method = Util.getGetMethodByName(this, methodName);
				if(method==null){
					reValue="";
				}else{
					Object obj = this.getMethodValue(method);
					reValue = encoder.execute(obj);
				}
			} else if (itemName.startsWith("[") && itemName.endsWith("]")) {
				itemName = itemName.replaceAll("\\[", "").replaceAll("\\]", "");
				reValue = itemName;
			}
		}
		return reValue;
	}

}
