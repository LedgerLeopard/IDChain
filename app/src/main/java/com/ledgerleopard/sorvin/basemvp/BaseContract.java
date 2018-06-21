package com.ledgerleopard.sorvin.basemvp;

import android.content.DialogInterface;

/**
 * Created by sergeybrazhnik on 04.09.17.
 */

public interface BaseContract {
    interface IBaseView {
        void showProgress(final boolean cancelable,
                          final DialogInterface.OnCancelListener cancelListener);
	    void showProgress(final String loadingMeessage,
	                      final boolean cancelable,
	                      final DialogInterface.OnCancelListener cancelListener);

        void hideProgress();
        void hideKeyboard();
        void createDialog(CharSequence title,
                          CharSequence commonText,
                          DialogInterface.OnClickListener okClick);

        void createDialog(CharSequence title,
                          CharSequence commonText,
                          DialogInterface.OnClickListener okClick,
                          DialogInterface.OnClickListener cancelClick);

	    void showError(CharSequence commonText,
	                   DialogInterface.OnClickListener okClick);

	    void setToolbarTitle(CharSequence title);
        void finish();
        void logout();
    }

    interface IBasePresenter<VIEW_MODEL extends BaseViewModel>{
	    void onStart();
    }

    interface IBaseModel {
        String getString(int id);
	    String getString(int id, Object... objects);
    }
}
