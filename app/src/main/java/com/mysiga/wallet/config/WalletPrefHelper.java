package com.mysiga.wallet.config;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 抢红包配置参数类
 *
 * @author Wilson  milin411@163.com
 */
public class WalletPrefHelper {

    public static void setWalletServiceState(@NonNull Context context, @WalletServiceSwitch.WalletServiceState int state) {
        SharePrefHelper.getSharePrefEditor(context, WalletConfig.APP_NAME).putInt(WalletConfig.SERVICE_STATE, state).commit();
    }

    public static int getWalletServiceState(@NonNull Context context) {
        return SharePrefHelper.getSharePref(context, WalletConfig.APP_NAME).getInt(WalletConfig.SERVICE_STATE, WalletServiceSwitch.STATE_NO_START);
    }

    private static class WalletConfig {
        private static final String APP_NAME = "redwallet";
        /**
         * 服务状态运行状态
         */
        private static final String SERVICE_STATE = "serivce_state";
    }

}
