package com.ledgerleopard.sorvin.basemvp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import com.ledgerleopard.sorvin.R;

/**
 * Created by sergeybrazhnik on 25.01.18.
 */

public abstract class BaseActivity<PRESENTER extends BaseContract.IBasePresenter> extends AppCompatActivity implements BaseContract.IBaseView{

    private ProgressDialog pd;
    private AlertDialog dialog;
    private InputMethodManager imm;
    protected PRESENTER presenter;
    protected LayoutInflater inflater;
	protected Toolbar toolbar;
	private FrameLayout llContent;

	protected abstract void initUI();

    protected abstract void initBack();


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

		initBack();
		initUI();
		presenter.onStart();
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
}
