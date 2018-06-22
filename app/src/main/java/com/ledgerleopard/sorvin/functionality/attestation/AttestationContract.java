package com.ledgerleopard.sorvin.functionality.attestation;

import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.api.BaseApiCallback;
import com.ledgerleopard.sorvin.api.response.GetCredentialOfferResponse;
import com.ledgerleopard.sorvin.basemvp.BaseContract;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;
import com.ledgerleopard.sorvin.basemvp.IndyBaseModelInterface;
import com.ledgerleopard.sorvin.model.ConnectionItem;

import java.util.List;

public interface AttestationContract {

    interface View extends BaseContract.IBaseView {
        void showCredentialOffersList(List<String> credentialOffers);
    }

    interface Presenter extends BaseContract.IBasePresenter<BaseViewModel>{
        void onCredentialClicked( int position );
    }

    interface Model extends IndyBaseModelInterface {
        void getCredentialOffers(String url, String authDid, BaseApiCallback<GetCredentialOfferResponse> callback);
        void sendCredentialOfferRequest();
        void getGovernmentMyDid(IndySDK.IndyCallback<ConnectionItem> callback);
    }
}
