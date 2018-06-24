package com.ledgerleopard.sorvin.functionality.connections;

import android.content.Context;

import com.ledgerleopard.sorvin.basemvp.IndyBaseModel;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class ConnectionsModelImpl extends IndyBaseModel implements ConnectionsContract.Model {

    private final OkHttpClient httpClient;

    public ConnectionsModelImpl(Context context) {
        super(context);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor( new HttpLoggingInterceptor());
        httpClient = builder.build();
    }

    @Override
    public void sendDIDback(String url, String token, byte[] requestBody, Callback callback) {
        RequestBody body = RequestBody.create(MediaType.parse("application/octa-stream"), requestBody);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        httpClient.newCall(request).enqueue(callback);
    }
}
