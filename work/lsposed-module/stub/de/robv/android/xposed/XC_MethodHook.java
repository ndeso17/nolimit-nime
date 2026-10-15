package de.robv.android.xposed;

import java.lang.reflect.Member;

public abstract class XC_MethodHook {
    public static final int PRIORITY_DEFAULT = 50;

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}

    public XC_MethodHook() {}
    public XC_MethodHook(int priority) {}

    public static class MethodHookParam {
        public Member method;
        public Object thisObject;
        public Object[] args;
        private Object result;
        private Throwable throwable;
        public boolean returnEarly;

        public Object getResult() { return result; }
        public void setResult(Object result) { this.result = result; this.returnEarly = true; }
        public Throwable getThrowable() { return throwable; }
        public void setThrowable(Throwable t) { this.throwable = t; this.returnEarly = true; }
        public boolean hasThrowable() { return throwable != null; }
        public Object getResultOrThrowable() throws Throwable { throw new UnsupportedOperationException(); }
    }

    public class Unhook {
        public void unhook() {}
    }
}
