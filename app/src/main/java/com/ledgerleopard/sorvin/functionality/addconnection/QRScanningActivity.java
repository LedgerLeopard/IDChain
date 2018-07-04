package com.ledgerleopard.sorvin.functionality.addconnection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseMVPActivity;

/**
 * Created by mbp15 on 09.07.17.
 */

public class QRScanningActivity extends BaseMVPActivity implements QRCodeReaderView.OnQRCodeReadListener {

    public static final String RESULT_QR_STRING = "RESULT_QR_STRING";
    private QRCodeReaderView qrCodeReaderView;

    public static void start(Activity context, int request_code ) {
        Intent intent = new Intent(context, QRScanningActivity.class);
        context.startActivityForResult(intent, request_code);
    }

	@Override
	protected void initUI() {
    	setToolbarTitle("SCAN QR");
		setContentView(R.layout.activity_qr_scan);
		qrCodeReaderView = findViewById(R.id.qrdecoderview);
		qrCodeReaderView.setOnQRCodeReadListener(this);

		qrCodeReaderView.setQRDecodingEnabled(true);
		qrCodeReaderView.setAutofocusInterval(2000L);
		qrCodeReaderView.setBackCamera();
	}

	@Override
	protected void initBack() {
    	presenter = new PresenterStub(null, null);
	}


    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
	    Intent intent = new Intent();
	    intent.putExtra(RESULT_QR_STRING, text);
	    setResult(RESULT_OK, intent);
    	finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }
}
