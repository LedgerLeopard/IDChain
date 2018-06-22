package com.ledgerleopard.sorvin.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Response;

public abstract class BaseApiCallback<RETURN_TYPE> implements okhttp3.Callback {

    @Override
    public void onFailure(Call call, IOException e) {
        onFailure(e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
            onFailure(response.body().string());
        } else {
            Type collectionType = new TypeToken<RETURN_TYPE>(){}.getType();
            RETURN_TYPE res = new Gson().fromJson(response.body().string(), collectionType);
            onSuccess(res);
        }
    }

    public abstract void onSuccess(RETURN_TYPE response);
    public abstract void onFailure(String message);
}
