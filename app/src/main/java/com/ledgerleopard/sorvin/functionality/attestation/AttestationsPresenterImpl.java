package com.ledgerleopard.sorvin.functionality.attestation;

import com.google.gson.Gson;
import com.ledgerleopard.sorvin.IDChainApplication;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.BaseApiCallback;
import com.ledgerleopard.sorvin.api.response.GetCredentialOfferResponse;
import com.ledgerleopard.sorvin.api.response.PostCredentialOfferResponse;
import com.ledgerleopard.sorvin.basemvp.BasePresenter;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import com.ledgerleopard.sorvin.model.CredentialOffer;
import com.ledgerleopard.sorvin.model.SchemaDefinition;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AttestationsPresenterImpl
        extends BasePresenter<AttestationContract.View, AttestationContract.Model, BaseViewModel>
        implements AttestationContract.Presenter {

    private GetCredentialOfferResponse currentResponse;
    private int currentPosition;

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
        IndySDK.getInstance().getSchemaAttributes(currentResponse.offersList.get(currentPosition).schema_id).thenAccept(new Consumer<SchemaDefinition>() {
            @Override
            public void accept(SchemaDefinition s) {
                view.hideProgress();
                view.showSchemaDialog(s);
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

	    IndySDK.getInstance().createOfferRequest(cred_def_id, offerJson, schemaId).thenAccept(new Consumer<AnoncredsResults.ProverCreateCredentialRequestResult>() {
            @Override
            public void accept(AnoncredsResults.ProverCreateCredentialRequestResult proverCreateCredentialRequestResult) {

	            IndySDK.getInstance().getGovernmentMyDid().thenAccept(new Consumer<ConnectionItem>() {
		            @Override
		            public void accept(ConnectionItem connectionItem) {
			            model.sendCredentialOfferRequest(IDChainApplication.getAppInstance().getAttestationGetCredentialOffersUrl(), connectionItem.myId, proverCreateCredentialRequestResult.getCredentialRequestJson(), new BaseApiCallback<PostCredentialOfferResponse>(null) {
				            @Override
				            public void onSuccess(PostCredentialOfferResponse response, String rawResponse) {
					            view.createDialog("Info", "Success = " + response.toString(), null);
					            //IndySDK.getInstance().storeCredentials("")
				            }

				            @Override
				            public void onFailure(String message) {
					            view.hideProgress();
					            view.createDialog("Info", "Error " + message, null);
				            }
			            });
		            }
	            });


//            	IndySDK.getInstance().getGovernmentMyDid().thenAccept(new Consumer<ConnectionItem>() {
//		            @Override
//		            public void accept(ConnectionItem connectionItem) {
//			            try {
//				            String myLocal = IndySDK.getInstance().getVerKeyForDidLocal(connectionItem.myId).get();
//				            String theirLedger = IndySDK.getInstance().getVerKeyForDidFromLedger(connectionItem.theirId).get();
//				            Log.e("","");
//
//				            IndySDK.getInstance().encryptAuth(myLocal, theirLedger, proverCreateCredentialRequestResult.getCredentialRequestJson().getBytes()).thenAccept(new Consumer<byte[]>() {
//					            @Override
//					            public void accept(byte[] bytes) {
//
//					            }
//				            });
//
//			            } catch (InterruptedException e) {
//				            e.printStackTrace();
//			            } catch (ExecutionException e) {
//				            e.printStackTrace();
//			            }
//
//
//		            }
//	            });
            }
        });
    }

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
