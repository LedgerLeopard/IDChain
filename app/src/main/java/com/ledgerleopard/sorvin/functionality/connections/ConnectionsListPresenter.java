package com.ledgerleopard.sorvin.functionality.connections;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ledgerleopard.sorvin.IDChainApplication;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.request.OnboadringRequest;
import com.ledgerleopard.sorvin.basemvp.BasePresenter;
import com.ledgerleopard.sorvin.functionality.addconnection.QRScanningActivity;
import com.ledgerleopard.sorvin.model.QRPayload;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.hyperledger.indy.sdk.did.DidResults;

import java.io.IOException;
import java.util.function.Consumer;

public class ConnectionsListPresenter
	extends BasePresenter<ConnectionsContract.View, ConnectionsContract.Model, ConnectionsViewModel>
	implements ConnectionsContract.Presenter {

	private Gson gson;
	private QRPayload qrPayload;

	public ConnectionsListPresenter(ConnectionsContract.View view, ConnectionsContract.Model model) {
		super(view, model);
		gson = new Gson();
	}

	@Override
	public ConnectionsViewModel createVM() {
		return null;
	}

	@Override
	public void onConnectionClicked(int position) {

	}

	@Override
	public void onAddClicked() {
		view.gotoAddConnection();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( resultCode != Activity.RESULT_OK )
			return;

		String qrContent = data.getStringExtra(QRScanningActivity.RESULT_QR_STRING);
		if (TextUtils.isEmpty(qrContent)) {
			view.showError("No payload in QR", null);
			return;
		}

		try {
			qrPayload = gson.fromJson(qrContent, QRPayload.class);
			IDChainApplication.getAppInstance().setAttestationGetCredentialOffersUrl(qrPayload.getBaseUrl());
		} catch (JsonSyntaxException e) {
			view.showError("Invalid QR code payload", null);
			return;
		}

		// Generate DID
		view.showProgress("Creating connection", false, null);
		IndySDK.getInstance().createAndStoreMyDid().thenAccept(new Consumer<DidResults.CreateAndStoreMyDidResult>() {
            @Override
            public void accept(DidResults.CreateAndStoreMyDidResult didResult) {
                OnboadringRequest request = new OnboadringRequest(didResult.getDid(), didResult.getVerkey(), qrPayload.token);

                String requestString = gson.toJson(request);
                model.encryptAnon( qrPayload.verkey, requestString).thenAccept(bytes -> {
	                String encode64Result = Base64.encodeToString(bytes, Base64.NO_WRAP);

                	model.sendDIDback(qrPayload.sendbackUrl(), qrPayload.token, encode64Result, new Callback() {
						@Override
						public void onFailure(Call call, IOException e) {
							view.hideProgress();
							view.showError(e.getMessage(), null);
						}

						@Override
						public void onResponse(Call call, Response response) {


							IndySDK.getInstance().connectMyDidWithForeignDid(didResult.getDid(), qrPayload.did, qrPayload.verkey);

							view.hideProgress();
							view.createDialog("Success", "Connection have been created successfully", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									updateConnections();
								}
							});
						}
					});



                }).exceptionally(throwable -> {
                    view.hideProgress();
                    view.showError(throwable.getMessage(), null);
                    return null;
                });
            }
        }).exceptionally(throwable -> {
            view.hideProgress();
            view.showError(throwable.getMessage(), null);
            return null;
        });
	}

	@Override
	public void onStart() {
		updateConnections();
	}

	private void updateConnections() {
		view.showProgress(false, null);
		model.getConnectionsList().thenAccept(connectionItems -> {
            view.hideProgress();
            if ( connectionItems.size() == 0 ) {
                view.showHideNoConnectionsError(true);
            } else {
                view.showConnectionsList(connectionItems);
                view.showHideNoConnectionsError(false);
            }
        }).exceptionally(throwable -> {
			view.hideProgress();
            view.showError(throwable.getMessage(), null);
            return null;
        });
	}
}
