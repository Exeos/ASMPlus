package me.exeos.asmplus.remapper.mapping;

import me.exeos.asmplus.JarLoader;

import java.util.HashMap;

public class Remapper {

    /**
     * All maps below follow this concept:
     * K = original V = new
     * K will be used for lookup purposes
     */

    private final HashMap<String, String> classMap = new HashMap<>();
    private final HashMap<ClassMember, String> fieldMap = new HashMap<>();
    private final HashMap<ClassMember, String> methodMap = new HashMap<>();

    public void mapClass(String name, String newName) {
        classMap.put(name, newName);
    }

    public void mapField(String owner, String name, String desc, String newName) {
        mapField(new ClassMember(owner, name, desc), newName);
    }

    public void mapField(ClassMember classMember, String newName) {
        fieldMap.put(classMember, newName);
    }

    public void mepMethod(String owner, String name, String desc, String newName) {
        mepMethod(new ClassMember(owner, name, desc), newName);
    }

    public void mepMethod(ClassMember classMember, String newName) {
        methodMap.put(classMember, newName);
    }

    public void applyMappings(JarLoader jarLoader) {
    }
}
