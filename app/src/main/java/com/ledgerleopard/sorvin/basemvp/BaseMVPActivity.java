package com.ledgerleopard.sorvin.basemvp;

import android.os.Bundle;
import android.support.annotation.Nullable;

public abstract class BaseMVPActivity<PRESENTER extends BaseContract.IBasePresenter> extends BaseActivity {

	protected PRESENTER presenter;
	protected abstract void initUI();
	protected abstract void initBack();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initBack();
		initUI();
		presenter.onStart();
	}
}
