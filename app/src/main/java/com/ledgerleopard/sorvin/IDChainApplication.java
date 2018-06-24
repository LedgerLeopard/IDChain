package com.ledgerleopard.sorvin;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.ledgerleopard.sorvin.utils.SharedPreferenceStorage;

import io.fabric.sdk.android.Fabric;

public class IDChainApplication extends Application {

	private static final String BASE_URL = "BASE_URL";
	private static IDChainApplication appInstance;
	private final String TAG = IDChainApplication.class.getCanonicalName();

	public static IDChainApplication getAppInstance(){
		return appInstance;
	}

	public void setAttestationGetCredentialOffersUrl(String attestationGetCredentialOffersUrl) {
		SharedPreferenceStorage.getInstanse(this).add(BASE_URL, attestationGetCredentialOffersUrl);
	}

	public String getAttestationGetCredentialOffersUrl() {
		return SharedPreferenceStorage.getInstanse(this).get(BASE_URL, String.class, null);
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
