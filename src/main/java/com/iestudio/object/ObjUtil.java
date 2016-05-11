package com.iestudio.object;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: [对象操作UTIL]
 * </p>
 * <br>
 * <p>
 * Company:
 * </p>
 * 
 * @author 冯贺亮 fenghl@neusoft.com
 * @version Revision: 1.0
 */
@SuppressWarnings("unchecked")
public class ObjUtil {

	/**
	 * <p>
	 * Discription:[判断STRING是否为空]
	 * </p>
	 * 
	 * @param args
	 * @return
	 * @author:[冯贺亮]
	 * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
	 */
	public static boolean isEmpty(String args) {
		// if (null==args || "".equalsIgnoreCase(args)) {
		// return true;
		// }
		// return false;
		return !(args != null && args.length() > 0);
	}

	/**
	 * <p>
	 * Discription:[判断STRING是否为空]
	 * </p>
	 * 
	 * @deprecated
	 * @param args
	 * @return
	 * @author:[冯贺亮]
	 * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
	 */
	public static boolean isEmpty(String args, boolean trimBlank) {
		if (trimBlank) {
			if (isEmpty(args) || "".equalsIgnoreCase(args.trim())) {
				return true;
			}
		} else {
			return isEmpty(args);
		}

		return false;
	}

	public static boolean isEmpty(Map map) {
		return map == null || map.isEmpty();
	}

	public static boolean isEmpty(Collection collection) {
		return collection == null || collection.isEmpty();
	}

//	public static boolean isEmpty(DataList dl) {
//		return dl == null || dl.getRowCount() <= 0;
//	}
//
//	public static boolean isEmpty(IMultKeysMap multMap) {
//		return multMap == null || multMap.isEmpty();
//	}

	public static void main(String[] args) {
		System.out.println(isEmpty(new String[] { "" }));
		System.out.println(isEmpty(new HashMap()));
		System.out.println(isEmpty((Object) new HashMap()));
		new Integer(-1);
		System.out.println(isEmpty(new ArrayList()));
	}

	/**
	 * 
	 * <p>
	 * Discription:判断对象是否为NULL 或 SIZE()为0
	 * </p>
	 * 
	 * @param object
	 * @return
	 * @author:冯贺亮
	 * @create:2007-10-3 14:02:19
	 * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
	 */
	public static boolean isEmpty(Object object) {
		if (object == null) {
			return true;
		}

		if (object instanceof Collection) {
			return isEmpty((Collection) object);
		} else if (object instanceof String) {
			return isEmpty((String) object);
		} else if (object.getClass().isArray()) {
			if (object instanceof String[]) {
				String[] strObj = (String[]) object;
				if (strObj.length == 1 && isEmpty(strObj[0])) {
					return true;
				}
				return false;
			} else if (object instanceof Number[]) {
				Number[] strObj = (Number[]) object;
				if (strObj.length == 1 && isEmpty(strObj[0])) {
					return true;
				}
				return false;
			} else if (object instanceof byte[]) {
				byte[] strObj = (byte[]) object;
				if (strObj.length == 1 && isEmpty(strObj[0])) {
					return true;
				}
				return false;
			} else if (object instanceof boolean[]) {
				boolean[] strObj = (boolean[]) object;
				if (strObj.length == 1 && isEmpty(strObj[0])) {
					return true;
				}
				return false;
			} else if (object instanceof File[]) {
				File[] strObj = (File[]) object;
				if (strObj.length == 1 && isEmpty(strObj[0])) {
					return true;
				}
				return false;
			}else {
				return false;
			}

		} else if (object instanceof Map) {
			return isEmpty((Map) object);
		}

		// else if (object instanceof String[]) {
		// String[] strObj = (String[]) object;
		// if (strObj.length == 0) {
		// return true;
		// }
		// if (strObj.length == 1 && isEmpty(strObj[0])) {
		// return true;
		// }
		//
		// } else if (object instanceof Object[]) {
		// Object[] tempObj = (Object[]) object;
		// if (tempObj.length == 0) {
		// return true;
		// }
		// if (tempObj.length == 1 && isEmpty(tempObj[0])) {
		// return true;
		// }
		// } else if (object instanceof Map) {
		// if (((Map) object).isEmpty()) {
		// return true;
		// }
		// }
		return false;

	}

	/**
	 * <p>
	 * Description: [是否为数字]
	 * <p>
	 * 
	 * @param args
	 * @return
	 * @author [冯贺亮]创建于[2007-11-22]
	 */
	public static boolean isNumber(String args) {
		boolean isnumber = true;
		if (ObjUtil.isEmpty(args)) {
			return false;
		}
		for (int i = 0; i < args.length(); i++) {
			if (Character.isDigit(args.charAt(i)) || args.charAt(i) == '.' || args.charAt(i) == '-') {
				continue;
			} else {
				isnumber = false;
			}
		}
		return isnumber;
	}

	/**
	 * <p>
	 * Description: [对数字的小数进行补位]
	 * <p>
	 * 
	 * @param temp
	 * @param Bits
	 * @param digit
	 * @return
	 * @author [冯贺亮]创建于[2007-12-3]
	 */
	public static String numberDigit(String source, int digit) {
		String temp = source;
		if (ObjUtil.isEmpty(temp) || !isNumber(temp) || digit < 0) {
			return temp;
		}
		if (temp.indexOf(".") != -1) {
			String[] numParts = temp.split("[.]");
			String digitPart = numParts[1];
			int digitLength = digitPart.length();
			if (digitLength > digit) {
				// return CalculateUtil.divideByBigDecimal(temp, "1", digit);
				return new BigDecimal(temp).divide(new BigDecimal(1), digit, BigDecimal.ROUND_HALF_UP).toString();
			} else if (digitLength < digit) {
				StringBuffer strtemp = new StringBuffer();
				strtemp.append(temp);
				for (int i = 1; i <= -(digitLength - digit); i++) {
					strtemp.append("0");
				}
				return strtemp.toString();
			} else {
				return temp;
			}
		} else {
			StringBuffer strtemp = new StringBuffer();
			strtemp.append(temp);
			if (digit != 0) {
				strtemp.append(".");
			}
			for (int i = 0; i < digit; i++) {
				strtemp.append("0");
			}
			return strtemp.toString();
		}
	}

	public static List<String> arrayToList(String[] args) {
		List<String> l = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			l.add(args[i]);
		}
		return l;
	}
	
	public static List<String> arrayToList(String[] args,int start) {
		List<String> l = new ArrayList<String>();
		for (int i = start; i < args.length; i++) {
			l.add(args[i]);
		}
		return l;
	}

}
