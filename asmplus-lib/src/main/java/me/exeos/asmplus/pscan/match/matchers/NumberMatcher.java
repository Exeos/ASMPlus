package me.exeos.asmplus.pscan.match.matchers;

import me.exeos.asmplus.pscan.match.AbstractInstructionMatcher;
import me.exeos.asmplus.utils.ASMUtils;
import me.exeos.jlib.number.NumberComparator;
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
            // todo implement < > <= >= ==
            return pushValue.filter(number -> NumberComparator.compareNumbers(number, value.get()) == 0).isPresent();
        }

        return false;
    }
}
