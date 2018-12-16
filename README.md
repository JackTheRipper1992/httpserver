# sk_httpserver

This is a HTTP 1.1 compliant server capable of serving files and folders from the base directory provided at the time of the start up of the server. The server can lists the files if URI is a directory or it will display the file if resource in given URI is of Content-Type is text/html.

Usage : java <<directory>> <<port>> <<Optional true|false>> com.sk.webserver.main.Main
  
  E.g:
        java "/Users/saurabhkakar/Desktop" 8080 com.sk.webserver.main.Main

You can also pass the optional 3rd argument that will enable the health check for the server by adding the scheduler thread. Health Check scheduler thread periodically sends the health check request to the server using /healthCheck uri.

As the main purpose of this server is to fetch the file/directory from the hosted root, server is configured to only cater HTTP GET and OPTIONS requests. You can check the server OPTIONS by sending OPTIONS http request /* .


# Improvements :

1. We can add security for URIs (files) exposed by our sever by either using some open source framework OAuth etc or by building in house platform to secure the APIs.
2. We can make our server reactive by implementing Reactive Programming such as RxJava. 
3. Scope of test coverage needs to be improved.
4. Add the support for other HTTP methods.


# Refernces :

https://www.ietf.org/rfc/rfc2616.txt
