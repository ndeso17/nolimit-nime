package com.ndeso17.nimesnolimit;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Signature spoofing for the modded APK to retain Google Sign-In.
 *
 * Hooks android.app.ApplicationPackageManager.getPackageInfo() in the
 * com.google.android.gms (Play Services) process and, for the target package,
 * replaces the returned Signature(s) and SigningInfo with the original
 * Google Play App Signing certificate. This makes the re-signed APK appear
 * to have the original signature to Play Services, so Google Sign-In and
 * any other SHA-1/256-based authentication pass.
 *
 * This technique is well-known (CorePatch, etc.) and only takes effect for
 * the specific target package; other apps are untouched.
 */
public class SignatureSpoof {

    private static final String LOG_TAG = "NimeNoLimit";
    private static final String TARGET_PACKAGE = "com.aniverseid.animelovers";
    // Original Google Play App Signing cert (extracted from original/base.apk)
    private static final String ORIGINAL_CERT_HEX =
            "3082058830820370a00302010202147c92634a42de1338fcadbb992a1f334c981f081e300d06092a864886f70d01010b05003074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643020170d3233313031383031323130335a180f32303533313031383031323130335a3074310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726f696430820222300d06092a864886f70d01010105000382020f003082020a0282020100d1138f13f6c5fe05ac857b79614c3b3b0851f184afb150dfa52cee043e6679983b4341c5667c5c2e0d09dc5223a7692621b53b220271e212def9e402e0999cbedfe466a40fb992749166234650191592aa4dd37c0befdc04b17b626c36eaee7fdee732afc25e18e5f8c8a2e7cc7092dff9a345fd4f2e9dbf54c7f1d54f646214de9dade1295af453910a28d87212843db7e9f04e8b3af7a625b75ffe021d6c7e222975cf632175dc05c7b4c9feb9be8f10ba5c65989072cba0b2ef835c8b96bf5e89cba4a26c7351a95f156e30c0a3b6fb09c7a5ada0d28ec8cadcb6271e898633c64f5dcd8af8d152110bd49c47f142b054f833948f6a55d05fa53a7a38da0830a77991459e3397664abcd2f19d3c1547767a6211b3d70dc3bfa3c0f6741a502cad68454ff3620da0cdb66b7f31b4979d184910add8172f7f3603bcd0982cfda737b66bdc157df629003d927c7fffd81bd670633ab3c94e69e303f6f70f59bda18da2084b4439f3c6de260ec4454bbad29cfdcccb6cb0fec8aeecae9941708da0024f055306010cf8e4d48217156c90c4f1bacc6d0e341295ff404c6990d80f79862debd4628bc3a23a4f30f3fecf5b6b44a24c8386f1f23cb247daf323e43d7b224e804bad1fe072438d6c4f652ff35cb4ba1df695c9a5481d2a3da1719efe13ce412b10e937131691ed992c9f33d4d9892f0043aa339c8297d54007d4c22f0203010001a310300e300c0603551d13040530030101ff300d06092a864886f70d01010b05000382020100bcceae052a332cabc3d8a68d0cf93bde2236cb2913cf10dcb9636f96d1f449b809e09ffe984135e748b317c681896f1a98315a0b8073d2e51f42296d1ec8f38898081b68a64f342f1e955ede81be6f291e2a3371e415c46f9c5d2e1b712156466e5b19f028bef1fc6e84b32857e81d652beee368e5b63ed449f4be969980b6507c8d67375d572c397788158d18dcb53d105e85ff09033a30c8db6e3189adf3d8ce4d4c2288e1ed56d35016ec936a2a2aacc2ed8c07edc510c5edbf627cde2e67f3d9177fac064b97d0edbd4c8d9048a286f52cfefb02a110f2222d44a921800df3bbe41736e059c76e4786a2348f60349d87d1478c46dab8b13523da0ede0f0529c3e1a5609e25e3b9ce31fcc189e58fe64d5ea2aceaa8ba101b665900f546f232f709bb872eec6d5aaadcb16a414bfc6bb2660ef34eaf7d8cf723ef4a8fe486eb8cd6ffdc8e7fd1f1b0eb8c16584da8036ac5a7da75f4046eaf69d15ff8d9ed4300c84307ee4f60756238546533c3f66e7584bb2b9363f689fc97c6e889856c179d8315a77e3113890ed5f24935785ad57a1698ceac86551b32c8eb6cf43432ec85d7f93da5c367768392ddd4f9df2d8b943709f5937808b040dcb5d4cc36a747a6fc29949290b700ce2fefaaece177e9506e7841d08746f96502673c3f8cdcef13e071367ea37c5390b41363585871be02c68029630815f7f90a8e6a1d463e";

    public void install(ClassLoader cl) {
        final byte[] originalCert = hexToBytes(ORIGINAL_CERT_HEX);
        final Signature spoofedSig = new Signature(originalCert);

        XC_MethodHook cb = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Object arg0 = param.args[0];
                if (!(arg0 instanceof String) || !TARGET_PACKAGE.equals(arg0)) return;
                Object result = param.getResult();
                if (!(result instanceof PackageInfo)) return;
                PackageInfo pi = (PackageInfo) result;
                pi.signatures = new Signature[] { spoofedSig };
                try {
                    Class<?> sigInfoCls = XposedHelpers.findClass(
                            "android.content.pm.SigningInfo", cl);
                    Object sigInfo = XposedHelpers.newInstance(
                            sigInfoCls, new Object[] { new Signature[] { spoofedSig } });
                    pi.signingInfo = (android.content.pm.SigningInfo) sigInfo;
                } catch (Throwable t) {
                    XposedBridge.log(LOG_TAG + ": signingInfo spoof failed: " + t);
                }
            }
        };
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.ApplicationPackageManager", cl,
                    "getPackageInfo", String.class, int.class, cb);
            XposedBridge.log(LOG_TAG + ": hooked ApplicationPackageManager.getPackageInfo");
        } catch (Throwable t) {
            XposedBridge.log(LOG_TAG + ": signature spoof install failed: " + t);
        }
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    | Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
