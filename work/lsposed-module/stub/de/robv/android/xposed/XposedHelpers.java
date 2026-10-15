package de.robv.android.xposed;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Set;

public class XposedHelpers {
    public static Class<?> findClass(String className, ClassLoader classLoader) { return null; }
    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) { return null; }
    public static Object getObjectField(Object obj, String fieldName) { return null; }
    public static void setObjectField(Object obj, String fieldName, Object value) {}
    public static Object getStaticObjectField(Class<?> clazz, String fieldName) { return null; }
    public static Object callMethod(Object obj, String methodName, Object... args) { return null; }
    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) { return null; }
    public static Field findField(Class<?> clazz, String fieldName) { return null; }
    public static Method findMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) { return null; }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) { return null; }
    public static XC_MethodHook.Unhook findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) { return null; }
    public static Set<XC_MethodHook.Unhook> findAndHookAllMethods(Class<?> clazz, String methodName, XC_MethodHook callback) { return null; }
    public static void findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {}
    public static Object newInstance(Class<?> clazz, Object... args) { return null; }
}
