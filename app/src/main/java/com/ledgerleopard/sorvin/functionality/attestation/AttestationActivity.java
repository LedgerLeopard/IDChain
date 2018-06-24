package com.ledgerleopard.sorvin.functionality.attestation;

import android.app.AlertDialog;
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
import com.ledgerleopard.sorvin.model.SchemaDefinition;

import java.util.ArrayList;
import java.util.List;

public class AttestationActivity extends BaseActivity<AttestationContract.Presenter> implements AttestationContract.View {

    private ListView lvCredentialOffers;
    private List<String> content = new ArrayList<>();
    private CredentialOffersAdapter adapter;
    private AlertDialog actionsDialog;

    public static void start(Context context){
        context.startActivity( new Intent(context, AttestationActivity.class) );
    }

    @Override
    protected void initUI() {
        setToolbarTitle("Attestation");
        setContentView(R.layout.activity_attestation);
        lvCredentialOffers = findViewById(R.id.lvCredentialOffers);
        lvCredentialOffers.setOnItemClickListener((parent, view, position, id) -> presenter.onCredentialClicked(position));
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

    @Override
    public void showActionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_credential_offer_actions, null);
        builder.setView(dialogView)
                .setTitle("Choose action");

        View touchView = dialogView.findViewById(R.id.btnGetDetails);
        touchView.setOnClickListener(v -> {
            actionsDialog.dismiss();
            presenter.onDialodDetailsClicked();
        });

        dialogView.findViewById(R.id.btnMakeRequest).setOnClickListener(v -> {
            actionsDialog.dismiss();
            presenter.onDialogMakeRequestClicked();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> actionsDialog.dismiss());

        actionsDialog = builder.create();
        actionsDialog.show();
    }

    @Override
    public void showSchemaDialog(SchemaDefinition schema) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.dialog_schema_definition, null);
        builder.setView(dialogView)
                .setTitle("Schema details");

        TextView tvSchemaName = dialogView.findViewById(R.id.tvSchemaName);
        tvSchemaName.setText(schema.name);

        TextView tvSchemaVersion = dialogView.findViewById(R.id.tvSchemaVersion);
        tvSchemaVersion.setText(schema.version);

        TextView tvSchemaAttributes = dialogView.findViewById(R.id.tvSchemaAttributes);
        for (String attrName : schema.attrNames) {
            tvSchemaAttributes.append(attrName);
            tvSchemaAttributes.append("\n");
        }

        runOnUiThread(() -> builder.create().show());

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
