package com.ndeso17.nimesnolimit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Removes every ad inside the target app.
 *
 * The Flutter google_mobile_ads plugin (obfuscated class U5/X) funnels every
 * ad operation - banner load, interstitial, rewarded, show, dispose - through
 * its single MethodChannel handler p(L5.m, K5.k). Hooking that handler and
 * returning early stops all AdMob traffic without needing the AdMob SDK
 * classes, which only exist after the dynamite module is downloaded.
 *
 * Unity Ads is hooked at its public static entry points and its implementation
 * class, which are present in the APK.
 */
public class AdBlocker {

    private static final String LOG_TAG = "NimeNoLimit";

    public void install(ClassLoader cl) {
        blockGoogleMobileAds(cl);
        blockUnityAds(cl);
    }

    private void blockGoogleMobileAds(ClassLoader cl) {
        XC_MethodHook noop = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        };
        try {
            Class<?> plugin = XposedHelpers.findClass("U5.X", cl);
            XposedHelpers.findAndHookMethod(plugin, "p", "L5.m", "K5.k", noop);
            XposedBridge.log(LOG_TAG + ": blocked google_mobile_ads channel handler");
        } catch (Throwable t) {
            XposedBridge.log(LOG_TAG + ": google_mobile_ads hook failed: " + t);
        }
    }

    private void blockUnityAds(ClassLoader cl) {
        XC_MethodHook noop = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        };
        try {
            Class<?> unityAds = XposedHelpers.findClass("com.unity3d.ads.UnityAds", cl);
            XposedHelpers.findAndHookMethod(unityAds, "show", "android.app.Activity",
                    "java.lang.String", noop);
            XposedBridge.log(LOG_TAG + ": blocked UnityAds.show");
        } catch (Throwable t) {
            XposedBridge.log(LOG_TAG + ": UnityAds.show hook failed: " + t);
        }
        try {
            Class<?> impl = XposedHelpers.findClass(
                    "com.unity3d.services.ads.UnityAdsImplementation", cl);
            XposedHelpers.findAndHookMethod(impl, "show", "android.app.Activity",
                    "java.lang.String", noop);
            XposedBridge.log(LOG_TAG + ": blocked UnityAdsImplementation.show");
        } catch (Throwable t) {
            XposedBridge.log(LOG_TAG + ": UnityAdsImplementation.show hook failed: " + t);
        }
    }
}
