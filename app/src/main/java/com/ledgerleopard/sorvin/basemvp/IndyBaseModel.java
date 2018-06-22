package com.ledgerleopard.sorvin.basemvp;

import android.content.Context;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.model.ConnectionItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IndyBaseModel extends BaseModel implements IndyBaseModelInterface {

	public IndyBaseModel(Context context) {
		super(context);
	}

	@Override
	public CompletableFuture<byte[]> encryptAnon(String verKey, String message) {
		return IndySDK.getInstance().encrypt(verKey, message);
	}

	@Override
	public CompletableFuture<byte[]> sign(String verKey, String message) {
		return IndySDK.getInstance().sign(verKey, message);
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
	public CompletableFuture<List<ConnectionItem>> getConnectionsList( ) {
		return IndySDK.getInstance().getConnectionsList();
	}

}
