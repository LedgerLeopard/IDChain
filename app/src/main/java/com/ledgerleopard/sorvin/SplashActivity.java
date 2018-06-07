package com.ledgerleopard.sorvin;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ledgerleopard.sorvin.functionality.login.LoginViewImpl;

public class SplashActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new Handler().postDelayed(() -> {
			LoginViewImpl.start(this);
		}, 1000);
	}
}
