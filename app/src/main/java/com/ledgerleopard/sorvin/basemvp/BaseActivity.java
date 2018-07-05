package com.ledgerleopard.sorvin.basemvp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import com.ledgerleopard.sorvin.R;

/**
 * Created by sergeybrazhnik on 25.01.18.
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseContract.IBaseView{

	private final int REQUEST_PERMISSION_FIRST = 101;
	private final int REQUEST_PERMISSION_SECOND = 102;
	private final int REQUEST_PERMISSION_SETTING = 103;
	protected final int DENY_PERMISSION = 1;
	protected final int DENY_PERMISSION_NO_ASK = 2;
	private PermissionCallback callback;

	private ProgressDialog pd;
    private AlertDialog dialog;
    private InputMethodManager imm;
    protected LayoutInflater inflater;
	protected Toolbar toolbar;
	private FrameLayout llContent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    super.setContentView(R.layout.activity_base);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inflater = LayoutInflater.from(BaseActivity.this);

	    toolbar = findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
	    toolbar.setNavigationOnClickListener(v -> {
		    super.onBackPressed();
	    });

	    llContent = findViewById(R.id.llContent);


	}

	@Override
	public void setContentView(@LayoutRes int layoutResID) {
		View view = getLayoutInflater().inflate(layoutResID, null);
		llContent.addView(view);
	}

	@Override
	public void setContentView(View view) {
		llContent.addView(view);
	}

    public void hideKeyboard() {
        runOnUiThread(() -> {
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(BaseActivity.this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });
    }

    public void showProgress(final boolean cancelable, final DialogInterface.OnCancelListener cancelListener) {
        showProgress("Loading", cancelable, cancelListener);
    }

	@Override
	public void showProgress(String loadingMessage, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
		runOnUiThread(() -> {
			if (pd == null) {
				pd = new ProgressDialog(BaseActivity.this);

				if (cancelListener != null)
					pd.setOnCancelListener(cancelListener);
				pd.setCanceledOnTouchOutside(cancelable);
			}
			pd.setTitle("");
			pd.setMessage(loadingMessage);
			pd.setIndeterminate(true);
			pd.setCancelable(cancelable);

			if (!isFinishing())
				pd.show();
		});
	}

	public void hideProgress() {
        runOnUiThread(() -> {
            if (pd != null && pd.isShowing() && !isFinishing())
                pd.cancel();
        });
    }

    @Override
    public void createDialog(CharSequence title, CharSequence commonText, DialogInterface.OnClickListener okClick) {
        runOnUiThread(() -> {
            createDialog(title, commonText, okClick, null);
        });
    }

    @Override
    public void createDialog(CharSequence title, CharSequence commonText, DialogInterface.OnClickListener okClick, DialogInterface.OnClickListener cancelClick) {
        if ((dialog != null && dialog.isShowing()) )
            dialog.dismiss();

		AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);

		builder.setTitle(title)
			.setCancelable(false)
			.setMessage(commonText)
			.setPositiveButton(getString(R.string.commons_ok), okClick);
			if ( cancelClick != null )
				builder.setNegativeButton(getString(R.string.commons_cancel), cancelClick);
		dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public void createEditableDialog( String title, String editHint, String editTextContent, String error, String okName, String cancelName, IEditDialog callback){
		if ((dialog != null && dialog.isShowing()) )
			dialog.dismiss();

		View inflatedView = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
		TextInputLayout tilEditText = inflatedView.findViewById(R.id.tilEditText);
		if (!TextUtils.isEmpty(editHint))
			tilEditText.setHint(editHint);

		if (!TextUtils.isEmpty(editTextContent))
			tilEditText.getEditText().setText(editTextContent);

		if (!TextUtils.isEmpty(error))
			tilEditText.setError(error);

		Button btnCancel = inflatedView.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(v -> {
			if ( dialog != null )
				dialog.dismiss();
		});
		if ( !TextUtils.isEmpty(cancelName))
			btnCancel.setText(cancelName);


		Button btnOk = inflatedView.findViewById(R.id.btnOk);
		btnOk.setOnClickListener(v -> {
			if ( dialog != null )
				dialog.dismiss();

			if ( TextUtils.isEmpty(tilEditText.getEditText().getText()) ){
				tilEditText.getEditText().setError("Field should not be empty");
			} else {
				if ( callback != null )
					callback.onFinish(tilEditText.getEditText().getText());
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
		builder.setView(inflatedView);
		builder.setTitle(title);
		dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public interface IEditDialog {
    	void onFinish( Editable text);
	}


    @Override
    public void showError(CharSequence commonText, DialogInterface.OnClickListener okClick) {
        runOnUiThread(() -> createDialog(getString(R.string.commons_error), commonText, okClick, null));

    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        setTitle(title);
    }

    @Override
    public void logout() {
        //todo implement me
    }


	// *********************************************************************************************
	// permissons
	protected void checkPermission(String permission, PermissionCallback callback) {
		this.callback = callback;
		if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
			requestPermission(permission);
		} else {
			this.callback.onSuccess();
		}
	}

	protected void requestPermission(String permission) {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
			ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION_SECOND);
		} else {
			ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION_FIRST);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case REQUEST_PERMISSION_FIRST:
				if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					callback.onSuccess();
				} else {
					if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
						callback.onFailure(DENY_PERMISSION_NO_ASK);
					} else {
						callback.onFailure(DENY_PERMISSION);
					}
				}
				break;
			case REQUEST_PERMISSION_SECOND:
				if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					callback.onSuccess();
				} else {
					callback.onFailure(DENY_PERMISSION);
				}
				break;
		}
	}

	protected boolean isPermission(String permission) {
		return ActivityCompat.checkSelfPermission(this, permission)
			== PackageManager.PERMISSION_GRANTED;
	}

	protected void showPermissionSettingDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("No permission provided")
			.setMessage("Please go user settings and provide permission!")
			.setCancelable(false)
			.setPositiveButton("Ok", (dialog, which) -> openPermissionSettings());
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void openPermissionSettings() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", getPackageName(), null);
		intent.setData(uri);
		startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
	}

	public interface PermissionCallback {
		void onSuccess();
		void onFailure(int attemptCount);
	}
}
