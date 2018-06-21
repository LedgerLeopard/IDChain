package com.ledgerleopard.sorvin.functionality.attestation;

import android.content.Context;
import android.content.Intent;

import com.ledgerleopard.sorvin.basemvp.BaseActivity;
import com.ledgerleopard.sorvin.functionality.addconnection.PresenterStub;

public class AttestationActivity extends BaseActivity<PresenterStub> {

    public static void start( Context context){
        context.startActivity( new Intent(context, AttestationActivity.class) );
    }

    @Override
    protected void initUI() {

    }

    @Override
    protected void initBack() {
        presenter = new PresenterStub(null, null);
    }
}
