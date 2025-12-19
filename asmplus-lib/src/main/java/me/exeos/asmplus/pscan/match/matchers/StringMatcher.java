package me.exeos.asmplus.pscan.match.matchers;

import me.exeos.asmplus.pscan.match.AbstractInstructionMatcher;
import me.exeos.asmplus.utils.ASMUtils;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Optional;

public record StringMatcher(Optional<String> stringToMatch) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        return ASMUtils.isString(toMatch) &&
                (stringToMatch.isEmpty() || ASMUtils.getStringValue(toMatch).equals(stringToMatch.get()));
    }
}
