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
import com.ledgerleopard.sorvin.model.ConnectionItem;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsViewImpl
	extends BaseActivity<ConnectionsContract.Presenter>
	implements ConnectionsContract.View {

	public final int QR_SCAN_REQUEST_CODE = 1;
	private ConnectionsAdapter connectionsAdapter;
	private List<ConnectionItem> content = new ArrayList<>();
	private ListView lvConnections;
	private TextView tvNoItems;

	public static void start( Context context){
		context.startActivity( new Intent(context, ConnectionsViewImpl.class) );
	}

	@Override
	protected void initUI() {
		setToolbarTitle("Connections");
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
		connectionsAdapter = new ConnectionsAdapter(this, -1, content);
		lvConnections.setAdapter(connectionsAdapter);

		tvNoItems = findViewById(R.id.tvNoItems);
	}

	@Override
	protected void initBack() {
		presenter = new ConnectionsListPresenter(this, new ConnectionsModelImpl(this));
	}


	@Override
	public void showConnectionsList(List<ConnectionItem> update) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				content.clear();
				content.addAll(update);
				connectionsAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void showHideNoConnectionsError( boolean visible ) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvNoItems.setVisibility( visible ? View.VISIBLE : View.GONE);
			}
		});
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
