package com.mmall.util;




import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by Summer on 2017/7/6.
 * desc：时间转换工具类
 */
public class DateTimeUtil {

    //使用jota-time开源包

    /*
    主要是两种：1.Date--->Str
    2.Str---->Date
     */


    public static final String STANDARD_FORMATE="yyyy-MM-dd HH:mm:ss";

    public static Date strToDate(String dateTimeStr,String formateStr){

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formateStr);

        DateTime dateTime =dateTimeFormatter.parseDateTime(dateTimeStr);

        return dateTime.toDate();
    }




    public static String dateToStr(Date date,String formateStr){
        if (date == null){
            return StringUtils.EMPTY;
        }

        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formateStr);
    }





    public static Date strToDate(String dateTimeStr){

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMATE);

        DateTime dateTime =dateTimeFormatter.parseDateTime(dateTimeStr);

        return dateTime.toDate();
    }




    public static String dateToStr(Date date){
        if (date == null){
            return StringUtils.EMPTY;
        }

        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMATE);
    }

}
