package me.exeos.asmplus.utils;

public class AsmUtil {

    public static boolean hasAccess(int access, int toCheck) {
        return (access & toCheck) != 0;
    }

    public static boolean bothHaveOrLackAccess(int first, int second, int toCheck) {
        return hasAccess(first, toCheck) == hasAccess(second, toCheck);
    }
}
