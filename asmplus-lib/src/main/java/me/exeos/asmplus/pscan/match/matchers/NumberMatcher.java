package me.exeos.asmplus.pscan.match.matchers;

import me.exeos.asmplus.pscan.match.AbstractInstructionMatcher;
import me.exeos.asmplus.utils.ASMUtils;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Optional;

public record NumberMatcher(Optional<Number> value) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        if (!ASMUtils.isNumberPush(toMatch)) {
            return false;
        }

        if (value.isPresent()) {
            Optional<Number> pushValue = ASMUtils.getValue(toMatch, Number.class);
            return pushValue.map(number -> number.equals(value.get())).orElse(false);

        }

        return false;
    }
}
