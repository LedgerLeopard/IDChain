package com.ledgerleopard.sorvin;

import android.app.Application;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class IDChainApplication extends Application {

	private static IDChainApplication appInstance;
	private final String TAG = IDChainApplication.class.getCanonicalName();

	public String attestationGetCredentialOffersUrl;

	public static IDChainApplication getAppInstance(){
		return appInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		appInstance = this;

		String internalPath = getFilesDir().getAbsolutePath();
		IndySDK.getInstance().setStoragePath(internalPath);
		Log.e(TAG, "Storage path = " + IndySDK.getInstance().getStoragePath());
	}
}
