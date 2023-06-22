package me.exeos.asmplus.pattern.result;

import org.objectweb.asm.tree.AbstractInsnNode;

public record InsnResult(AbstractInsnNode[] pattern) {

    public AbstractInsnNode getFirst() {
        return pattern[0];
    }

    public AbstractInsnNode getLast() {
        return pattern[pattern.length - 1];
    }
}
