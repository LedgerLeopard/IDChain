package com.ledgerleopard.sorvin.functionality.login;

import com.ledgerleopard.sorvin.basemvp.BaseContract;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;
import ru.bscmsk.fingerly.interfaces.UnionTouchIdCallback;

public interface LoginContract {

	interface ILoginView extends BaseContract.IBaseView {
		void showTouchDialog();
		void hideTouchDialog();
		void gotoMain();
	}

	interface ILoginPresenter extends BaseContract.IBasePresenter<BaseViewModel>{
		void onLoginTouch();
	}

	interface ILoginModel extends BaseContract.IBaseModel{
		// fingerprint
		void releaseTouchHardware();
		boolean isTouchIdApiSupported();
		boolean isHasFingerPrints();
		boolean isKeyStoreCompromissed();
		void requestDecryptionChipher(UnionTouchIdCallback callback);
		void resetKeys();
		void touchIdCancel();
	}
}
