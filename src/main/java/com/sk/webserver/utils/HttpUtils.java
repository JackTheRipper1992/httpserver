package com.sk.webserver.utils;

import com.sk.webserver.http.parser.HttpRequestParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class.getName());
    private static final String datePattern = "EEE, dd MMM yyyy HH:mm:ss z";

    public static String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
            logger.error("Encoding not supported, ignored", ignored);
        }
        return decoded;
    }

    public static byte[] getBytes(String... strings) {
        int n = 0;
        for (String s : strings)
            n += s.length();
        byte[] dest = new byte[n];
        n = 0;
        for (String s : strings)
            for (int i = 0, len = s.length(); i < len; i++)
                dest[n++] = (byte)s.charAt(i);
        return dest;
    }

    public static String getServerTime() {
        return DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(ZoneOffset.UTC));
    }


    public static String formatDate(long lastModified) {
        final Date date = new Date(lastModified);
        Instant instant = date.toInstant();
        LocalDateTime ldt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();
        return DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ldt);
    }

    public static String trimRight(String s, char c) {
        int len = s.length() - 1;
        int end;
        for (end = len; end >= 0 && s.charAt(end) == c; end--);
        return end == len ? s : s.substring(0, end + 1);
    }

    /**
     *
     * @param path
     * @return parent of the path without trailing slash
     */
    public static String getParentPath(String path) {
        path = trimRight(path, '/'); // remove trailing slash
        int slash = path.lastIndexOf('/');
        return slash < 0 ? null : path.substring(0, slash);
    }

    public static Date getDate(String time) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(datePattern, Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            return df.parse(time);
        } catch (ParseException parseExcepton) {

        }
        return null;

    }

    public static boolean isMatching(boolean strong, String[] etags, String etag) {
        if (etag == null || strong && etag.startsWith("W/"))
            return false;
        for (String e : etags)
            if (e.equals("*") || (e.equals(etag) && !(strong && (e.startsWith("W/")))))
                return true;
        return false;
    }
}
