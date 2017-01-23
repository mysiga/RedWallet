package com.mysiga.wallet.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;

import com.mysiga.wallet.R;
import com.mysiga.wallet.presenter.WalletServicePresenter;
import com.mysiga.wallet.interfaces.IWalletServiceView;

/**
 * redWallet service
 *
 * @author Wilson milin411@163.com
 */
public class WalletService extends AccessibilityService implements IWalletServiceView,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /***
     * change red wallet mode
     */
    public static final String INTENT_ACTION_CHANGE_MODE = "com.mysiga.wallet.INTENT_ACTION_CHANGE_MODE";


    private WalletServicePresenter mWalletServicePresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        mWalletServicePresenter = new WalletServicePresenter(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (mWalletServicePresenter != null) {
            mWalletServicePresenter.onAccessibilityEvent(event, this);
        }
    }

    @Override
    public void onInterrupt() {
        
    }

    @Override
    protected void onServiceConnected() {
        changeMode(PreferenceManager.getDefaultSharedPreferences(this));
        super.onServiceConnected();
    }

    @Override
    public AccessibilityService getAccessibilityService() {
        return this;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        changeMode(sharedPreferences);
    }

    private void changeMode(SharedPreferences sharedPreferences) {
        boolean isMode = sharedPreferences.getBoolean(getString(R.string.pref_key_mode), true);
        AccessibilityServiceInfo serviceInfo = getServiceInfo();
        if (isMode) {
            serviceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            setServiceInfo(serviceInfo);
        } else {
            serviceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            setServiceInfo(serviceInfo);
            mWalletServicePresenter.setIsFirstChecked(false);
        }
    }

    class RedWalletBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getAction();
            if (type.equals(INTENT_ACTION_CHANGE_MODE)) {
            }

        }
    }
}
