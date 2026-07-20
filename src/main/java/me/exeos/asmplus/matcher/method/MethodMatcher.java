package me.exeos.asmplus.matcher.method;

import java.util.HashSet;
import java.util.Set;

public class MethodMatcher {

    private final Set<MethodMatchEntry> entries = new HashSet<>();

    public MethodMatcher() {
    }

    public MethodMatcher(Set<MethodMatchEntry> initList) {
        entries.addAll(initList);
    }

    public void add(MethodMatcher other) {
        entries.addAll(other.get());
    }

    public void add(MethodMatchEntry mw) {
        entries.add(mw);
    }

    public Set<MethodMatchEntry> get() {
        return entries;
    }

    public boolean match(MethodMatchEntry other) {
        for (MethodMatchEntry methodMatchEntry : entries) {
            if (methodMatchEntry.equals(other)) {
                return true;
            }
        }
        return false;
    }

    public enum Mode {
        OWNER_NAME_DESC,
        OWNER_NAME,
        NAME
    }
}
