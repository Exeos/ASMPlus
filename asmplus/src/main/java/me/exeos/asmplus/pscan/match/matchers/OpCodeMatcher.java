package me.exeos.asmplus.pscan.match.matchers;

import me.exeos.asmplus.pscan.match.AbstractInstructionMatcher;
import org.objectweb.asm.tree.AbstractInsnNode;

public class OpCodeMatcher implements AbstractInstructionMatcher {

    private final int opcode;

    public OpCodeMatcher(int opcode) {
        this.opcode = opcode;
    }

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        return toMatch.getOpcode() == opcode;
    }
}
