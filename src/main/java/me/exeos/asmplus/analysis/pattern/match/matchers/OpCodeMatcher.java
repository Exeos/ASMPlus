package me.exeos.asmplus.analysis.pattern.match.matchers;

import me.exeos.asmplus.analysis.pattern.match.AbstractInstructionMatcher;
import org.objectweb.asm.tree.AbstractInsnNode;

public record OpCodeMatcher(int opcode) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        return toMatch.getOpcode() == opcode;
    }
}
