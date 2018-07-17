package com.ledgerleopard.sorvin.functionality.verification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseActivity;
import com.ledgerleopard.sorvin.functionality.addconnection.QRScanningActivity;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VerificationActivity extends BaseActivity {


	private static final int QR_SCAN_REQUEST_CODE = 1001;

	public String verificationProof = "{\n" +
		"  \"nonce\": \"123432421212\",\n" +
		"  \"name\": \"Loan-Application-Basic\",\n" +
		"  \"version\": \"0.1\",\n" +
		"  \"requested_attributes\": {\n" +
		"    \"attr1_referent\": {\n" +
		"      \"name\": \"givenname@http://schema.org/text\",\n" +
		"      \"restrictions\": [\n" +
		"        {\n" +
		"          \"cred_def_id\": \"7d1trfrU7BbE3pZ85hMZGX:3:CL:30\"\n" +
		"        }\n" +
		"      ]\n" +
		"    },\n" +
		"    \"attr2_referent\": {\n" +
		"      \"name\": \"surname@http://schema.org/text\",\n" +
		"      \"restrictions\": [\n" +
		"        {\n" +
		"          \"cred_def_id\": \"7d1trfrU7BbE3pZ85hMZGX:3:CL:30\"\n" +
		"        }\n" +
		"      ]\n" +
		"    },\n" +
		"    \"attr3_referent\": {\n" +
		"      \"name\": \"bsn@https://nl.wikipedia.org/wiki/burgerservicenummer\",\n" +
		"      \"restrictions\": [\n" +
		"        {\n" +
		"          \"cred_def_id\": \"7d1trfrU7BbE3pZ85hMZGX:3:CL:30\"\n" +
		"        }\n" +
		"      ]\n" +
		"    },\n" +
		"    \"attr4_referent\": {\n" +
		"      \"name\": \"SELF_ASSERTED\"\n" +
		"    }\n" +
		"  },\n" +
		"  \"requested_predicates\": {}\n" +
		"}";

	private Map<String, AttributeDescription> attributesNeedApprove;
	private Map<String, AttributeDescription> attributesSelfAsserted;
	private int attributesCount;
	private LinearLayout llAttributesHolder;
	private LayoutInflater layoutInflater;

	public class AttributeDescription {
		public String name;
		public String schemaId;
		public String credDefId;
		public String credUdid;

		public AttributeDescription(String name, String schemaId, String credDefId, String credUdid) {
			this.name = name;
			this.schemaId = schemaId;
			this.credDefId = credDefId;
			this.credUdid = credUdid;
		}
	}


	public static void start(Context context){
		context.startActivity(new Intent(context, VerificationActivity.class));
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verification);
		setToolbarTitle("Verification");
		llAttributesHolder = findViewById(R.id.llRoot);
		layoutInflater = LayoutInflater.from(this);

		findViewById(R.id.btnCreateProof).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<String> schemaIdList = new ArrayList<>();
				List<String> credDefIdList = new ArrayList<>();

				// *****************************************************************************************************
				// prepare request credentials json
				JsonObject jsonRequestedAttributes = new JsonObject();
				for (String key : attributesNeedApprove.keySet()) {
					AttributeDescription attributeDescription = attributesNeedApprove.get(key);

					JsonObject credId = new JsonObject();
					credId.addProperty("cred_id", attributeDescription.credUdid);
					credId.addProperty("revealed", true);
					jsonRequestedAttributes.add(key, credId);


					if ( !schemaIdList.contains(attributeDescription.schemaId) ){
						schemaIdList.add(attributeDescription.schemaId);
					}

					if ( !credDefIdList.contains(attributeDescription.credDefId) ){
						credDefIdList.add(attributeDescription.credDefId);
					}
				}

				JsonObject jsonSelfAssertedAttributes = new JsonObject();
				for (String key : attributesSelfAsserted.keySet()) {
					EditText etValue = llAttributesHolder.findViewWithTag(key);
					if ( TextUtils.isEmpty(etValue.getText()) ) {
						showError("Field " + key + " is empty", null);
						break;
					}
					jsonSelfAssertedAttributes.addProperty(key, etValue.getText().toString());
				}

				JsonObject requestedCredentialsJson = new JsonObject();
				requestedCredentialsJson.add("requested_attributes", jsonRequestedAttributes);
				requestedCredentialsJson.add("self_attested_attributes", jsonSelfAssertedAttributes);
				requestedCredentialsJson.add("requested_predicates", new JsonObject());

				// *****************************************************************************************************
				// get definitions for schema's

				CompletableFuture<String> getSchemasFeature = CompletableFuture.supplyAsync(() -> {
					StringBuilder builder = new StringBuilder("{");
					for (String schemaId : schemaIdList) {
						try {
							LedgerResults.ParseResponseResult parseResponseResult = IndySDK.getInstance().getSchemaAttributes(schemaId).get();
							builder.append( String.format("\"%s\":%s", parseResponseResult.getId(), parseResponseResult.getObjectJson()) );
							builder.append(",");

						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
					builder.deleteCharAt(builder.length()- 1);
					builder.append("}");
					return builder.toString();
				});


				CompletableFuture<String> getCredDefsFeature = CompletableFuture.supplyAsync(new Supplier<String>() {
					@Override
					public String get() {
						StringBuilder builder = new StringBuilder("{");
						for (String credDefId : credDefIdList) {
							try {
								LedgerResults.ParseResponseResult parseResponseResult = IndySDK.getInstance().getCredDefDetails(credDefId).get();
								builder.append( String.format("\"%s\":%s", parseResponseResult.getId(), parseResponseResult.getObjectJson()) );
								builder.append(",");
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
						}
						builder.deleteCharAt(builder.length()-1);
						builder.append("}");

						return builder.toString();
					}
				});

				getSchemasFeature.thenAcceptBoth(getCredDefsFeature, new BiConsumer<String, String>() {
					@Override
					public void accept(String schamasJson, String credDefJson) {
						try {
							String proof = IndySDK.getInstance().proverCreateProof(verificationProof, requestedCredentialsJson.toString(), schamasJson, credDefJson, new JsonObject().toString()).get();
							Log.e("","");
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}

					}
				});

			}
		});





//		IndySDK.getInstance().getConnectionsList().thenAccept(connectionItems -> {
//			if ( connectionItems.size() == 0 ){
//				showError("No connections in storage. Please go to municaipal office and create your digital passport", (dialog, which) -> {
//					finish();
//				});
//			} else {
//				checkPermission(Manifest.permission.CAMERA, new PermissionCallback() {
//					@Override
//					public void onSuccess() {
//						QRScanningActivity.start(VerificationActivity.this, QR_SCAN_REQUEST_CODE);
//					}
//
//					@Override
//					public void onFailure(int attemptCount) {
//						finish();
//					}
//				});
//			}
//		});




		try {
			//JsonObject proofObj = (JsonObject) new JsonParser().parse(qrContent);
			JsonObject proofObj = (JsonObject) new JsonParser().parse(verificationProof);
			getAllAttributesName(proofObj.get("requested_attributes").getAsJsonObject());

			IndySDK.getInstance().getCredentialsForProofRequest(verificationProof).thenAccept(new Consumer<String>() {
				@Override
				public void accept(String s) {
					JSONObject credentialsForProof = null;
					try {
						credentialsForProof = new JSONObject(s);
						for (int i = 1; i <= attributesCount; i++) {
							String attrName = "";
							String attrReferent = "";
							String attrValue = "";
							String credentialUuid = "";
							String schemaId = "";
							String credDefId = "";

							attrReferent = String.format("attr%s_referent", i);

							if ( attributesNeedApprove.get(attrReferent) != null ) {
								attrName = attributesNeedApprove.get(attrReferent).name;
							} else if (attributesSelfAsserted.get(attrReferent) != null) {
								attrName = attributesSelfAsserted.get(attrReferent).name;
							}

							JSONArray credentialsForAttribute = credentialsForProof.getJSONObject("attrs").getJSONArray(attrReferent);
							try {
								JSONObject credInfo = credentialsForAttribute.getJSONObject(0).getJSONObject("cred_info");

								schemaId = credInfo.getString("schema_id");
								credDefId = credInfo.getString("cred_def_id");
								credentialUuid = credInfo.getString("referent");
								JSONObject jsonObject = credInfo.getJSONObject("attrs");
								if ( jsonObject.has(attrName) ) {
									attrValue = jsonObject.getString(attrName);
								}


							} catch (JSONException e) {
								e.printStackTrace();
							}

							if ( attributesNeedApprove.get(attrReferent) != null ) {
								AttributeDescription attributeDescription = attributesNeedApprove.get(attrReferent);
								attributeDescription.credUdid = credentialUuid;
								attributeDescription.schemaId = schemaId;
								attributeDescription.credDefId = credDefId;
							} else if (attributesSelfAsserted.get(attrReferent) != null) {
								AttributeDescription attributeDescription = attributesSelfAsserted.get(attrReferent);
								attributeDescription.credUdid = credentialUuid;
								attributeDescription.schemaId = schemaId;
								attributeDescription.credDefId = credDefId;
							}
							putAttributeOnUi(attrName, attrReferent, attrValue);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (JsonSyntaxException e) {
			showError("Invalid QR code payload", null);
			return;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if ( requestCode == QR_SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

			String qrContent = data.getStringExtra(QRScanningActivity.RESULT_QR_STRING);
			if (TextUtils.isEmpty(qrContent)) {
				showError("No payload in QR", null);
				return;
			}

			try {
				//JsonObject proofObj = (JsonObject) new JsonParser().parse(qrContent);
				JsonObject proofObj = (JsonObject) new JsonParser().parse(verificationProof);
				getAllAttributesName(proofObj.get("requested_attributes").getAsJsonObject());

				IndySDK.getInstance().getCredentialsForProofRequest(verificationProof).thenAccept(new Consumer<String>() {
					@Override
					public void accept(String s) {
						JSONObject credentialsForProof = null;
						try {
							credentialsForProof = new JSONObject(s);
							for (int i = 1; i <= attributesCount; i++) {
								String attrName = "";
								String attrReferent = "";
								String attrValue = "";
								String credentialUuid = "";
								String schemaId = "";
								String credDefId = "";

								attrReferent = String.format("attr%s_referent", i);

								if ( attributesNeedApprove.get(attrReferent) != null ) {
									attrName = attributesNeedApprove.get(attrReferent).name;
								} else if (attributesSelfAsserted.get(attrReferent) != null) {
									attrName = attributesSelfAsserted.get(attrReferent).name;
								}

								JSONArray credentialsForAttribute = credentialsForProof.getJSONObject("attrs").getJSONArray(attrReferent);
								try {
									JSONObject credInfo = credentialsForAttribute.getJSONObject(0).getJSONObject("cred_info");

									schemaId = credInfo.getString("schema_id");
									credDefId = credInfo.getString("cred_def_id");
									credentialUuid = credInfo.getString("referent");
									JSONObject jsonObject = credInfo.getJSONObject("attrs");
									if ( jsonObject.has(attrName) ) {
										attrValue = jsonObject.getString(attrName);
									}


								} catch (JSONException e) {
									e.printStackTrace();
								}

								if ( attributesNeedApprove.get(attrReferent) != null ) {
									AttributeDescription attributeDescription = attributesNeedApprove.get(attrReferent);
									attributeDescription.credUdid = credentialUuid;
									attributeDescription.schemaId = schemaId;
									attributeDescription.credDefId = credDefId;
								} else if (attributesSelfAsserted.get(attrReferent) != null) {
									AttributeDescription attributeDescription = attributesSelfAsserted.get(attrReferent);
									attributeDescription.credUdid = credentialUuid;
									attributeDescription.schemaId = schemaId;
									attributeDescription.credDefId = credDefId;
								}
								putAttributeOnUi(attrName, attrReferent, attrValue);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (JsonSyntaxException e) {
				showError("Invalid QR code payload", null);
				return;
			}
		}
	}

	private void getAllAttributesName( JsonObject attributesObject ){
		attributesNeedApprove = new HashMap<>();
		attributesSelfAsserted = new HashMap<>();
		attributesCount = 1;

		while ( true ) {
			try {
				String attrReferent = String.format("attr%s_referent", attributesCount);
				JsonElement jsonElement = attributesObject.get(attrReferent);
				String attrName = jsonElement.getAsJsonObject().get("name").getAsString();

				// if this restrictions key exist -> need proof
				// if not - this attrubute is self asserted
				try {
					jsonElement.getAsJsonObject().get("restrictions").getAsJsonArray();
					attributesNeedApprove.put( attrReferent, new AttributeDescription(attrName, null, null, null) );
				} catch (Exception e) {
					attributesSelfAsserted.put( attrReferent, new AttributeDescription(attrName,null, null, null) );
				}

				attributesCount++;
			} catch (Exception e) {
				break;
			}
		}
	}

	private void putAttributeOnUi( String name, String attrName, String value ){
		runOnUiThread(() -> {
			View view = inflater.inflate(R.layout.item_attribute, null);
			TextView tvName = view.findViewById(R.id.tvName);
			TextView tvAttribute = view.findViewById(R.id.tvAttribute);
			EditText tilValue = view.findViewById(R.id.etValue);
			tilValue.setTag(attrName);



			tvName.setText(name);
			tvAttribute.setText(attrName);
			if ( !TextUtils.isEmpty(value) ) {
				tilValue.setText(value);
				tilValue.setEnabled(false);
			}
			llAttributesHolder.addView(view);
		});

	}
}
