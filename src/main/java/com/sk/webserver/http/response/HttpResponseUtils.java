package com.sk.webserver.http.response;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpResponseUtils {

    /** The HTTP status description strings. */
    protected static final String[] statuses = new String[600];

    static {
        // initialize status descriptions lookup table
        Arrays.fill(statuses, "Unknown Status");
        statuses[100] = "Continue";
        statuses[200] = "OK";
        statuses[204] = "No Content";
        statuses[206] = "Partial Content";
        statuses[301] = "Moved Permanently";
        statuses[302] = "Found";
        statuses[304] = "Not Modified";
        statuses[307] = "Temporary Redirect";
        statuses[400] = "Bad Request";
        statuses[401] = "Unauthorized";
        statuses[403] = "Forbidden";
        statuses[404] = "Not Found";
        statuses[405] = "Method Not Allowed";
        statuses[408] = "Request Timeout";
        statuses[412] = "Precondition Failed";
        statuses[413] = "Request Entity Too Large";
        statuses[414] = "Request-URI Too Large";
        statuses[416] = "Requested Range Not Satisfiable";
        statuses[417] = "Expectation Failed";
        statuses[500] = "Internal Server Error";
        statuses[501] = "Not Implemented";
        statuses[502] = "Bad Gateway";
        statuses[503] = "Service Unavailable";
        statuses[504] = "Gateway Time-out";
    }

    protected static final Map<String, String> contentTypes =
            new ConcurrentHashMap<String, String>();

    static {
        // add some default common content types
        // see http://www.iana.org/assignments/media-types/ for full list
        addContentType("application/font-woff", "woff");
        addContentType("application/font-woff2", "woff2");
        addContentType("application/java-archive", "jar");
        addContentType("application/javascript", "js");
        addContentType("application/json", "json");
        addContentType("application/octet-stream", "exe");
        addContentType("application/pdf", "pdf");
        addContentType("application/x-7z-compressed", "7z");
        addContentType("application/x-compressed", "tgz");
        addContentType("application/x-gzip", "gz");
        addContentType("application/x-tar", "tar");
        addContentType("application/xhtml+xml", "xhtml");
        addContentType("application/zip", "zip");
        addContentType("audio/mpeg", "mp3");
        addContentType("image/gif", "gif");
        addContentType("image/jpeg", "jpg", "jpeg");
        addContentType("image/png", "png");
        addContentType("image/svg+xml", "svg");
        addContentType("image/x-icon", "ico");
        addContentType("text/css", "css");
        addContentType("text/csv", "csv");
        addContentType("text/html; charset=utf-8", "htm", "html");
        addContentType("text/plain", "txt", "text", "log");
        addContentType("text/xml", "xml");
    }

    public static void addContentType(String contentType, String... suffixes) {
        for (String suffix : suffixes)
            contentTypes.put(suffix.toLowerCase(Locale.US), contentType.toLowerCase(Locale.US));
    }

    /**
     * Returns an HTML-escaped version of the given string for safe display
     * within a web page. The characters '&amp;', '&gt;' and '&lt;' must always
     * be escaped, and single and double quotes must be escaped within
     * attribute values; this method escapes them always. This method can
     * be used for generating both HTML and XHTML valid content.
     *
     * @param s the string to escape
     * @return the escaped string
     * @see <a href="http://www.w3.org/International/questions/qa-escapes">The W3C FAQ</a>
     */
    public static String escapeHTML(String s) {
        int len = s.length();
        StringBuilder sb = new StringBuilder(len + 30);
        int start = 0;
        for (int i = 0; i < len; i++) {
            String ref = null;
            switch (s.charAt(i)) {
                case '&': ref = "&amp;"; break;
                case '>': ref = "&gt;"; break;
                case '<': ref = "&lt;"; break;
                case '"': ref = "&quot;"; break;
                case '\'': ref = "&#39;"; break;
            }
            if (ref != null) {
                sb.append(s.substring(start, i)).append(ref);
                start = i + 1;
            }
        }
        return start == 0 ? s : sb.append(s.substring(start)).toString();
    }

    public static String getContentType(String path, String def) {
        int dot = path.lastIndexOf('.');
        String type = dot < 0 ? def : contentTypes.get(path.substring(dot + 1).toLowerCase(Locale.US));
        return type != null ? type : def;
    }
}
