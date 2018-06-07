package com.ledgerleopard.sorvin.basemvp;

/**
 * Parent presenter. Made for compatibility of child presenters
 * It's goal is to store links for view, model and configuration objects
 */
public abstract class BasePresenter<
		VIEW extends BaseContract.IBaseView,
		MODEL extends BaseContract.IBaseModel,
		VIEW_MODEL extends BaseViewModel>
	implements BaseContract.IBasePresenter<VIEW_MODEL>{

	protected VIEW view;
	protected MODEL model;
	protected VIEW_MODEL vm;

	public BasePresenter(VIEW view, MODEL model) {
		this.view = view;
		this.model = model;

		this.vm = createVM();
	}

	public abstract VIEW_MODEL createVM();
}
