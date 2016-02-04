package wallet.mysiga.com.redwallet;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Parcelable;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

import wallet.mysiga.com.redwallet.config.WalletPrefHelper;
import wallet.mysiga.com.redwallet.config.WalletServiceSwitch;

/**
 * 抢红包服务类
 *
 * @author Wilson milin411@163.com
 */
public class WalletService extends AccessibilityService {

    public static final String TAG = "WalletService";
    /**
     * 红包消息的关键字
     */
    public static final String WECHAT_RED_TEXT_KEY = "[微信红包]";
    /**
     * 拆红包
     */
    public static final String RECEIVE_RED_TEXT_KEY = "领取红包";
    public static final String LOOK_DETAIL_TEXT_KEY = "查看领取详情";
    public static final String LOOK_ALL_TEXT_KEY = "看看大家的手气";
    /***
     * 设置后台抢红包
     */
    public static final String INTENT_ACTION_NOTIFICATION_OPEN_RED = "com.redwallet.action_notification_open_red";
    /**
     * 设置当前界面抢红包
     */
    public static final String INTENT_ACTION_WINDOWS_OPEN_RED = "com.redwallet.action_windows_open_red";
    /**
     * 抢红包id
     */
    public static final String WHART_VIEW_ID = "com.tencent.mm:id/b43";

    private boolean isFirstChecked;
    private RedWalletBroadcastReceiver mBroadcastReceiver;

    @Override
    public synchronized void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        Log.d(TAG, "事件---->" + event);
        //通知栏事件
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> messages = event.getText();
                if (!messages.isEmpty()) {
                    String message = String.valueOf(messages.get(0));
                    if (!message.contains(WECHAT_RED_TEXT_KEY)) {
                        return;
                    }
                    openNotification(event);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                switchClickRedWallet(event);
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                windowScrollClickRedView(event);
                break;
        }
    }

    @Override
    public void onInterrupt() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        WalletPrefHelper.setWalletServiceState(this, WalletServiceSwitch.STATE_NO_START);
        sendBroadcast(new Intent(MainActivity.INTENT_ACTION_END));
        Toast.makeText(this, getString(R.string.stop_service), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        sendBroadcast(new Intent(MainActivity.INTENT_ACTION_END));
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
        sendBroadcast(new Intent(MainActivity.INTENT_ACTION_CONNECTED));
        Toast.makeText(this, getString(R.string.service_start_default_notification), Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotification(AccessibilityEvent event) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable == null || !(parcelable instanceof Notification)) {
            return;
        }
        PendingIntent pendingIntent = ((Notification) parcelable).contentIntent;
        isFirstChecked = true;
        try {
            pendingIntent.send();
            clickRedWalletView();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void switchClickRedWallet(AccessibilityEvent event) {
        String eventName = String.valueOf(event.getClassName());
        switch (eventName) {
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI":
                //拆红包
                openRedWalletView();
                break;
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                //拆完红包后看详细的纪录界面
                break;
            case "com.tencent.mm.ui.LauncherUI":
                //点中领取红包
                clickRedWalletView();
                break;
            default:
                break;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openRedWalletView() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        //V4.3.13.49
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(WHART_VIEW_ID);
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(LOOK_DETAIL_TEXT_KEY);
            if (list.isEmpty()) {
                list = nodeInfo.findAccessibilityNodeInfosByText(LOOK_ALL_TEXT_KEY);
            }
            if (!list.isEmpty()) {
                AccessibilityNodeInfo parent = list.get(list.size() - 1).getParent();
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        } else {
            list.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

    }

    /**
     * 检查屏幕是否亮着并且唤醒屏幕
     */
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private void checkScreen(Context context) {
        PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (!powerManager.isInteractive()) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
            // 解锁
            keyguardLock.disableKeyguard();
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            // 点亮屏幕
            wakeLock.acquire();
            // 释放
            wakeLock.release();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void clickRedWalletView() {
        checkScreen(this);
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(RECEIVE_RED_TEXT_KEY);
        if (list != null && !list.isEmpty()) {
            //最新的红包领起
            AccessibilityNodeInfo parent = list.get(list.size() - 1).getParent();
            if (parent != null) {
                if (isFirstChecked) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isFirstChecked = false;
                }
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void windowScrollClickRedView(AccessibilityEvent event) {
        String eventName = String.valueOf(event.getClassName());
        switch (eventName) {
            case "android.widget.ListView":
                //在聊天界面,点击"领取红包"
                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                if (nodeInfo == null) {
                    return;
                }
                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(RECEIVE_RED_TEXT_KEY);
                if (list == null || list.isEmpty()) {
                    return;
                }
                //最新的红包领起
                AccessibilityNodeInfo parent = list.get(list.size() - 1).getParent();
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                break;
        }

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
                    WalletPrefHelper.setWalletServiceState(context, WalletServiceSwitch.STATE_WINDOWS_SERVICE);
                    Toast.makeText(context, context.getString(R.string.mode_window), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
