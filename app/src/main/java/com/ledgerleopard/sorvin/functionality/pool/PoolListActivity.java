package com.ledgerleopard.sorvin.functionality.pool;

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
import com.ledgerleopard.sorvin.IndySDK;
import com.ledgerleopard.sorvin.R;
import com.ledgerleopard.sorvin.basemvp.BaseMVPActivity;
import com.ledgerleopard.sorvin.functionality.addconnection.PresenterStub;
import com.ledgerleopard.sorvin.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoolListActivity extends BaseMVPActivity<PresenterStub> {

    private ListView lvConfigurations;
    private List<String> content = new ArrayList<>();
    private ConfigurationsAdapter adapter;

    public static void start( Context context ){
        context.startActivity( new Intent(context, PoolListActivity.class));
    }


    @Override
    protected void initUI() {
        setToolbarTitle("Pool settings");
        setContentView(R.layout.activity_pool_list);
        lvConfigurations = findViewById(R.id.lvConfigurations);
        lvConfigurations.setOnItemClickListener((adapterView, view, i, l) -> {
            showProgress(false, null);
            File configFile = Utils.writeConfigToTempFile(this, content.get(i));

            IndySDK.getInstance().deletePoolConfig(configFile.getName()).thenAccept(aVoid -> {
                createAndOpenPool(configFile);
            }).exceptionally(throwable -> {
                createAndOpenPool(configFile);
                return null;
            });

        });
        adapter = new ConfigurationsAdapter(this, -1, content);
        lvConfigurations.setAdapter(adapter);

        String[] poolconfigNames = Utils.getPoolConfigNames(this);
        if ( poolconfigNames == null )
            showError("No config provided", (dialogInterface, i) -> finish());
        else {
            content.clear();
            content.addAll(Arrays.asList(poolconfigNames));
            adapter.notifyDataSetChanged();
        }
    }

    private void createAndOpenPool(File configFile) {
        IndySDK.getInstance().createAndOpenPoolFromConfigFile(configFile).thenAccept(Void -> {
            hideProgress();
            createDialog(getString(R.string.commons_warning), "Pool have been successfully opened", (dialogInterface, i1) -> finish());
        }).exceptionally(throwable -> {
            hideProgress();
            showError(throwable.getMessage(), null);
            return null;
        });
    }

    @Override
    protected void initBack() {
        presenter = new PresenterStub(null, null);
    }

    class ConfigurationsAdapter extends ArrayAdapter<String> {

        public ConfigurationsAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
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
