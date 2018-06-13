package com.ledgerleopard.sorvin.functionality.connections;

import android.content.Context;
import com.google.gson.Gson;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.request.OnboadringRequest;
import com.ledgerleopard.sorvin.basemvp.BaseModel;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import okhttp3.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ConnectionsModelImpl extends BaseModel implements ConnectionsContract.Model {

	private final Gson gson;
	private final OkHttpClient httpClient;

	public ConnectionsModelImpl(Context context) {
		super(context);
		gson = new Gson();
		httpClient = new OkHttpClient();
	}

	@Override
	public CompletableFuture<Void> initializeWallet() {
		return CompletableFuture.runAsync(() -> {
			if ( !IndySDK.getInstance().isWalletExist() ){
				IndySDK.getInstance().createAndOpenWallet();
			}

		});
	}

	@Override
	public void createAndStoreDidAndConnectWithForeignDid( String foreignDid, IndySDK.IndyCallback callback ) {
		IndySDK.getInstance().createStoreMyDidAndConnectWithForeignDid(foreignDid, callback);
	}

	@Override
	public void sendDIDback(String url, OnboadringRequest requestBody, Callback callback) {
		RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(requestBody));
		Request request = new Request.Builder()
			.url(url)
			.put(body)
			.build();

		httpClient.newCall(request).enqueue(callback);
	}

	@Override
	public void getConnectionsList( IndySDK.IndyCallback<List<ConnectionItem>> callback ) {
		IndySDK.getInstance().getConnectionsList(callback);
	}
}
