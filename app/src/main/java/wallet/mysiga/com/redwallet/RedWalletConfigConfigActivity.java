package wallet.mysiga.com.redwallet;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import wallet.mysiga.com.redwallet.config.WalletPrefHelper;
import wallet.mysiga.com.redwallet.config.WalletServiceSwitch;
import wallet.mysiga.com.redwallet.model.WalletModeModel;
import wallet.mysiga.com.redwallet.presenter.WalletPresenter;
import wallet.mysiga.com.redwallet.view.IWalletConfigView;

/**
 * 主页
 *
 * @author Wilson milin411@163.com
 */
public class RedWalletConfigConfigActivity extends AppCompatActivity implements View.OnClickListener, IWalletConfigView {
    public static final String INTENT_ACTION_CONNECTED = "com.redwallet.action_connected";
    public static final String INTENT_ACTION_END = "com.redwallet.action_end";

    private ArrayAdapter mModeAdapter;
    private ModeBroadcastReceiver modeBroadcastReceiver;
    private WalletPresenter mWalletPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.common_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        Spinner modeView = (Spinner) findViewById(R.id.red_wallet_mode);
        mWalletPresenter = new WalletPresenter(this);
        mModeAdapter = new ArrayAdapter(this, R.layout.item_common_title_spinner, mWalletPresenter.getModeName());
        modeView.setAdapter(mModeAdapter);
        modeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<WalletModeModel> models = mWalletPresenter.getModes();
                if (!models.isEmpty()) {
                    WalletModeModel mode = models.get(position);
                    if (mode != null && !TextUtils.isEmpty(mode.mode)) {
                        sendBroadcast(new Intent(mode.mode));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        modeBroadcastReceiver = new ModeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_CONNECTED);
        filter.addAction(INTENT_ACTION_END);
        registerReceiver(modeBroadcastReceiver, filter);

        if (isServiceRunning()) {
            int state = WalletPrefHelper.getWalletServiceState(getApplicationContext());
            switch (state) {
                case WalletServiceSwitch.STATE_NOTIFICATION_SERVICE:
                    mWalletPresenter.initNotificationServiceState();
                    break;
                case WalletServiceSwitch.STATE_WINDOWS_SERVICE:
                    mWalletPresenter.initWindowServiceState();
                    break;
            }
        }

        findViewById(R.id.open_red_wallet_service).setOnClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        String simpleName = WalletServiceView.class.getName();
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (simpleName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (modeBroadcastReceiver != null) {
            unregisterReceiver(modeBroadcastReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void updateWalletServiceState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mModeAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_red_wallet_service:
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(this, "找到微信抢红包助手，并开启", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    class ModeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case INTENT_ACTION_CONNECTED:
                    mWalletPresenter.initNotificationServiceState();
                    break;
                case INTENT_ACTION_END:
                    mWalletPresenter.noStartService();
                    break;
            }
        }
    }

}
