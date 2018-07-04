package com.ledgerleopard.sorvin.functionality.attestation;

import android.content.Context;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.BaseApiCallback;
import com.ledgerleopard.sorvin.api.response.BaseResponse;
import com.ledgerleopard.sorvin.api.response.GetCredentialOfferResponse;
import com.ledgerleopard.sorvin.basemvp.IndyBaseModel;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.CompletableFuture;

public class AttestationModelImpl extends IndyBaseModel implements AttestationContract.Model {

    private OkHttpClient httpClient;

    public AttestationModelImpl(Context context) {
        super(context);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(httpLoggingInterceptor);
        httpClient = builder.build();
    }


    @Override
    public void getCredentialOffers(String url, String authDid, BaseApiCallback<GetCredentialOfferResponse> callback) {
        Request request = new Request.Builder()
                .addHeader("Authorization", String.format("DID %s", authDid))
                .addHeader("Content-Type", "text/plain")
                .addHeader("Accept", "text/plain")
                .url(url+"/credentials")
                .get()
                .build();

        httpClient.newCall(request).enqueue(callback);
    }

    @Override
    public void sendCredentialOfferRequest( String url, String authDid, String encryptedBody, BaseApiCallback<BaseResponse> callback) {
	    RequestBody body = RequestBody.create(MediaType.parse("application/json"), encryptedBody);
    	Request request = new Request.Builder()
		    .addHeader("Authorization", String.format("DID %s", authDid))
		    .url(url+"/credentials")
		    .post(body)
		    .build();
	    httpClient.newCall(request).enqueue(callback);
    }

    @Override
    public CompletableFuture<ConnectionItem> getGovernmentMyDid() {
        return IndySDK.getInstance().getGovernmentMyDid();
    }
}
