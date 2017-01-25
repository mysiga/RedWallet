package com.mysiga.wallet.presenter;

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

import com.mysiga.wallet.interfaces.IWalletServiceView;

/**
 * wallet service control
 *
 * @author Wilson milin411@163.com
 */
public class WalletServicePresenter {
    /**
     * 红包消息的关键字
     */
    private static final String WECHAT_RED_TEXT_KEY = "[微信红包]";
    /**
     * 拆红包
     */
    private static final String RECEIVE_RED_TEXT_KEY = "领取红包";
    private static final String LOOK_DETAIL_TEXT_KEY = "查看领取详情";
    private static final String LOOK_ALL_TEXT_KEY = "看看大家的手气";
    /**
     * 抢红包id
     */
    private static final String WHART_VIEW_ID = "com.tencent.mm:id/bi3";

    /**
     * 拆红包界面
     */
    public static final String LUCKY_MONEY_RECEIVE_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    /**
     * //红包详情页面
     */
    public static final String LUCKY_MONEY_DETAIL_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    /**
     * 聊天界面
     */
    public static final String LAUNCHER_UI = "com.tencent.mm.ui.LauncherUI";
    public static final String LIST_VIEW = "android.widget.ListView";
    private IWalletServiceView mWalletServiceView;
    private boolean mIsFirstChecked;
    private Handler mHandler;

    public WalletServicePresenter(IWalletServiceView walletServiceView) {
        mWalletServiceView = walletServiceView;
    }

    public synchronized void setIsFirstChecked(Boolean isFirstChecked) {
        mIsFirstChecked = isFirstChecked;
    }

    public void onAccessibilityEvent(AccessibilityEvent event, Context context) {
        final int eventType = event.getEventType();
        Log.d(this.getClass().getSimpleName(), "事件---->" + event);
        //通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            if (isWeChatRedWallet(event)) {
                openRedWalletNotification(event, context);
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openRedWalletStep(event, context);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            openListRedView(event);
        }
    }

    private boolean isWeChatRedWallet(AccessibilityEvent event) {
        List<CharSequence> messages = event.getText();
        if (!messages.isEmpty()) {
            String message = String.valueOf(messages.get(0));
            if (message.contains(WECHAT_RED_TEXT_KEY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openRedWalletNotification(AccessibilityEvent event, Context context) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable == null || !(parcelable instanceof Notification)) {
            return;
        }
        PendingIntent pendingIntent = ((Notification) parcelable).contentIntent;
        setIsFirstChecked(true);
        try {
            pendingIntent.send();
            //解决在微信首页微信红包通知后无法触发AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED事件问题
            searchRedWalletView(context);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void openRedWalletStep(AccessibilityEvent event, Context context) {
        String eventName = String.valueOf(event.getClassName());
        //拆红包界面
        if (eventName.equals(LUCKY_MONEY_RECEIVE_UI)) {
            //拆红包
            clickRedWalletView();
        } else if (eventName.equals(LUCKY_MONEY_DETAIL_UI)) {
            ////红包详情页面,拆完红包后看详细的纪录界面
        } else if (eventName.equals(LAUNCHER_UI)) {
            ////红包详情页面,点中领取红包
            searchRedWalletView(context);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void clickRedWalletView() {
        AccessibilityNodeInfo nodeInfo = mWalletServiceView.getAccessibilityService().getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
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

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /**
     * 检查屏幕是否亮着并且唤醒屏幕
     */
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private void wakeScreen(Context context) {
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
    private void searchRedWalletView(Context context) {
        wakeScreen(context);
        AccessibilityNodeInfo nodeInfo = mWalletServiceView.getAccessibilityService().getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(WalletServicePresenter.RECEIVE_RED_TEXT_KEY);
        if (list != null && !list.isEmpty()) {
            //最新的红包领起
            AccessibilityNodeInfo parent = list.get(list.size() - 1).getParent();
            if (parent != null && mIsFirstChecked) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                setIsFirstChecked(false);
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openListRedView(AccessibilityEvent event) {
        String eventName = String.valueOf(event.getClassName());
        if (eventName.equals(LIST_VIEW)) {
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
        }

    }
}
