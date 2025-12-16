package me.exeos.asmplus.pscan.match;

import org.objectweb.asm.tree.AbstractInsnNode;

public interface AbstractInstructionMatcher {

    boolean match(AbstractInsnNode toMatch);
}
