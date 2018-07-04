package com.ledgerleopard.sorvin.functionality.attestation;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;

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
	private AnoncredsResults.ProverCreateCredentialRequestResult proverCreateCredentialRequest;

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
					            String payload = String.format("{\"credOffer\": %s, \"credRequest\": %s }", offerJson, proverCreateCredentialRequestResult.getCredentialRequestJson());
					            IndySDK.getInstance().encryptAuth(verKeyMy, verKeyTheir, payload.getBytes(Charset.forName("utf-8"))).thenAccept(new Consumer<byte[]>() {
						            @Override
						            public void accept(byte[] bytes) {

//						            	String testString = "hello";
//							            String b64encoded = Base64.encodeToString(testString.getBytes(), Base64.NO_WRAP);



							            String b64encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);


//							            String helloDefault = Base64.encodeToString("Hello \nworld\r".getBytes(), Base64.DEFAULT);
//							            String helloNoWrap = Base64.encodeToString("Hello \nworld\r".getBytes(), Base64.NO_WRAP);
//							            String helloNoPadding = Base64.encodeToString("Hello \nworld\r".getBytes(), Base64.NO_PADDING);
//							            String helloNoClose = Base64.encodeToString("Hello \nworld\r".getBytes(), Base64.NO_CLOSE);
//							            String helloCRLF = Base64.encodeToString("Hello \nworld\r".getBytes(), Base64.CRLF);


							            model.sendCredentialOfferRequest(IDChainApplication.getAppInstance().getAttestationGetCredentialOffersUrl(), connectionItem.myId, b64encoded, new BaseApiCallback<BaseResponse>(verKeyMy) {
								            @Override
								            public void onSuccess(BaseResponse response, String rawResponse) {
									            // I hope somebody will remove the double stringify at last and I will remove this code
								            	StringBuilder stringBuilder = new StringBuilder(rawResponse);
									            stringBuilder.deleteCharAt(stringBuilder .indexOf("\""));
									            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("\""));
									            String correctString = stringBuilder.toString().replace("\\\"", "\"");

									            try {
										            JsonElement jsonElement = new JsonParser().parse("{\"credentials\":{\"schema_id\":\"7d1trfrU7BbE3pZ85hMZGX:2:Passport:1.0\",\"cred_def_id\":\"7d1trfrU7BbE3pZ85hMZGX:3:CL:30\",\"rev_reg_id\":null,\"values\":{\"nationalitycode@https://en.wikipedia.org/wiki/iso_3166-1_alpha-3\":{\"raw\":\"NLD\",\"encoded\":\"787668\"},\"credentialoffered@https://en.wikipedia.org/wiki/unix_time\":{\"raw\":\"1530723192\",\"encoded\":\"1530723192\"},\"gender@http://schema.org/gendertype\":{\"raw\":\"Male\",\"encoded\":\"7797108101\"},\"birthplace@http://schema.org/text\":{\"raw\":\"6823 Wit laan Suite 546 Kreielt Fleurstroom\",\"encoded\":\"54565051328710511632108979711032831171051161013253525432751141011051011081163270108101117114115116114111111109\"},\"authority@http://schema.org/place\":{\"raw\":\"Gemeente Amsterdam\",\"encoded\":\"71101109101101110116101326510911511610111410097109\"},\"dateofissue@https://en.wikipedia.org/wiki/unix_time\":{\"raw\":\"1530489600\",\"encoded\":\"1530489600\"},\"surname@http://schema.org/text\":{\"raw\":\"Groot\",\"encoded\":\"71114111111116\"},\"dateofexpiry@https://en.wikipedia.org/wiki/unix_time\":{\"raw\":\"1845849600\",\"encoded\":\"1845849600\"},\"birthdate@https://en.wikipedia.org/wiki/unix_time\":{\"raw\":\"937612800\",\"encoded\":\"937612800\"},\"documentnumber@http://schema.org/number\":{\"raw\":\"875561993\",\"encoded\":\"875561993\"},\"givenname@http://schema.org/text\":{\"raw\":\"Jesse\",\"encoded\":\"74101115115101\"},\"bsn@https://nl.wikipedia.org/wiki/burgerservicenummer\":{\"raw\":\"124874047\",\"encoded\":\"124874047\"}},\"signature\":{\"p_credential\":{\"m_2\":\"55850936617797495961631989089059181221298192594295307668770821130073382598245\",\"a\":\"59009924968484308741059346843102031428054331752656714227664580781259241539989517484410951824649180015870924642891361040221347272584866122782579664488244283467938341672781586703640468034127654355389585767832832730084886129278467670689023286107672891237288791319967132351509539592396624137496473240986771555866423331234809207963586779245156699455496478411210029729162435640726313208870154651370618751920117139723641466444257819926786634503448902318254624462368700804163704452762451343806919304152499950436006600794611312003905290876964120056765809713142522460183680303852998336522330235385417505872951110585191381118232\",\"e\":\"259344723055062059907025491480697571938277889515152306249728583105665800713306759149981690559193987143012367913206299323899696942213235956742929911632397584285316187821631842427311\",\"v\":\"6341922667195142783006057949201254927388968938460639188033905795429298532710611464159893950862401062922088860802801967844225946002019474824725425900271013253484417225850925952738886634280607799976605555583352061587207225455574873086016530808058232943920662830611642573680927035341987408554088163465027161513251685395068612419680974787779164874412349345404759972321030239163978956232398987966137363431330019766183989381261811949379887541827152629431564371554527025497911278412797721596312841866449187794354946092971145803313783151111433827718401969059573124460790583869196279147910584144259832090470539237274728814210527954929512432579439328181689732024658332819936475961457958442179216953277285660375052295040989867874906728825011218299716856373315921528031365080739973078339257614232216516197241998762125330897544572028\"},\"r_credential\":null},\"signature_correctness_proof\":{\"se\":\"3160865894168094972844216984853434016304028583636143951820572796820630505012409916298580934214045397030298227706475799765507481039756471540163003076825071679405136911201646412559470507157724095484405699551378183928843191546573519220276188859522163472154107511074517618930793099874108442590136848351536022687587245231385521687445535453297333594763705550384918941414822611122794902552192497302896019834064547257178926975689757382949877731110507135130633565195428421095911034948730006397262735781031270860009943968016429367372694518420738401000994904523336501782892016928266133919171686500667788161903199863384367140654\",\"c\":\"18949383320758363942874292962158241060131729052681643205456868722958122194861\"},\"rev_reg\":null,\"witness\":null},\"credRevocId\":null,\"revocRegDelta\":null}");
									            } catch (JsonSyntaxException e) {
										            e.printStackTrace();
									            }

									            view.hideProgress();
									            showEnterCredNameDialog(null);

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

    private void showEnterCredNameDialog(String error){
	    view.createEditableDialog("Credential received","Enter credential identifier", null, error, null, null, text -> {
		    if (TextUtils.isEmpty(text)){
			    showEnterCredNameDialog( "Name should not be empty");
		    } else {
			    //IndySDK.getInstance().storeCredentials(text.toString(), proverCreateCredentialRequest.getCredentialRequestMetadataJson(), )
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
