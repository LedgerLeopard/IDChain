package com.ledgerleopard.sorvin.functionality.connections;

import android.content.Context;

import com.google.gson.Gson;
import com.ledgerleopard.sorvin.api.request.OnboadringRequest;
import com.ledgerleopard.sorvin.basemvp.IndyBaseModel;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ConnectionsModelImpl extends IndyBaseModel implements ConnectionsContract.Model {

    private final Gson gson;
    private final OkHttpClient httpClient;

    public ConnectionsModelImpl(Context context) {
        super(context);
        gson = new Gson();
        httpClient = new OkHttpClient();
    }

    @Override
    public void sendDIDback(String url, String token, OnboadringRequest requestBody, Callback callback) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(requestBody));
        Request request = new Request.Builder()
                .addHeader("Authorization", String.format("Bearer %s", token))
                .url(url)
                .put(body)
                .build();

        httpClient.newCall(request).enqueue(callback);
    }
}
