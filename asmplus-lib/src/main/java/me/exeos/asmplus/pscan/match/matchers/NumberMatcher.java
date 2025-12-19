package me.exeos.asmplus.pscan.match.matchers;

import me.exeos.asmplus.pscan.match.AbstractInstructionMatcher;
import me.exeos.asmplus.utils.ASMUtils;
import me.exeos.jlib.number.NumberComparator;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Optional;

public record NumberMatcher(Optional<Number> value, MatchMode matchMode) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        if (!ASMUtils.isNumberPush(toMatch)) {
            return false;
        }

        if (value.isPresent()) {
            Optional<Number> pushValue = ASMUtils.getValue(toMatch, Number.class);
            return pushValue.filter(number -> NumberComparator.compareNumbers(number, value.get()) == matchMode.value).isPresent();
        }

        return false;
    }

    public enum MatchMode {
        LESS((byte) -1),
        EQUALS((byte) 0),
        GREATER((byte) 1),
        NONE((byte) 2);

        public final byte value;

        MatchMode(byte value) {
            this.value = value;
        }
    }
}
