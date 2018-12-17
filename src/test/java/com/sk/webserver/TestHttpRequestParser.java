package com.sk.webserver;

import com.sk.webserver.http.parser.HttpRequestParser;
import com.sk.webserver.http.request.HttpMethod;
import com.sk.webserver.http.request.HttpRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

import static org.mockito.Mockito.when;

public class TestHttpRequestParser {

    Socket socket;

    @Before
    public void before(){
        socket = Mockito.mock(Socket.class);
    }
    @Test
    public void testFirstRequestLine() throws URISyntaxException, IOException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("request.txt");
        HttpRequestParser httpRequestParser = new HttpRequestParser(socket);
        when(socket.getInputStream()).thenReturn(inputStream);
        HttpRequest httpRequest = httpRequestParser.parse();
        Assert.assertEquals(httpRequest.getMethod(), HttpMethod.GET);

    }

    @Test
    public void testHeaders() throws URISyntaxException, IOException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("headers.txt");
        HttpRequestParser httpRequestParser = new HttpRequestParser(socket);
        when(socket.getInputStream()).thenReturn(inputStream);
        HttpRequest httpRequest = httpRequestParser.parse();
        Assert.assertEquals(httpRequest.getHeaders().size(), 10);

    }

    @Test(expected = IOException.class)
    public void testInvalidHeaders() throws URISyntaxException, IOException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("invalid_request.txt");
        HttpRequestParser httpRequestParser = new HttpRequestParser(socket);
        when(socket.getInputStream()).thenReturn(inputStream);
        httpRequestParser.parse();

    }
}
