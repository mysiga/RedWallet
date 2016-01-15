package wallet.mysiga.com.redwallet;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
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
     * 微信的包名
     */
    public static final String WECHAT_PACKAGENAME = "com.tencent.mm";
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
    public static final String LOOK_ALL_TEXT_KEY = "看大家的手气";

    private boolean isFirstChecked;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        Log.d(TAG, "事件---->" + event);
        //通知栏事件
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence t : texts) {
                        String text = String.valueOf(t);
                        if (text.contains(WECHAT_RED_TEXT_KEY)) {
                            Log.i(TAG, WECHAT_RED_TEXT_KEY);
                            openNotify(event);
                            break;
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.i(TAG, "TYPE_WINDOW_STATE_CHANGED");
                openHongBao(event);
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                Log.i(TAG, "TYPE_VIEW_SCROLLED");
                //// TODO: 16/1/15 聊天页面抢红包,有问题
//                violenceClick(event);
                break;
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "服务器解绑", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "抢红包服务开启", Toast.LENGTH_SHORT).show();
    }

    private void sendNotifyEvent() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = WECHAT_RED_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable == null || !(parcelable instanceof Notification)) {
            return;
        }
        //以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) parcelable;
        PendingIntent pendingIntent = notification.contentIntent;
        Log.d(TAG, "开始--> pendingIntent.send()");
        isFirstChecked = true;
        try {
            pendingIntent.send();
            Log.d(TAG, "--> pendingIntent.send()---end");
            clickRed();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        String eventName = String.valueOf(event.getClassName());
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        switch (eventName) {
            case "com.tencent.mm.ui.base.o":
                //点中了红包，下一步就是去拆红包
                openRed();
                break;
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI":
                //点中了红包，下一步就是去拆红包
                openRed();
                break;
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                //拆完红包后看详细的纪录界面
                //nonething
                break;
            case "com.tencent.mm.ui.LauncherUI":
                //在聊天界面,去点中红包
                clickRed();
                break;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openRed() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(OPEN_RED_TEXT_KEY);
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(LOOK_DETAIL_TEXT_KEY);
            if (list.isEmpty()) {
                list = nodeInfo.findAccessibilityNodeInfosByText(LOOK_ALL_TEXT_KEY);
            }
        }
        for (AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void clickRed() {
        Log.i(TAG, "clickRed():");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(RECEIVE_RED_TEXT_KEY);
        if (list.isEmpty()) {
            Log.i(TAG, "clickRed():list.isEmpty()");
            list = nodeInfo.findAccessibilityNodeInfosByText(WECHAT_RED_TEXT_KEY);
            if (!list.isEmpty()) {
                for (AccessibilityNodeInfo n : list) {
                    Log.i(TAG, "-->微信红包:" + n);
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        } else {
            //最新的红包领起
            Log.i(TAG, "clickRed():list!=null");
            //最新的红包领起
            int size = list.size();
            AccessibilityNodeInfo parent = list.get(size - 1).getParent();
            Log.i(TAG, "-->领取红包:" + parent);
            if (parent != null) {
                if (isFirstChecked) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isFirstChecked = false;
                    openRed();
                    return;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void violenceClick(AccessibilityEvent event) {
        String eventName = String.valueOf(event.getClassName());
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        switch (eventName) {
            case "android.widget.ListView":
                //在聊天界面,去点中红包
                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                if (nodeInfo == null) {
                    Log.w(TAG, "rootWindow为空");
                    return;
                }
                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(RECEIVE_RED_TEXT_KEY);
                if (list == null || list.isEmpty()) {
                    return;
                }
                //最新的红包领起
                int size = list.size();
                AccessibilityNodeInfo parent = list.get(size - 1).getParent();
                Log.i(TAG, "-->领取红包:" + parent);
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                return;
        }

    }
}
