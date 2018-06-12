package com.ledgerleopard.sorvin.functionality.addconnection;

import com.ledgerleopard.sorvin.basemvp.BaseContract;
import com.ledgerleopard.sorvin.basemvp.BasePresenter;
import com.ledgerleopard.sorvin.basemvp.BaseViewModel;

public class PresenterStub extends BasePresenter<BaseContract.IBaseView, BaseContract.IBaseModel, BaseViewModel> {

	public PresenterStub(BaseContract.IBaseView view, BaseContract.IBaseModel model) {
		super(view, model);
	}

	@Override
	public BaseViewModel createVM() {
		return null;
	}

	@Override
	public void onStart() {

	}
}
