package com.ledgerleopard.sorvin.functionality.login;

import android.content.Context;
import com.ledgerleopard.sorvin.basemvp.BaseModel;
import com.ledgerleopard.sorvin.utils.SharedPreferenceStorage;
import ru.bscmsk.fingerly.TouchIdFactory;
import ru.bscmsk.fingerly.interfaces.IUnionTouchId;
import ru.bscmsk.fingerly.interfaces.UnionTouchIdCallback;

/**
 * Created by sergeybrazhnik on 05.02.18.
 */
public class LoginModel extends BaseModel implements LoginContract.ILoginModel{
	private final IUnionTouchId touchId;
	private SharedPreferenceStorage storage;

	public LoginModel(Context context, SharedPreferenceStorage storage) {
		super(context);
		this.storage = storage;
		touchId = TouchIdFactory.getTouchId(context, storage);
		touchId.createKey();
	}

	@Override
	public void releaseTouchHardware() {
		touchId.cancel();
	}

	@Override
	public boolean isTouchIdApiSupported() {
		return touchId.isApiSupported();
	}

	@Override
	public boolean isHasFingerPrints() {
		return touchId.hasFingerPrints();
	}

	@Override
	public boolean isKeyStoreCompromissed() {
		return touchId.isKeyPermanentlyInvalidated();
	}

	@Override
	public void requestDecryptionChipher(UnionTouchIdCallback callback) {
		touchId.requestFinger(IUnionTouchId.CipherMode.DECRYPT, callback);
	}

	@Override
	public void resetKeys() {
		touchId.clearKeystore();
		touchId.createKey();
	}

	@Override
	public void touchIdCancel() {
		touchId.cancel();
	}
}
