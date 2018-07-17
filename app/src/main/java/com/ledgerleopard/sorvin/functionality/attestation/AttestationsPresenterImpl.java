package com.ledgerleopard.sorvin.functionality.attestation;

import android.util.Base64;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ledgerleopard.sorvin.IDChainApplication;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.BaseApiCallback;
import com.ledgerleopard.sorvin.api.response.BaseResponse;
import com.ledgerleopard.sorvin.api.response.GetCredentialOfferResponse;
import com.ledgerleopard.sorvin.basemvp.BasePresenter;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import com.ledgerleopard.sorvin.model.CredentialOffer;
import com.ledgerleopard.sorvin.model.SchemaDefinition;
import org.hyperledger.indy.sdk.ledger.LedgerResults;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AttestationsPresenterImpl
        extends BasePresenter<AttestationContract.View, AttestationContract.Model, BaseViewModel>
        implements AttestationContract.Presenter {

    private GetCredentialOfferResponse currentResponse;
    private int currentPosition;
	private IndySDK.CreateOfferRequestResult proverCreateCredentialRequest;
	private JsonObject createCredentialsJsonObject;

	public AttestationsPresenterImpl(AttestationContract.View view, AttestationContract.Model model) {
        super(view, model);
    }

    @Override
    public BaseViewModel createVM() {
        return new BaseViewModel();
    }

    @Override
    public void onCredentialClicked(int position) {
        currentPosition = position;
        view.showActionsDialog();
    }

    @Override
    public void onDialodDetailsClicked() {
        view.showProgress(false, null);

	    IndySDK.getInstance().getSchemaAttributes(currentResponse.offersList.get(currentPosition).schema_id).thenAccept(new Consumer<LedgerResults.ParseResponseResult>() {
		    @Override
		    public void accept(LedgerResults.ParseResponseResult parseResponseResult) {
			    SchemaDefinition res = new Gson().fromJson(parseResponseResult.getObjectJson(), SchemaDefinition.class);
			    view.hideProgress();
			    view.showSchemaDialog(res);
		    }
	    });
    }

    @Override
    public void onDialogMakeRequestClicked() {
        view.showProgress(false, null);

	    CredentialOffer credentialOffer = currentResponse.offersList.get(currentPosition);
	    String offerJson = new Gson().toJson(credentialOffer);
	    System.out.print(offerJson);
	    String cred_def_id = currentResponse.offersList.get(currentPosition).cred_def_id;
	    String schemaId = currentResponse.offersList.get(currentPosition).schema_id;

	    IndySDK.getInstance().createOfferRequest(cred_def_id, offerJson).thenAccept(new Consumer<IndySDK.CreateOfferRequestResult>() {
            @Override
            public void accept(IndySDK.CreateOfferRequestResult proverCreateCredentialRequestResult) {
	            proverCreateCredentialRequest = proverCreateCredentialRequestResult;
	            Log.e("","");
            	IndySDK.getInstance().getGovernmentMyDid().thenAccept(new Consumer<ConnectionItem>() {
		            @Override
		            public void accept(ConnectionItem connectionItem) {
			            // get verification keys for connection
		            	CompletableFuture<String> verKeyForMyDid = IndySDK.getInstance().getVerKeyForDidLocal(connectionItem.myId);
			            CompletableFuture<String> verKeyForTheirDid = IndySDK.getInstance().getVerKeyForDidLocal(connectionItem.theirId);
			            verKeyForMyDid.thenAcceptBoth(verKeyForTheirDid, new BiConsumer<String, String>() {
				            @Override
				            public void accept(String verKeyMy, String verKeyTheir) {

				            	// encrypt auth message
					            String payload = String.format("{\"credOffer\": %s, \"credRequest\": %s }", offerJson, proverCreateCredentialRequest.proverCreateCredentialResult.getCredentialRequestJson());
					            IndySDK.getInstance().encryptAuth(verKeyMy, verKeyTheir, payload.getBytes(Charset.forName("utf-8"))).thenAccept(new Consumer<byte[]>() {
						            @Override
						            public void accept(byte[] bytes) {
							            String b64encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);

							            model.sendCredentialOfferRequest(IDChainApplication.getAppInstance().getAttestationGetCredentialOffersUrl(), connectionItem.myId, b64encoded, new BaseApiCallback<BaseResponse>(verKeyMy) {
								            @Override
								            public void onSuccess(BaseResponse response, String rawResponse) {
									            view.hideProgress();

									            try {
										            createCredentialsJsonObject = (JsonObject) new JsonParser().parse(rawResponse);
										            view.createEditableDialog("Credential received","Enter credential identifier", null, null, null, null, text -> {
											            // todo make verification for name already exist part

										            	IndySDK.getInstance().storeCredentials(text.toString(),
												            proverCreateCredentialRequest.proverCreateCredentialResult.getCredentialRequestMetadataJson(),
												            createCredentialsJsonObject.get("credentials").toString(),
												            proverCreateCredentialRequest.credDef.getObjectJson()).thenAccept(new Consumer<String>() {
												            @Override
												            public void accept(String s) {
													            view.createDialog("Info", "Credentials have been successfully saved" , null);
												            }
											            });
										            });
									            } catch (JsonSyntaxException e) {
										            view.showError(e.getLocalizedMessage(), null);
									            }
								            }

								            @Override
								            public void onFailure(String message) {
									            view.hideProgress();
									            view.createDialog("Info", "Error " + message, null);
								            }
							            });
						            }
					            });
				            }
			            });
		            }
	            });
            }
        });
    }

//    private void showEnterCredNameDialog(String error) {
//	    view.createEditableDialog("Credential received","Enter credential identifier", null, error, null, null, text -> {
//		    if (TextUtils.isEmpty(text)){
//		    	// todo make verification for name already exist part
//			    showEnterCredNameDialog( "Name should not be empty");
//		    } else {
//			    IndySDK.getInstance().storeCredentials(text.toString(), proverCreateCredentialRequest.getCredentialRequestMetadataJson(), )
//		    }
//	    });
//    }

    @Override
    public void onStart() {
        view.showProgress(true, null);
        model.getGovernmentMyDid().thenAccept(new Consumer<ConnectionItem>() {
            @Override
            public void accept(ConnectionItem connectionItem) {
            	if ( connectionItem != null ){
		            IndySDK.getInstance().getVerKeyForDidLocal(connectionItem.myId).thenAccept(new Consumer<String>() {
			            @Override
			            public void accept(String decryptVerKey) {
				            model.getCredentialOffers(IDChainApplication.getAppInstance().getAttestationGetCredentialOffersUrl(), connectionItem.myId, new BaseApiCallback<GetCredentialOfferResponse>(decryptVerKey) {
					            @Override
					            public void onSuccess(GetCredentialOfferResponse response, String rawResponse) {
						            currentResponse = response;
						            view.hideProgress();
						            List<String> credentialOffersList = new ArrayList<>();
						            for (CredentialOffer credentialOffer : response.offersList) {
							            credentialOffersList.add(getSchemaNameFromId(credentialOffer.schema_id));
						            }
						            view.showCredentialOffersList(credentialOffersList);
					            }

					            @Override
					            public void onFailure(String message) {
						            view.hideProgress();
						            view.showError(message, null);
					            }
				            });
			            }
		            });
	            } else {
		            view.hideProgress();
		            view.showError("No connections created", (dialog, which) -> view.finish());
	            }
            }
        });
    }

    private String getSchemaNameFromId( String schemaId ){
        String[] split = schemaId.split(":");
        return split[2];
    }
}
