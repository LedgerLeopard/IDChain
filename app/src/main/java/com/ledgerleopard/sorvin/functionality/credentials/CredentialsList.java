package com.ledgerleopard.sorvin.functionality.credentials;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;
import android.widget.TextView;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseActivity;

public class CredentialsList extends BaseActivity {

	private ListView lvCredentials;
	private TextView tvError;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credentials);

		lvCredentials = findViewById(R.id.lvCredentials);
		tvError = findViewById(R.id.tvError);

	}
}
