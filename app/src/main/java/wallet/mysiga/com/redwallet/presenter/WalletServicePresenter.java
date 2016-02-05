package wallet.mysiga.com.redwallet.presenter;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import wallet.mysiga.com.redwallet.view.IWalletServiceView;

/**
 * 服务处理类
 * @author Wilson milin411@163.com
 */
public class WalletServicePresenter {
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
    /**
     * 抢红包id
     */
    public static final String WHART_VIEW_ID = "com.tencent.mm:id/b43";
    private IWalletServiceView mWalletServiceView;
    private boolean mIsFirstChecked;
    private Handler mHandler;

    public WalletServicePresenter(@NonNull IWalletServiceView walletServiceView) {
        mWalletServiceView = walletServiceView;
    }

    public synchronized void setIsFirstChecked(Boolean isFirstChecked) {
        mIsFirstChecked = isFirstChecked;
    }

    public synchronized void onAccessibilityEvent(@NonNull AccessibilityEvent event, @NonNull Context context) {
        final int eventType = event.getEventType();
        Log.d(this.getClass().getSimpleName(), "事件---->" + event);
        //通知栏事件
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (isRedWallet(event)) {
                    openNotification(event, context);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                switchClickRedWallet(event, context);
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                windowScrollClickRedView(event);
                break;
        }
    }

    private boolean isRedWallet(AccessibilityEvent event) {
        List<CharSequence> messages = event.getText();
        if (!messages.isEmpty()) {
            String message = String.valueOf(messages.get(0));
            if (message.contains(WalletServicePresenter.WECHAT_RED_TEXT_KEY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotification(@NonNull AccessibilityEvent event, @NonNull Context context) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable == null || !(parcelable instanceof Notification)) {
            return;
        }
        PendingIntent pendingIntent = ((Notification) parcelable).contentIntent;
        setIsFirstChecked(true);
        try {
            pendingIntent.send();
            //解决在微信首页微信红包通知后无法触发AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED事件问题
            clickRedWalletView(context);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void switchClickRedWallet(@NonNull AccessibilityEvent event, @NonNull Context context) {
        String eventName = String.valueOf(event.getClassName());
        switch (eventName) {
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI"://拆红包界面
                //拆红包
                openRedWalletView();
                break;
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"://红包详情页面
                //拆完红包后看详细的纪录界面
                break;
            case "com.tencent.mm.ui.LauncherUI"://聊天界面
                //点中领取红包
                clickRedWalletView(context);
                break;
            default:
                break;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openRedWalletView() {
        AccessibilityNodeInfo nodeInfo = mWalletServiceView.getAccessibilityService().getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        //V4.3.13.49
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(WalletServicePresenter.WHART_VIEW_ID);
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(WalletServicePresenter.LOOK_DETAIL_TEXT_KEY);
            if (list.isEmpty()) {
                list = nodeInfo.findAccessibilityNodeInfosByText(WalletServicePresenter.LOOK_ALL_TEXT_KEY);
            }
            if (!list.isEmpty()) {
                AccessibilityNodeInfo parent = list.get(list.size() - 1).getParent();
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        } else {
            list.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            //// 延时1s判断是否抢到红包，没有抢到点击"查看领取详情"跳转到红包详情
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AccessibilityNodeInfo nodeInfo = mWalletServiceView.getAccessibilityService().getRootInActiveWindow();
                    List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(WalletServicePresenter.LOOK_DETAIL_TEXT_KEY);
                    if (!list.isEmpty()) {
                        AccessibilityNodeInfo parent = list.get(list.size() - 1).getParent();
                        if (parent != null) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }, 1000);
        }

    }

    /**
     * 检查屏幕是否亮着并且唤醒屏幕
     */
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private void checkScreen(@NonNull Context context) {
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
    private void clickRedWalletView(@NonNull Context context) {
        checkScreen(context);
        AccessibilityNodeInfo nodeInfo = mWalletServiceView.getAccessibilityService().getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(WalletServicePresenter.RECEIVE_RED_TEXT_KEY);
        if (list != null && !list.isEmpty()) {
            //最新的红包领起
            AccessibilityNodeInfo parent = list.get(list.size() - 1).getParent();
            if (parent != null) {
                if (mIsFirstChecked) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    setIsFirstChecked(false);
                }
            }
        }
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void windowScrollClickRedView(AccessibilityEvent event) {
        String eventName = String.valueOf(event.getClassName());
        switch (eventName) {
            case "android.widget.ListView":
                //在聊天界面,点击"领取红包"
                AccessibilityNodeInfo nodeInfo = mWalletServiceView.getAccessibilityService().getRootInActiveWindow();
                if (nodeInfo == null) {
                    return;
                }
                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(WalletServicePresenter.RECEIVE_RED_TEXT_KEY);
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
}
