package com.sk.webserver.http.validator;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class HttpRequestValidator implements Validator {

    @Override
    public boolean validate(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {

        Map<String, String> requestHeaders = httpRequest.getHeaders();
        String version = httpRequest.getProtocolVersion();
        if (version.equals("HTTP/1.1")) {
            if (!requestHeaders.containsKey("Host")) {
                /**
                    A "host" without any trailing port information implies the default
                    port for the service requested. A client MUST include a Host header
                    field in all HTTP/1.1 request messages .
                    400 - Bad Request
                 */
                httpResponse.sendError(400, "Host header is missing");
                return false;
            }
            /**
             * If a client will wait for a 100 (Continue) response before
             sending the request body, it MUST send an Expect request-header
             field (section 14.20) with the "100-continue" expectation.
             */
            String expect = requestHeaders.get("Expect");
            if (expect != null) {
                if (expect.equalsIgnoreCase("100-continue")) {
                    HttpResponse tempResp = new HttpResponse(httpResponse.getResponseOutputStream(),httpRequest);
                    tempResp.sendHeaders(100);
                    httpResponse.getResponseOutputStream().flush();
                } else {
                    /**
                     * A server that does not understand or is unable to comply with any of
                     the expectation values in the Expect field of a request MUST respond
                     with appropriate error status. The server MUST respond with a 417
                     (Expectation Failed) status if any of the expectations cannot be met
                     or, if there are other problems with the request, some other 4xx
                     status.
                     */
                    httpResponse.sendError(417);
                    return false;
                }
            }
        }
        return true;
    }
}
