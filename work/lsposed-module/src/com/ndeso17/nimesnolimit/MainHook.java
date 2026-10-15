package com.ndeso17.nimesnolimit;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * NimeNoLimit — runtime patcher for the target app.
 *
 * The official APK is never modified, so its signature stays valid and any
 * sign-in that depends on it (e.g. Google Sign-In) keeps working. All changes
 * are applied at runtime inside the target process:
 *
 *  1. Premium/limits - the Flutter shared_preferences plugin reads every entry
 *     at once via SharedPreferences.getAll(); forcing the premium / quota keys
 *     there unlocks premium features and removes in-app limits.
 *  2. Permissions - runtime permission requests are answered as DENIED and the
 *     result callback is invoked immediately, so no system dialog is shown.
 *  3. Notifications - every NotificationManager.notify call is dropped.
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.aniverseid.animelovers";
    private static final String K_VIP_LEVEL = "flutter.vipLevel";
    private static final String K_VIP_EXP = "flutter.vipExp";
    private static final String K_TIKET = "flutter.tiket";

    private static final Long FORCED_VIP_LEVEL = 3L;
    private static final Long FORCED_TIKET = 99999L;
    private static final Long FORCED_VIP_EXP = 2147483647L;

    private static final String LOG_TAG = "NimeNoLimit";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }
        log("loading for " + lpparam.packageName);
        ClassLoader cl = lpparam.classLoader;
        hookGetAll(cl);
        hookScalar(cl, "getLong", long.class);
        hookScalar(cl, "getInt", int.class);
        denyRuntimePermissions(cl);
        silenceNotifications(cl);
        new AdBlocker().install(cl);
    }

    @SuppressWarnings("unchecked")
    private void hookGetAll(ClassLoader cl) {
        XC_MethodHook cb = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Object result = param.getResult();
                if (!(result instanceof Map)) {
                    return;
                }
                Map<String, Object> map = new HashMap<>((Map<String, Object>) result);
                map.put(K_VIP_LEVEL, FORCED_VIP_LEVEL);
                map.put(K_VIP_EXP, FORCED_VIP_EXP);
                map.put(K_TIKET, FORCED_TIKET);
                param.setResult(map);
            }
        };
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.SharedPreferencesImpl", cl, "getAll", cb);
            log("hooked SharedPreferencesImpl#getAll");
        } catch (Throwable t) {
            log("hook getAll failed: " + t);
        }
    }

    private void hookScalar(ClassLoader cl, String method, Class<?> defType) {
        XC_MethodHook cb = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.args.length < 1 || !(param.args[0] instanceof String)) {
                    return;
                }
                Long forced = forcedValue((String) param.args[0]);
                if (forced != null) {
                    param.setResult(forced);
                }
            }
        };
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.SharedPreferencesImpl", cl, method, String.class, defType, cb);
            log("hooked SharedPreferencesImpl#" + method);
        } catch (Throwable t) {
            log("hook " + method + " failed: " + t);
        }
    }

    private static Long forcedValue(String key) {
        if (K_VIP_LEVEL.equals(key)) {
            return FORCED_VIP_LEVEL;
        }
        if (K_TIKET.equals(key)) {
            return FORCED_TIKET;
        }
        if (K_VIP_EXP.equals(key)) {
            return FORCED_VIP_EXP;
        }
        return null;
    }

    private void denyRuntimePermissions(ClassLoader cl) {
        XC_MethodHook deny = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    Object[] args = param.args;
                    int count = (args.length >= 2 && args[1] instanceof String[])
                            ? ((String[]) args[1]).length : 1;
                    int[] results = new int[count];
                    java.util.Arrays.fill(results, -1);
                    if (args.length >= 3 && args[2] != null) {
                        XposedHelpers.callMethod(args[2], "onRequestPermissionsResult",
                                args[0], args[1], results);
                    }
                } catch (Throwable t) {
                    log("permission callback failed: " + t);
                }
                param.setResult(null);
            }
        };
        try {
            Class<?> activity = XposedHelpers.findClass("android.app.Activity", cl);
            XposedHelpers.findAndHookMethod(activity, "requestPermissions",
                    String[].class, int.class, deny);
            log("hooked Activity#requestPermissions");
        } catch (Throwable t) {
            log("hook requestPermissions failed: " + t);
        }
    }

    private void silenceNotifications(ClassLoader cl) {
        XC_MethodHook noop = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        };
        try {
            Class<?> nm = XposedHelpers.findClass("android.app.NotificationManager", cl);
            Class<?> notif = XposedHelpers.findClass("android.app.Notification", cl);
            Class<?> str = String.class;
            hookIfPresent(nm, "notify", noop, int.class, notif);
            hookIfPresent(nm, "notify", noop, str, int.class, notif);
            hookIfPresent(nm, "notify", noop, str, int.class, notif, str, str);
            hookIfPresent(nm, "notifyAsPackage", noop, str, str, int.class, notif);
            log("hooked NotificationManager notify overloads");
        } catch (Throwable t) {
            log("hook notify failed: " + t);
        }
    }

    private void hookIfPresent(Class<?> clazz, String method, XC_MethodHook cb, Object... params) {
        try {
            XposedHelpers.findAndHookMethod(clazz, method, params);
        } catch (Throwable ignored) {
        }
    }

    private static void log(String msg) {
        XposedBridge.log(LOG_TAG + ": " + msg);
    }
}
