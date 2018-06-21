package com.ledgerleopard.sorvin.basemvp;

import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.model.ConnectionItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IndyBaseModelInterface extends BaseContract.IBaseModel{
	CompletableFuture<Void> initializeWallet();
	void getConnectionsList( IndySDK.IndyCallback<List<ConnectionItem>> callback );

	CompletableFuture<byte[]> encryptAnon( String verKey, String message);
	CompletableFuture<byte[]> sign( String verKey, String message);
}
