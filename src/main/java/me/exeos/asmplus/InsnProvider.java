package me.exeos.asmplus;

import org.objectweb.asm.tree.InsnList;

@FunctionalInterface
public interface InsnProvider {
    InsnList get();
}
