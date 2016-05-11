package com.iestudio.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: [处理日期的工具类]
 * </p>
 * <br>
 * <p>
 * Company: 
 * </p>
 * Neusoft Co,Ltd. Tax Department CopyRight 2007-2010
 * 
 * @author 冯贺亮 fenghl@neusoft.com
 * @version Revision: 1.0
 */
/*--------------------------------------------------------
 修改履历：
 2007-10-19 冯贺亮 创建
 ----------------------------------------------------------
 */
public class BaseDate {

    public static final Date nextYearFirstDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curYearLastDay(innerDate));
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();

    }

    public static final Date nextYearLastDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nextYearFirstDay(nextYearFirstDay(innerDate)));
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static final Date curYearFirstDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(innerDate);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static final Date curYearLastDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(innerDate);
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DATE, 31);
        return calendar.getTime();
    }

    public static final Date preYearFirstDay(Date innerDate) {
        return curYearFirstDay(preYearLastDay(innerDate));
    }

    public static final Date preYearLastDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curYearFirstDay(innerDate));
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static final Date nextMonthFirstDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curMonthFirstDay(innerDate));
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    public static final Date nextMonthLastDay(Date innerDate) {
        return curMonthLastDay(nextMonthFirstDay(innerDate));
    }

    public static final Date curMonthFirstDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(innerDate);
        calendar.set(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static final Date curMonthLastDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nextMonthFirstDay(innerDate));
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static final Date preMonthFirstDay(Date innerDate) {
        return curMonthFirstDay(preMonthLastDay(innerDate));
    }

    public static final Date preMonthLastDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curMonthFirstDay(innerDate));
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static final Date nextQuartarFirstDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curQuartarLastDay(innerDate));
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static final Date nextQuartarLastDay(Date innerDate) {
        return curQuartarLastDay(nextQuartarFirstDay(innerDate));
    }
    @SuppressWarnings("deprecation")
    public static final Date curQuartarFirstDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        int month = innerDate.getMonth() + 1;
        calendar.setTime(innerDate);
        if (month >= 1 && month <= 3) {
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DATE, 1);
        }
        else if (month >= 4 && month <= 6) {
            calendar.set(Calendar.MONTH, 3);
            calendar.set(Calendar.DATE, 1);
        }
        else if (month >= 7 && month <= 9) {
            calendar.set(Calendar.MONTH, 6);
            calendar.set(Calendar.DATE, 1);
        }
        else if (month >= 10 && month <= 12) {
            calendar.set(Calendar.MONTH, 9);
            calendar.set(Calendar.DATE, 1);
        }
        return calendar.getTime();
    }
    @SuppressWarnings("deprecation")
    public static final Date curQuartarLastDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        int month = innerDate.getMonth() + 1;
        calendar.setTime(innerDate);
        if (month >= 1 && month <= 3) {
            calendar.set(Calendar.MONTH, 2);
            calendar.set(Calendar.DATE, 31);
        }
        else if (month >= 4 && month <= 6) {
            calendar.set(Calendar.MONTH, 5);
            calendar.set(Calendar.DATE, 30);
        }
        else if (month >= 7 && month <= 9) {
            calendar.set(Calendar.MONTH, 8);
            calendar.set(Calendar.DATE, 30);
        }
        else if (month >= 10 && month <= 12) {
            calendar.set(Calendar.MONTH, 11);
            calendar.set(Calendar.DATE, 31);
        }
        return calendar.getTime();
    }

    public static final Date preQuartarFirstDay(Date innerDate) {
        return curQuartarFirstDay(preQuartarLastDay(innerDate));
    }

    public static final Date preQuartarLastDay(Date innerDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curQuartarFirstDay(innerDate));
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public static final Date getNextYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, 1);
        c.setTime(c.getTime());
        c.add(Calendar.DATE, -1);
        return c.getTime();
    }

    public static final Date getPreYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, -1);
        c.setTime(c.getTime());
        c.add(Calendar.DATE, 1);
        return c.getTime();
    }

    /**
     *
     *
     *isLeapYear = function (innerDate):判断是否为闰年
     *
     */

    /**
     * <p>
     * Discription:将传入的YYYY-MM-DD形式的字符串转换为Date类型，如果传入字符串为空则返回null
     * </p>
     * 
     * @param strDate
     * @return
     * @author:[冯贺亮]
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    public static Date parseDate(String strDate) {
        if(strDate!=null&&!"".equalsIgnoreCase(strDate)&&strDate.length()>0){
            return parseDate(strDate, 3);
        }
        else {
            return null;
        }
    }

    /**
     * 将String类型数据转化成Date数据类型
     * @param dateStr
     * @param index
     * 其中index表示dateStr类型格式:<br>
     * 0: "yyyy-MM-dd HH:mm:ss"<br>
     * 1: "yyyy/MM/dd HH:mm:ss"<br>
     * 2: "yyyy年MM月dd日HH时mm分ss秒"<br>
     * 3: "yyyy-MM-dd"<br>
     * 4: "yyyy/MM/dd"<br>
     * 5: "yy-MM-dd"<br>
     * 6: "yy/MM/dd"<br>
     * 7: "yyyy年MM月dd日"<br>
     * 8: "HH:mm:ss"<br>
     * 9: "yyyyMMddHHmmss"<br>
     * 10: "yyyyMMdd"<br>
     * 11: "yyyy.MM.dd"<br>
     * 12: "yy.MM.dd"<br>
     * @return
     */
    public static Date parseDate(String dateStr, int index) {
        DateFormat df = null;
        df = new SimpleDateFormat(dateFormat[index]);
        try {
            return df.parse(dateStr);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Date类型数据转化成String数据类型
     * @param date
     * @param index
     * 其中index表示Date类型格式:<br>
     * 0: "yyyy-MM-dd HH:mm:ss"<br>
     * 1: "yyyy/MM/dd HH:mm:ss"<br>
     * 2: "yyyy年MM月dd日HH时mm分ss秒"<br>
     * 3: "yyyy-MM-dd"<br>
     * 4: "yyyy/MM/dd"<br>
     * 5: "yy-MM-dd"<br>
     * 6: "yy/MM/dd"<br>
     * 7: "yyyy年MM月dd日"<br>
     * 8: "HH:mm:ss"<br>
     * 9: "yyyyMMddHHmmss"<br>
     * 10: "yyyyMMdd"<br>
     * 11: "yyyy.MM.dd"<br>
     * 12: "yy.MM.dd"<br>
     * @return
     */
    public static String parseDate(Date date, int index) {
        DateFormat df = null;
        df = new SimpleDateFormat(dateFormat[index]);
        return df.format(date);
    }

    private static String dateFormat[] = { "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss", "yyyy年MM月dd日HH时mm分ss秒", "yyyy-MM-dd",
            "yyyy/MM/dd", "yy-MM-dd", "yy/MM/dd", "yyyy年MM月dd日", "HH:mm:ss",
            "yyyyMMddHHmmss", "yyyyMMdd", "yyyy.MM.dd", "yy.MM.dd" };

    /**
     * <p>
     * Discription:返回相差年数
     * </p>
     */
    public final static String TYPE_YEAR = "year";

    /**
     * <p>
     * Discription:返回相差月份
     * </p>
     */
    public final static String TYPE_MONTH = "month";

    /**
     * <p>
     * Discription:返回相差天数
     * </p>
     */
    public final static String TYPE_DAY = "day";

    /**
     * <p>
     * Discription:返回相差周数
     * </p>
     */
    public final static String TYPE_WEEK = "week";

    /**
     * 
     * <p>
     * Discription:计算两个日期间的相差的天数、周数、月份、年数
     * </p>
     * 
     * @param strDate
     *            开始时间
     * @param endDate
     *            结束时间
     * @param format
     *            传入日期的格式
     * @param type
     *            返回类型 day :相差日期 week：相差星期 month:相差月份 year：相差年数 不区分大小写
     * @return int 如果传入type类型不正确返回0
     * @throws ParseException
     *             String日期转date型日期 需要用到SimpleDateFormat.parse 可能会出现异常
     * @author:冯贺亮
     * @update:日期YYYY-MM-DD 更改人姓名 变更描述
     */
    public static int getDateDifference(Object strDate, Object endDate,
            String format, String type) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Date date1 = null;
        Date date2 = null;
        if (strDate instanceof String) {
            date1 = df.parse((String) strDate);
        }
        else if (strDate instanceof Date) {
            date1 = (Date) strDate;
        }
        else {
            return 0;
        }
        if (endDate instanceof String) {
            date2 = df.parse((String) endDate);
        }
        else if (endDate instanceof Date) {
            date2 = (Date) endDate;
        }
        else {
            return 0;
        }

        Calendar cal1 = null;
        Calendar cal2 = null;

        cal1 = Calendar.getInstance();
        cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        long ldate1 = date1.getTime() + cal1.get(Calendar.ZONE_OFFSET)
                + cal1.get(Calendar.DST_OFFSET);

        long ldate2 = date2.getTime() + cal2.get(Calendar.ZONE_OFFSET)
                + cal2.get(Calendar.DST_OFFSET);

        int hr1 = (int) (ldate1 / (60 * 60 * 1000));
        int hr2 = (int) (ldate2 / (60 * 60 * 1000));
        int days1 = (int) hr1 / 24;
        int days2 = (int) hr2 / 24;

        int dateDiff = days2 - days1;
        int weekOffset = (cal2.get(Calendar.DAY_OF_WEEK) - cal1
                .get(Calendar.DAY_OF_WEEK)) < 0 ? 1 : 0;
        int weekDiff = dateDiff / 7 + weekOffset;
        int yearDiff = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
        int monthDiff = yearDiff * 12 + cal2.get(Calendar.MONTH)
                - cal1.get(Calendar.MONTH);

        if ("day".equalsIgnoreCase(type)) {
            return dateDiff;
        }
        else if ("week".equalsIgnoreCase(type)) {
            return weekDiff;
        }
        else if ("month".equalsIgnoreCase(type)) {
            return monthDiff;
        }
        else if ("year".equalsIgnoreCase(type)) {
            return yearDiff;
        }
        else {
            return 0;
        }
    }
}
