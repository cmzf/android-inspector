package com.github.cmzf.androidinspector;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;

public class InspectorServer {
    private static InspectorServer instance;
    private AsyncServer asyncServer = new AsyncServer();
    private AsyncHttpServer httpServer = new AsyncHttpServer();

    private InspectorServer() {
    }

    public static InspectorServer getInstance() {
        if (instance == null) {
            instance = new InspectorServer();
        }
        return instance;
    }

    public void start() {
        start(8080);
    }

    public void stop() {
        httpServer.stop();
        asyncServer.stop();
    }

    private void pageIndex(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.send("Hello, World!");
    }

    public void start(Integer port) {
        stop();
        httpServer.get("/", this::pageIndex);
        httpServer.listen(asyncServer, port);
    }

}
