package com.ledgerleopard.sorvin.functionality.actions;

import android.content.Context;
import android.content.Intent;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseMVPActivity;
import com.ledgerleopard.sorvin.functionality.addconnection.PresenterStub;
import com.ledgerleopard.sorvin.functionality.attestation.AttestationActivity;
import com.ledgerleopard.sorvin.functionality.connections.ConnectionsViewImpl;
import com.ledgerleopard.sorvin.functionality.credentials.CredentialsList;
import com.ledgerleopard.sorvin.functionality.pool.PoolListActivity;
import com.ledgerleopard.sorvin.utils.Utils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ActionsActivity extends BaseMVPActivity<PresenterStub> {

    public static void start( Context context){
        context.startActivity( new Intent(context, ActionsActivity.class) );
    }

    @Override
    protected void initUI() {
        setToolbarTitle("Actions");
        setContentView(R.layout.activity_actions);

        findViewById(R.id.btnPool).setOnClickListener(view -> PoolListActivity.start(ActionsActivity.this));
        findViewById(R.id.btnConnections).setOnClickListener(view -> ConnectionsViewImpl.start(ActionsActivity.this));
        findViewById(R.id.btnAttestation).setOnClickListener(view -> AttestationActivity.start(ActionsActivity.this));
        findViewById(R.id.btnCredentials).setOnClickListener(view -> CredentialsList.start(ActionsActivity.this));
    }

    @Override
    protected void initBack() {
        presenter = new PresenterStub(null, null );

        showProgress("Wallet initialization", false, null);
        CompletableFuture.runAsync(() -> {
            if ( !IndySDK.getInstance().isWalletExist() ){
                IndySDK.getInstance().createAndOpenWallet();
            }
        }).thenAccept(new Consumer<Void>() {
	        @Override
	        public void accept(Void aVoid) {
		        File configFile = Utils.writeConfigToTempFile(ActionsActivity.this, "oleg_pool.txn");
		        IndySDK.getInstance().deletePoolConfig(configFile.getName()).thenAccept(new Consumer<Void>() {
			        @Override
			        public void accept(Void aVoid) {
				        createAndOpenPool(configFile);
			        }
		        }).exceptionally(throwable -> {
			        createAndOpenPool(configFile);
			        return null;
		        });
	        }
        });
    }

	private void createAndOpenPool(File configFile) {
		IndySDK.getInstance().createAndOpenPoolFromConfigFile(configFile).thenAccept(Void -> {
			hideProgress();
			createDialog(getString(R.string.commons_warning), "Wallet initialized.",null);
		}).exceptionally(throwable -> {
			hideProgress();
			showError(throwable.getMessage(), null);
			return null;
		});
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IndySDK.getInstance().closeWallet();
    }
}
