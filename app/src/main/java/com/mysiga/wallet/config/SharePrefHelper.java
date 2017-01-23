package com.mysiga.wallet.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * SharedPreferences帮助类
 *
 * @author Wilson milin411@163.com
 */
public class SharePrefHelper {

    public static SharedPreferences getSharePref(@NonNull Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSharePrefEditor(@NonNull Context context, String name) {
        return getSharePref(context, name).edit();
    }
}
