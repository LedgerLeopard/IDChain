package com.ledgerleopard.sorvin.functionality.connections;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.model.ConnectionItem;

import java.util.List;

public class ConnectionsAdapter extends ArrayAdapter<ConnectionItem> {

	private final LayoutInflater inflater;

	public ConnectionsAdapter(@NonNull Context context, int resource, @NonNull List<ConnectionItem> objects) {
		super(context, resource, objects);
		inflater = LayoutInflater.from(context);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		ConnectionItem item = getItem(position);

		View listItem = convertView;
		if(listItem == null)
			listItem = inflater.inflate(R.layout.item_connection, null);


		TextView tvConnectionName = listItem.findViewById(R.id.tvConnectionName);
		TextView tvMyDid = listItem.findViewById(R.id.tvMyDid);
		TextView tvTheirDid = listItem.findViewById(R.id.tvTheirDid);

		tvConnectionName.setText(item.connectionName);
		tvMyDid.setText(item.myId);
		tvTheirDid.setText(item.theirId);

		return listItem;
	}
}
