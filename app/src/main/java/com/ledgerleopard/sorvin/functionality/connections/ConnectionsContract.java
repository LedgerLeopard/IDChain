package com.ledgerleopard.sorvin.functionality.connections;

import android.content.Intent;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.request.OnboadringRequest;
import com.ledgerleopard.sorvin.basemvp.BaseContract;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import okhttp3.Callback;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ConnectionsContract {


	interface View extends BaseContract.IBaseView {
		void showConnectionsList(List<ConnectionItem> content);
		void showHideNoConnectionsError( boolean visible );
		void gotoAddConnection();
	}

	interface Presenter extends BaseContract.IBasePresenter<ConnectionsViewModel>{
		void onConnectionClicked( int position );
		void onAddClicked();
		void onActivityResult(int requestCode, int resultCode, Intent data);
	}

	interface Model extends BaseContract.IBaseModel {
		CompletableFuture<Void> initializeWallet();
		void createAndStoreDidAndConnectWithForeignDid(String foreignDid, IndySDK.IndyCallback callback);
		void sendDIDback(String url, OnboadringRequest requestBody, Callback callback);
		void getConnectionsList( IndySDK.IndyCallback<List<ConnectionItem>> callback );
	}
}
