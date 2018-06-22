package com.ledgerleopard.sorvin.functionality.login;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseActivity;
import com.ledgerleopard.sorvin.functionality.actions.ActionsActivity;
import com.ledgerleopard.sorvin.utils.SharedPreferenceStorage;

public class LoginViewImpl extends BaseActivity<LoginContract.ILoginPresenter>
	implements LoginContract.ILoginView {

	private AlertDialog touchIdDialog;

	public static void start(Context context ) {
		context.startActivity(new Intent(context, LoginViewImpl.class));
	}

	@Override
	protected void initUI() {
		setToolbarTitle(getString(R.string.commons_authentification));
		setContentView(R.layout.activity_login);
		findViewById(R.id.btnAuth).setOnClickListener(v -> {
			presenter.onLoginTouch();
		});
	}

	@Override
	protected void initBack() {
		presenter = new LoginPresenterImpl(this, new LoginModel(this, SharedPreferenceStorage.getInstanse(this)));
	}

	@Override
	public void showTouchDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
		builder.setView(R.layout.dialog_fingerprint);

		touchIdDialog = builder.create();
		touchIdDialog.setCanceledOnTouchOutside(false);
		touchIdDialog.show();
	}

	@Override
	public void hideTouchDialog() {
		if ( touchIdDialog != null )
			touchIdDialog.dismiss();
	}

	@Override
	public void gotoMain() {
		ActionsActivity.start(this);
	}
}
