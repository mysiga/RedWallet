package wallet.mysiga.com.redwallet.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import wallet.mysiga.com.redwallet.R;
import wallet.mysiga.com.redwallet.RedWalletConfigActivity;
import wallet.mysiga.com.redwallet.config.WalletPrefHelper;
import wallet.mysiga.com.redwallet.config.WalletServiceSwitch;
import wallet.mysiga.com.redwallet.service.mvp.WalletServicePresenter;
import wallet.mysiga.com.redwallet.service.mvp.IWalletService;

/**
 * 抢红包服务类
 *
 * @author Wilson milin411@163.com
 */
public class WalletService extends AccessibilityService implements IWalletService {

    /***
     * 设置后台抢红包
     */
    public static final String INTENT_ACTION_NOTIFICATION_OPEN_RED = "com.redwallet.action_notification_open_red";
    /**
     * 设置当前界面抢红包
     */
    public static final String INTENT_ACTION_WINDOWS_OPEN_RED = "com.redwallet.action_windows_open_red";


    private RedWalletBroadcastReceiver mBroadcastReceiver;
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
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        WalletPrefHelper.setWalletServiceState(this, WalletServiceSwitch.STATE_NO_START);
        sendBroadcast(new Intent(RedWalletConfigActivity.INTENT_ACTION_END));
        Toast.makeText(this, getString(R.string.stop_service), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        sendBroadcast(new Intent(RedWalletConfigActivity.INTENT_ACTION_END));
        WalletPrefHelper.setWalletServiceState(this, WalletServiceSwitch.STATE_NO_START);
        Toast.makeText(this, getString(R.string.unbind_service), Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }


    @Override
    protected void onServiceConnected() {
        mBroadcastReceiver = new RedWalletBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_NOTIFICATION_OPEN_RED);
        intentFilter.addAction(INTENT_ACTION_WINDOWS_OPEN_RED);
        registerReceiver(mBroadcastReceiver, intentFilter);
        WalletPrefHelper.setWalletServiceState(this, WalletServiceSwitch.STATE_NOTIFICATION_SERVICE);
        sendBroadcast(new Intent(RedWalletConfigActivity.INTENT_ACTION_CONNECTED));
        Toast.makeText(this, getString(R.string.service_start_default_notification), Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    @Override
    public AccessibilityService getAccessibilityService() {
        return this;
    }

    class RedWalletBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getAction();
            if (type.isEmpty()) {
                return;
            }
            AccessibilityServiceInfo serviceInfo = getServiceInfo();
            switch (type) {
                case INTENT_ACTION_NOTIFICATION_OPEN_RED:
                    serviceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
                    setServiceInfo(serviceInfo);
                    WalletPrefHelper.setWalletServiceState(context, WalletServiceSwitch.STATE_NOTIFICATION_SERVICE);
                    Toast.makeText(context, context.getString(R.string.mode_notification), Toast.LENGTH_SHORT).show();
                    break;
                case INTENT_ACTION_WINDOWS_OPEN_RED:
                    serviceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
                    setServiceInfo(serviceInfo);
                    mWalletServicePresenter.setIsFirstChecked(false);
                    WalletPrefHelper.setWalletServiceState(context, WalletServiceSwitch.STATE_WINDOWS_SERVICE);
                    Toast.makeText(context, context.getString(R.string.mode_window), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
