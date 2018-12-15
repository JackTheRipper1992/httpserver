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
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class.getName());

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
}
