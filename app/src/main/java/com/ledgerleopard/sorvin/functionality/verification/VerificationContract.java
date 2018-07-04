package com.ledgerleopard.sorvin.functionality.verification;

import android.content.Intent;
import com.ledgerleopard.sorvin.basemvp.BaseContract;
import com.ledgerleopard.sorvin.basemvp.IndyBaseModelInterface;
import com.ledgerleopard.sorvin.functionality.connections.ConnectionsViewModel;
import okhttp3.Callback;

public interface VerificationContract {

	interface View extends BaseContract.IBaseView {

	}

	interface Presenter extends BaseContract.IBasePresenter<ConnectionsViewModel>{
		void startScanning();
		void onActivityResult(int requestCode, int resultCode, Intent data);
	}

	interface Model extends IndyBaseModelInterface {
		void sendDIDback(String url, String tokenHeader, String requestBody, Callback callback);
	}

}
