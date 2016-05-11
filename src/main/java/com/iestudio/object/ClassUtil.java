package com.iestudio.object;

import java.util.List;

public class ClassUtil {
	public static Object createObject(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
	}
	
	@SuppressWarnings("unchecked")
	public static Class createClass(String className) throws ClassNotFoundException{
		return Thread.currentThread().getContextClassLoader().loadClass(className);
	}
	
	@SuppressWarnings("unchecked")
	public static void arrayToList(Object[] args,List list){
		for (int i = 0; i < args.length; i++) {
			list.add(args[i]);
		}
	}
}
