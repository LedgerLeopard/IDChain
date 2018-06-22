package com.ledgerleopard.sorvin.functionality.attestation;

import com.ledgerleopard.sorvin.IDChainApplication;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.BaseApiCallback;
import com.ledgerleopard.sorvin.api.response.GetCredentialOfferResponse;
import com.ledgerleopard.sorvin.basemvp.BasePresenter;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import com.ledgerleopard.sorvin.model.CredentialOffer;

import java.util.ArrayList;
import java.util.List;

public class AttestationsPresenterImpl
        extends BasePresenter<AttestationContract.View, AttestationContract.Model, BaseViewModel>
        implements AttestationContract.Presenter {

    public AttestationsPresenterImpl(AttestationContract.View view, AttestationContract.Model model) {
        super(view, model);
    }

    @Override
    public BaseViewModel createVM() {
        return new BaseViewModel();
    }

    @Override
    public void onCredentialClicked(int position) {

    }

    @Override
    public void onStart() {
        model.getGovernmentMyDid(new IndySDK.IndyCallback<ConnectionItem>() {
            @Override
            public void onDone(ConnectionItem result, String errorMessage) {
                model.getCredentialOffers(IDChainApplication.getAppInstance().attestationGetCredentialOffersUrl, result.myId, new BaseApiCallback<GetCredentialOfferResponse>(){

                    @Override
                    public void onSuccess(GetCredentialOfferResponse response) {
                        List<String> credentialOffersList = new ArrayList<>();
                        for (CredentialOffer credentialOffer : response.offersList) {
                            credentialOffersList.add(getSchemaNameFromId(credentialOffer.schema_id));
                        }
                        view.showCredentialOffersList(credentialOffersList);
                    }

                    @Override
                    public void onFailure(String message) {
                        view.showError(errorMessage, null);
                    }
                });
            }
        });
    }

    private String getSchemaNameFromId( String schemaId ){
        String[] split = schemaId.split(":");
        return split[2];
    }
}
