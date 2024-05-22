package me.exeos.asmplus.pattern.result;

import org.objectweb.asm.tree.AbstractInsnNode;

public class InsnResult {

    public InsnResult(AbstractInsnNode[] pattern) {
        this.pattern = pattern;
    }

    public final AbstractInsnNode[] pattern;

    public AbstractInsnNode getFirst() {
        return pattern[0];
    }

    public AbstractInsnNode getLast() {
        return pattern[pattern.length - 1];
    }
}
