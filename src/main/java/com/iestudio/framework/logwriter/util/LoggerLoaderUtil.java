package com.iestudio.framework.logwriter.util;

import java.lang.reflect.Field;

import com.kevin.iesutdio.tools.clazz.ObjUtil;
import org.apache.log4j.Level;

public class LoggerLoaderUtil {
	
	public static Level getLevel(String levelName) {
		Field[] fields = Level.class.getDeclaredFields();
		for (Field f : fields) {
			if (f.getName().equalsIgnoreCase(levelName)) {
				Object level = null;
				try {
					level = f.get(null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (!ObjUtil.isEmpty(level) && (level instanceof Level)) {
					return (Level) level;
				}
			}
		}
		return Level.DEBUG;
	}
}
