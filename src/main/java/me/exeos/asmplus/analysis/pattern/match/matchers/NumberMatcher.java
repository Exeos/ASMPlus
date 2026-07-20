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

        return true;
    }

    public enum MatchMode {
        LESS(-1),
        EQUALS(0),
        GREATER(1),
        NONE(2);

        public final int value;

        MatchMode(int value) {
            this.value = value;
        }
    }
}
