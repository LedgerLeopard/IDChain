package com.ledgerleopard.sorvin.functionality.login;

import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BasePresenter;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;
import ru.bscmsk.fingerly.TouchIdCodes;
import ru.bscmsk.fingerly.interfaces.UnionTouchIdCallback;

import javax.crypto.Cipher;

public class LoginPresenterImpl
	extends BasePresenter<LoginContract.ILoginView, LoginContract.ILoginModel, BaseViewModel>
	implements LoginContract.ILoginPresenter {

	public LoginPresenterImpl(LoginContract.ILoginView view, LoginContract.ILoginModel model) {
		super(view, model);
	}

	@Override
	public void onStart() { }

	@Override
	public BaseViewModel createVM() {
		return new BaseViewModel();
	}

	@Override
	public void onLoginTouch() {
		if ( !model.isHasFingerPrints() ){
			view.hideTouchDialog();
			model.resetKeys();
			view.showError("No fingerprints in storage. Please go to settings and add at least one", null);
		} else if ( model.isKeyStoreCompromissed() ){
			view.hideTouchDialog();
			model.resetKeys();
			view.showError("Keystore was compromissed. TO discuss how to handle this", null);
		} else {
			view.showTouchDialog();
			model.requestDecryptionChipher(new UnionTouchIdCallback() {
				@Override
				public void onAuthenticationError(int errMsgId, CharSequence errString) {
					if (errMsgId != TouchIdCodes.FINGERPRINT_ERROR_CANCELED) {
						view.hideTouchDialog();
						model.touchIdCancel();
						view.showError(errString, null);
					}
				}

				@Override
				public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
					view.hideTouchDialog();
					view.createDialog(model.getString(R.string.commons_warning), helpString, null);
					model.touchIdCancel();
				}

				@Override
				public void onAuthenticationSucceeded(Cipher fingerCipher) {
					view.hideTouchDialog();
					model.touchIdCancel();
					view.gotoMain();
				}
			});
		}
	}
}
