package com.sk.webserver.http.handlers;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;
import com.sk.webserver.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import static com.sk.webserver.http.response.HttpResponseUtils.getContentType;
import static com.sk.webserver.utils.HttpUtils.formatDate;
import static com.sk.webserver.utils.HttpUtils.getDate;
import static com.sk.webserver.utils.HttpUtils.isMatching;

public class FileContextHandler implements Handler {

    public static final int PRECONDITION_FAILED = Status.PRECONDITION_FAILED.getRequestStatus();
    public static final int NOT_MODIFIED = Status.NOT_MODIFIED.getRequestStatus();
    protected final File root ;
    public FileContextHandler(File dir) throws IOException {
        this.root = dir.getCanonicalFile();
    }

    @Override
    public int execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        return getFile(root, httpRequest.getUri().getPath(), httpRequest, httpResponse);
    }

    private int getFile(File root, String uri, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        String relativePath = uri;
        File file = new File(root, relativePath).getCanonicalFile();
        if (!file.exists() || file.isHidden() || file.getName().startsWith(".")) {
            return 404;
        } else if (!file.canRead() || !file.getPath().startsWith(root.getPath())) { // validate
            return 403;
        } else if (file.isDirectory()) {
            if (relativePath.endsWith("/")) {

                httpResponse.send(200, "Success");
            } else { // redirect to the normalized directory URL ending with '/'
                httpResponse.redirect(httpRequest.getUrl() + httpRequest.getPath() + "/");
            }
        } else if (relativePath.endsWith("/")) {
            return 404; // non-directory ending with slash (File constructor removed it)
        } else {
            serveFileContent(file, httpRequest, httpResponse);
        }
        return 0;
    }

    /**
     * Serves the contents of a file, with its corresponding content type,
     * last modification time, etc. conditional and partial retrievals are
     * handled according to the RFC.
     *
     * @param file the existing and readable file whose contents are served
     * @param httpRequest the request
     * @param httpResponse the response into which the content is written
     * @throws IOException if an error occurs
     */
    public static void serveFileContent(File file, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        long len = file.length();
        long lastModified = file.lastModified();
        String etag = "W/\"" + lastModified + "\""; // a weak tag based on date
        int status = 200;

        // send the response
        Map<String, String> headers = httpResponse.getHeaders();
        status = checkConditionalHeaders(httpRequest,lastModified,etag);
        switch (status) {
            case 304: // no other headers or body allowed
                httpResponse.getHeaders().put("ETag", etag);
                httpResponse.getHeaders().put("Vary", "Accept-Encoding");
                httpResponse.getHeaders().put("Last-Modified", formatDate(lastModified));
                httpResponse.sendHeaders(304);
                break;
            case 412:
                httpResponse.sendHeaders(412);
                break;
            case 200:
                // send OK response
                httpResponse.sendHeaders(200, len, lastModified, etag,
                        getContentType(file.getName(), "application/octet-stream"));
                // send body
                InputStream in = new FileInputStream(file);
                try {
                    httpResponse.sendBody(in, len);
                } finally {
                    in.close();
                }
                break;
            default:
                httpResponse.sendHeaders(500); // should never happen
                break;
        }
    }

    /**
     * When an origin server generates a full response, it attaches some sort of validator to it,
     * which is kept with the cache entry. When a client (user agent or
     * proxy cache) makes a conditional request for a resource for which it
     * has a cache entry, it includes the associated validator in the
     * request.
     *
     * The server then checks that validator against the current validator
     * for the entity, and, if they match (see section 13.3.3), it responds
     * with a special status code (usually, 304 (Not Modified)) and no
     * entity-body. Otherwise, it returns a full response (including
     * entity-body).
     *
     * A Last-Modified time, when used as a validator in a request, is
     * implicitly weak
     * @param httpRequest the request
     * @param lastModified the resource's last modified time
     * @param etag the resource's ETag
     * @return the appropriate response status for the request
     */

    public static int checkConditionalHeaders(final HttpRequest httpRequest,
                                              final long lastModified,
                                              final String etag) {
        Map<String, String> headers = httpRequest.getHeaders();
        // If-Match
        String ifMatchHeader = headers.get("If-Match");
        String[] etags = ifMatchHeader.split(",");

        if (ifMatchHeader != null && !isMatching(true, etags, etag))
            return PRECONDITION_FAILED;
        // If-Unmodified-Since
        /**
         * The If-Unmodified-Since request-header field is used with a method to
         make it conditional. If the requested resource has not been modified
         since the time specified in this field, the server SHOULD perform the
         requested operation as if the If-Unmodified-Since header were not
         present.

         If the requested variant has been modified since the specified time,
         the server MUST NOT perform the requested operation, and MUST return
         a 412 (Precondition Failed).
         */
        String ifUnmodifiedSinceStr = headers.get("If-Unmodified-Since");
        Date ifUnmodifiedSince = getDate(ifUnmodifiedSinceStr);
        if (ifUnmodifiedSince != null && lastModified > ifUnmodifiedSince.getTime())
            return PRECONDITION_FAILED;
        // If-Modified-Since
        int status = Status.OK.getRequestStatus();
        boolean force = false;
        /**
         * The If-Modified-Since request-header field is used with a method to
         make it conditional: if the requested variant has not been modified
         since the time specified in this field, an entity will not be
         returned from the server; instead, a 304 (not modified) response will
         be returned without any message-body.
         */
        String ifModifiedSinceStr = headers.get("If-Modified-Since");
        Date ifModifiedSince = getDate(ifModifiedSinceStr);
        if (ifModifiedSince != null && ifModifiedSince.getTime() <= System.currentTimeMillis()) {
            if (lastModified > ifModifiedSince.getTime())
                force = true;
            else
                status = NOT_MODIFIED;
        }
        // If-None-Match
        String ifNoneMatchHeader = headers.get("If-None-Match");
        String[] none = ifNoneMatchHeader.split(",");
        if (ifNoneMatchHeader != null) {
            if (isMatching(false, none, etag)) // RFC7232#3.2: use weak matching
                status = httpRequest.getMethod().equals("GET") ? NOT_MODIFIED : PRECONDITION_FAILED;
            else
                force = true;
        }
        return force ? Status.OK.getRequestStatus() : status;
    }


}
