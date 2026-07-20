package me.exeos.asmplus.analysis.pattern.match.matchers;

import me.exeos.asmplus.analysis.pattern.match.AbstractInstructionMatcher;
import org.objectweb.asm.tree.AbstractInsnNode;

public record InstructionTypeMatcher(int instructionType) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        return toMatch.getType() == instructionType;
    }
}
