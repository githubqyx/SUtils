package com.liyi.sutils.utils.time;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.liyi.sutils.utils.log.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 时间工具类
 */
public class TimeUtil {
    private static final String TAG = TimeUtil.class.getClass().getSimpleName();
    /* 默认的日期格式 */
    private static final String DEF_DATE_TYPE = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将时间戳转换为指定格式的时间字符串
     *
     * @param timeStamp 时间戳
     * @param dateType  日期类型
     * @return
     */
    public static String getTimeStr(long timeStamp, @NonNull String dateType) {
        if (TextUtils.isEmpty(dateType)) {
            dateType = DEF_DATE_TYPE;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateType);
        return sdf.format(new Date(timeStamp));
    }

    /**
     * 将时间字符串转换为时间戳
     *
     * @param timeStr
     * @param dateType
     * @return
     */
    public static long getTimeStamp(@NonNull String timeStr, String dateType) {
        if (TextUtils.isEmpty(dateType)) {
            dateType = DEF_DATE_TYPE;
        }
        long timeStamp = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(dateType);
        try {
            Date date = sdf.parse(timeStr);
            timeStamp = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            LogUtil.e(TAG, "Failure to execute the getTimeStamp () method ========> timeStr: " + timeStr);
        }
        return timeStamp;
    }

    /**
     * 计算时间差
     *
     * @return 返回毫秒
     */
    public static long caculateTimeDiff(@NonNull Object startTime, @NonNull Object endTime, String dateType) {
        if (TextUtils.isEmpty(dateType)) {
            dateType = DEF_DATE_TYPE;
        }
        long longStart, longEnd;
        if (startTime instanceof String) {
            longStart = getTimeStamp((String) startTime, dateType);
        } else if (startTime instanceof Long) {
            longStart = (long) startTime;
        } else {
            LogUtil.e(TAG, "The startTime format error in the getTimeDiffAsSecond () method ========> startTime: " + startTime);
            return -1;
        }
        if (endTime instanceof String) {
            longEnd = getTimeStamp((String) endTime, dateType);
        } else if (endTime instanceof Long) {
            longEnd = (long) endTime;
        } else {
            LogUtil.e(TAG, "The endTime format error in the getTimeDiffAsSecond () method ========> endTime: " + endTime);
            return -2;
        }
        return (longEnd - longStart);
    }

    /**
     * 计算时间差
     *
     * @return 返回天数、小时数、分钟数、秒数
     */
    public static int[] caculateTimeDiffArray(@NonNull Object startTime, @NonNull Object endTime, String dateType) {
        long longDiff = caculateTimeDiff(startTime, endTime, dateType) / 1000;
        int days = (int) (longDiff / (60 * 60 * 24));
        int hours = (int) ((longDiff - days * (60 * 60 * 24)) / (60 * 60));
        int minutes = (int) ((longDiff - days * (60 * 60 * 24) - hours * (60 * 60)) / 60);
        int seconds = (int) ((longDiff - days * (60 * 60 * 24) - hours * (60 * 60) - minutes * 60));
        return new int[]{days, hours, minutes, seconds};
    }
}