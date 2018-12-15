package com.sk.webserver.http.handlers;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.sk.webserver.http.response.HttpResponseUtils.getContentType;

public class FileContextHandler implements Handler {

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
        switch (status) {

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
}
