package com.ledgerleopard.sorvin.api;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.ledgerleopard.sorvin.IndySDK;
import okhttp3.Call;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.ExecutionException;

public abstract class BaseApiCallback<RETURN_TYPE> implements okhttp3.Callback {

    private Class<RETURN_TYPE> return_typeClass;

    private String decryptVerKey;

    public BaseApiCallback( String decryptVerKey) {
        this.decryptVerKey = decryptVerKey;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        onFailure(e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
            onFailure(response.body().string());
        } else {


            String bodyString = null;
            if (!TextUtils.isEmpty(decryptVerKey)) {
                try {
                    byte[] decryptedContent = IndySDK.getInstance().decryptAuth(decryptVerKey, response.body().bytes()).get();
                    bodyString = new String(decryptedContent, "utf-8");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                bodyString = response.body().string();
            }

            Class<RETURN_TYPE> return_typeClass = (Class<RETURN_TYPE>)
                    ((ParameterizedType) getClass()
                            .getGenericSuperclass())
                            .getActualTypeArguments()[0];

            RETURN_TYPE res = new Gson().fromJson(bodyString, return_typeClass);
	        String finalBodyString = bodyString;
	        new Handler(Looper.getMainLooper()).post(() -> onSuccess(res, finalBodyString));


	        StringBuilder logString = new StringBuilder()
		        .append(call.request().method())
		        .append(" ")
		        .append(call.request().url().toString())
		        .append("\n")
		        .append(finalBodyString);

	        Log.e("HTTP", logString.toString());

        }
    }

    public abstract void onSuccess(RETURN_TYPE response, String rawResponse);
    public abstract void onFailure(String message);
}
