package me.exeos.asmplus.analysis.pattern.match.matchers;

import me.exeos.asmplus.analysis.pattern.match.AbstractInstructionMatcher;
import me.exeos.asmplus.utils.InsnUtil;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Optional;

public record StringMatcher(Optional<String> stringToMatch) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        Optional<String> insnValue = InsnUtil.getStingPushValue(toMatch);
        return insnValue.filter(s -> stringToMatch.isEmpty() || s.equals(stringToMatch.get())).isPresent();

    }
}
