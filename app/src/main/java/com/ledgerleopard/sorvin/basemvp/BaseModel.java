package com.ledgerleopard.sorvin.basemvp;

import android.content.Context;
import java.lang.ref.WeakReference;


/**
 * Created by pc-izashkalov on 26.09.2017.
 */

public class BaseModel implements BaseContract.IBaseModel {

    protected WeakReference<Context> context;

    public BaseModel(Context context) {
        this.context = new WeakReference<Context>(context);
    }

    @Override
    public String getString(int id) {
        return context.get().getString(id);
    }

	@Override
	public String getString(int id, Object... objects) {
		return context.get().getString(id, objects);
	}
}
