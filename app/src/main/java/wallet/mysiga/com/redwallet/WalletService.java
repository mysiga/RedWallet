package wallet.mysiga.com.redwallet;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * 抢红包服务类
 *
 * @author Wilson
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
    public static final String OPEN_RED_TEXT_KEY = "拆红包";
    public static final String RECEIVE_RED_TEXT_KEY = "领取红包";
    public static final String LOOK_DETAIL_TEXT_KEY = "查看领取详情";
    public static final String LOOK_ALL_TEXT_KEY = "看看大家的手气";
    /***
     * 设置后台抢红包
     */
    public static final String ACTION_NOTIFICATION_OPEN_RED = "action_notification_open_red";
    /**
     * 设置当前界面抢红包
     */
    public static final String ACTION_WINDOWS_OPEN_RED = "action_windows_open_red";

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
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        Toast.makeText(this, "服务器解绑", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }


    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "抢红包服务开启", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
        mBroadcastReceiver = new RedWalletBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFICATION_OPEN_RED);
        intentFilter.addAction(ACTION_WINDOWS_OPEN_RED);
        registerReceiver(mBroadcastReceiver, intentFilter);
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
            Log.w(TAG, "rootWindow为空");
            return;
        }
        //4.3.8text获取list
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(OPEN_RED_TEXT_KEY);
        if (list.isEmpty()) {
            //4.3.9更改id
            list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2c");
        }
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(LOOK_DETAIL_TEXT_KEY);
            if (list.isEmpty()) {
                list = nodeInfo.findAccessibilityNodeInfosByText(LOOK_ALL_TEXT_KEY);
            }
        }
        if (!list.isEmpty()) {
            list.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void clickRedWalletView() {
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
                    openRedWalletView();
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
            AccessibilityServiceInfo serviceInfo = getServiceInfo();
            switch (type) {
                case ACTION_NOTIFICATION_OPEN_RED:
                    serviceInfo.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
                    setServiceInfo(serviceInfo);
                    Toast.makeText(context, "切换后台抢红包成功", Toast.LENGTH_SHORT).show();
                    break;
                case ACTION_WINDOWS_OPEN_RED:
                    serviceInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
                    setServiceInfo(serviceInfo);
                    Toast.makeText(context, "切换window抢红包成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
