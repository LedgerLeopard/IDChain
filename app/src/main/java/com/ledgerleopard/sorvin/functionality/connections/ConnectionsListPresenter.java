package com.ledgerleopard.sorvin.functionality.connections;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.request.OnboadringRequest;
import com.ledgerleopard.sorvin.basemvp.BasePresenter;
import com.ledgerleopard.sorvin.functionality.addconnection.QRScanningActivity;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import com.ledgerleopard.sorvin.model.QRPayload;

import org.hyperledger.indy.sdk.did.DidResults;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
		} catch (JsonSyntaxException e) {
			view.showError("Invalid QR code payload", null);
			return;
		}




		// Generate DID
		view.showProgress("Creating connection", false, null);
		IndySDK.getInstance().createAndStoreMyDid().thenAccept(new Consumer<DidResults.CreateAndStoreMyDidResult>() {
            @Override
            public void accept(DidResults.CreateAndStoreMyDidResult didResult) {
                OnboadringRequest request = new OnboadringRequest(didResult.getDid(), didResult.getVerkey());




                // todo add encryption then it will be done on server
//                String requestString = gson.toJson(request);
//                model.encryptAnon( qrPayload.verkey, requestString).thenAccept(bytes -> {
//
//                }).exceptionally(throwable -> {
//                    view.hideProgress();
//                    view.showError(throwable.getMessage(), null);
//                    return null;
//                });



                model.sendDIDback(qrPayload.sendbackUrl(), qrPayload.token, request, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        view.hideProgress();
                        view.showError(e.getMessage(), null);
                    }

                    @Override
                    public void onResponse(Call call, Response response)  {
                        IndySDK.getInstance().connectMyDidWithForeignDid(didResult.getDid(), qrPayload.did);

                    	view.hideProgress();
                        view.createDialog("Success", "Connection have been created successfully", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateConnections();
                            }
                        });
                    }
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
		view.showProgress(false, null);
		updateConnections();
	}

	private void updateConnections(){
		view.showProgress(false, null);
		model.getConnectionsList().thenAccept(new Consumer<List<ConnectionItem>>() {
			@Override
			public void accept(List<ConnectionItem> connectionItems) {
				if ( connectionItems.size() == 0 ) {
					view.showHideNoConnectionsError(true);
				} else {
					view.showConnectionsList(connectionItems);
				}
			}
		}).exceptionally(throwable -> {
            view.showError(throwable.getMessage(), null);
            return null;
        });
	}
}
