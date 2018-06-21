package com.ledgerleopard.sorvin.functionality.connections;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
		// todo 1. Parse string to Object
		String qrContent = data.getStringExtra(QRScanningActivity.RESULT_QR_STRING);
		if (TextUtils.isEmpty(qrContent)) {
			view.showError("No payload in QR", null);
			return;
		}

		try {
			qrPayload = gson.fromJson(qrContent, QRPayload.class);
		} catch (JsonSyntaxException e) {
			view.showError("Invalid QR code payload", null);
			return;
		}

		// Generate DID
		view.showProgress("Creating connection", false, null);
		model.createAndStoreDidAndConnectWithForeignDid(qrPayload.did, (IndySDK.IndyCallback<DidResults.CreateAndStoreMyDidResult>) (result, errorMessage) -> {
			if ( result != null ){
				// Send result to server back

				model.encryptAnon( qrPayload.did, qrPayload.nonce ).thenAccept(bytes -> {
					OnboadringRequest request = new OnboadringRequest(result.getDid(), result.getVerkey(), new String(bytes));
					model.sendDIDback(qrPayload.sendBackUrl, request, new Callback() {
						@Override
						public void onFailure(Call call, IOException e) {
							view.hideProgress();
							view.showError(e.getMessage(), null);
						}

						@Override
						public void onResponse(Call call, Response response) throws IOException {
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
					view.showError(errorMessage, null);
					return null;
				});
			} else {
				view.hideProgress();
				view.showError(errorMessage, null);
			}
		});
	}

	@Override
	public void onStart() {
		view.showProgress(false, null);
		model.initializeWallet().thenAccept(aVoid -> {
			updateConnections();
		});
	}

	private void updateConnections(){
		view.showProgress(false, null);
		model.getConnectionsList((result, errorMessage) -> {
			view.hideProgress();

			if (!TextUtils.isEmpty(errorMessage) ){
				view.showError(errorMessage, null);
			} else {
				if ( result.size() == 0 ) {
					view.showHideNoConnectionsError(true);
				} else {
					view.showConnectionsList(result);
				}
			}
		});
	}
}
