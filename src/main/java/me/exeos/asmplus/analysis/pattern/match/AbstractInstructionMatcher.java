package me.exeos.asmplus.analysis.pattern.match;

import org.objectweb.asm.tree.AbstractInsnNode;

public interface AbstractInstructionMatcher {

    boolean match(AbstractInsnNode toMatch);
}
