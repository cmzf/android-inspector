package com.github.cmzf.androidinspector;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.AsyncHttpServerRouter;

import java.io.IOException;

public class InspectorServer {
    private static InspectorServer instance;
    private AsyncServer asyncServer = new AsyncServer();
    private AsyncHttpServer httpServer = new AsyncHttpServer();
    private Context mAppContext;

    private InspectorServer() {
    }

    public static InspectorServer getInstance() {
        if (instance == null) {
            instance = new InspectorServer();
        }
        return instance;
    }

    public void startServer(Context context) {
        startServer(context, 8080);
    }

    public void stop() {
        httpServer.stop();
        asyncServer.stop();
    }

    private void apiTree(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.setContentType("application/json");
        response.getHeaders().set("Access-Control-Allow-Origin", "*");
        response.send(JSON.toJSONString(AccessibilityService.getInstance().getRootUiObject().uiTree()));
        response.end();
    }

    private void apiScreen(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        response.getHeaders().set("Access-Control-Allow-Origin", "*");
        response.send("image/jpg", ScreenCaptureService.getInstance().getScreenImage());
        response.end();
    }

    public void startServer(Context context, int port) {
        stop();
        mAppContext = context;
        httpServer.get("/api/tree", this::apiTree);
        httpServer.get("/api/screen", this::apiScreen);
        httpServer.get("/.*", this::assetLoader);
        httpServer.listen(asyncServer, port);
    }

    private void assetLoader(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        AsyncHttpServerRouter.Asset asset = AsyncHttpServer.getAssetStream(mAppContext, request.getPath().substring(1));
        if (asset != null) {
            response.sendStream(asset.inputStream, asset.available);
        }
        else {
            response.code(404);
        }
        response.end();
    }

}
