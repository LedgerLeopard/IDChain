package com.ledgerleopard.sorvin.functionality.attestation;

import com.ledgerleopard.sorvin.api.BaseApiCallback;
import com.ledgerleopard.sorvin.api.response.GetCredentialOfferResponse;
import com.ledgerleopard.sorvin.basemvp.BaseContract;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;
import com.ledgerleopard.sorvin.basemvp.IndyBaseModelInterface;
import com.ledgerleopard.sorvin.model.ConnectionItem;
import com.ledgerleopard.sorvin.model.SchemaDefinition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AttestationContract {

    interface View extends BaseContract.IBaseView {
        void showCredentialOffersList(List<String> credentialOffers);
        void showActionsDialog();
        void showSchemaDialog(SchemaDefinition schema);
    }

    interface Presenter extends BaseContract.IBasePresenter<BaseViewModel>{
        void onCredentialClicked( int position );
        void onDialodDetailsClicked();
        void onDialogMakeRequestClicked();
    }

    interface Model extends IndyBaseModelInterface {
        void getCredentialOffers(String url, String authDid, BaseApiCallback<GetCredentialOfferResponse> callback);
        void sendCredentialOfferRequest();
        CompletableFuture<ConnectionItem> getGovernmentMyDid();
    }
}
