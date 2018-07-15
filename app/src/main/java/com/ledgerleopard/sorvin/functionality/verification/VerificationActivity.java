package com.ledgerleopard.sorvin.functionality.verification;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class VerificationActivity extends BaseActivity {


	private static final int QR_SCAN_REQUEST_CODE = 1001;

	public String verificationProof = "{\n" +
		"  \"nonce\": \"123432421212\",\n" +
		"  \"name\": \"Loan-Application-Basic\",\n" +
		"  \"version\": \"0.1\",\n" +
		"  \"requested_attributes\": {\n" +
		"    \"attr1_referent\": {\n" +
		"      \"name\": \"givenname@http://schema.org/text\"\n" +
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
		"      \"name\": \"bsn@https://nl.wikipedia.org/wiki/burgerservicenummer\"\n" +
		"    }\n" +
		"  },\n" +
		"  \"requested_predicates\": {}\n" +
		"}";
	private Map<String, String> attributesNeedApprove;
	private Map<String, String> attributesSelfAsserted;
	private int attributesCount;
	private LinearLayout llAttributesHolder;
	private LayoutInflater layoutInflater;

	public static void start(Context context){
		context.startActivity(new Intent(context, VerificationActivity.class));
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verification);
		llAttributesHolder = findViewById(R.id.llRoot);
		layoutInflater = LayoutInflater.from(this);

		IndySDK.getInstance().getConnectionsList().thenAccept(connectionItems -> {
			if ( connectionItems.size() == 0 ){
				showError("No connections in storage. Please go to municaipal office and create your digital passport", (dialog, which) -> {
					finish();
				});
			} else {
				checkPermission(Manifest.permission.CAMERA, new PermissionCallback() {
					@Override
					public void onSuccess() {
						QRScanningActivity.start(VerificationActivity.this, QR_SCAN_REQUEST_CODE);
					}

					@Override
					public void onFailure(int attemptCount) {
						finish();
					}
				});
			}
		});
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
								String attrReferent = String.format("attr%s_referent", i);
								JSONArray credentialsForAttribute1 = credentialsForProof.getJSONObject("attrs").getJSONArray(attrReferent);

								String credentialUuid = credentialsForAttribute1.getJSONObject(0).getJSONObject("cred_info").getString("referent");

								String name = "";
								name = attributesNeedApprove.get(attrReferent);
								if ( TextUtils.isEmpty(name) ) {
									name = attributesSelfAsserted.get(attrReferent);
								}

								putAttributeOnUi(name, attrReferent, "NOT YEAT");
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
					String restrictions = jsonElement.getAsJsonObject().get("restrictions").getAsString();
					attributesNeedApprove.put( attrReferent, attrName );
				} catch (Exception e) {
					attributesSelfAsserted.put( attrReferent, attrName );
				}

				attributesCount++;
			} catch (Exception e) {
				break;
			}
		}
	}

	private void putAttributeOnUi( String name, String attrName, String value ){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				View view = inflater.inflate(R.layout.item_attribute, null);
				TextView tvName = view.findViewById(R.id.tvName);
				TextView tvAttribute = view.findViewById(R.id.tvAttribute);
				TextInputLayout tilValue = view.findViewById(R.id.tilValue);

				tvName.setText(name);
				tvAttribute.setText(attrName);
				if ( !TextUtils.isEmpty(value) ){
					tilValue.getEditText().setText(value);
					tilValue.setEnabled(false);
				}
				llAttributesHolder.addView(view);
			}
		});

	}
}
