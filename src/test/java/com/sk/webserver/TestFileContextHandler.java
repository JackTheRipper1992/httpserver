package com.sk.webserver;


import com.sk.webserver.http.handlers.FileContextHandler;
import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.request.HttpRequestBuilder;
import com.sk.webserver.http.response.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class TestFileContextHandler {

    @Mock
    Socket socket;

    File dir ;
    FileContextHandler fileContextHandler ;


    @Before
    public void before() throws IOException {
        socket = Mockito.mock(Socket.class);
        dir = new File("/Users/saurabhkakar/Desktop");
        fileContextHandler = new FileContextHandler(dir);
        Map<String,String > headers = new HashMap<>();
        populateHeadersMap(headers);

    }

    private void populateHeadersMap(Map<String, String> headers) {

    }

    @Test
    public void testFetchingRootDirectory() throws IOException, URISyntaxException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);

        HttpRequest httpRequest = createHttpRequest("/");
        HttpResponse httpResponse = new HttpResponse(byteArrayOutputStream,httpRequest);
        fileContextHandler.execute(httpRequest, httpResponse);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        int n = in.available();
        byte[] bytes = new byte[n];
        in.read(bytes, 0, n);
        String s = new String(bytes, StandardCharsets.UTF_8); // Or any encoding.
        Assert.assertTrue(s.contains("200 OK"));

    }

    @Test
    public void testFileNotExists() throws IOException, URISyntaxException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);

        HttpRequest httpRequest = createHttpRequest("/nothingexists");
        HttpResponse httpResponse = new HttpResponse(byteArrayOutputStream,httpRequest);
        int status = fileContextHandler.execute(httpRequest, httpResponse);
        Assert.assertEquals(404,status);

    }


    @Test
    public void testSearchDirectoryAsFile() throws IOException, URISyntaxException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);

        HttpRequest httpRequest = createHttpRequest("/Geet%20DS-160_files");
        HttpResponse httpResponse = new HttpResponse(byteArrayOutputStream,httpRequest);
        fileContextHandler.execute(httpRequest, httpResponse);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        int n = in.available();
        byte[] bytes = new byte[n];
        in.read(bytes, 0, n);
        String s = new String(bytes, StandardCharsets.UTF_8); // Or any encoding.
        Assert.assertTrue(s.contains("301"));

    }

    @Test
    public void testLocationHeaderWhenDirectoryPathInvalid() throws IOException, URISyntaxException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);

        HttpRequest httpRequest = createHttpRequest("/Geet%20DS-160_files");
        HttpResponse httpResponse = new HttpResponse(byteArrayOutputStream,httpRequest);
        fileContextHandler.execute(httpRequest, httpResponse);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        int n = in.available();
        byte[] bytes = new byte[n];
        in.read(bytes, 0, n);
        String s = new String(bytes, StandardCharsets.UTF_8); // Or any encoding.
        Assert.assertTrue(s.contains("Location"));

    }
    private HttpRequest createHttpRequest(String path) throws URISyntaxException {

        return new HttpRequestBuilder().setProtocolVersion("HTTP1.1")
                .setUri(new URI(path))
                .create();
    }
}
