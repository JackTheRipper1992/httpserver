package com.sk.webserver.http.worker;

import java.io.IOException;

public interface Task {

    void run() throws IOException;
}
