package com.gcscout.trackerdemo;

public class Log {

    public static void e(String message) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callerName = stackTraceElements[stackTraceElements.length - 1].getClassName();
        android.util.Log.e(callerName, message);
    }

    public static void i(String message) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callerName = stackTraceElements[stackTraceElements.length - 1].getClassName();
        android.util.Log.i(callerName, message);
    }
}
