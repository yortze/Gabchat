package com.gabon.gabchat.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static String formatMessageTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatConversationTime(long timestamp) {
        Calendar msgCal = Calendar.getInstance();
        msgCal.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();

        if (isSameDay(msgCal, now)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        } else if (isYesterday(msgCal, now)) {
            return "Hier";
        } else if (isSameWeek(msgCal, now)) {
            return new SimpleDateFormat("EEE", Locale.FRENCH).format(new Date(timestamp));
        } else {
            return new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date(timestamp));
        }
    }

    public static String getLastSeenText(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60_000) return "à l'instant";
        if (diff < 3_600_000) return "il y a " + (diff / 60_000) + " min";
        if (diff < 86_400_000) return "il y a " + (diff / 3_600_000) + "h";
        return new SimpleDateFormat("dd/MM/yy à HH:mm", Locale.FRENCH).format(new Date(timestamp));
    }

    private static boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isYesterday(Calendar c1, Calendar c2) {
        Calendar yesterday = (Calendar) c2.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(c1, yesterday);
    }

    private static boolean isSameWeek(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR);
    }
}
