package com.ledgerleopard.sorvin.functionality.credentials;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CredentialsList extends BaseActivity {

	private ListView lvCredentials;
	private TextView tvError;
	private CredentialsAdapter credentialsAdapter;

	public static void start(Context context){
		context.startActivity( new Intent(context, CredentialsList.class) );
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credentials);
		setToolbarTitle("Credentials");
		lvCredentials = findViewById(R.id.lvCredentials);
		tvError = findViewById(R.id.tvError);

		showProgress(false, null);
		IndySDK.getInstance().getAllCredentials().thenAccept(new Consumer<String>() {
			@Override
			public void accept(String s) {
				hideProgress();

				JsonArray array = (JsonArray) new JsonParser().parse(s);
				List<JsonElement> credentialslist = new ArrayList<>();
				Iterator<JsonElement> iterator = array.iterator();
				while ( iterator.hasNext() ){
					credentialslist.add(iterator.next());
				}
				credentialsAdapter = new CredentialsAdapter(CredentialsList.this, -1, credentialslist);
				lvCredentials.setAdapter(credentialsAdapter);
			}
		}).exceptionally(new Function<Throwable, Void>() {
			@Override
			public Void apply(Throwable throwable) {
				showError(throwable.getLocalizedMessage(), null);
				return null;
			}
		});
	}


	public class CredentialsAdapter extends ArrayAdapter<JsonElement> {

		private final LayoutInflater inflater;

		public CredentialsAdapter(@NonNull Context context, int resource, @NonNull List<JsonElement> objects) {
			super(context, resource, objects);
			inflater = LayoutInflater.from(context);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
			JsonElement item = getItem(position);

			View listItem = convertView;
			if(listItem == null)
				listItem = inflater.inflate(R.layout.item_credential, null);

			TextView tvConnectionName = listItem.findViewById(R.id.tvCredentialName);
			tvConnectionName.setText(((JsonObject) item).get("referent").toString());
			TextView tvMyDid = listItem.findViewById(R.id.tvValue);
			tvMyDid.setText(((JsonObject) item).get("attrs").toString());

			return listItem;
		}
	}
}
