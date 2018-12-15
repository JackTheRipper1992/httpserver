package com.sk.webserver.worker;

import java.io.IOException;
import java.net.Socket;

public interface Task {

    void run() throws IOException;
}
