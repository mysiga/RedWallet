package com.mysiga.wallet.config;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 服务器状态
 *
 * @author Wilson milin411@163.com
 */
public class WalletServiceSwitch {
    @IntDef({STATE_NO_START, STATE_NOTIFICATION_SERVICE, STATE_WINDOWS_SERVICE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WalletServiceState {
    }

    /**
     * 未启动服务
     */
    public static final int STATE_NO_START = 0;
    /**
     * 外挂模式
     */
    public static final int STATE_NOTIFICATION_SERVICE = 1;
    /**
     * 窗口模式
     */
    public static final int STATE_WINDOWS_SERVICE = 2;
}
