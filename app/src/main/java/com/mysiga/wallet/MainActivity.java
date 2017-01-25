package com.mysiga.wallet;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;

import com.mysiga.wallet.interfaces.IWalletConfigView;
import com.mysiga.wallet.presenter.WalletPresenter;
import com.mysiga.wallet.service.WalletService;

/**
 * 主页
 *
 * @author Wilson milin411@163.com
 */
public class MainActivity extends PreferenceActivity implements View.OnClickListener, IWalletConfigView, AccessibilityManager.AccessibilityStateChangeListener {
    private Button mStartServer;
    //AccessibilityService 管理
    private AccessibilityManager accessibilityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartServer = (Button) findViewById(R.id.open_red_wallet_service);
        Toolbar toolbar = (Toolbar) findViewById(R.id.common_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        addPreferencesFromResource(R.xml.pref_mode);
        Preference preference = findPreference(getString(R.string.pref_key_mode));
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sendBroadcast(new Intent(WalletService.INTENT_ACTION_CHANGE_MODE));
                return true;
            }
        });

        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);
        updateServiceStatus();

        findViewById(R.id.open_red_wallet_service).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        if (accessibilityManager != null) {
            accessibilityManager.removeAccessibilityStateChangeListener(this);
        }
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        String simpleName = WalletService.class.getName();
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (simpleName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void updateWalletServiceState() {
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        updateServiceStatus();
    }

    private void updateServiceStatus() {
        mStartServer.setText(isServiceRunning() ? R.string.service_running : R.string.open_service);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_red_wallet_service:
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(this, R.string.open_wechat_assister, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
