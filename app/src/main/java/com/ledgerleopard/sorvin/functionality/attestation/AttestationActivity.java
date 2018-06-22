package com.ledgerleopard.sorvin.functionality.attestation;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class AttestationActivity extends BaseActivity<AttestationContract.Presenter> implements AttestationContract.View {

    private ListView lvCredentialOffers;
    private List<String> content = new ArrayList<>();
    private CredentialOffersAdapter adapter;

    public static void start(Context context){
        context.startActivity( new Intent(context, AttestationActivity.class) );
    }

    @Override
    protected void initUI() {
        setContentView(R.layout.activity_attestation);
        lvCredentialOffers = findViewById(R.id.lvCredentialOffers);
        adapter = new CredentialOffersAdapter(this, -1, content);
        lvCredentialOffers.setAdapter(adapter);
    }

    @Override
    protected void initBack() {
        presenter = new AttestationsPresenterImpl(this, new AttestationModelImpl(this));
    }

    @Override
    public void showCredentialOffersList(List<String> credentialOffers) {
        content.clear();
        content.addAll(credentialOffers);
        adapter.notifyDataSetChanged();
    }

    class CredentialOffersAdapter extends ArrayAdapter<String> {

        public CredentialOffersAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if ( v == null ){
                v = LayoutInflater.from(getContext()).inflate(R.layout.item_configuration, null);
            }

            TextView tvConfigName = v.findViewById(R.id.tvConfiguration);
            tvConfigName.setText(getItem(position));

            return v;
        }
    }
}
