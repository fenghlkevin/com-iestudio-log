package com.iestudio.framework.logwriter.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class Util {
    public static Method getSetMethodByName(Object object, String fieldName) {
        return getMethodByName(object,"set"+fieldName,true);
    }
    
    public static Method getMethodByName(Object object, String fieldName,boolean declared) {
        Method[] methodes = null;
        if (declared) {
            methodes = object.getClass().getDeclaredMethods();
        } else {
            methodes = object.getClass().getMethods();
        }
        for (int i = 0; i < methodes.length; i++) {
            if (methodes[i].getName().equalsIgnoreCase(fieldName)) {
                return methodes[i];
            }
        }
        return null;
    }
    
    public static Method getGetMethodByName(Object object, String fieldName) {
        return getMethodByName(object,"get"+fieldName,false);
    }
    
    /**
     * 将JSON字符串转换成MAP对象
     * @param jsonStr  JSON字符串
     * @return              MAP对象
     */
    @SuppressWarnings("unchecked")
	public static Map<String, String> jsonString2Map(String jsonStr) {
		ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = new HashMap<String, String>();
        
        try {
			map = (Map<String, String>)objectMapper.readValue(jsonStr, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return map;
	}
}
