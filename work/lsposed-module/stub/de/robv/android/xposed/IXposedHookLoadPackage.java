package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface IXposedHookLoadPackage {
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;

    class Wrapper extends XC_LoadPackage {
        private final IXposedHookLoadPackage instance;
        public Wrapper(IXposedHookLoadPackage instance) { this.instance = instance; }
    }
}
