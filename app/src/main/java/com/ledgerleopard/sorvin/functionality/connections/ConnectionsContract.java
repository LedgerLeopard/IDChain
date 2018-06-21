package com.ledgerleopard.sorvin.functionality.connections;

import android.content.Intent;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.request.OnboadringRequest;
import com.ledgerleopard.sorvin.basemvp.BaseContract;
import com.ledgerleopard.sorvin.basemvp.IndyBaseModelInterface;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import okhttp3.Callback;

import java.util.List;

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

	interface Model extends IndyBaseModelInterface {
		void createAndStoreDidAndConnectWithForeignDid(String foreignDid, IndySDK.IndyCallback callback);
		void sendDIDback(String url, OnboadringRequest requestBody, Callback callback);
	}
}
