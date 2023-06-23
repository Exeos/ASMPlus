package me.exeos.asmplus;

import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.List;

public interface JarInterface {

    default void addClass(ClassNode classNode) {
        JarLoader.CLASSES.put(classNode.name, classNode);
    }

    default void addFiles(String name, byte[] bytes) {
        JarLoader.FILES.put(name, bytes);
    }

    default List<ClassNode> getClasses() {
        return JarLoader.CLASSES.values().stream().toList();
    }

    default HashMap<String, byte[]> getFiles() {
        return JarLoader.FILES;
    }

    default ClassNode getClass(String name) {
        return JarLoader.CLASSES.get(name);
    }
    default byte[] getFile(String name) {
        return JarLoader.FILES.get(name);
    }
}