package com.yy.httpproxy.thirdparty;

import android.content.Context;
import android.os.Build;

import com.yy.httpproxy.util.Log;

import com.yy.httpproxy.util.SystemProperty;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class ProviderFactory {

    private static final String KEY_HUAWEI_VERSION = "ro.build.version.emui";
    private static final String KEY_MIUI_VERSION = "ro.miui.ui.version.name";
    private static final String TAG = "ProviderFactory";
    private static final String HUAWEI_BUG_NAME = "NXT-AL10";
    private static final String HUAWEI_BUG_VERSION = "EmotionUI_4.1";

    public static NotificationProvider getProvider(Context context) {
        Class provider = checkProvider(context);
        if (HuaweiProvider.class.equals(provider)) {
            return new HuaweiProvider(context);
        } else if (XiaomiProvider.class.equals(provider)) {
            return new XiaomiProvider(context);
        } else if (UmengProvider.class.equals(provider)) {
            return new UmengProvider(context);
        } else if (MeizuProvider.class.equals(provider)) {
            return new MeizuProvider(context);
        } else {
            return null;
        }
    }

    public static Class checkProvider(Context context) {
        final SystemProperty prop = new SystemProperty(context);
        boolean isHuaweiSystem = isSystem(prop, KEY_HUAWEI_VERSION);
        boolean isHuaweiAvailable = HuaweiProvider.available(context);
        boolean huaweiBug = huaweiBug(prop);
        Log.i(TAG, "isHuaweiSystem " + isHuaweiSystem + ", isHuaweiAvailable " + isHuaweiAvailable + ", huaweiBug " + huaweiBug);
        if (isHuaweiSystem && isHuaweiAvailable && !huaweiBug) {
            Log.i(TAG, "HuaweiProvider");
            return HuaweiProvider.class;
        } else {
            boolean isXiaomi = isSystem(prop, KEY_MIUI_VERSION);
            if (isXiaomi && XiaomiProvider.available(context)) {
                Log.i(TAG, "XiaomiProvider");
                return XiaomiProvider.class;
            }  else if (isFlyme() && MeizuProvider.available(context)) {
                Log.i(TAG, "MeizuProvider");
                return MeizuProvider.class;
            } else if (UmengProvider.available(context)) {
                Log.i(TAG, "UmengProvider");
                return UmengProvider.class;
            } else {
                Log.i(TAG, "No provider");
                return null;
            }
        }
    }

    private static boolean isFlyme() {
        return Build.FINGERPRINT.contains("Flyme") || Pattern.compile("Flyme", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find();
    }

    private static boolean isSystem(SystemProperty prop, String key) {
        String value = prop.get(key);
        boolean b = value != null && !value.isEmpty();
        Log.d(TAG, key + " " + value + " " + b);
        return b;
    }

    private static boolean huaweiBug(SystemProperty prop) {
        String productName = prop.get("ro.product.name");
        String emuiVersion = prop.get(KEY_HUAWEI_VERSION);
        Log.d(TAG, "huawei productName " + productName + " emuiVersion " + emuiVersion);
        return HUAWEI_BUG_VERSION.equals(emuiVersion) && HUAWEI_BUG_NAME.equals(productName);
    }

}
