package com.example.todotask.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    public static long convertToMillis(String timeString, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            Date date = sdf.parse(timeString);
            return date.getTime(); // returns the timestamp in milliseconds
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
