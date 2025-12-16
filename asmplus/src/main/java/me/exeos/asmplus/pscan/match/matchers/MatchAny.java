package me.exeos.asmplus.pscan.match.matchers;

import me.exeos.asmplus.pscan.match.AbstractInstructionMatcher;
import org.objectweb.asm.tree.AbstractInsnNode;

public class MatchAny implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        return true;
    }
}
