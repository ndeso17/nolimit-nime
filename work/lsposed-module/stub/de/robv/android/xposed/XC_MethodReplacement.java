package de.robv.android.xposed;

public abstract class XC_MethodReplacement extends XC_MethodHook {
    public static final XC_MethodReplacement DO_NOTHING = new XC_MethodReplacement() {
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable { return null; }
    };

    public XC_MethodReplacement() {}
    public XC_MethodReplacement(int priority) {}

    protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;

    protected final void beforeHookedMethod(MethodHookParam param) throws Throwable {
        param.setResult(replaceHookedMethod(param));
    }
}
