package com.ledgerleopard.sorvin.functionality.attestation;

import android.content.Context;

import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.BaseApiCallback;
import com.ledgerleopard.sorvin.api.response.GetCredentialOfferResponse;
import com.ledgerleopard.sorvin.basemvp.IndyBaseModel;
import com.ledgerleopard.sorvin.model.ConnectionItem;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AttestationModelImpl extends IndyBaseModel implements AttestationContract.Model {

    private OkHttpClient httpClient;

    public AttestationModelImpl(Context context) {
        super(context);
        httpClient = new OkHttpClient();
    }


    @Override
    public void getCredentialOffers(String url, String authDid, BaseApiCallback<GetCredentialOfferResponse> callback) {
        Request request = new Request.Builder()
                .addHeader("Authorization", String.format("DID %s", authDid))
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(callback);
    }

    @Override
    public void sendCredentialOfferRequest() {

    }

    @Override
    public void getGovernmentMyDid( IndySDK.IndyCallback<ConnectionItem> callback ) {
        IndySDK.getInstance().getGovernmentMyDid(callback);
    }
}
