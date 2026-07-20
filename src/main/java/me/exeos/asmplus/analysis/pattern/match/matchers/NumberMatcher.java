package me.exeos.asmplus.analysis.pattern.match.matchers;

import me.exeos.asmplus.analysis.pattern.match.AbstractInstructionMatcher;
import me.exeos.asmplus.analysis.pattern.match.NumberComparator;
import me.exeos.asmplus.utils.InsnUtil;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Optional;

public record NumberMatcher(Optional<Number> value, MatchMode matchMode) implements AbstractInstructionMatcher {

    @Override
    public boolean match(AbstractInsnNode toMatch) {
        Optional<Number> numVal = InsnUtil.getNumberValue(toMatch);
        if (numVal.isEmpty()) {
            return false;
        }

        if (value.isPresent()) {
            return numVal.filter(number -> NumberComparator.compareNumbers(number, value.get()) == matchMode.value).isPresent();
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
