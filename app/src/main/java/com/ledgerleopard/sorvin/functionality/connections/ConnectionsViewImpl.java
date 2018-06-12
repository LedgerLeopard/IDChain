package com.ledgerleopard.sorvin.functionality.connections;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseActivity;
import com.ledgerleopard.sorvin.functionality.addconnection.QRScanningActivity;

import java.util.List;

public class ConnectionsViewImpl
	extends BaseActivity<ConnectionsContract.Presenter>
	implements ConnectionsContract.View {

	public final int QR_SCAN_REQUEST_CODE = 1;

	public static void start( Context context){
		context.startActivity( new Intent(context, ConnectionsViewImpl.class) );
	}


	private ListView lvConnections;
	private TextView tvNoItems;


	@Override
	protected void initUI() {
		setContentView(R.layout.activity_connections);
		findViewById(R.id.fab).setOnClickListener(v -> {
			checkPermission(Manifest.permission.CAMERA, new PermissionCallback() {
				@Override
				public void onSuccess() {
					presenter.onAddClicked();
				}

				@Override
				public void onFailure(int attemptCount) {}
			});
		});

		lvConnections = findViewById(R.id.lvConnections);
		tvNoItems = findViewById(R.id.tvNoItems);
	}

	@Override
	protected void initBack() {
		presenter = new ConnectionsListPresenter(this, new ConnectionsModelImpl(this));
	}

	@Override
	public void showConnectionsList(List<Object> content) {

	}

	@Override
	public void showHideNoConnectionsError( boolean visible ) {
		tvNoItems.setVisibility( visible ? View.VISIBLE : View.GONE);
		lvConnections.setVisibility( !visible ? View.VISIBLE : View.GONE);
	}

	@Override
	public void gotoAddConnection() {
		QRScanningActivity.start(this, QR_SCAN_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		presenter.onActivityResult(requestCode, resultCode, data);
	}
}
